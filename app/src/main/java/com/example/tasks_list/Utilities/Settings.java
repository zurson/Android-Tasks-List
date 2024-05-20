package com.example.tasks_list.Utilities;

import android.graphics.Color;

import com.example.tasks_list.Enums.Category;

public class Settings {

    private Settings() {
    }

    public static final int LANDSCAPE_SPAN_COUNT = 2;

    public final static String DATABASE_TASKS_NAME = "tasks";
    public final static String SHARED_PREFERENCES_FILENAME = "filter_settings";
    public final static String FILTER_HIDE_COMPLETED_KEY = "FILTER_HIDE_COMPLETED_KEY";
    public final static String FILTER_BY_CATEGORY_SWITCH_KEY = "FILTER_SWITCH_FILTER_BY_CATEGORY_KEY";
    public final static String FILTER_CATEGORY_KEY = "FILTER_CATEGORY_FILTER_BY_CATEGORY_KEY";
    public static final String DATABASE_OBJECT_PASS_KEY = "DB_PASS_KEY";
    public static final String ALERT_DIALOG_BUTTON_TEXT = "CLOSE";

    public static final Category DEFAULT_CATEGORY = Category.OTHER;

    public static final String LOG_TAG_DATABASE = "DATABASE";

    public static final String NOTIFICATIONS_CHANNEL_ID = "Notification_Channel";
    public static final String NOTIFICATIONS_CHANNEL_NAME = "Notifications Channel";
    public static final String NOTIFICATIONS_CHANNEL_DESCRIPTION = "Channel for displaying notifications";
    public static final long NOTIFICATIONS_INTERVAL_MILLISECONDS = 60_000; // 1 minute

    public static final int DELETE_SWIPE_COLOR = Color.RED;
}
