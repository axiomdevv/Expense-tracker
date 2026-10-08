package com.axiomdevv.expensetracker.ui;

import com.axiomdevv.expensetracker.model.Expense;
import com.axiomdevv.expensetracker.service.ExpenseAnalytics;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Owns the transaction table and its selection. */
public class ExpenseTableController {
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    @FXML private TableView<Expense> table;
    @FXML private TableColumn<Expense, String> dateColumn;
    @FXML private TableColumn<Expense, String> descriptionColumn;
    @FXML private TableColumn<Expense, String> categoryColumn;
    @FXML private TableColumn<Expense, Double> amountColumn;
    @FXML private Button deleteButton;

    private final ExpenseAnalytics analytics = new ExpenseAnalytics();
    private Consumer<List<Expense>> onDelete = selected -> {
    };

    public void initialize() {
        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                analytics.parseDate(cell.getValue().getDate())
                        .map(DISPLAY_DATE::format)
                        .orElse(cell.getValue().getDate())
        ));
        descriptionColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getDescription()));
        categoryColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getCategory().getDisplayName()));
        amountColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getAmount()));
        amountColumn.setCellFactory(column -> new CurrencyCell());

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setPlaceholder(new Label("No transactions yet. Add your first one from the form."));
        deleteButton.disableProperty().bind(
                table.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    public void setItems(ObservableList<Expense> expenses) {
        table.setItems(expenses);
    }

    public void setOnDelete(Consumer<List<Expense>> onDelete) {
        this.onDelete = onDelete;
    }

    @FXML
    private void handleDelete() {
        List<Expense> selected = new ArrayList<>(
                table.getSelectionModel().getSelectedItems()
        );
        onDelete.accept(selected);
    }

    private static class CurrencyCell extends TableCell<Expense, Double> {
        @Override
        protected void updateItem(Double amount, boolean empty) {
            super.updateItem(amount, empty);
            getStyleClass().removeAll("amount-income", "amount-expense");

            if (empty || amount == null) {
                setText(null);
                return;
            }

            setText(amount < 0
                    ? String.format(Locale.US, "-$%,.2f", Math.abs(amount))
                    : String.format(Locale.US, "+$%,.2f", amount));
            getStyleClass().add(amount < 0 ? "amount-expense" : "amount-income");
        }
    }
}
