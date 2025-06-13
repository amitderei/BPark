package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StaffMainController implements ClientAware {

    @FXML
    private Label welcomeLabel;

    private ClientController client;

    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Optionally update the welcome message if needed later.
     */
    public void setWelcomeMessage(String nameOrRole) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + nameOrRole + "!");
        }
    }
}
