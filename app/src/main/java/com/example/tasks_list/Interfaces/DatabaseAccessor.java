package com.example.tasks_list.Interfaces;

import com.example.tasks_list.Database.AttachmentsDao;
import com.example.tasks_list.Database.TasksDao;

public interface DatabaseAccessor {

    TasksDao getTasksDao();
    AttachmentsDao getAttachmentsDao();
}
