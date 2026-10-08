package com.axiomdevv.expensetracker.service;

import com.axiomdevv.expensetracker.model.Category;
import com.axiomdevv.expensetracker.model.Expense;
import com.axiomdevv.expensetracker.model.ExpenseSummary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Performs dashboard calculations without depending on JavaFX controls. */
public class ExpenseAnalytics {
    private static final DateTimeFormatter LEGACY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public ExpenseSummary calculateSummary(Collection<Expense> expenses) {
        double income = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .filter(amount -> amount > 0)
                .sum();

        double expenseTotal = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .filter(amount -> amount < 0)
                .map(Math::abs)
                .sum();

        return new ExpenseSummary(income - expenseTotal, income, expenseTotal);
    }

    public Map<Category, Double> expensesByCategory(Collection<Expense> expenses) {
        Map<Category, Double> totals = new LinkedHashMap<>();

        for (Category category : Category.values()) {
            if (!category.isIncome()) {
                totals.put(category, 0.0);
            }
        }

        for (Expense expense : expenses) {
            if (expense.getAmount() < 0
                    && expense.getCategory() != null
                    && !expense.getCategory().isIncome()) {
                totals.merge(expense.getCategory(), Math.abs(expense.getAmount()), Double::sum);
            }
        }

        totals.entrySet().removeIf(entry -> entry.getValue() == 0);
        return totals;
    }

    public Map<LocalDate, Double> dailyExpenses(Collection<Expense> expenses) {
        Map<LocalDate, Double> totals = new TreeMap<>();
        forEachDatedExpense(expenses, (date, amount) ->
                totals.merge(date, amount, Double::sum));
        return totals;
    }

    public Map<YearMonth, Double> monthlyExpenses(Collection<Expense> expenses) {
        Map<YearMonth, Double> totals = new TreeMap<>();
        forEachDatedExpense(expenses, (date, amount) ->
                totals.merge(YearMonth.from(date), amount, Double::sum));
        return totals;
    }

    public Map<Integer, Double> yearlyExpenses(Collection<Expense> expenses) {
        Map<Integer, Double> totals = new TreeMap<>();
        forEachDatedExpense(expenses, (date, amount) ->
                totals.merge(date.getYear(), amount, Double::sum));
        return totals;
    }

    public Optional<LocalDate> parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDate.parse(value, LEGACY_DATE_FORMAT));
        } catch (DateTimeParseException ignored) {
            try {
                return Optional.of(LocalDate.parse(value));
            } catch (DateTimeParseException invalidDate) {
                return Optional.empty();
            }
        }
    }

    private void forEachDatedExpense(
            Collection<Expense> expenses,
            DatedExpenseConsumer consumer
    ) {
        for (Expense expense : expenses) {
            if (expense.getAmount() >= 0) {
                continue;
            }

            parseDate(expense.getDate()).ifPresent(date ->
                    consumer.accept(date, Math.abs(expense.getAmount())));
        }
    }

    @FunctionalInterface
    private interface DatedExpenseConsumer {
        void accept(LocalDate date, double amount);
    }
}
