package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Home screen a subscriber sees right after login.
 * Shows a friendly greeting and exposes the side-menu
 * (handled by the parent layout controller).
 */
public class SubscriberMainController implements ClientAware {

    /** 
     * Label that displays a personalized greeting, e.g. "Welcome, John!".
     * Bound to the FXML layout.
     */
    @FXML
    private Label welcomeLabel;

    /** 
     * Reference to the shared ClientController instance.
     * Used for sending messages to the server if needed.
     */
    private ClientController client;

    /**
     * Injects the shared ClientController so this screen
     * can communicate with the server.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Updates the welcome label with the subscriber’s first name.
     * Skips update if welcomeLabel is not yet loaded (e.g., during unit tests).
     *
     * @param name first name of the subscriber
     */
    public void setSubscriberName(String name) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + name + "!");
        }
    }
}
