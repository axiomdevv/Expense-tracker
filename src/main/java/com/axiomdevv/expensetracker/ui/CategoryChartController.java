package com.axiomdevv.expensetracker.ui;

import com.axiomdevv.expensetracker.model.Category;
import com.axiomdevv.expensetracker.model.Expense;
import com.axiomdevv.expensetracker.model.ExpenseSummary;
import com.axiomdevv.expensetracker.service.ExpenseAnalytics;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.shape.Circle;

import java.util.Map;

/** Owns the category and income-versus-expense doughnut chart. */
public class CategoryChartController {
    @FXML private PieChart categoryChart;
    @FXML private Label chartEmptyLabel;
    @FXML private ToggleButton categoryToggle;
    @FXML private ToggleButton incomeExpenseToggle;
    @FXML private ToggleGroup categoryModeGroup;


    private ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private ExpenseAnalytics analytics = new ExpenseAnalytics();

    public void initialize() {
        categoryModeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });
    }

    public void setData(ObservableList<Expense> expenses, ExpenseAnalytics analytics) {
        this.expenses = expenses;
        this.analytics = analytics;
        refresh();
    }

    @FXML
    public void refresh() {
        ObservableList<PieChart.Data> slices = categoryToggle.isSelected()
                ? categorySlices()
                : incomeExpenseSlices();

        categoryChart.setData(slices);
        boolean empty = slices.isEmpty();
        categoryChart.setVisible(!empty);
        chartEmptyLabel.setVisible(empty);
        chartEmptyLabel.setManaged(empty);
    }

    private ObservableList<PieChart.Data> categorySlices() {
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();
        Map<Category, Double> totals = analytics.expensesByCategory(expenses);
        double grandTotal = totals.values().stream().mapToDouble(Double::doubleValue).sum();

        totals.forEach((category, amount) -> slices.add(new PieChart.Data(
                percentageLabel(category.getDisplayName(), amount, grandTotal),
                amount
        )));
        return slices;
    }

    private ObservableList<PieChart.Data> incomeExpenseSlices() {
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();
        ExpenseSummary summary = analytics.calculateSummary(expenses);
        double grandTotal = summary.income() + summary.expenses();

        if (summary.income() > 0) {
            slices.add(new PieChart.Data(
                    percentageLabel("Income", summary.income(), grandTotal),
                    summary.income()
            ));
        }
        if (summary.expenses() > 0) {
            slices.add(new PieChart.Data(
                    percentageLabel("Expenses", summary.expenses(), grandTotal),
                    summary.expenses()
            ));
        }
        return slices;
    }

    private String percentageLabel(String name, double value, double total) {
        double percentage = total == 0 ? 0 : value / total * 100;
        return "%s (%.0f%%)".formatted(name, percentage);
    }
}
