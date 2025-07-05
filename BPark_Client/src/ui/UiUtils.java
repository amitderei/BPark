package ui;

import client.ClientController;
import common.Operation;
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
 * A utility class that provides reusable methods for JavaFX UI management.
 * This includes:
 * - Setting status messages on labels with color
 * - Displaying pop-up alerts
 * - Switching between screens (FXML) while injecting the active ClientController
 */
public final class UiUtils {

    /** Shared client instance for screen loading and controller access */
    public static ClientController client;

    /** Private constructor to prevent instantiation of this utility class */
    private UiUtils() {}

    /**
     * Updates a label with a message and sets its text color based on status.
     *
     * @param label     the label to update
     * @param message   the message to display
     * @param isSuccess true for green (success), false for red (error)
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
     * Displays a JavaFX alert pop-up.
     *
     * @param title   the title of the dialog
     * @param content the message body
     * @param type    the type of alert (INFO, WARNING, ERROR, etc.)
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
     * Loads a new FXML screen and switches to it.
     * Also injects the ClientController into any controller that implements ClientAware.
     * Some known controllers also register themselves back to the ClientController.
     *
     * @param source    the button (or any node) from the current scene
     * @param fxmlPath  the path to the FXML file
     * @param title     the new window title
     * @param client1   the active client instance (can be null for guest mode)
     */
    public static void loadScreen(Button source,
                                  String fxmlPath,
                                  String title,
                                  ClientController client1) {
        try {
            FXMLLoader loader = new FXMLLoader(UiUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Get the controller of the loaded FXML
            Object ctrl = loader.getController();

            // Assign specific controller references (so client can send updates to them)
            if (ctrl instanceof TerminalMainLayoutController t) {
                client.setTerminalController(t);
                t.setClient(client);
            }
            if (ctrl instanceof MainController m) {
                client.setMainController(m);
                m.setClient(client);
                m.setVideos();
            }
            if (ctrl instanceof LoginController l) {
                client.setLoginController(l);
                l.setClient(client);
            }
            if (ctrl instanceof GuestMainController g) {
                client.setGuestMainController(g);
                g.setClient(client);
            }
            
            // inject the Stage if the controller supports it
            if (ctrl instanceof StageAware stageAware) {
                Stage stage = (Stage) source.getScene().getWindow();
                stageAware.setStage(stage); 
            }

            // General injection for any controller that implements ClientAware
            if (ctrl instanceof ClientAware aware) {
                aware.setClient(client1);
            }

            // Switch to the new screen
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
            
            if (ctrl instanceof MainController m) {
                m.setVideos();
            }

        } catch (Exception ex) {
            showAlert("BPARK – Error",
                      "Failed to load screen:\n" + ex.getMessage(),
                      Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    /**
     * Replaces the current scene with a new one, setting a fixed window size.
     *
     * @param stage the JavaFX stage (window)
     * @param root  the root node of the new scene
     * @param title the title to display in the window
     */
    public static void setScene(Stage stage, Parent root, String title) {
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setWidth(800);
        stage.setHeight(500);
        stage.show();
    }
    
    
    /**
     * Handles the Exit button press — disconnects from the server (if needed)
     * and closes the application.
     */
    public static void exitFromSystem() {
    	try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[] { Operation.DISCONNECT });
                client.closeConnection();
            }
        } catch (Exception ignored) {
        }
        javafx.application.Platform.exit();
        System.exit(0);
    }
}
