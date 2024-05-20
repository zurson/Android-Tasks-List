package com.example.tasks_list.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface AttachmentsDao {

    @Query("SELECT * FROM attachment")
    List<Attachment> getAll();

    @Query("SELECT * FROM attachment WHERE taskId = :taskId")
    List<Attachment> getAll(long taskId);

    @Insert
    void insert(Attachment attachment);

    @Query("DELETE FROM attachment")
    void clearDb();

    @Upsert
    void upsertAttachment(Attachment attachment);

    @Delete
    void deleteAttachment(Attachment attachment);

    @Query("DELETE FROM attachment WHERE filename = :filename")
    void deleteAttachment(String filename);

    @Query("DELETE FROM attachment WHERE taskId = :taskId")
    void deleteTaskAttachments(long taskId);

    @Query("SELECT * FROM attachment WHERE taskId = :taskId LIMIT 1")
    long hasAttachments(long taskId);
}
