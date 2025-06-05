package ui;

import client.ClientController;
import controllers.ClientAware;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
    
    /**
     * Loads an FXML file, injects the ClientController (if needed),
     * and replaces the current scene.
     *
     * @param source   any control already in the active scene (e.g., the button that triggered the action)
     * @param fxmlPath FXML resource path (starting with “/” and relative to class-path)
     * @param title    window title after the switch
     * @param client   active ClientController; may be {@code null} for guest screens
     */
    public static void loadScreen(Button source,
                                  String fxmlPath,
                                  String title,
                                  ClientController client) {

        try {
            FXMLLoader loader = new FXMLLoader(UiUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Inject client into the new controller (if it implements ClientAware)
            Object ctrl = loader.getController();
            if (client != null && ctrl instanceof ClientAware aware) {
                aware.setClient(client);
            }

            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception ex) {
            showAlert("BPARK – Error",
                      "Failed to load screen:\n" + ex.getMessage(),
                      Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }
    public static void setScene(Stage stage, Parent root, String title) {
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setWidth(800);
        stage.setHeight(500);
        stage.show();
    }

}

