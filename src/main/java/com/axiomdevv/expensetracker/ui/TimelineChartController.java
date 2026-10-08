package com.axiomdevv.expensetracker.ui;

import com.axiomdevv.expensetracker.model.Expense;
import com.axiomdevv.expensetracker.service.ExpenseAnalytics;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/** Owns the daily, monthly, and yearly expense timeline. */
public class TimelineChartController {
    private static final DateTimeFormatter DAILY_LABEL =
            DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter MONTHLY_LABEL =
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    @FXML private AreaChart<String, Number> timelineChart;
    @FXML private Label timelineEmptyLabel;
    @FXML private ToggleButton dayToggle;
    @FXML private ToggleButton monthToggle;
    @FXML private ToggleButton yearToggle;
    @FXML private ToggleGroup timelinePeriodGroup;

    private ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private ExpenseAnalytics analytics = new ExpenseAnalytics();

    public void initialize() {
        timelinePeriodGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
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
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        if (dayToggle.isSelected()) {
            addDailyData(series, analytics.dailyExpenses(expenses));
        } else if (monthToggle.isSelected()) {
            addMonthlyData(series, analytics.monthlyExpenses(expenses));
        } else if (yearToggle.isSelected()) {
            analytics.yearlyExpenses(expenses).forEach((year, amount) ->
                    series.getData().add(new XYChart.Data<>(year.toString(), amount)));
        }

        timelineChart.getData().clear();
        timelineChart.getData().add(series);
        boolean empty = series.getData().isEmpty();
        timelineChart.setVisible(!empty);
        timelineEmptyLabel.setVisible(empty);
        timelineEmptyLabel.setManaged(empty);
    }

    private void addDailyData(
            XYChart.Series<String, Number> series,
            Map<LocalDate, Double> totals
    ) {
        totals.forEach((date, amount) -> series.getData().add(
                new XYChart.Data<>(DAILY_LABEL.format(date), amount)
        ));
    }

    private void addMonthlyData(
            XYChart.Series<String, Number> series,
            Map<YearMonth, Double> totals
    ) {
        totals.forEach((month, amount) -> series.getData().add(
                new XYChart.Data<>(MONTHLY_LABEL.format(month), amount)
        ));
    }
}
