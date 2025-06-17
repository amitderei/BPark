package ui;

import client.ClientController;
import controllers.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Small collection of static helper methods for JavaFX screens:
 * – Coloured status messages on labels  
 * – Standard alert pop-ups  
 * – Scene / screen switching with automatic ClientController injection
 */
public final class UiUtils {

    /** Shared client instance, mainly for static access inside loadScreen. */
    public static ClientController client;

    /** Utility class – prevent instantiation. */
    private UiUtils() { }

    /**
     * Styles a label green for success or red for error and sets the text.
     *
     * @param label     label to update
     * @param message   text to show
     * @param isSuccess true ⇒ green, false ⇒ red
     */
    public static void setStatus(Label label,
                                 String message,
                                 boolean isSuccess) {
        Platform.runLater(() -> {
            label.setText(message);
            label.setStyle(isSuccess ? "-fx-text-fill: green;"
                                     : "-fx-text-fill: red;");
        });
    }

    /**
     * Opens a modal alert dialog on the JavaFX thread.
     *
     * @param title   dialog title
     * @param content body text
     * @param type    INFORMATION / WARNING / ERROR …
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
     * Loads an FXML file, injects a ClientController when the controller
     * implements ClientAware, and swaps the current scene.
     *
     * @param source    node already attached to the active window
     * @param fxmlPath  class-path location of the FXML
     * @param title     new window title
     * @param client1   active ClientController; may be null for guest screens
     */
    public static void loadScreen(Button source,
                                  String fxmlPath,
                                  String title,
                                  ClientController client1) {

        try {
            FXMLLoader loader = new FXMLLoader(UiUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            /* ------------------------------------------------------
             *  Pass client reference to controllers that need it.
             *  Some specific controllers also need to register
             *  themselves inside ClientController for callbacks.
             * ------------------------------------------------------ */
            Object ctrl = loader.getController();

            if (ctrl instanceof TerminalMainLayoutController t) {
                client.setTerminalController(t);
                t.setClient(client);
            }
            if (ctrl instanceof MainController m) {
                client.setMainController(m);
                m.setClient(client);
            }
            if (ctrl instanceof LoginController l) {
                client.setLoginController(l);
                l.setClient(client);
            }
            if (ctrl instanceof GuestMainController g) {
                client.setGuestMainController(g);
                g.setClient(client);
            }

            // Generic injection for any other controller
            if (ctrl instanceof ClientAware aware) {
                aware.setClient(client1);
            }

            /* ------------------------------------------------------
             *  Swap scene
             * ------------------------------------------------------ */
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

    /**
     * Convenience method for directly setting a scene with fixed width/height.
     *
     * @param stage  target Stage
     * @param root   root node for the new scene
     * @param title  window title
     */
    public static void setScene(Stage stage, Parent root, String title) {
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setWidth(800);
        stage.setHeight(500);
        stage.show();
    }
}
