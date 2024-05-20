package com.example.tasks_list.Utilities;

import static com.example.tasks_list.Utilities.Settings.ALERT_DIALOG_BUTTON_TEXT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GlobalFunctions {

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static Intent getIntent(Context context, Class<? extends AppCompatActivity> activityClass) {
        return new Intent(context, activityClass);
    }

    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        String formattedDate = dateFormat.format(date);
        String formattedTime = timeFormat.format(date);

        String text = String.format("%s\t\t\t%s", formattedDate, formattedTime);

        return capitalizeFirstLetter(text);
    }


    public static void showAlertDialog(Context context, String title, String message, Runnable closeAction) {
        if (context == null || title == null || message == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(ALERT_DIALOG_BUTTON_TEXT, (dialog, id) -> {
            if (closeAction != null)
                closeAction.run();

            dialog.cancel();
        });

        builder.setOnDismissListener(dialog -> {
            if (closeAction != null)
                closeAction.run();
        });

        Activity activity = (Activity) context;
        activity.runOnUiThread(() -> builder.create().show());
    }


    public static void showConfirmAlert(Context context, String message, Runnable condition) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage(message);

        builder.setPositiveButton("ACCEPT", (dialog, which) -> condition.run());

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    public static Date cutToMinutes(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTime();
    }

}
