package com.example.tasks_list.Attachments;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasks_list.ExternalStorage.ExternalStorageManager;
import com.example.tasks_list.R;
import com.example.tasks_list.Utilities.GlobalFunctions;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class AttachmentRecyclerViewAdapter extends RecyclerView.Adapter<AttachmentViewHolder> implements Serializable {
    private static final String TAG = "AttachmentRecyclerViewAdapter";
    private static final String ERROR_TITLE = "Error";
    private static final String ERROR_MESSAGE = "Unable to open file: ";

    @Getter
    private ArrayList<String> attachmentsFilenames;
    private Context context;
    private ExternalStorageManager externalStorageManager;

    public AttachmentRecyclerViewAdapter(@NonNull Context context, @NonNull List<String> attachmentsFilenames, @NonNull ExternalStorageManager externalStorageManager) {
        this.context = context;
        this.attachmentsFilenames = new ArrayList<>(attachmentsFilenames);
        this.externalStorageManager = externalStorageManager;
    }


    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.attachment_item_view, parent, false);
        return new AttachmentViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        String filename = attachmentsFilenames.get(position);
        holder.setData(filename);

        setOnAttachmentClickListener(filename, holder.itemView);
    }

    @Override
    public int getItemCount() {
        return attachmentsFilenames.size();
    }


    private void setOnAttachmentClickListener(String filename, View itemView) {
        itemView.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Opening file: " + filename);
                externalStorageManager.openFile(filename);
            } catch (FileNotFoundException e) {
                Log.d(TAG, ERROR_MESSAGE + filename);
                e.printStackTrace();
                GlobalFunctions.showAlertDialog(context, ERROR_TITLE, ERROR_MESSAGE + filename, null);
            }
        });
    }


    public void addAttachment(String filename) {
        attachmentsFilenames.add(filename);
        notifyDataSetChanged();

        Log.d(TAG, "Added: " + filename);
    }


    public void removeAttachment(String filename) {
        attachmentsFilenames.removeIf(s -> s.equals(filename));
        notifyDataSetChanged();

        Log.d(TAG, "Removed: " + filename);
    }


    public void updateDataSet(List<String> filenames) {
        this.attachmentsFilenames = new ArrayList<>(filenames);
        notifyDataSetChanged();
    }

}
