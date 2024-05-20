package com.example.tasks_list.Database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Attachment implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long taskId;

    private String filename;


    public Attachment(long taskId, @NonNull String filename) {
        this.taskId = taskId;
        this.filename = filename;
    }


    public long getId() {
        return id;
    }

    public void setId(long attachmentId) {
        this.id = attachmentId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "attachmentId=" + id +
                ", taskId=" + taskId +
                ", filename='" + filename + '\'' +
                '}';
    }
}
