package com.axiomdevv.expensetracker.service;

import com.axiomdevv.expensetracker.model.Expense;
import com.axiomdevv.expensetracker.model.ExpenseDraft;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Validates form values and creates an Expense after validation succeeds. */
public class ExpenseValidator {
    private static final DateTimeFormatter STORED_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public List<String> validate(ExpenseDraft draft) {
        List<String> errors = new ArrayList<>();

        if (draft.description() == null || draft.description().trim().isEmpty()) {
            errors.add("Enter a description.");
        }
        if (draft.category() == null) {
            errors.add("Choose a category.");
        }
        if (draft.date() == null) {
            errors.add("Choose a date.");
        }

        Double amount = parseAmount(draft.amountText());
        if (amount == null) {
            errors.add("Enter a valid amount, for example 12.50.");
        } else if (!Double.isFinite(amount) || amount <= 0) {
            errors.add("Amount must be greater than zero.");
        }

        return errors;
    }

    public Expense createExpense(ExpenseDraft draft) {
        double unsignedAmount = Double.parseDouble(draft.amountText().trim());
        double signedAmount = draft.category().isIncome()
                ? Math.abs(unsignedAmount)
                : -Math.abs(unsignedAmount);

        return new Expense(
                draft.description().trim(),
                STORED_DATE_FORMAT.format(draft.date()),
                draft.category(),
                signedAmount
        );
    }

    private Double parseAmount(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
