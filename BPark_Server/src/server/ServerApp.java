package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for launching the BPARK server application.
 * Loads the connection screen and starts the server on the specified port.
 */
public class ServerApp extends Application {
	
    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) throws Exception {
        launch(args);  // Starts JavaFX, calls start() below
    }

    /**
     * Starts the server on the provided port.
     * Converts the port from String to integer and starts listening for clients.
     * Returns the server instance for further use in the controller.
     *
     * @param p the port number as a String
     * @return the started Server instance, or null if failed
     */
    public Server runServer(String p) {
        try {
            // Convert the port input from String to integer
            int port = Integer.parseInt(p);

            // Create a Server instance with the specified port
            Server server = new Server(port);

            // Start listening for client connections
            server.listen();

            // Log server status to console
            System.out.println("Server started on port " + port);

            // Return the created server instance
            return server;

        } catch (NumberFormatException e) {
            // Handle invalid port input (not a number)
            System.out.println("ERROR - Invalid port number.");
        } catch (Exception e) {
            // Handle any other error that occurs when starting the server
            System.out.println("Server error: " + e.getMessage());
        }

        // Return null if failed to start the server
        return null;
    }

    /**
     * Loads and displays the server connection screen (GUI).
     * This allows the user to enter the port before starting the server.
     *
     * @param primaryStage the primary window of the JavaFX application
     * @throws Exception if loading the FXML file fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the connection screen layout from FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/ConnectionToServerView.fxml"));
        Parent root = loader.load();

        // Get the controller instance associated with the FXML
        ServerController controller = loader.getController();

        // Link this ServerApp instance to the controller
        controller.setApp(this);
        

        // Create a scene using the loaded layout
        Scene scene = new Scene(root);

        // Set the window title
        primaryStage.setTitle("BPark Server");

        // Set the scene to the window
        primaryStage.setScene(scene);

        // Show the window to the user
        primaryStage.show();
        
       
    }
}