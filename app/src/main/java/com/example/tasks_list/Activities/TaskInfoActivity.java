package com.example.tasks_list.Activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.tasks_list.Utilities.Settings.LOG_TAG_DATABASE;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.tasks_list.Database.Task;
import com.example.tasks_list.Enums.TaskStatus;
import com.example.tasks_list.Utilities.AsyncManager;
import com.example.tasks_list.Utilities.GlobalFunctions;
import com.example.tasks_list.Utilities.TaskManager;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;

public class TaskInfoActivity extends TaskManager {

    public static final String TASK_INFO_KEY = "TASK_INFO_KEY";
    private static final String EDIT_BUTTON_TEXT = "Edit";
    private static final String SAVE_BUTTON_TEXT = "Save";
    private static final String CANCEL_BUTTON_TEXT = "Cancel";
    private static final String MARK_AS_COMPLETE_MESSAGE = "Mark as complete?";
    private static final String COMPLETE_TASK_BUTTON_MESSAGE = "Complete\nTask";
    private static final String ERROR_TITLE = "Error";
    private static final String TASK_MARKED_AS_COMPLETED_TOAST = "Task marked as completed!";
    private static final String EDIT_MODE_ENABLED_TOAST = "Edit mode enabled!";
    private static final String EDIT_MODE_DISABLED_TOAST = "Edit mode disabled!";
    private static final String TASK_EDITED_TOAST = "Task edited!";
    private static final String GO_BACK_BUTTON_TEXT = "Back";

    private Task task;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        task = (Task) getIntent().getSerializableExtra(TASK_INFO_KEY);

        initializeAttachmentsSector(task.getId());

        prepareElements();
        disableFieldsInteraction();
    }


    private void prepareElements() {
        super.titleEditText.setText(task.getTitle());
        super.descriptionEditText.setText(task.getDescription());

        super.setDisplayedDate(task.getEndDate());
        super.setEndDate(task.getEndDate());
        super.startDateTextView.setText(GlobalFunctions.formatDate(task.getStartDate()));

        super.setNotificationsStatus(task.getNotifications(), task.getAlertBeforeTime());
        super.notificationsCheckBox.setChecked(task.getNotifications());

        super.extraButton.setText(COMPLETE_TASK_BUTTON_MESSAGE);
        super.extraButton.setVisibility(VISIBLE);

        super.setSelectedCategory(task.getCategory());
        super.unAttachSwipeManager();
    }


    private void disableFieldsInteraction() {
        titleMode(false);
        descriptionMode(false);
        categoriesListMode(false);
        buttonsMode(false);
        notificationsMode(false);

        super.unAttachSwipeManager();
    }


    private void makeFieldsEditable() {
        titleMode(true);
        descriptionMode(true);
        categoriesListMode(true);
        buttonsMode(true);
        notificationsMode(true);

        super.attachSwipeManager();

        super.mainButton.setOnClickListener(v -> {
            saveChanges();
        });

        super.extraButton.setOnClickListener(v -> {
            editCancelCondition();
            Toast.makeText(this, EDIT_MODE_DISABLED_TOAST, Toast.LENGTH_SHORT).show();
        });

        Toast.makeText(this, EDIT_MODE_ENABLED_TOAST, Toast.LENGTH_SHORT).show();
    }


    private void saveChanges() {
        Task changesTask = super.createTaskFromInput();
        changesTask.setId(task.getId());

        Thread thread = AsyncManager.runAsyncTask(() -> {
            try {
                saveAllAttachmentsToDbAndStorage(task.getId());
                getTasksDao().upsertTask(changesTask);

                Log.d(LOG_TAG_DATABASE, String.format("Task: %s UPSERTED!", changesTask.getTitle()));
                Toast.makeText(this, TASK_EDITED_TOAST, Toast.LENGTH_LONG).show();
                backToMainActivity();
            } catch (SQLiteException | IOException e) {
                GlobalFunctions.showAlertDialog(this, ERROR_TITLE, e.getMessage(), this::backToMainActivity);
            }
        });

        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }


    private void titleMode(boolean editable) {
        super.titleEditText.setFocusable(editable);
        super.titleEditText.setFocusableInTouchMode(editable);

        super.titleInputLayout.setCounterEnabled(editable);
    }


    private void descriptionMode(boolean editable) {

        if (!editable) {
            super.descriptionEditText.setFocusable(false);
            super.descriptionEditText.setFocusableInTouchMode(false);
            super.descriptionEditText.setHint("");
            super.descriptionInputLayout.setCounterEnabled(false);
            return;
        }

        super.descriptionEditText.setFocusable(true);
        super.descriptionEditText.setFocusableInTouchMode(true);
        super.descriptionInputLayout.setCounterEnabled(true);
    }


    private void categoriesListMode(boolean editable) {
        if (!editable) {
            super.categoriesAutoCompleteTextView.setEnabled(false);
            super.categoriesInputLayout.setEnabled(false);
            return;
        }

        super.categoriesAutoCompleteTextView.setEnabled(true);
        super.categoriesInputLayout.setEnabled(true);
    }


    private void buttonsMode(boolean editable) {
        if (!editable) {
            super.selectDateButton.setVisibility(GONE);
            super.addAttachmentButton.setVisibility(GONE);
            super.startDateLabel.setVisibility(VISIBLE);
            super.startDateTextView.setVisibility(VISIBLE);

            if (task.getStatus() == TaskStatus.COMPLETED) {
                super.extraButton.setVisibility(GONE);

                super.mainButton.setText(GO_BACK_BUTTON_TEXT);
                super.mainButton.setOnClickListener(v -> backToMainActivity());

                return;
            }

            super.mainButton.setText(EDIT_BUTTON_TEXT);
            return;
        }

        super.selectDateButton.setVisibility(VISIBLE);
        super.addAttachmentButton.setVisibility(VISIBLE);
        super.startDateLabel.setVisibility(GONE);
        super.startDateTextView.setVisibility(GONE);

        super.mainButton.setText(SAVE_BUTTON_TEXT);
        super.extraButton.setText(CANCEL_BUTTON_TEXT);
    }


    private void notificationsMode(boolean editable) {
        if (!editable) {
            super.notificationsCheckBox.setEnabled(false);
            super.notificationSeekBar.setVisibility(View.INVISIBLE);
            return;
        }

        super.notificationsCheckBox.setEnabled(true);
        if (getNotificationsStatus())
            super.notificationSeekBar.setVisibility(VISIBLE);
    }


    private void editCancelCondition() {
        disableFieldsInteraction();
        prepareElements();

        initializeExtraButtonCondition();
        initializeMainButtonCondition();

        refreshAttachmentsRecyclerView(task.getId());
    }


    private void markAsComplete() {
        AsyncManager.runAsyncTask(() -> {
            task.setStatus(TaskStatus.COMPLETED);
            task.setEndDate(Date.from(Instant.now()));

            try {
                getTasksDao().upsertTask(task);
                Log.d(LOG_TAG_DATABASE, String.format("Task: %s UPSERTED!", task.getTitle()));

                Toast.makeText(this, TASK_MARKED_AS_COMPLETED_TOAST, Toast.LENGTH_LONG).show();
                backToMainActivity();
            } catch (SQLiteException e) {
                GlobalFunctions.showAlertDialog(this, ERROR_TITLE, e.getMessage(), this::backToMainActivity);
            }

        });

    }


    @Override
    protected void initializeMainButtonCondition() {
        super.mainButton.setOnClickListener(v -> {
            makeFieldsEditable();
        });
    }


    @Override
    protected void initializeExtraButtonCondition() {
        super.extraButton.setOnClickListener(v -> {
            GlobalFunctions.showConfirmAlert(this, MARK_AS_COMPLETE_MESSAGE, this::markAsComplete);
        });
    }

}
