package com.example.tasks_list.Utilities;

import static com.example.tasks_list.Utilities.Settings.LOG_TAG_DATABASE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasks_list.Attachments.AttachmentRecyclerViewAdapter;
import com.example.tasks_list.Attachments.AttachmentViewHolder;
import com.example.tasks_list.Database.Attachment;
import com.example.tasks_list.Database.AttachmentsDao;
import com.example.tasks_list.Database.Task;
import com.example.tasks_list.Database.TasksDao;
import com.example.tasks_list.Enums.Category;
import com.example.tasks_list.Enums.SwipeType;
import com.example.tasks_list.ExternalStorage.ExternalStorageManager;
import com.example.tasks_list.Interfaces.AttachmentSelector;
import com.example.tasks_list.Interfaces.DatabaseAccessor;
import com.example.tasks_list.Interfaces.DatabaseItemRemover;
import com.example.tasks_list.R;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public abstract class TaskManager extends AppCompatActivity implements DatabaseItemRemover, AttachmentSelector, DatabaseAccessor {

    private static final String END_DATE_SAVE_KEY = "END_DATE_SAVE_KEY";
    private static final String SELECTED_CATEGORY_SAVE_KEY = "SELECTED_CATEGORY_SAVE_KEY";
    private static final int MIN_TIME_PERIOD_FROM_NOW = 10; // in minutes
    private static final int MAX_SEEKBAR_VALUE = 60;
    private static final String ALERT_BEFORE_TEXT_PREFIX = " min before";
    private static final String DATE_NOT_SELECTED_TEXT = "DATE NOT SELECTED";
    private static final String ERROR_TITLE = "Error!";
    private static final String ERROR_BUTTON_TEXT = "CLOSE";
    private static final String TIME_ERROR_MESSAGE = "You cannot select a time earlier than " + MIN_TIME_PERIOD_FROM_NOW + " minutes from now.";
    private static final String EXTERNAL_STORAGE_TAG = "EXTERNAL STORAGE";
    public static final String UNABLE_TO_SETUP_EXTERNAL_STORAGE_MANAGER_MSG = "Unable to setup ExternalStorageManager!";

    protected DatabaseAccessor databaseAccessor;

    protected EditText titleEditText, descriptionEditText;
    protected AutoCompleteTextView categoriesAutoCompleteTextView;
    protected Button selectDateButton, addAttachmentButton, mainButton, extraButton;
    protected CheckBox notificationsCheckBox;
    protected TextView alertBeforeTextView, selectedDateTextView, startDateLabel, startDateTextView;
    protected SeekBar notificationSeekBar;
    protected Date endDate;
    protected TextInputLayout titleInputLayout, descriptionInputLayout, categoriesInputLayout;

    private ArrayAdapter<String> categoriesAdapter;
    private Category selectedCategory;
    private List<String> categories;

    private AttachmentRecyclerViewAdapter attachmentAdapter;
    protected RecyclerView attachmentRecyclerView;
    private ExternalStorageManager externalStorageManager;
    private SwipeToDeleteManager swipeManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_manager);

        databaseAccessor = getDatabaseAccessor();

        setupLayoutElements();

        initializeCategoriesItems();
        initializeNotificationsSector();
        initializeEndDateSector();

        initializeMainButtonCondition();
        initializeExtraButtonCondition();
    }

    private void setupLayoutElements() {
        titleEditText = findViewById(R.id.title_edittext);

        descriptionEditText = findViewById(R.id.description_edittext);

        categoriesAutoCompleteTextView = findViewById(R.id.categories_spinner);

        selectDateButton = findViewById(R.id.date_button);
        mainButton = findViewById(R.id.confirm_button);
        extraButton = findViewById(R.id.extra_button);
        addAttachmentButton = findViewById(R.id.add_attachment_button);

        notificationsCheckBox = findViewById(R.id.notifications_checkbox);

        alertBeforeTextView = findViewById(R.id.alert_before_textview);
        selectedDateTextView = findViewById(R.id.selected_date_textview);
        startDateLabel = findViewById(R.id.start_date_label);
        startDateTextView = findViewById(R.id.start_date_textview);

        notificationSeekBar = findViewById(R.id.notification_seekbar);

        titleInputLayout = findViewById(R.id.title_text_input_layout);
        descriptionInputLayout = findViewById(R.id.description_text_input_layout);
        categoriesInputLayout = findViewById(R.id.categories_text_input_layout);

        setDefaultHintColor(titleInputLayout);
        setDefaultHintColor(descriptionInputLayout);
        setDefaultHintColor(categoriesInputLayout);
    }


    private void setDefaultHintColor(TextInputLayout textInputLayout) {
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.task_manager_hint_text_color)));
    }


    /* Attachments */


    protected void initializeAttachmentsSector(Long taskId) {
        List<Attachment> attachments = getAllAttachments(taskId);

        try {
            externalStorageManager = new ExternalStorageManager(this, this, attachments);
        } catch (IOException e) {
            GlobalFunctions.showAlertDialog(this, ERROR_TITLE, UNABLE_TO_SETUP_EXTERNAL_STORAGE_MANAGER_MSG, this::backToMainActivity);
            Log.e(EXTERNAL_STORAGE_TAG, UNABLE_TO_SETUP_EXTERNAL_STORAGE_MANAGER_MSG);
        }

        registerAddAttachmentButton();

        setupAttachmentsRecyclerView(attachments);
    }


    private void registerAddAttachmentButton() {
        externalStorageManager.registerButton(addAttachmentButton, this);
    }


    protected List<Attachment> getAllAttachments(Long taskId) {
        if (taskId == null)
            return new ArrayList<>();

        CompletableFuture<List<Attachment>> asyncTask = CompletableFuture.supplyAsync(() -> databaseAccessor.getAttachmentsDao().getAll(taskId));

        try {
            Log.d(LOG_TAG_DATABASE, "Got all attachments from task ID: " + taskId);
            return asyncTask.get();
        } catch (ExecutionException | InterruptedException e) {
            return new ArrayList<>();
        }
    }


    protected void setupAttachmentsRecyclerView(List<Attachment> attachments) {
        attachmentRecyclerView = findViewById(R.id.attachments_recycler_view);
        attachmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> filenames = attachments.stream().map(Attachment::getFilename).collect(Collectors.toList());

        attachmentAdapter = new AttachmentRecyclerViewAdapter(this, filenames, externalStorageManager);
        attachmentRecyclerView.setAdapter(attachmentAdapter);

        setupAttachmentSwipeAction();
    }


    protected void refreshAttachmentsRecyclerView(Long taskId) {
        attachmentAdapter.updateDataSet(getAllAttachments(taskId).stream().map(Attachment::getFilename).collect(Collectors.toList()));
        ;
    }


    private void setupAttachmentSwipeAction() {
        swipeManager = new SwipeToDeleteManager(getApplicationContext(), this,
                SwipeType.ATTACHMENT, (viewHolder, databaseItemRemover) -> {
            String attachmentFilename = ((AttachmentViewHolder) viewHolder).getFilename();
            removeAttachmentFromList(attachmentFilename);
        });

        attachSwipeManager();
    }


    protected void attachSwipeManager() {
        swipeManager.attachToRecyclerView(attachmentRecyclerView);
    }


    protected void unAttachSwipeManager() {
        swipeManager.attachToRecyclerView(null);
    }


    private void removeAttachmentFromList(String filename) {
        attachmentAdapter.removeAttachment(filename);
        externalStorageManager.removeAttachmentFromList(filename);
    }


    protected void saveAllAttachmentsToDbAndStorage(long taskId) throws IOException {
        externalStorageManager.saveAllAttachmentsToDbAndStorage(taskId);
    }


    @Override
    public void notifyAttachmentSelection(String filename) {
        attachmentAdapter.addAttachment(filename);
    }


    /* Categories */

    private List<String> getCategoriesAsStrings() {
        return Arrays.stream(Category.values())
                .map(category -> GlobalFunctions.capitalizeFirstLetter(category.toString()))
                .collect(Collectors.toList());
    }


    private void initializeCategoriesItems() {
        categories = getCategoriesAsStrings();

        this.categoriesAdapter = new ArrayAdapter<>(this, R.layout.task_manager_list_item, categories);
        this.categoriesAutoCompleteTextView.setAdapter(this.categoriesAdapter);

        setOnCategoryChangeListener();
    }


    private void setOnCategoryChangeListener() {
        this.categoriesAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            Category selectedCategory = convertToCategory((String) parent.getItemAtPosition(position));
            setSelectedCategory(selectedCategory);

            this.selectedCategory = selectedCategory;
        });
    }


    protected void setSelectedCategory(Category selectedCategory) {
        this.categoriesAutoCompleteTextView.setText(GlobalFunctions.capitalizeFirstLetter(selectedCategory.toString()), false);
        this.selectedCategory = selectedCategory;
    }


    protected Category getSelectedCategory() {
        return selectedCategory;
    }


    private Category convertToCategory(String category) {
        category = category.toUpperCase();
        return Category.valueOf(category);
    }


    /* Notifications */


    private void initializeNotificationsSector() {
        this.notificationSeekBar.setVisibility(View.GONE);
        this.alertBeforeTextView.setVisibility(View.GONE);

        initializeSeekBarListener();
        initializeNotificationsCheckBoxListener();

        this.notificationSeekBar.setProgress(0);
        this.notificationsCheckBox.setActivated(false);

        setAlertBeforeTextView(0);

        setNotificationsCheckboxEnabled(false);
    }


    protected void setNotificationsStatus(boolean status, int timeBeforeAlert) {
        int state = status ? View.VISIBLE : View.GONE;

        this.notificationSeekBar.setVisibility(state);
        this.alertBeforeTextView.setVisibility(state);

        this.notificationSeekBar.setProgress(timeBeforeAlert);
    }


    private void initializeSeekBarListener() {
        this.notificationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setAlertBeforeTextView(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void initializeNotificationsCheckBoxListener() {
        this.notificationsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setNotificationsStatus(isChecked, this.notificationSeekBar.getProgress());
        });
    }


    private void setAlertBeforeTextView(int minutes) {
        alertBeforeTextView.setText(minutes + ALERT_BEFORE_TEXT_PREFIX);
    }


    protected boolean getNotificationsStatus() {
        return this.notificationsCheckBox.isChecked();
    }


    private int getAlertBeforeTime() {
        return this.notificationSeekBar.getProgress();
    }


    private void resetAlertBefore() {
        setAlertBeforeTextView(0);
        notificationSeekBar.setProgress(0);
    }


    private void setNewSeekBarCap(Date date) {
        Date currentDate = new Date();

        long diffInMilliseconds = Math.abs(date.getTime() - currentDate.getTime());
        long diffInMinutes = diffInMilliseconds / (MAX_SEEKBAR_VALUE * 1000);

        int maxSeekBarValue;
        if (diffInMinutes >= MAX_SEEKBAR_VALUE) {
            maxSeekBarValue = MAX_SEEKBAR_VALUE;
        } else {
            maxSeekBarValue = (int) diffInMinutes;
        }

        notificationSeekBar.setMax(maxSeekBarValue);
    }


    protected void setNotificationsCheckboxEnabled(boolean enabled) {
        this.notificationsCheckBox.setEnabled(enabled);
    }


    /* Select Date */


    private void initializeEndDateSector() {
        setSelectDateButtonClickListener();
        this.selectedDateTextView.setText(DATE_NOT_SELECTED_TEXT);
    }


    private void setSelectDateButtonClickListener() {
        this.selectDateButton.setOnClickListener(v -> {
            showDateTimePicker(this);
        });
    }


    protected void setDisplayedDate(Date date) {
        this.selectedDateTextView.setText(GlobalFunctions.formatDate(date));
    }


    private void showDateTimePicker(final Context context) {
        TimeZone deviceTimeZone = TimeZone.getDefault();
        final Calendar calendar = Calendar.getInstance(deviceTimeZone);

        // DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);

            // TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                Calendar minTime = Calendar.getInstance(deviceTimeZone);
                minTime.add(Calendar.MINUTE, MIN_TIME_PERIOD_FROM_NOW);

                if (calendar.before(minTime))
                    showErrorDialog(TIME_ERROR_MESSAGE);
                else {
                    setDisplayedDate(calendar.getTime());
                    this.endDate = calendar.getTime();

                    setNotificationsCheckboxEnabled(true);
                    resetAlertBefore();
                    setNewSeekBarCap(calendar.getTime());
                }

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            timePickerDialog.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }


    /* Main Button */


    protected abstract void initializeMainButtonCondition();


    protected boolean allRequiredFieldsFilledIn() {
        String title = titleEditText.getText().toString().trim();
        String date = selectedDateTextView.getText().toString();

        return !title.isEmpty() && !date.equals(DATE_NOT_SELECTED_TEXT) && selectedCategory != null;
    }


    protected Task createTaskFromInput() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        Category category = getSelectedCategory();
        Date startDate = Date.from(Calendar.getInstance().toInstant());
        boolean notifications = getNotificationsStatus();
        boolean attachments = externalStorageManager.hasAttachments();
        int alertBeforeTime = getAlertBeforeTime();

        return new Task(title, description, category, startDate, endDate, notifications, attachments, alertBeforeTime);
    }


    protected void setEndDate(Date date) {
        endDate = date;
    }


    /* Extra button */


    protected abstract void initializeExtraButtonCondition();


    /* Other */


    private DatabaseAccessor getDatabaseAccessor() {
        return (DatabaseAccessor) getIntent().getSerializableExtra(Settings.DATABASE_OBJECT_PASS_KEY);
    }


    protected void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(ERROR_TITLE);
        builder.setMessage(message);
        builder.setPositiveButton(ERROR_BUTTON_TEXT, null);
        builder.show();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(END_DATE_SAVE_KEY, endDate);
        outState.putSerializable(SELECTED_CATEGORY_SAVE_KEY, selectedCategory);
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Date date = (Date) savedInstanceState.getSerializable(END_DATE_SAVE_KEY);
        if (date != null) {
            endDate = date;
            setDisplayedDate(endDate);
        }

        initializeCategoriesItems();

        Category category = (Category) savedInstanceState.getSerializable(SELECTED_CATEGORY_SAVE_KEY);
        if (category != null)
            setSelectedCategory(category);
    }


    @Override
    public void deleteTask(Task task) {
    }


    @Override
    public void deleteAttachment(Attachment attachment) {
    }


    @Override
    public TasksDao getTasksDao() {
        return databaseAccessor.getTasksDao();
    }


    @Override
    public AttachmentsDao getAttachmentsDao() {
        return databaseAccessor.getAttachmentsDao();
    }


    protected void backToMainActivity() {
        runOnUiThread(() -> getOnBackPressedDispatcher().onBackPressed());
    }


    protected Long insertTaskToDatabase(Task task) throws ExecutionException, InterruptedException {
        CompletableFuture<Long> asyncTask = CompletableFuture.supplyAsync(() -> getTasksDao().upsertTask(task));
        return asyncTask.get();
    }

}
