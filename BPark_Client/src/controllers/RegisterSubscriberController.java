package controllers;

import client.ClientController;
import common.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.UiUtils;

/**
 * Controller for the staff-facing registration screen.
 * Enables attendants to create new subscriber accounts by filling a form.
 * Includes client-side validation and sends a Subscriber object to the server.
 */
public class RegisterSubscriberController implements ClientAware {
	
    /** Shared client controller used to send requests to the server */
    private ClientController client;

    /** Input field for national ID (exactly 9 digits) */
    @FXML 
    private TextField txtUserId;

    /** Input field for subscriber's first name */
    @FXML 
    private TextField txtFirstName;

    /** Input field for subscriber's last name */
    @FXML 
    private TextField txtLastName;

    /** Input field for phone number (must begin with 05) */
    @FXML 
    private TextField txtPhone;

    /** Input field for valid email address */
    @FXML
    private TextField txtEmail;

    /** Input field for username (4+ alphanumeric characters) */
    @FXML
    private TextField txtUsername;

    /** Button that triggers the registration request */
    @FXML 
    private Button btnRegister;

    /** Label used to display live validation or server result status */
    @FXML 
    private Label lblStatus;

    /**
     * Injects the shared ClientController instance so this screen
     * can communicate with the server.
     *
     * @param client the active client controller instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Triggered when the user presses the "Register" button.
     * Validates input fields, and if all are valid, creates a Subscriber
     * object and sends it to the server for registration.
     */
    @FXML
    private void handleRegisterClick() {
        StringBuilder errors = new StringBuilder();

        String userId    = txtUserId.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName.getText().trim();
        String phone     = txtPhone.getText().trim();
        String email     = txtEmail.getText().trim();
        String username  = txtUsername.getText().trim();

        // validation rules
        if (userId.isEmpty() || !userId.matches("\\d{9}"))
            errors.append("- ID must be exactly 9 digits.\n");

        if (firstName.isEmpty())
            errors.append("- First name is required.\n");

        if (lastName.isEmpty())
            errors.append("- Last name is required.\n");

        if (phone.isEmpty() || !phone.matches("^05\\d{8}$"))
            errors.append("- Phone must start with 05 and be 10 digits.\n");

        if (email.isEmpty() || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$"))
            errors.append("- Email format is invalid.\n");

        if (username.isEmpty() || !username.matches("\\w{4,}"))
            errors.append("- Username must be at least 4 characters.\n");

        if (errors.length() > 0) {
            // Display validation errors
            UiUtils.setStatus(lblStatus, errors.toString().trim(), false);
            return;
        }

        //build and send subscriber to server 
        Subscriber sub = new Subscriber(
                0,          // Server generates the subscriber code
                userId,
                firstName,
                lastName,
                phone,
                email,
                username,
                null        // Tag ID is assigned by the server
        );

        client.registerSubscriber(sub);
        UiUtils.setStatus(lblStatus,
                "Registration request sent to server.",
                true);
    }

    /**
     * Called by ClientController after receiving a response from the server.
     * Updates the status label with success or error message.
     *
     * @param message server response message
     * @param success whether the operation succeeded
     */
    public void showStatusFromServer(String message, boolean success) {
        UiUtils.setStatus(lblStatus, message, success);
    }
}