package com.axiomdevv.expensetracker;

import com.axiomdevv.expensetracker.ui.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TrackerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TrackerApplication.class.getResource(

                "/com/axiomdevv/expensetracker/views/expense-tracker.fxml"
        ));
        Scene scene = new Scene(fxmlLoader.load());

        new ThemeManager().apply(scene, ThemeManager.Theme.LIGHT);

        stage.setTitle("Expense Tracker");
        stage.setMinWidth(920);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }
}
