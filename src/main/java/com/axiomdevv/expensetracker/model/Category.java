package com.axiomdevv.expensetracker.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** The supported income and expense categories. */
public enum Category {
    INCOME("Income"),
    HOUSING("Housing"),
    UTILITIES("Utilities"),
    FOOD("Food"),
    TRANSPORT("Transport"),
    INSURANCE("Insurance"),
    DEBT("Debt"),
    HEALTHCARE("Healthcare"),
    ENTERTAINMENT("Entertainment"),
    CLOTHING("Clothing");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isIncome() {
        return this == INCOME;
    }

    @JsonValue
    @Override
    public String toString() {
        return displayName;
    }

    @JsonCreator
    public static Category fromString(String value) {
        if (value == null) {
            return null;
        }

        for (Category category : values()) {
            if (category.displayName.equalsIgnoreCase(value)
                    || category.name().equalsIgnoreCase(value)) {
                return category;
            }
        }

        throw new IllegalArgumentException("Unknown category: " + value);
    }
}
