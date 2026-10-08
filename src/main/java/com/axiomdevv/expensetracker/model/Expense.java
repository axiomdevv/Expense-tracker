package com.axiomdevv.expensetracker.model;

/** A single transaction. Income is positive and an expense is negative. */
public class Expense {
    private String description;
    private String date;
    private Category category;
    private double amount;

    public Expense() {
        // Jackson needs a no-argument constructor when loading data.json.
    }

    public Expense(String description, String date, Category category, double amount) {
        this.description = description;
        this.date = date;
        this.category = category;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
