package com.example.tasks_list.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.tasks_list.Enums.Category;
import com.example.tasks_list.Exceptions.PreferencesNoDataFoundException;
import com.example.tasks_list.R;
import com.example.tasks_list.Utilities.GlobalFunctions;
import com.example.tasks_list.Utilities.Settings;
import com.example.tasks_list.Utilities.SharedPreferencesManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FiltersActivity extends AppCompatActivity {

    private static final String FILTERS_SAVED_TOAST = "Filters saved!";
    private ArrayAdapter<String> categoriesFilterSpinnerAdapter;
    private Spinner categoriesFilterSpinner;
    private SwitchCompat categoriesFilterSwitch, hideCompletedSwitch;
    private Button saveButton;
    private SharedPreferencesManager preferencesManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_settings);

        preferencesManager = new SharedPreferencesManager(this, Settings.SHARED_PREFERENCES_FILENAME);

        initializeHideCompletedFilter();
        initializeCategoriesFilter();
        initializeSaveButton();

        loadAndRestoreFilterSettings();
    }

    // Hide Completed Filter
    private void initializeHideCompletedFilter() {
        this.hideCompletedSwitch = findViewById(R.id.hide_completed_switch);
    }


    // Save Button
    private void initializeSaveButton() {
        this.saveButton = findViewById(R.id.filter_save_button);
        setSaveButtonClickListener();
    }

    private void setSaveButtonClickListener() {
        this.saveButton.setOnClickListener(v -> {
            saveFilters();
            Toast.makeText(this, FILTERS_SAVED_TOAST, Toast.LENGTH_SHORT).show();
            backToMainActivity();
        });
    }

    private void backToMainActivity() {
        runOnUiThread(() -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void saveFilters() {
        boolean hideCompleted = hideCompletedSwitch.isChecked();
        boolean categoriesFilter = categoriesFilterSwitch.isChecked();

        preferencesManager.saveBoolean(Settings.FILTER_HIDE_COMPLETED_KEY, hideCompleted);
        preferencesManager.saveBoolean(Settings.FILTER_BY_CATEGORY_SWITCH_KEY, categoriesFilter);

        if (categoriesFilter) {
            String selectedCategory = (String) categoriesFilterSpinner.getSelectedItem();
            preferencesManager.saveString(Settings.FILTER_CATEGORY_KEY, selectedCategory);
        } else
            preferencesManager.remove(Settings.FILTER_CATEGORY_KEY);
    }


    // Categories Filter
    private List<String> getSpinnerItemsAsStrings() {
        return Arrays.stream(Category.values())
                .map(category -> GlobalFunctions.capitalizeFirstLetter(category.toString()))
                .collect(Collectors.toList());
    }

    private void initializeCategoriesFilter() {
        this.categoriesFilterSwitch = findViewById(R.id.filter_by_category_switch);

        setupCategoriesFilterSpinner();
        setCategoriesFilterSpinnerVisible(false);

        setOnCheckedChangeListener(this.categoriesFilterSwitch, (buttonView, isChecked) -> {
            setCategoriesFilterSpinnerVisible(isChecked);
        });
    }

    private void setupCategoriesFilterSpinner() {
        this.categoriesFilterSpinner = findViewById(R.id.filter_categories_spinner);

        List<String> categories = getSpinnerItemsAsStrings();

        this.categoriesFilterSpinnerAdapter = new ArrayAdapter<>(this, R.layout.categories_filter_spinner_item, categories);
        this.categoriesFilterSpinnerAdapter.setDropDownViewResource(R.layout.categories_filter_spinner_item);

        this.categoriesFilterSpinner.setAdapter(this.categoriesFilterSpinnerAdapter);
    }

    private void setCategoriesFilterSpinnerVisible(boolean visible) {
        int state = visible ? View.VISIBLE : View.GONE;
        this.categoriesFilterSpinner.setVisibility(state);
    }


    // Other

    private void setOnCheckedChangeListener(SwitchCompat switchCompat, CompoundButton.OnCheckedChangeListener listener) {
        switchCompat.setOnCheckedChangeListener(listener);
    }

    private void setSelectedCategory(String selectedCategory) {
        List<String> categories = getSpinnerItemsAsStrings();
        String selected = GlobalFunctions.capitalizeFirstLetter(selectedCategory);

        int index = categories.indexOf(selected);
        if (index == -1)
            return;

        this.categoriesFilterSpinner.setSelection(index);
    }

    private void loadAndRestoreFilterSettings() {
        boolean hideCompleted = preferencesManager.getBoolean(Settings.FILTER_HIDE_COMPLETED_KEY);
        boolean categoriesFilter = preferencesManager.getBoolean(Settings.FILTER_BY_CATEGORY_SWITCH_KEY);

        this.hideCompletedSwitch.setChecked(hideCompleted);
        this.categoriesFilterSwitch.setChecked(categoriesFilter);

        if (categoriesFilter) {
            try {
                String category = preferencesManager.getString(Settings.FILTER_CATEGORY_KEY);
                setSelectedCategory(category);
            } catch (PreferencesNoDataFoundException ignored) {
            }
        }

    }

}
