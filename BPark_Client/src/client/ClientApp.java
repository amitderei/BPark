package client;

import controllers.ConnectController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Entry point for launching the client-side application of BPARK. This class
 * sets up the JavaFX UI and initializes the client-server connection.
 */
public class ClientApp extends Application {

	// Instance-level client used for communication with the server
	private ClientController client;

	/**
	 * Called when the JavaFX application starts. Loads the client UI and waits for
	 * the user to initiate the server connection.
	 *
	 * @param primaryStage the primary window of the JavaFX application.
	 * @throws Exception if loading the FXML file fails.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
	    // 1. Load ConnectScreen.fxml
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ConnectScreen.fxml"));
	    Parent root = loader.load();

	    // 2. Get controller and inject ClientApp
	    ConnectController controller = loader.getController();
	    controller.setApp(this);

	    //remove the top lane of app
	    primaryStage.initStyle(StageStyle.UNDECORATED);
	    
	    // 3. Show the scene
	    primaryStage.setTitle("BPARK Client");
	    primaryStage.setScene(new Scene(root));
	    primaryStage.show();
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
	public void setClient(ClientController client) {
		this.client = client;
	}
}
