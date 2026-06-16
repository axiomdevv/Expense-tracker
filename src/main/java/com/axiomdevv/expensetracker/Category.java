package com.axiomdevv.expensetracker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    @Override
    public String toString() {
        return displayName;
    }

    public static String[] toStringArray(){
        String[] stringArray = new String[values().length];
        int i = 0;
        for(Category c : values()){
            stringArray[i++] = c.toString();
        }
        return stringArray;
    }

    @JsonCreator
    public static Category fromString(String displayName) {
        for (Category c : values()) {
            if (c.displayName.equals(displayName)) {
                return c;
            }
        }
        return null;
    }
}


