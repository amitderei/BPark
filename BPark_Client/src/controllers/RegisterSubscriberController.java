package controllers;

import client.ClientController;
import common.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.UiUtils;

/**
 * Controller for registering a new subscriber by the attendant.
 * Validates input fields and sends a registration request to the server.
 * Subscriber code and tag ID are generated on the server side.
 */
public class RegisterSubscriberController implements ClientAware {

    private ClientController client;

    @FXML private TextField txtUserId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private Button btnRegister;
    @FXML private Label lblStatus;

    /**
     * Sets the client controller used for server communication.
     *
     * @param client the active ClientController
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Handles the click event for the Register button.
     * Validates all input fields, builds a Subscriber object, and sends it to the server.
     */
    @FXML
    private void handleRegisterClick() {
        String userId = txtUserId.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim();

        // Check required fields
        if (userId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                || phone.isEmpty() || email.isEmpty() || username.isEmpty()) {
            UiUtils.setStatus(lblStatus, "All fields must be filled.", false);
            return;
        }

        // Validate user ID: must be 9 digits
        if (!userId.matches("\\d{9}")) {
            UiUtils.setStatus(lblStatus, "ID must be exactly 9 digits.", false);
            return;
        }

        // Validate username: at least 4 characters, no spaces
        if (!username.matches("\\w{4,}")) {
            UiUtils.setStatus(lblStatus, "Username must be at least 4 characters with no spaces.", false);
            return;
        }

        // Validate email format
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            UiUtils.setStatus(lblStatus, "Email format is invalid.", false);
            return;
        }

        // Build and send subscriber object
        Subscriber subscriber = new Subscriber(0, userId, firstName, lastName, phone, email, username, null);
        client.registerSubscriber(subscriber);
        UiUtils.setStatus(lblStatus, "Registration request sent to server.", true);
    }

    /**
     * Displays server response on the status label.
     *
     * @param message the message to display
     * @param success whether the operation was successful
     */
    public void showStatusFromServer(String message, boolean success) {
        UiUtils.setStatus(lblStatus, message, success);
    }
}
