package com.axiomdevv.expensetracker.ui;

import javafx.scene.Scene;

import java.net.URL;
import java.util.Objects;

/** Applies the shared stylesheet and one color theme to a Scene. */
public class ThemeManager {

    private static final String BASE_STYLES =
            "/com/axiomdevv/expensetracker/styles/base.css";
    private static final String LIGHT_STYLES =
            "/com/axiomdevv/expensetracker/styles/light-theme.css";
    private static final String DARK_STYLES =
            "/com/axiomdevv/expensetracker/styles/dark-theme.css";

    public enum Theme {
        LIGHT,
        DARK
    }

    public void apply(Scene scene, Theme theme) {
        scene.getStylesheets().setAll(
                resource(BASE_STYLES),
                resource(theme == Theme.DARK ? DARK_STYLES : LIGHT_STYLES)
        );
    }

    private String resource(String path) {
        URL resource = ThemeManager.class.getResource(path);
        return Objects.requireNonNull(resource, "Missing stylesheet: " + path)
                .toExternalForm();
    }
}
