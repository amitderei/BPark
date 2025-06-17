package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Landing screen shown to users who are not logged in.
 * Displays a simple welcome message and can later be extended
 * to show available parking spots or promotional info.
 */
public class GuestMainController implements ClientAware {

    /** Label that shows the greeting text */
    @FXML private Label welcomeLabel;

    /** Reference to the central ClientController */
    private ClientController client;

    /**
     * JavaFX initialisation hook.
     * Sets the default greeting when the FXML is loaded.
     */
    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, Guest!");
        }
    }

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

}

