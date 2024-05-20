package com.example.tasks_list.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Task.class, Attachment.class}, version = 1)
@TypeConverters({DatabaseConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TasksDao taskDao();
    public abstract AttachmentsDao attachmentsDao();
}
