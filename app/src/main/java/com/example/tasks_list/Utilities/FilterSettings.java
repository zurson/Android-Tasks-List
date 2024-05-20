package com.example.tasks_list.Utilities;

import com.example.tasks_list.Enums.Category;

public class FilterSettings {

    private boolean hideCompleted;
    private boolean filterByCategory;
    private Category filteredCategory;

    public FilterSettings(boolean hideCompleted, boolean filterByCategory, Category filteredCategory) {
        this.hideCompleted = hideCompleted;
        this.filterByCategory = filterByCategory;
        this.filteredCategory = filteredCategory;
    }

    public boolean isHideCompleted() {
        return hideCompleted;
    }

    public boolean isFilterByCategory() {
        return filterByCategory;
    }

    public Category getFilteredCategory() {
        return filteredCategory;
    }

}
