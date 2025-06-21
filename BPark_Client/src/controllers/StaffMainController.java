package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Dashboard header for staff roles (Attendant / Manager).
 * Presents a personalized greeting on login.
 * Additional widgets or metrics can be added in the future.
 */
public class StaffMainController implements ClientAware {

    /** Label that displays the personalized welcome message */
    @FXML
    private Label welcomeLabel;

    /** Reference to the shared client controller used for server communication */
    private ClientController client;

    /**
     * Injects the ClientController instance for server access.
     * Used for future interactions or data retrieval.
     *
     * @param client the active client controller instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Updates the welcome label with the staff member’s name or role.
     * This method ensures the label is not null before updating,
     * which makes it safe to call even during non-GUI testing.
     *
     * @param nameOrRole the text to show after "Welcome,"
     */
    public void setWelcomeMessage(String nameOrRole) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + nameOrRole + "!");
        }
    }
}
