package com.axiomdevv.expensetracker.ui;

import com.axiomdevv.expensetracker.model.ExpenseSummary;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Locale;

/** Displays balance, income, and expense totals. */
public class SummaryCardsController {
    @FXML private Label totalBalance;
    @FXML private Label totalIncome;
    @FXML private Label totalExpenses;

    public void display(ExpenseSummary summary) {
        totalBalance.setText(formatSignedCurrency(summary.balance()));
        totalIncome.setText(formatCurrency(summary.income()));
        totalExpenses.setText(formatCurrency(summary.expenses()));
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "$%,.2f", amount);
    }

    private String formatSignedCurrency(double amount) {
        return amount < 0
                ? String.format(Locale.US, "-$%,.2f", Math.abs(amount))
                : formatCurrency(amount);
    }
}
