package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Dashboard header for staff roles (attendant / manager).
 * Presents a personalized greeting; more staff-specific
 * widgets can be added later.
 */
public class StaffMainController implements ClientAware {

    /* ---------- FXML label ---------- */
    @FXML private Label welcomeLabel;

    /* ---------- runtime ---------- */
    private ClientController client;

    /**
     * Supplies the shared ClientController so the staff
     * dashboard can trigger server actions when expanded.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Updates the greeting text.
     *
     * @param nameOrRole text to display after “Welcome,”
     */
    public void setWelcomeMessage(String nameOrRole) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + nameOrRole + "!");
        }
    }
}
