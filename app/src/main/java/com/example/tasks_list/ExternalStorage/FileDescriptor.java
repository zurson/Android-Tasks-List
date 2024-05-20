package com.example.tasks_list.ExternalStorage;

import android.net.Uri;

import lombok.Getter;

@Getter
public class FileDescriptor {

    private final Uri uri;

    private final String filename;

    public FileDescriptor(Uri uri, String filename) {
        this.uri = uri;
        this.filename = filename;
    }


}
