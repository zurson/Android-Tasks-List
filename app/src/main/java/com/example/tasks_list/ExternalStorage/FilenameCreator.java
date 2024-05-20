package com.example.tasks_list.ExternalStorage;

import androidx.annotation.NonNull;

public class FilenameCreator {

    private final String baseName;
    private final String extension;

    public FilenameCreator(@NonNull String baseName, @NonNull String extension) {
        this.baseName = baseName;
        this.extension = extension;
    }

    public String createFilename(@NonNull String uniqueIdentifier) {
        return baseName + uniqueIdentifier + getDot() + extension;
    }

    public String createFilename() {
        return baseName + getDot() + extension;
    }

    private String getDot() {
        return hasExtension() ? "." : " ";
    }

    private boolean hasExtension() {
        return !extension.trim().isEmpty();
    }

}
