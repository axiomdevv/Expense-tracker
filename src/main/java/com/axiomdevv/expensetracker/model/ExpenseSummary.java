package com.axiomdevv.expensetracker.model;

/** The three totals displayed at the top of the dashboard. */
public record ExpenseSummary(double balance, double income, double expenses) {
}
