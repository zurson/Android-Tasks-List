package com.example.tasks_list.Utilities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasks_list.Database.Task;
import com.example.tasks_list.Enums.Category;
import com.example.tasks_list.Enums.TaskStatus;
import com.example.tasks_list.R;
import com.example.tasks_list.Utilities.GlobalFunctions;

import java.time.LocalDateTime;
import java.util.Date;

public class TaskViewHolder extends RecyclerView.ViewHolder {

    private final ImageView notifications, attachments;
    private final TextView title, category, endDate, status;
    private final CardView statusCardView;
    private final Context context;
    private Task task;

    public TaskViewHolder(@NonNull View itemView, Context context) {
        super(itemView);

        notifications = itemView.findViewById(R.id.notifications_imageview);
        attachments = itemView.findViewById(R.id.attachments_imageview);

        title = itemView.findViewById(R.id.title_textview);
        category = itemView.findViewById(R.id.category_textview);
        endDate = itemView.findViewById(R.id.end_date_textview);
        status = itemView.findViewById(R.id.status_textview);

        statusCardView = itemView.findViewById(R.id.status_cardview);

        this.context = context;
    }

    public void setData(@NonNull Task task) {
        this.task = task;

        this.setTitle(task.getTitle());
        this.setCategory(task.getCategory());
        this.setEndDate(task.getEndDate());
        this.setAttachments(task.getAttachments());
        this.setStatus(task.getStatus());
        this.setNotifications(task.getNotifications());
    }

    public Task getTask() {
        return this.task;
    }

    public void setNotificationsHidden(boolean hidden) {
        int state = hidden ? GONE : VISIBLE;
        notifications.setVisibility(state);
    }

    public void setNotifications(boolean status) {
        int imgId = status ? R.mipmap.ic_notifications_on : R.mipmap.ic_notifications_off;
        this.notifications.setImageResource(imgId);
    }

    public void setAttachments(boolean status) {
        int visibility = status ? VISIBLE : GONE;
        this.attachments.setVisibility(visibility);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setCategory(Category category) {
        category = category == null ? Settings.DEFAULT_CATEGORY : category;
        this.category.setText(GlobalFunctions.capitalizeFirstLetter(category.toString()));
    }

    public void setEndDate(Date endDate) {
        this.endDate.setText(GlobalFunctions.formatDate(endDate));
    }


    public void setStatus(TaskStatus status) {
        if (status == TaskStatus.PENDING)
            setNotificationsHidden(false);

        int colorId = status == TaskStatus.PENDING ? R.color.orange : R.color.green;
        int color = ContextCompat.getColor(context, colorId);
        this.statusCardView.setCardBackgroundColor(color);

        this.status.setText(GlobalFunctions.capitalizeFirstLetter(status.toString()));
    }


}