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
        // 1. Create a new BparkClient instance pointing to localhost:5555
        client = new BparkClient("localhost", 5555);

        // 2. Establish connection to the server
        client.openConnection();

        // 3. Load the GUI layout from the FXML file (orders_view.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/orders_view.fxml"));
        Parent root = loader.load(); // Load the full UI hierarchy

        // 4. Get the controller associated with the FXML and connect it to the client
        BparkClientController controller = loader.getController();
        controller.setClient(client);           // Controller knows the client
        client.setController(controller);       // Client knows the controller

        // 5. Setup and show the JavaFX window (stage)
        primaryStage.setTitle("BPARK Client");           // Set window title
        primaryStage.setScene(new Scene(root));          // Set the GUI layout
        primaryStage.show();                             // Show the window
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
     * Main method to launch the JavaFX application.
     * Triggers the JavaFX runtime to call the start() method.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
