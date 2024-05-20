package com.example.tasks_list.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.tasks_list.Exceptions.PreferencesNoDataFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferencesManager {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesManager(Context context, String filename) {
        this.preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        this.editor = this.preferences.edit();
    }

    public void saveStringList(List<String> list, String key) {
        Set<String> set = new HashSet<>(list);

        editor.putStringSet(key, set);

        editorApply();
    }

    public ArrayList<String> loadStringList(String key) throws PreferencesNoDataFoundException {
        Set<String> set = preferences.getStringSet(key, null);

        if (set == null)
            throw new PreferencesNoDataFoundException();

        return new ArrayList<>(set);
    }

    public void saveString(String key, String text) {
        editor.putString(key, text);
        editorApply();
    }

    public String getString(String key) throws PreferencesNoDataFoundException {
        String value = preferences.getString(key, null);

        if (value == null)
            throw new PreferencesNoDataFoundException();

        return value;
    }

    public void saveBoolean(String key, boolean bool) {
        editor.putBoolean(key, bool);
        editorApply();
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public void remove(String key) {
        editor.remove(key);
        editorApply();
    }

    private void editorApply() {
        editor.apply();
    }

}
