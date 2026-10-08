package com.axiomdevv.expensetracker.ui;

import com.axiomdevv.expensetracker.data.ExpenseRepository;
import com.axiomdevv.expensetracker.model.Expense;
import com.axiomdevv.expensetracker.model.ExpenseDraft;
import com.axiomdevv.expensetracker.service.ExpenseAnalytics;
import com.axiomdevv.expensetracker.service.ExpenseValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Coordinates the screen while delegating specialized work to small classes. */
public class TrackerController {
    @FXML private ToggleButton themeToggle;
    @FXML private Label statusLabel;

    // FXMLLoader adds "Controller" to each fx:include id when injecting it.
    @FXML private ExpenseFormController expenseFormController;
    @FXML private SummaryCardsController summaryCardsController;
    @FXML private ExpenseTableController expenseTableController;
    @FXML private CategoryChartController categoryChartController;
    @FXML private TimelineChartController timelineChartController;

    private final ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private final ExpenseAnalytics analytics = new ExpenseAnalytics();
    private final ExpenseValidator validator = new ExpenseValidator();
    private final ExpenseRepository repository = new ExpenseRepository(Path.of("data.json"));
    private final ThemeManager themeManager = new ThemeManager();

    public void initialize() {
        expenseFormController.setOnAdd(this::addExpense);
        expenseTableController.setOnDelete(this::deleteExpenses);
        expenseTableController.setItems(expenses);
        categoryChartController.setData(expenses, analytics);
        timelineChartController.setData(expenses, analytics);

        loadExpenses();
        refreshDashboard();
    }

    private void addExpense(ExpenseDraft draft) {
        List<String> errors = validator.validate(draft);
        if (!errors.isEmpty()) {
            expenseFormController.showErrors(errors);
            return;
        }

        Expense expense = validator.createExpense(draft);
        expenses.add(expense);
        if (saveExpenses()) {
            expenseFormController.clearForm();
            expenseFormController.showSuccess("Transaction added.");
            showStatus("All changes saved locally.", false);
            refreshDashboard();
        } else {
            expenses.remove(expense);
        }
    }

    private void deleteExpenses(List<Expense> selectedExpenses) {
        if (selectedExpenses.isEmpty() || !confirmDeletion(selectedExpenses.size())) {
            return;
        }

        List<Expense> previousExpenses = new ArrayList<>(expenses);
        expenses.removeAll(selectedExpenses);
        if (saveExpenses()) {
            showStatus(selectedExpenses.size() == 1
                    ? "Transaction deleted."
                    : selectedExpenses.size() + " transactions deleted.", false);
            refreshDashboard();
        } else {
            expenses.setAll(previousExpenses);
        }
    }

    private boolean confirmDeletion(int count) {
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                count == 1
                        ? "Delete the selected transaction?"
                        : "Delete the " + count + " selected transactions?",
                ButtonType.CANCEL,
                ButtonType.OK
        );
        confirmation.setHeaderText("This action cannot be undone");
        confirmation.setTitle("Confirm deletion");
        return confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void loadExpenses() {
        try {
            expenses.setAll(repository.load());
            showStatus("Local data loaded.", false);
        } catch (IOException | RuntimeException exception) {
            showStatus("Could not load data.json: " + exception.getMessage(), true);
        }
    }

    private boolean saveExpenses() {
        try {
            repository.save(new ArrayList<>(expenses));
            return true;
        } catch (IOException exception) {
            showStatus("Could not save data.json: " + exception.getMessage(), true);
            return false;
        }
    }

    /** Refreshes every dashboard section from the same expense list. */
    private void refreshDashboard() {
        summaryCardsController.display(analytics.calculateSummary(expenses));
        categoryChartController.refresh();
        timelineChartController.refresh();
    }

    @FXML
    private void handleThemeToggle() {
        boolean darkMode = themeToggle.isSelected();
        themeManager.apply(
                themeToggle.getScene(),
                darkMode ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT
        );
        themeToggle.setText(darkMode ? "☀  Light mode" : "☾  Dark mode");
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-normal", "status-error");
        statusLabel.getStyleClass().add(error ? "status-error" : "status-normal");
    }
}
