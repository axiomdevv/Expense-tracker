package com.axiomdevv.expensetracker;


import javafx.beans.property.*;

public class Expense {
    private StringProperty description;
    private StringProperty date;
    private ObjectProperty<Category> category;
    private DoubleProperty amount;

    public Expense(){
        this.description = new SimpleStringProperty("");
        this.date = new SimpleStringProperty("");
        this.category = new SimpleObjectProperty<>();
        this.amount = new SimpleDoubleProperty(0);
    }

    public Expense(String description,String date,Category category,double amount){
        this();
        this.description.set(description);
        this.date.set(date);
        this.category.set(category) ;
        this.amount.set(amount);
    }

    public String getDescription() {
        return description.get();
    }
    public void setDescription(String description) {
        this.description.set(description);
    }
    public StringProperty descriptionProperty() {
        return description;
    }

    public Category getCategory() {
        return category.get();
    }
    public void setCategory(Category category) {
        this.category.set(category);
    }
    public ObjectProperty<Category> categoryProperty() {
        return category;
    }

    public double getAmount() {
        return amount.get();
    }
    public void setAmount(double amount) {
        this.amount.set(amount);
    }
    public DoubleProperty amountProperty() {
        return amount;
    }

    public String getDate() {
        return date.get();
    }
    public void setDate(String date) {
        this.date.set(date);
    }
    public StringProperty dateProperty() {
        return date;
    }

}
