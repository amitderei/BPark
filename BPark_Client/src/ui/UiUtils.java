package ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * Simple JavaFX helper utilities for showing messages in controllers.
 */
public final class UiUtils {

    private UiUtils() {}           // prevent instantiation

    /**
     * Updates the given label with a success / error style.
     *
     * @param label     Label to update.
     * @param message   Message text.
     * @param isSuccess true ⇒ green, false ⇒ red.
     */
    public static void setStatus(Label label, String message, boolean isSuccess) {
        Platform.runLater(() -> {
            label.setText(message);
            label.setStyle(isSuccess ? "-fx-text-fill: green;"
                                      : "-fx-text-fill: red;");
        });
    }

    /**
     * Opens a modal alert dialog.
     *
     * @param title   Window title.
     * @param content Message body.
     * @param type    JavaFX Alert type (INFORMATION, WARNING, ERROR…).
     */
    public static void showAlert(String title,
                                 String content,
                                 Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
