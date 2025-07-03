package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Launches the BPARK server GUI and starts the socket server
 * after the user enters a port.
 */
public class ServerApp extends Application {

    /**
     * Program entry-point.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if JavaFX fails to start
     */
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    /**
     * Creates a Server on the given port and starts listening.
     *
     * @param portStr the port number as text
     * @return the running server, or null if startup failed
     */
    public Server runServer(String portStr) {
        try {
            int    port   = Integer.parseInt(portStr);
            Server server = new Server(port);
            server.listen();                       // open socket
            System.out.println("Server started on port " + port);
            return server;
        } catch (NumberFormatException nfe) {
            System.out.println("ERROR – port must be numeric.");
        } catch (Exception ex) {
            System.out.println("Server error: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Loads the "Connect" window where the user types the port.
     *
     * @param stage primary JavaFX window
     * @throws Exception when the FXML cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxml = new FXMLLoader(
                getClass().getResource("/server/ConnectionToServerView.fxml"));
        Parent root = fxml.load();

        // give the controller a handle back to this class
        ServerController ctrl = fxml.getController();
        ctrl.setApp(this);

		// Set up the stage with no window borders
		stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("BPARK – Server");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
