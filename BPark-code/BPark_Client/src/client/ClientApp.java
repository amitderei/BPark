package client;

import controllers.ConnectController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Entry point for launching the client side application of BPARK.
 * Sets up the JavaFX UI and initializes the connection to the server.
 */
public class ClientApp extends Application {

	/** Active controller responsible for client-server communication */
	private ClientController client;

	/**
	 * Main entry point. Launches the JavaFX runtime.
	 *
	 * @param args command-line arguments (not used)
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Initializes the JavaFX application.
	 * Loads the connection screen and links the ConnectController to this app instance.
	 *
	 * @param primaryStage the primary JavaFX window
	 * @throws Exception if the FXML file fails to load
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		// Load the connection screen layout
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ConnectScreen.fxml"));
		Parent root = loader.load();

		// Give the controller access to this ClientApp instance
		ConnectController controller = loader.getController();
		controller.setApp(this);
		
		// Pass the stage to the controller so it can support window dragging
		controller.setStage(primaryStage); 

		// Set up the stage with no window borders
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setTitle("BPARK Client");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	/**
	 * Called automatically when the application is closed.
	 * Closes the client connection to the server, if it exists.
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
	 * Sets the active ClientController for this application.
	 * This is called after the client successfully connects to the server.
	 *
	 * @param client the connected client controller
	 */
	public void setClient(ClientController client) {
		this.client = client;
	}
}