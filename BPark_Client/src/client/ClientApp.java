package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Entry point for launching the client-side application of BPARK. This class
 * sets up the JavaFX UI and initializes the client-server connection.
 */
public class ClientApp extends Application {

	// Instance-level client used for communication with the server
	private Client client;

	/**
	 * Called when the JavaFX application starts. Loads the client UI and waits for
	 * the user to initiate the server connection.
	 *
	 * @param primaryStage the primary window of the JavaFX application.
	 * @throws Exception if loading the FXML file fails.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		// 1. Load the GUI layout from the FXML file (orders_view.fxml)
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/orders_view.fxml"));
		Parent root = loader.load(); // Load the full UI hierarchy

		// 2. Display the GUI and wait for user to connect to the server
		primaryStage.setTitle("BPARK Client"); // Set window title
		primaryStage.setScene(new Scene(root)); // Set the GUI layout
		primaryStage.show(); // Show the window
	}

	/**
	 * Called automatically when the JavaFX application exits. Closes the connection
	 * to the server gracefully if it was opened.
	 */
	@Override
	public void stop() {
		if (client != null) { // Check if client is connected
			try {
				client.closeConnection();
				System.out.println("Connection to server closed.");
			} catch (Exception e) {
				System.err.println("Failed to close connection: " + e.getMessage());
			}
		}
	}

	/**
	 * Main entry point of the application. Starts the JavaFX application by calling
	 * the start() method.
	 *
	 * @param args command-line arguments (not used in this application).
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Allows setting the client instance, for example from a controller. This
	 * enables dynamic client management without relying on static state.
	 */
	public void setClient(Client client) {
		this.client = client;
	}
}
