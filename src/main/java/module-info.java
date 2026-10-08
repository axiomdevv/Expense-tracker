module com.axiomdevv.expensetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    exports com.axiomdevv.expensetracker;
    exports com.axiomdevv.expensetracker.model;

    opens com.axiomdevv.expensetracker.ui to javafx.fxml;
    opens com.axiomdevv.expensetracker.model to com.fasterxml.jackson.databind;
}
