package com.example.tasks_list.Exceptions;

public class PreferencesNoDataFoundException extends Exception{

    public PreferencesNoDataFoundException() {
    }

    public PreferencesNoDataFoundException(String message) {
        super(message);
    }

}
