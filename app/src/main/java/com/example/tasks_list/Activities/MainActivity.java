package com.example.tasks_list.Activities;

import static androidx.annotation.Dimension.SP;
import static com.example.tasks_list.Utilities.Settings.DATABASE_OBJECT_PASS_KEY;
import static com.example.tasks_list.Utilities.Settings.DATABASE_TASKS_NAME;
import static com.example.tasks_list.Utilities.Settings.LANDSCAPE_SPAN_COUNT;
import static com.example.tasks_list.Utilities.Settings.LOG_TAG_DATABASE;
import static com.example.tasks_list.Utilities.Settings.NOTIFICATIONS_CHANNEL_ID;
import static com.example.tasks_list.Utilities.Settings.NOTIFICATIONS_INTERVAL_MILLISECONDS;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.tasks_list.Database.AppDatabase;
import com.example.tasks_list.Database.Attachment;
import com.example.tasks_list.Database.AttachmentsDao;
import com.example.tasks_list.Database.Task;
import com.example.tasks_list.Database.TasksDao;
import com.example.tasks_list.Enums.Category;
import com.example.tasks_list.Enums.SwipeType;
import com.example.tasks_list.Enums.TaskStatus;
import com.example.tasks_list.Exceptions.PreferencesNoDataFoundException;
import com.example.tasks_list.ExternalStorage.ExternalStorageManager;
import com.example.tasks_list.Interfaces.ActivityStarter;
import com.example.tasks_list.Interfaces.DatabaseAccessor;
import com.example.tasks_list.Interfaces.DatabaseItemRemover;
import com.example.tasks_list.Notifications.AlarmReceiver;
import com.example.tasks_list.R;
import com.example.tasks_list.Utilities.AsyncManager;
import com.example.tasks_list.Utilities.FilterSettings;
import com.example.tasks_list.Utilities.GlobalFunctions;
import com.example.tasks_list.Utilities.RecyclerViewAdapter;
import com.example.tasks_list.Utilities.Settings;
import com.example.tasks_list.Utilities.SharedPreferencesManager;
import com.example.tasks_list.Utilities.SwipeToDeleteManager;
import com.example.tasks_list.Utilities.TaskViewHolder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements ActivityStarter, DatabaseAccessor, Serializable, DatabaseItemRemover {
    public static final String NOTIFICATIONS_TAG = "NOTIFICATIONS";
    private transient ExtendedFloatingActionButton addTaskButton;
    private transient RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private transient ImageView filtersImageView;
    private transient SearchView searchView;

    private static AppDatabase db;
    private static TasksDao tasksDao;
    private static AttachmentsDao attachmentsDao;

    private transient List<Task> tasks;
    private transient ExternalStorageManager externalStorageManager;
    private static boolean isSchedulerRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeExternalStorageManager();

        setupDatabase();
        tasks = loadAndFilterTasks();

        setupRecyclerView(tasks);

        initializeAddTaskButton();
        initializeFiltersMenuButton();
        initializeSearchView();
    }


    @Override
    protected void onStart() {
        super.onStart();
        startNotificationsScheduler();
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }


    private void setupRecyclerView(List<Task> tasks) {
        recyclerView = findViewById(R.id.recyclerview);

        int orientation = getScreenOrientation();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            recyclerView.setLayoutManager(new GridLayoutManager(this, LANDSCAPE_SPAN_COUNT));
        else
            recyclerView.setLayoutManager(new LinearLayoutManager(this));


        recyclerViewAdapter = new RecyclerViewAdapter(this, this, tasks);
        recyclerView.setAdapter(recyclerViewAdapter);

        setupItemSwipeAction();
    }


    private int getScreenOrientation() {
        return getResources().getConfiguration().orientation;
    }


    private void setupItemSwipeAction() {
        SwipeToDeleteManager swipeManager = new SwipeToDeleteManager(getApplicationContext(),
                this, SwipeType.TASK, (viewHolder, databaseItemRemover) -> {
            Task task = ((TaskViewHolder) viewHolder).getTask();
            databaseItemRemover.deleteTask(task);
        });

        swipeManager.attachToRecyclerView(recyclerView);
    }


    private void setupDatabase() {
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DATABASE_TASKS_NAME).build();

        tasksDao = db.taskDao();
        attachmentsDao = db.attachmentsDao();
    }


    private FilterSettings loadFilterSettings() {
        SharedPreferencesManager preferencesManager = new SharedPreferencesManager(this, Settings.SHARED_PREFERENCES_FILENAME);

        boolean hideCompleted = preferencesManager.getBoolean(Settings.FILTER_HIDE_COMPLETED_KEY);
        boolean filterByCategory = preferencesManager.getBoolean(Settings.FILTER_BY_CATEGORY_SWITCH_KEY);
        Category filteredCategory;

        try {
            String categoryString = preferencesManager.getString(Settings.FILTER_CATEGORY_KEY);
            filteredCategory = Category.valueOf(categoryString.toUpperCase());
        } catch (PreferencesNoDataFoundException e) {
            filteredCategory = null;
        }

        return new FilterSettings(hideCompleted, filterByCategory, filteredCategory);
    }


    private List<Task> loadTasksFromDb() {
        CompletableFuture<List<Task>> asyncTask = CompletableFuture.supplyAsync(() -> tasksDao.getAll());

        try {
            Log.d(LOG_TAG_DATABASE, "Got all tasks!");
            return asyncTask.get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }

    }


    private List<Task> filterTasks(FilterSettings filterSettings) {
        List<Task> filteredList = new ArrayList<>(tasks);
        List<Task> tasksToRemove = new ArrayList<>();

        for (Task task : filteredList) {
            if (filterSettings.isHideCompleted() && task.getStatus() == TaskStatus.COMPLETED) {
                tasksToRemove.add(task);
                continue;
            }

            if (filterSettings.isFilterByCategory() && !task.getCategory().equals(filterSettings.getFilteredCategory())) {
                tasksToRemove.add(task);
            }
        }

        filteredList.removeAll(tasksToRemove);

        return filteredList;
    }


    private void initializeAddTaskButton() {
        addTaskButton = findViewById(R.id.fab);

        addTaskButton.setOnClickListener(v -> {
            Intent intent = GlobalFunctions.getIntent(this, AddTaskActivity.class);
            intent.putExtra(DATABASE_OBJECT_PASS_KEY, this);
            startActivity(intent);
        });

    }


    private void initializeFiltersMenuButton() {
        filtersImageView = findViewById(R.id.filter_imageview);

        filtersImageView.setOnClickListener(v -> {
            Intent intent = GlobalFunctions.getIntent(this, FiltersActivity.class);
            startActivity(intent);
        });

    }


    private void initializeSearchView() {
        searchView = findViewById(R.id.searchview);

        setSearchViewTextSize();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();
                String taskTitle;

                List<Task> filteredList = new ArrayList<>();

                for (Task task : tasks) {
                    taskTitle = task.getTitle().toLowerCase();

                    if (taskTitle.contains(newText))
                        filteredList.add(task);

                }

                recyclerViewAdapter.updateDataSet(filteredList);
                return true;
            }
        });
    }


    private void initializeExternalStorageManager() {
        try {
            externalStorageManager = new ExternalStorageManager(this, this, null);
        } catch (IOException e) {
            e.printStackTrace();
            externalStorageManager = null;
        }
    }


    private List<Task> loadAndFilterTasks() {
        tasks = loadTasksFromDb();
        FilterSettings filterSettings = loadFilterSettings();
        tasks = filterTasks(filterSettings);

        fillTaskAttachmentsInfo();

        return tasks;
    }


    private void fillTaskAttachmentsInfo() {
        CompletableFuture<Void> asyncTask = CompletableFuture.supplyAsync(() -> {
            long id;

            for (Task task : tasks) {
                id = attachmentsDao.hasAttachments(task.getId());
                task.setAttachments(id != 0);
            }

            return null;
        });

        try {
            Log.d(LOG_TAG_DATABASE, "Got all tasks!");
            asyncTask.get();
        } catch (ExecutionException | InterruptedException e) {

        }

    }


    private void setSearchViewTextSize() {
        int pxValue = (int) getResources().getDimension(R.dimen.main_search_view_text_size);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float scaledDensity = displayMetrics.scaledDensity;
        float spValue = pxValue / scaledDensity;

        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(SP, spValue);

        ViewGroup.LayoutParams params = autoComplete.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        autoComplete.setLayoutParams(params);
    }


    public void updateData() {
        tasks = loadAndFilterTasks();
        runOnUiThread(() -> recyclerViewAdapter.updateDataSet(tasks));
    }


    @Override
    public void deleteTask(Task task) {
        AsyncManager.runAsyncTask(() -> {
            List<Attachment> attachments = attachmentsDao.getAll(task.getId());
            externalStorageManager.deleteAttachmentsFromExternalStorage(attachments);

            attachmentsDao.deleteTaskAttachments(task.getId());

            tasksDao.deleteTask(task);
            Log.d(LOG_TAG_DATABASE, String.format("Task: %s DELETED!", task.getTitle()));
            updateData();
        });
    }


    @Override
    public void deleteAttachment(Attachment attachment) {
        AsyncManager.runAsyncTask(() -> {
            getAttachmentsDao().deleteAttachment(attachment);
            Log.d(LOG_TAG_DATABASE, String.format("Attachment: %s DELETED!", attachment.getFilename()));
        });
    }


    @Override
    public void startActivityFromIntent(Intent intent) {
        startActivity(intent);
    }


    @Override
    public TasksDao getTasksDao() {
        return tasksDao;
    }


    @Override
    public AttachmentsDao getAttachmentsDao() {
        return attachmentsDao;
    }


    /* NOTIFICATIONS */


    public void startNotificationsScheduler() {
        if (isSchedulerRunning) {
            Log.d(NOTIFICATIONS_TAG, "SCHEDULER ALREADY RUNNING");
            return;
        }

        isSchedulerRunning = true;
        Log.d(NOTIFICATIONS_TAG, "SCHEDULER STARTED");

        createNotificationChannel();

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        int delay = calculateDelayToTopMinute();
        calendar.add(Calendar.MILLISECOND, delay);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                NOTIFICATIONS_INTERVAL_MILLISECONDS,
                pendingIntent
        );
    }


    private int calculateDelayToTopMinute() {
        long currentTime = System.currentTimeMillis();
        long nextMinute = ((currentTime / 60_000) + 1) * 60_000; // Next full minute
        nextMinute += 15_000; // 15sec to full minute
        nextMinute += 5_000; // delay from top minute

        return (int) (nextMinute - currentTime);
    }


    private void createNotificationChannel() {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(NOTIFICATIONS_CHANNEL_ID, Settings.NOTIFICATIONS_CHANNEL_NAME, importance);
        channel.setDescription(Settings.NOTIFICATIONS_CHANNEL_DESCRIPTION);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}