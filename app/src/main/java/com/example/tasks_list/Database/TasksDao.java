package com.example.tasks_list.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface TasksDao {
    @Query("SELECT * FROM task ORDER BY endDate")
    List<Task> getAll();

    @Insert
    void insert(Task task);

    @Query("DELETE FROM task")
    void clearDb();

    @Upsert
    long upsertTask(Task task);

    @Delete
    void deleteTask(Task task);

//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    List<Task> loadAllByIds(int[] userIds);

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    User findByName(String first, String last);

//    @Insert
//    void insertAll(User... users);

//    @Delete
//    void delete(User user);
}
