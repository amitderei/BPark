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

    /** Input field for subscriber's national ID (must be 9 digits) */
    @FXML private TextField txtUserId;

    /** Input field for subscriber's first name */
    @FXML private TextField txtFirstName;

    /** Input field for subscriber's last name */
    @FXML private TextField txtLastName;

    /** Input field for subscriber's phone number (must start with 05 and be 10 digits) */
    @FXML private TextField txtPhone;

    /** Input field for subscriber's email address */
    @FXML private TextField txtEmail;

    /** Input field for username (at least 4 alphanumeric characters) */
    @FXML private TextField txtUsername;

    /** Input field for vehicle license plate number (7–10 digits) */
    @FXML private TextField txtVehicleId;

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
        // Clear all previous error messages before validating again
        clearErrors();

        // Read and trim all inputs
        String userId    = txtUserId.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName.getText().trim();
        String phone     = txtPhone.getText().trim();
        String email     = txtEmail.getText().trim();
        String username  = txtUsername.getText().trim();
        String vehicleId = txtVehicleId.getText().trim();

        boolean valid = true;

        if (userId.isEmpty() || !userId.matches("\\d{9}")) {
            errUserId.setText("Must be exactly 9 digits.");
            errUserId.setVisible(true);
            valid = false;
        }

        if (firstName.isEmpty()) {
            errFirstName.setText("First name is required.");
            errFirstName.setVisible(true);
            valid = false;
        }

        if (lastName.isEmpty()) {
            errLastName.setText("Last name is required.");
            errLastName.setVisible(true);
            valid = false;
        }

        if (phone.isEmpty() || !phone.matches("^05\\d{8}$")) {
            errPhone.setText("Phone must start with 05 and be 10 digits.");
            errPhone.setVisible(true);
            valid = false;
        }

        if (email.isEmpty() || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            errEmail.setText("Invalid email format.");
            errEmail.setVisible(true);
            valid = false;
        }

        if (username.isEmpty() || !username.matches("\\w{4,}")) {
            errUsername.setText("At least 4 alphanumeric characters.");
            errUsername.setVisible(true);
            valid = false;
        }

        if (vehicleId.isEmpty() || !vehicleId.matches("\\d{7,10}")) {
            errVehicleId.setText("Vehicle ID must be 7–10 digits.");
            errVehicleId.setVisible(true);
            valid = false;
        }

        // If any field was invalid, do not continue
        if (!valid) {
            lblStatus.setText("");
            return;
        }


        Subscriber sub = new Subscriber(
                0,          // Subscriber code is generated server-side
                userId,
                firstName,
                lastName,
                phone,
                email,
                username,
                null        // Tag ID will be generated on the server
        );

        client.getRequestSender().registerSubscriber(sub, vehicleId);

        // wait for server response
        lblStatus.setText("");
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
