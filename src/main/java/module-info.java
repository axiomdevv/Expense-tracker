module com.axiomdevv.expensetracker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;

    opens com.axiomdevv.expensetracker to javafx.fxml;
    exports com.axiomdevv.expensetracker;
}