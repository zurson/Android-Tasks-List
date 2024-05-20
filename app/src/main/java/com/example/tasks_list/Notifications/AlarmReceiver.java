package com.example.tasks_list.Notifications;

import static com.example.tasks_list.Enums.TaskStatus.COMPLETED;
import static com.example.tasks_list.Utilities.Settings.DATABASE_OBJECT_PASS_KEY;
import static com.example.tasks_list.Utilities.Settings.DATABASE_TASKS_NAME;
import static com.example.tasks_list.Utilities.Settings.LOG_TAG_DATABASE;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import com.example.tasks_list.Activities.TaskInfoActivity;
import com.example.tasks_list.Database.AppDatabase;
import com.example.tasks_list.Database.AttachmentsDao;
import com.example.tasks_list.Database.Task;
import com.example.tasks_list.Database.TasksDao;
import com.example.tasks_list.Enums.TaskStatus;
import com.example.tasks_list.Interfaces.DatabaseAccessor;
import com.example.tasks_list.R;
import com.example.tasks_list.Utilities.AsyncManager;
import com.example.tasks_list.Utilities.GlobalFunctions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class AlarmReceiver extends BroadcastReceiver implements DatabaseAccessor, Serializable {
    private static final int MAX_ID_VALUES = Integer.MAX_VALUE - 1;

    private static AppDatabase appDatabase;
    private transient Context context;
    private static final AtomicInteger notificationId = new AtomicInteger(0);
    private static final AtomicInteger requestCode = new AtomicInteger(0);


    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null)
            return;

        this.context = context;

        if (appDatabase == null)
            appDatabase = setupDatabase();

        Log.d("Notification", " ");
        Log.d("Notification", " ");
        Log.d("Notification", " ");

        Log.d("Notification", "Scheduler waked up!");

        Set<Task> tasksToSendNotification = getTasksToSendNotifications();
        Log.d("Notification", "Notifications to send: " + tasksToSendNotification.size());
        showNotification(context, tasksToSendNotification);

        Log.d("Notification", "Scheduler slept!");
    }


    /* DATABASE */


    private AppDatabase setupDatabase() {
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_TASKS_NAME).build();
    }


    private List<Task> getAllTasks() {
        CompletableFuture<List<Task>> asyncTask = CompletableFuture.supplyAsync(() -> getTasksDao().getAll());

        try {
            return asyncTask.get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }


    private void updateTask(Task task) {
        AsyncManager.runAsyncTask(() -> {
            getTasksDao().upsertTask(task);
            Log.d(LOG_TAG_DATABASE, String.format("Task: %s UPSERTED!", task.getTitle()));
        });
    }


    /* Notifications Manager */


    private Set<Task> getTasksToSendNotifications() {
        List<Task> allTasks = getAllTasks();
        Set<Task> tasksToSendNotification = new HashSet<>();

        boolean update;

        if (allTasks == null || allTasks.isEmpty())
            return tasksToSendNotification;

        for (Task task : allTasks) {
            update = false;

            if (task.getStatus() == COMPLETED || !task.getNotifications()) {
                Log.d("Notification", String.format("Task REJECTED: %s status: %s notifications: %s", task.getTitle(), task.getStatus(), task.getNotifications()));
                continue;
            }

            Log.d("Notification", String.format("Task: %s time before: %d end date: %s", task.getTitle(), task.getAlertBeforeTime(), task.getEndDate()));


            if (isTimeForBeforeNotification(task)) {
                tasksToSendNotification.add(task);
                task.setBeforeNotificationSent(true);
                update = true;
                Log.d("Notification", String.format("BEFORE: %s time before: %d end date: %s", task.getTitle(), task.getAlertBeforeTime(), task.getEndDate()));
            }

            if (isTimeForEndNotification(task)) {
                tasksToSendNotification.add(task);
                task.setEndNotificationSent(true);
                task.setNotifications(false);
                update = true;
                Log.d("Notification", String.format("END: %s time before: %d end date: %s", task.getTitle(), task.getAlertBeforeTime(), task.getEndDate()));
            }

            if (update) {
                updateTask(task);
                Log.d("Notification", String.format("Task: %s WILL SEND NOTIFICATION", task.getTitle()));
            } else {
                Log.d("Notification", String.format("Task: %s WON'T SEND NOTIFICATION", task.getTitle()));
            }

            Log.d("Notification", " ");

        }

        return tasksToSendNotification;
    }


    private boolean isTimeForBeforeNotification(Task task) {
        boolean isTimeForAlert = isTimeForAlert(task.getEndDate(), task.getAlertBeforeTime());
        Log.d("Notification", String.format("BEFORE: isTimeForAlert %s = %s", task.getTitle(), isTimeForAlert));
        return isTimeForAlert && !task.isBeforeNotificationSent();
    }


    private boolean isTimeForEndNotification(Task task) {
        boolean isTimeForAlert = isTimeForAlert(task.getEndDate(), 0);
        Log.d("Notification", String.format("END: isTimeForAlert %s = %s", task.getTitle(), isTimeForAlert));
        return isTimeForAlert && !task.isEndNotificationSent();
    }


    private boolean isTimeForAlert(Date endDate, int minutesBeforeAlert) {
        Calendar currentTime = Calendar.getInstance(TimeZone.getDefault());

        Calendar alertTime = Calendar.getInstance();
        alertTime.setTime(endDate);
        alertTime.add(Calendar.MINUTE, -minutesBeforeAlert);

//        Log.d("Notification", String.format("current: %s alertDate: %s", currentTime.getTime(), alertTime.getTime()));

        return currentTime.after(alertTime);
    }


    private Intent prepareIntent(Task task) {
        Intent intent = GlobalFunctions.getIntent(context, TaskInfoActivity.class);
        intent.putExtra(TaskInfoActivity.TASK_INFO_KEY, task);
        intent.putExtra(DATABASE_OBJECT_PASS_KEY, this);

        return intent;
    }


    private PendingIntent preparePendingIntent(Intent intent) {
        return PendingIntent.getActivity(context, getNextId(requestCode), intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void showNotification(Context context, Set<Task> tasks) {
        for (Task task : tasks) {
            Intent intent = prepareIntent(task);
            PendingIntent pendingIntent = preparePendingIntent(intent);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Notification_Channel")
                    .setSmallIcon(R.drawable.ic_clock)
                    .setContentTitle(task.getTitle())
                    .setContentText(GlobalFunctions.formatDate(task.getEndDate()))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                return;

            notificationManager.notify(getNextId(notificationId), builder.build());

            Log.d("NOTIFICATION", "Notification: " + task.getTitle() + "\tEnd date: " + task.getEndDate() + "\tTime before: " + task.getAlertBeforeTime());
        }

    }


    private int getNextId(AtomicInteger id) {
        try {
            return id.get();
        } finally {
            id.set(id.get() + 1);

            if (id.get() >= MAX_ID_VALUES)
                id.set(0);
        }
    }


    @Override
    public TasksDao getTasksDao() {
        return appDatabase.taskDao();
    }

    @Override
    public AttachmentsDao getAttachmentsDao() {
        return appDatabase.attachmentsDao();
    }

}