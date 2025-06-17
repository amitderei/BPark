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

    /** Label that displays “Welcome, <name>!” */
    @FXML
    private Label welcomeLabel;

    /** Shared socket handler, injected by the parent. */
    private ClientController client;

    /**
     * Saves the ClientController reference so this screen
     * can talk to the server later if needed.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Updates the welcome label with the subscriber’s first name.
     * Guarded with a null-check because FXML fields may be null
     * during unit tests that skip UI loading.
     *
     * @param name first name of the subscriber
     */
    public void setSubscriberName(String name) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + name + "!");
        }
    }
}
