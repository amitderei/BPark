package controllers;

import client.ClientController;
import common.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.UiUtils;

/**
 * Controller for the staff-facing registration screen.
 * Handles input validation and communicates with the server to register a new subscriber.
 */
public class RegisterSubscriberController implements ClientAware {

    /** Shared client controller used to send requests to the server */
    private ClientController client;

    // Input fields for subscriber details
    @FXML private TextField txtUserId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private TextField txtVehicleId;

    // Error labels for invalid fields
    @FXML private Label errUserId;
    @FXML private Label errFirstName;
    @FXML private Label errLastName;
    @FXML private Label errPhone;
    @FXML private Label errEmail;
    @FXML private Label errUsername;
    @FXML private Label errVehicleId;

    /** Button that triggers the registration process */
    @FXML private Button btnRegister;

    /** Label that displays status from the server (confirmation or error) */
    @FXML private Label lblStatus;

    /**
     * Injects the active client instance used for server communication.
     *
     * @param client the shared ClientController instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Called when the "Register Subscriber" button is clicked.
     * Validates the input fields and sends the new subscriber to the server if valid.
     */
    @FXML
    private void handleRegisterClick() {
        clearErrors(); // Clear previous errors first

        // Read and trim all inputs
        String userId    = txtUserId.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName.getText().trim();
        String phone     = txtPhone.getText().trim();
        String email     = txtEmail.getText().trim();
        String username  = txtUsername.getText().trim();
        String vehicleId = txtVehicleId.getText().trim();

        boolean valid = true; // Flag to track validation success

        // Validate ID: must be exactly 9 digits
        if (userId.isEmpty() || !userId.matches("\\d{9}")) {
            errUserId.setText("Must be exactly 9 digits.");
            errUserId.setVisible(true);
            valid = false;
        }

        // Validate first name
        if (firstName.isEmpty()) {
            errFirstName.setText("First name is required.");
            errFirstName.setVisible(true);
            valid = false;
        } else if (!firstName.matches("[A-Za-z]+")) {
            errFirstName.setText("First name must contain only english letters.");
            errFirstName.setVisible(true);
            valid = false;
        } else if (firstName.length() < 2 || firstName.length() > 20) {
            errFirstName.setText("First name must be 2–20 characters long.");
            errFirstName.setVisible(true);
            valid = false;
        }

        // Validate last name
        if (lastName.isEmpty()) {
            errLastName.setText("Last name is required.");
            errLastName.setVisible(true);
            valid = false;
        } else if (!lastName.matches("[A-Za-z]+")) {
            errLastName.setText("Last name must contain only english letters.");
            errLastName.setVisible(true);
            valid = false;
        } else if (lastName.length() < 2 || lastName.length() > 20) {
            errLastName.setText("Last name must be 2–20 characters long.");
            errLastName.setVisible(true);
            valid = false;
        }

        // Validate phone number: starts with 05 and 10 digits total
        if (phone.isEmpty() || !phone.matches("^05\\d{8}$")) {
            errPhone.setText("Phone must start with 05 and be 10 digits.");
            errPhone.setVisible(true);
            valid = false;
        }

        // Validate email format
        if (email.isEmpty() || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            errEmail.setText("Invalid email format.");
            errEmail.setVisible(true);
            valid = false;
        }

        // Validate username: at least 4 characters, alphanumeric
        if (username.isEmpty() || !username.matches("\\w{4,}")) {
            errUsername.setText("At least 4 alphanumeric characters.");
            errUsername.setVisible(true);
            valid = false;
        }

        // Validate vehicle ID: 7 to 10 digits
        if (vehicleId.isEmpty() || !vehicleId.matches("\\d{7,10}")) {
            errVehicleId.setText("Vehicle ID must be 7–10 digits.");
            errVehicleId.setVisible(true);
            valid = false;
        }

        // Stop if any validation failed
        if (!valid) {
            lblStatus.setText("");
            return;
        }

        // Construct a new Subscriber object (code and tag generated by server)
        Subscriber sub = new Subscriber(
                0,
                userId,
                firstName,
                lastName,
                phone,
                email,
                username,
                null
        );

        // Send registration request to server
        client.getRequestSender().registerSubscriber(sub, vehicleId);

        lblStatus.setText(""); // Clear status while waiting for response
    }

    /**
     * Hides all error labels before re-validating fields.
     */
    private void clearErrors() {
        errUserId.setVisible(false);
        errFirstName.setVisible(false);
        errLastName.setVisible(false);
        errPhone.setVisible(false);
        errEmail.setVisible(false);
        errUsername.setVisible(false);
        errVehicleId.setVisible(false);
    }

    /**
     * Displays the server response message in the status label.
     * If registration was successful, disables all input fields and the register button
     * to prevent further editing.
     *
     * @param message the message returned from the server (success or error)
     * @param success true if registration succeeded, false otherwise
     */
    public void showStatusFromServer(String message, boolean success) {
        UiUtils.setStatus(lblStatus, message, success);

        if (success) {
            txtUserId.setDisable(true);
            txtFirstName.setDisable(true);
            txtLastName.setDisable(true);
            txtPhone.setDisable(true);
            txtEmail.setDisable(true);
            txtUsername.setDisable(true);
            txtVehicleId.setDisable(true);
            btnRegister.setDisable(true);
        }
    }
    
    /**
     * Shows duplicate-field error received from the server
     * and re-enables the Register button.
     *
     * @param field one of: "username", "email", "phone", "id", "vehicle"
     */
    public void showDuplicateError(String field) {
        switch (field) {
            case "username" -> {
                errUsername.setText("Username already exists.");
                errUsername.setVisible(true);
            }
            case "email" -> {
                errEmail.setText("Email already exists.");
                errEmail.setVisible(true);
            }
            case "phone" -> {
                errPhone.setText("Phone number already exists.");
                errPhone.setVisible(true);
            }
            case "id" -> {
                errUserId.setText("ID already exists.");
                errUserId.setVisible(true);
            }
            case "vehicle" -> {
                errVehicleId.setText("Vehicle ID already exists.");
                errVehicleId.setVisible(true);
            }
            default -> UiUtils.showAlert(
                    "System Message", "Unknown field: " + field, Alert.AlertType.ERROR);
        }

        // Allow the user to correct and resubmit
        btnRegister.setDisable(false);
        UiUtils.setStatus(lblStatus, "", true);   // Clear bottom status
    }



}
