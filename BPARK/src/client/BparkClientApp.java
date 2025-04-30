package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Entry point for launching the client-side application of BPARK.
 * This class sets up the JavaFX UI and initializes the client-server connection.
 */
public class BparkClientApp extends Application {

    // Shared instance of the PrototypeClient used for communication with the server
    public static BparkClient client;

    /**
     * Called when the JavaFX application is launched.
     * Initializes the network client, loads the FXML-based GUI, and binds the controller.
     *
     * @param primaryStage the primary window of the JavaFX application.
     * @throws Exception if loading FXML or connecting to server fails.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        client = new BparkClient("localhost", 5555);
        client.openConnection();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/orders_view.fxml"));
        Parent root = loader.load();

        BparkClientController controller = loader.getController();
        controller.setClient(client);
        client.setController(controller); // Allows client to send data back to GUI

        primaryStage.setTitle("BPARK Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Called automatically when the JavaFX application exits.
     * Closes the connection to the server gracefully.
     *
     * @throws Exception if closing the client connection fails.
     */
    @Override
    public void stop() throws Exception {
        client.closeConnection();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
