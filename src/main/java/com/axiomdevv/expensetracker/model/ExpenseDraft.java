package com.axiomdevv.expensetracker.model;

import java.time.LocalDate;

/** Unvalidated values read from the expense form. */
public record ExpenseDraft(
        String description,
        LocalDate date,
        Category category,
        String amountText
) {
}
