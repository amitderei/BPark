package client;

import controllers.ConnectController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Launches the BPARK client application. Sets up the first connection screen
 * and ensures the socket is closed on exit.
 */
public class ClientApp extends Application {

    /** Active controller that handles communication with the server. */
    private ClientController client;

    /**
     * Application entry point. Hands control to the JavaFX runtime.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the UI by loading ConnectScreen.fxml, wiring its
     * controller and showing the primary stage without window chrome.
     *
     * @param primaryStage the main window provided by JavaFX
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ConnectScreen.fxml"));
        Parent root = loader.load();

        // Give the controller a reference to this application
        ConnectController controller = loader.getController();
        controller.setApp(this);

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("BPARK Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Shuts down the client‑server connection if it is still open when the
     * application exits.
     */
    @Override
    public void stop() {
        if (client != null) {
            try {
                client.closeConnection();
            } catch (Exception e) {
                System.err.println("Couldn't close connection: " + e.getMessage());
            }
        }
    }

    /**
     * Injects the ClientController after a successful connection.
     *
     * @param client the connected controller instance
     */
    public void setClient(ClientController client) {
        this.client = client;
    }
}
