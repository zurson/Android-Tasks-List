package com.example.tasks_list.Database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.tasks_list.Enums.Category;
import com.example.tasks_list.Enums.TaskStatus;
import com.example.tasks_list.Utilities.GlobalFunctions;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String title, description;

    private Category category;

    private Date startDate, endDate;

    private Boolean notifications, attachments;

    private Integer alertBeforeTime; // in minutes

    private TaskStatus status;

    private boolean beforeNotificationSent, endNotificationSent;


    public Task(@NonNull String title, @NonNull String description, @NonNull Category category,
                @NonNull Date startDate, @NonNull Date endDate, boolean notifications, boolean attachments, int alertBeforeTime) {

        this.title = title;
        this.description = description;
        this.category = category;

        setStartDate(startDate);
        setEndDate(endDate);

        this.notifications = notifications;
        this.alertBeforeTime = alertBeforeTime;

        this.attachments = attachments;
        this.status = TaskStatus.PENDING;

        this.beforeNotificationSent = false;
        this.endNotificationSent = false;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = GlobalFunctions.cutToMinutes(startDate);
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = GlobalFunctions.cutToMinutes(endDate);
    }

    public Boolean getNotifications() {
        return notifications;
    }

    public void setNotifications(Boolean notifications) {
        this.notifications = notifications;
    }

    public Boolean getAttachments() {
        return attachments;
    }

    public void setAttachments(Boolean attachments) {
        this.attachments = attachments;
    }

    public Integer getAlertBeforeTime() {
        return alertBeforeTime;
    }

    public void setAlertBeforeTime(Integer alertBeforeTime) {
        this.alertBeforeTime = alertBeforeTime;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public boolean isBeforeNotificationSent() {
        return beforeNotificationSent;
    }

    public void setBeforeNotificationSent(boolean beforeNotificationSent) {
        this.beforeNotificationSent = beforeNotificationSent;
    }

    public boolean isEndNotificationSent() {
        return endNotificationSent;
    }

    public void setEndNotificationSent(boolean endNotificationSent) {
        this.endNotificationSent = endNotificationSent;
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", notifications=" + notifications +
                ", attachments=" + attachments +
                ", status=" + status +
                '}';
    }

}
