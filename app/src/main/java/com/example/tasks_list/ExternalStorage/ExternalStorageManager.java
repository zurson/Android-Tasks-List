package com.example.tasks_list.ExternalStorage;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.tasks_list.Database.Attachment;
import com.example.tasks_list.Interfaces.AttachmentSelector;
import com.example.tasks_list.Interfaces.DatabaseAccessor;
import com.example.tasks_list.Utilities.AsyncManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExternalStorageManager {
    public static final String ATTACHMENT_MANAGER_TAG = "ATTACHMENT MANAGER";
    private static final int ATTACHMENT_WRITE_BUFFER_LEN = 1024;
    private static final String ATTACHMENT_USING_PUBLIC_EXTERNAL_STORAGE_FOLDER = Environment.DIRECTORY_DOCUMENTS;
    private static final String ATTACHMENT_MAIN_FOLDER_NAME = "Tasks-List attachments";
    public static final String ATTACHMENTS_FAILED_TO_CREATE_MAIN_DIRECTORY_MSG = "Failed to create directory";
    public static final String ATTACHMENTS_SUPPORTED_FILETYPES = "*/*"; // all file types are supported


    private final ActivityResultLauncher<String> content;
    private final Context context;
    private final AppCompatActivity activity;
    private AttachmentSelector attachmentSelector;
    private ArrayList<FileDescriptor> fileDescriptors;
    private List<Attachment> taskAttachments;
    private final File appDir;
    private final ContentResolver contentResolver;
    private final DatabaseAccessor databaseAccessor;

    // TODO: Loading existing attachments to fileDescriptors list
    // TODO: Deleting copies and saving

    public ExternalStorageManager(@NonNull AppCompatActivity activity, @NonNull DatabaseAccessor databaseAccessor, List<Attachment> taskAttachments) throws IOException {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.databaseAccessor = databaseAccessor;

        this.content = getContent();

        this.appDir = getOrCreateAppDirectory();
        this.contentResolver = getContentResolver();

        prepareLists(taskAttachments);
    }


    public void registerButton(@NonNull Button button, @NonNull AttachmentSelector attachmentSelector) {
        this.attachmentSelector = attachmentSelector;
        button.setOnClickListener(v -> openFileSelector(content));
    }


    private void prepareLists(List<Attachment> taskAttachments) {
        this.taskAttachments = taskAttachments == null ? new ArrayList<>() : taskAttachments;
        this.fileDescriptors = loadCurrentAttachments(this.taskAttachments);
    }


    public void refreshData(List<Attachment> taskAttachments) {
        prepareLists(taskAttachments);
    }


    private ArrayList<FileDescriptor> loadCurrentAttachments(List<Attachment> attachments) {
        ArrayList<FileDescriptor> descriptors = new ArrayList<>();

        if (attachments == null)
            return descriptors;

        for (Attachment att : attachments) {
            try {
                descriptors.add(new FileDescriptor(getUriOfFileFromAppDir(att.getFilename()), att.getFilename()));
            } catch (FileNotFoundException e) {
                Log.e(ATTACHMENT_MANAGER_TAG, "Error loading file: " + e.getMessage());
            }
        }

        return descriptors;
    }


    private ActivityResultLauncher<String> getContent() {
        return activity.registerForActivityResult(new ActivityResultContracts.GetContent(), this::fileSelectCondition);
    }


    private void fileSelectCondition(Uri uri) {
        if (uri == null)
            return;

        String filename = getFilenameFromUri(uri);
        filename = makeFilenameUnique(filename);

        fileDescriptors.add(new FileDescriptor(uri, filename));
        attachmentSelector.notifyAttachmentSelection(filename);
    }


    private void openFileSelector(ActivityResultLauncher<String> content) {
        content.launch(ATTACHMENTS_SUPPORTED_FILETYPES);
    }


    private InputStream getInputStream(Uri uri) throws FileNotFoundException {
        return contentResolver.openInputStream(uri);
    }


    private File getOrCreateAppDirectory() throws IOException {
        File externalDir = getExternalStoragePublicDirectory();
        File appDir = new File(externalDir, ATTACHMENT_MAIN_FOLDER_NAME);

        if (appDir.exists())
            return appDir;

        if (!appDir.mkdirs()) {
            Log.e(ATTACHMENT_MANAGER_TAG, ATTACHMENTS_FAILED_TO_CREATE_MAIN_DIRECTORY_MSG);
            throw new IOException(ATTACHMENTS_FAILED_TO_CREATE_MAIN_DIRECTORY_MSG);
        }

        return appDir;
    }


    private File getExternalStoragePublicDirectory() {
        return Environment.getExternalStoragePublicDirectory(ATTACHMENT_USING_PUBLIC_EXTERNAL_STORAGE_FOLDER);
    }


    private ContentResolver getContentResolver() {
        return context.getContentResolver();
    }


    private void deleteAttachmentFromDb(String filename) {
        databaseAccessor.getAttachmentsDao().deleteAttachment(filename);
    }


    private void removeFilenameCopies() {
        List<Attachment> taskAttachmentsToRemove = new ArrayList<>();

        for (Attachment att : taskAttachments) {
            String filename = att.getFilename();

            if (!filenameExistsInDescriptorsList(att.getFilename()))
                continue;

            fileDescriptors.removeIf(fileDescriptor -> fileDescriptor.getFilename().equals(filename));
            taskAttachmentsToRemove.add(att);
        }

        for (Attachment att : taskAttachmentsToRemove)
            taskAttachments.remove(att);

    }


    private void deleteFileIfNotInDescriptorsList() {
        Thread thread = AsyncManager.runAsyncTask(() -> {
            for (Attachment taskAttachment : taskAttachments) {
                String taskFilename = taskAttachment.getFilename();
                if (!filenameExistsInDescriptorsList(taskFilename)) {
                    deleteFile(taskFilename);
                    deleteAttachmentFromDb(taskFilename);
                }
            }
        });

        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }


    public void saveAllAttachmentsToDbAndStorage(long taskId) throws IOException {
        AtomicBoolean allSaved = new AtomicBoolean(false);

        System.out.println("TASK: ");
        taskAttachments.forEach(attachment -> System.out.println(attachment.getFilename()));
        System.out.println(" ");

        System.out.println("DESCRIPTORS: ");
        fileDescriptors.forEach(fileDescriptor -> System.out.println(fileDescriptor.getFilename()));
        System.out.println(" ");

        Thread thread = AsyncManager.runAsyncTask(() -> {
            removeFilenameCopies();

            System.out.println("AFTER TASK: ");
            taskAttachments.forEach(attachment -> System.out.println(attachment.getFilename()));
            System.out.println(" ");

            System.out.println("AFTER DESCRIPTORS: ");
            fileDescriptors.forEach(fileDescriptor -> System.out.println(fileDescriptor.getFilename()));
            System.out.println(" ");

            deleteFileIfNotInDescriptorsList();

            try {
                for (FileDescriptor fileDescriptor : fileDescriptors) {
                    saveFile(fileDescriptor);
                    Attachment att = new Attachment(taskId, fileDescriptor.getFilename());
                    databaseAccessor.getAttachmentsDao().upsertAttachment(att);
                }

                allSaved.set(true);
            } catch (IOException ignored) {
            }

        });

        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }

        if (!allSaved.get())
            throw new IOException("Unable to save all files");
    }


    private void saveFile(FileDescriptor fileDescriptor) throws IOException {
        String filename = fileDescriptor.getFilename();
        File file = new File(appDir, filename);

        try (InputStream inputStream = getInputStream(fileDescriptor.getUri()); OutputStream outputStream = Files.newOutputStream(file.toPath())) {
            byte[] buffer = new byte[ATTACHMENT_WRITE_BUFFER_LEN];
            int length;

            while ((length = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, length);

            Log.d(ATTACHMENT_MANAGER_TAG, "File saved to: " + file.getAbsolutePath());
        }

    }


    private boolean fileExistsInExternalStorage(String filename) {
        return new File(appDir, filename).exists();
    }


    private String makeFilenameUnique(@NonNull String filename) throws IllegalArgumentException {
        String uniqueFilenameFromAppDir = getUniqueFilenameFromAppDir(filename);
        String uniqueFilename = getUniqueFilenameFromDescriptorsList(uniqueFilenameFromAppDir);

        return uniqueFilename;
    }


    private String getUniqueFilenameFromAppDir(String filename) {
        if (!fileExistsInExternalStorage(filename))
            return filename;

        FilenameSplitter filenameSplitter = new FilenameSplitter(filename);

        int counter = 1;
        String extension = filenameSplitter.getExtension();
        String baseName = filenameSplitter.getBaseName();

        FilenameCreator filenameCreator = new FilenameCreator(baseName, extension);
        String uniqueName;
        File file;

        while (true) {
            uniqueName = filenameCreator.createFilename("_" + counter);
            file = new File(appDir, uniqueName);

            if (!file.exists())
                return uniqueName;

            counter++;
        }
    }


    private String getUniqueFilenameFromDescriptorsList(String filename) {
        if (!filenameExistsInDescriptorsList(filename))
            return filename;

        FilenameSplitter filenameSplitter = new FilenameSplitter(filename);

        int counter = 1;
        String extension = filenameSplitter.getExtension();
        String baseName = filenameSplitter.getBaseName();

        FilenameCreator filenameCreator = new FilenameCreator(baseName, extension);
        String uniqueName;

        while (true) {
            uniqueName = filenameCreator.createFilename("_" + counter);

            if (!filenameExistsInDescriptorsList(uniqueName))
                return uniqueName;

            counter++;
        }
    }


    private boolean filenameExistsInDescriptorsList(String filename) {
        for (FileDescriptor fileDescriptor : fileDescriptors) {
            if (fileDescriptor.getFilename().equals(filename))
                return true;
        }

        return false;
    }


    private String getFilenameFromUri(Uri uri) throws IllegalArgumentException {
        String fileName = null;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            cursor.close();
        }

        if (fileName == null)
            fileName = uri.getLastPathSegment();

        return fileName;
    }


    public boolean hasAttachments() {
        return !fileDescriptors.isEmpty();
    }


    public void deleteAttachmentsFromExternalStorage(List<Attachment> attachments) {
        attachments.forEach(attachment -> {
            deleteFile(attachment.getFilename());
            Log.d(ATTACHMENT_MANAGER_TAG, "Deleted from external storage: " + attachment.getFilename());
        });
    }


    public void deleteFile(String filename) {
        File file = new File(appDir, filename);

        if (file.exists())
            file.delete();
    }


    public void removeAttachmentFromList(String filename) {
        fileDescriptors.removeIf(fileDescriptor -> fileDescriptor.getFilename().equals(filename));
    }


    private Uri getUriOfFileFromAppDir(String filename) throws FileNotFoundException {
        File file = new File(appDir, filename);

        if (!file.exists()) {
            Log.e("ERROR", "File does not exist: " + file.getAbsolutePath());
            throw new FileNotFoundException();
        }

        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }


    public void openFile(@NonNull String filename) throws FileNotFoundException {
        Uri uri = getUriOfFileFromAppDir(filename);
        Intent intent = prepareShowFileIntent(uri, getMimeType(filename));

        if (isAppToOpenFileFound(intent)) {
            activity.startActivity(intent);
        } else {
            intent = prepareShowFileIntent(uri, "*/*");
            activity.startActivity(intent);
        }
    }


    private String getMimeType(String filename) {
        FilenameSplitter filenameSplitter = new FilenameSplitter(filename);
        String extension = filenameSplitter.getExtension();

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getMimeTypeFromExtension(extension);

        if (mimeType == null)
            mimeType = "*/*";

        return mimeType;
    }


    private Intent prepareShowFileIntent(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return intent;
    }


    private boolean isAppToOpenFileFound(Intent intent) {
        return intent.resolveActivity(activity.getPackageManager()) != null;
    }
}

