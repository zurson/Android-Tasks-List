package com.example.tasks_list.Utilities;

import static com.example.tasks_list.Utilities.Settings.DATABASE_OBJECT_PASS_KEY;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasks_list.Activities.TaskInfoActivity;
import com.example.tasks_list.Database.Task;
import com.example.tasks_list.Enums.TaskStatus;
import com.example.tasks_list.Interfaces.ActivityStarter;
import com.example.tasks_list.Interfaces.DatabaseAccessor;
import com.example.tasks_list.R;

import java.io.Serializable;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<TaskViewHolder> implements Serializable {

    private final Context context;
    private final ActivityStarter activityStarter;
    private final DatabaseAccessor databaseAccessor;
    private List<Task> tasks;

    public RecyclerViewAdapter(ActivityStarter activityStarter, DatabaseAccessor databaseAccessor, List<Task> tasks) {
        this.context = (Context) activityStarter;
        this.activityStarter = activityStarter;
        this.tasks = tasks;
        this.databaseAccessor = databaseAccessor;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_view, parent, false);
        return new TaskViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        if (task.getStatus() == TaskStatus.COMPLETED)
            holder.setNotificationsHidden(true);

        holder.setData(task);

        setOnTaskClickListener(task, holder.itemView);
    }

    private void setOnTaskClickListener(Task task, View itemView) {
        itemView.setOnClickListener(v -> {
            Intent intent = GlobalFunctions.getIntent(context, TaskInfoActivity.class);
            intent.putExtra(TaskInfoActivity.TASK_INFO_KEY, task);
            intent.putExtra(DATABASE_OBJECT_PASS_KEY, (Serializable) databaseAccessor);
            activityStarter.startActivityFromIntent(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateDataSet(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

}
