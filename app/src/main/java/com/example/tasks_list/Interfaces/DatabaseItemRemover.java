package com.example.tasks_list.Interfaces;

import com.example.tasks_list.Database.Attachment;
import com.example.tasks_list.Database.Task;

public interface DatabaseItemRemover {

    void deleteTask(Task task);
    void deleteAttachment(Attachment attachment);

}
