package com.example.tasks_list.ExternalStorage;

import androidx.annotation.NonNull;

import lombok.Getter;

public class FilenameSplitter {

    private final String filename;

    private final boolean hasExtension;

    @Getter
    private String extension;

    @Getter
    private String baseName;


    public FilenameSplitter(@NonNull String filename) {
        this.filename = filename;
        this.hasExtension = hasExtension();

        setExtension();
        setName();
    }

    private boolean hasExtension() {
        return filename.lastIndexOf('.') != -1;
    }

    private void setExtension() {
        extension = hasExtension ? filename.substring(filename.lastIndexOf('.') + 1) : "";
    }

    private void setName() {
        baseName = hasExtension ? filename.substring(0, filename.lastIndexOf('.')) : filename;
    }

}
