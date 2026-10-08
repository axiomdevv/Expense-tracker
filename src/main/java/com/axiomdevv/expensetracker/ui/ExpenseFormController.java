package com.axiomdevv.expensetracker.ui;

import com.axiomdevv.expensetracker.model.Category;
import com.axiomdevv.expensetracker.model.ExpenseDraft;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.function.Consumer;

/** Owns the controls inside expense-form.fxml. */
public class ExpenseFormController {
    @FXML private TextField descriptionField;
    @FXML private ComboBox<Category> categoryBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField amountField;
    @FXML private Label feedbackLabel;

    private Consumer<ExpenseDraft> onAdd = draft -> {
    };

    public void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(Category.values()));

        amountField.setTextFormatter(new TextFormatter<>(change -> {
            String newValue = change.getControlNewText();
            return newValue.matches("\\d*(\\.\\d{0,2})?") ? change : null;
        }));
    }

    @FXML
    private void handleAdd() {
        onAdd.accept(readDraft());
    }

    @FXML
    public void clearForm() {
        descriptionField.clear();
        categoryBox.setValue(null);
        datePicker.setValue(null);
        amountField.clear();
        clearFeedback();
    }

    public void setOnAdd(Consumer<ExpenseDraft> onAdd) {
        this.onAdd = onAdd;
    }

    public void showErrors(List<String> errors) {
        showFeedback(String.join("\n", errors), "feedback-error");
    }

    public void showSuccess(String message) {
        showFeedback(message, "feedback-success");
    }

    private ExpenseDraft readDraft() {
        return new ExpenseDraft(
                descriptionField.getText(),
                datePicker.getValue(),
                categoryBox.getValue(),
                amountField.getText()
        );
    }

    private void showFeedback(String message, String styleClass) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll("feedback-error", "feedback-success");
        feedbackLabel.getStyleClass().add(styleClass);
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    private void clearFeedback() {
        feedbackLabel.setText("");
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
    }
}
