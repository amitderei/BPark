package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the guest home screen shown to users who are not logged in.
 * Displays a basic welcome message and can be expanded with more guest content.
 * to show available parking spots or promotional info.
 */
public class GuestMainController implements ClientAware {

    /** Label that shows the greeting text */
    @FXML private Label welcomeLabel;

    /** Reference to the active client used for server communication */
    private ClientController client;

    /**
     * Receives the ClientController and registers this controller
     * inside it so the server can push updates later on.
     *
     * @param client active client instance, may be null before connection
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setGuestMainController(this);
        }
    }
    
    /**
     * Called automatically when the FXML is loaded.
     * Sets the default welcome message.
     */
    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, Guest!");
        }
    }
}

