package com.example.tasks_list.Attachments;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasks_list.Database.Attachment;
import com.example.tasks_list.R;

public class AttachmentViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final TextView filenameTextView;
    private String filename;

    public AttachmentViewHolder(@NonNull View itemView, @NonNull Context context) {
        super(itemView);
        this.context = context;

        filenameTextView = itemView.findViewById(R.id.filename_text_view);
    }

    public void setData(@NonNull String filename) {
        this.filename = filename;
        setFileName(filename);
    }


    public void setFileName(String filename) {
        if (filename == null)
            return;

        filenameTextView.setText(filename);
    }


    public String getFilename() {
        return filenameTextView.getText().toString();
    }
}
