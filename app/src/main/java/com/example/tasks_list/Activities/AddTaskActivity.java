package com.example.tasks_list.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.tasks_list.Database.Task;
import com.example.tasks_list.Utilities.GlobalFunctions;
import com.example.tasks_list.Utilities.TaskManager;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AddTaskActivity extends TaskManager {
    private static final String SAVE_ERROR_MESSAGE = "Fill in the Title, Date and Category fields!";
    private static final String NEW_TASK_CREATED_TOAST = "New task created!";
    private static final String ALERT_DIALOG_ERROR_TITLE = "Error";
    private static final String CONFIRM_BUTTON_TEXT = "Create Task";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareElements();
        initializeAttachmentsSector(null);
    }

    private void prepareElements() {
        super.selectedDateTextView.setVisibility(View.GONE);
        super.mainButton.setText(CONFIRM_BUTTON_TEXT);
    }

    @Override
    protected void setDisplayedDate(Date date) {
        super.setDisplayedDate(date);
        super.selectedDateTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initializeMainButtonCondition() {
        super.mainButton.setOnClickListener(v -> {

            if (!allRequiredFieldsFilledIn()) {
                super.showErrorDialog(SAVE_ERROR_MESSAGE);
                return;
            }

            Task task = null;

            try {
                task = createTaskFromInput();
                long taskId = insertTaskToDatabase(task);

                saveAllAttachmentsToDbAndStorage(taskId);

                Toast.makeText(this, NEW_TASK_CREATED_TOAST, Toast.LENGTH_SHORT).show();
                backToMainActivity();
            } catch (ExecutionException | InterruptedException | IOException e) {
                GlobalFunctions.showAlertDialog(this, ALERT_DIALOG_ERROR_TITLE, e.getMessage(), this::backToMainActivity);
                deleteTaskFromDb(task);
            }

        });
    }


    @Override
    protected void initializeExtraButtonCondition() {

    }

    private void deleteTaskFromDb(Task task) {
        CompletableFuture<Void> asyncTask = CompletableFuture.supplyAsync(() -> {
            getTasksDao().deleteTask(task);
            return null;
        });

        try {
            asyncTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
