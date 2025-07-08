package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import ui.UiUtils;

/**
 * Handles the "Retrieve Vehicle" flow at the terminal.  
 * Step 1 – subscriber proves identity (manual code or RFID tag).  
 * Step 2 – subscriber enters the six-digit parking code to release the car.
 */
public class VehiclePickupController implements ClientAware {
    /** Text field for entering the subscriber code manually */
    @FXML private TextField txtSubscriberCode;

    /** Text field for entering the tag ID (RFID-based authentication) */
    @FXML private TextField txtTagId;

    /** Button to proceed with subscriber code validation */
    @FXML private Button btnNext;

    /** Button to validate using tag ID */
    @FXML private Button btnReadTag;

    /** Text field for entering the parking code to retrieve the vehicle */
    @FXML private TextField txtParkingCode;

    /** Button to collect the vehicle after entering a valid parking code */
    @FXML private Button btnCollectCar;

    /** Button to request a resend of the parking code if lost */
    @FXML private Button btnLostCode;

    /** Label that prompts the user to enter the parking code (step 2) */
    @FXML private Label lblParkingPrompt;

    /** Label used to display live status messages (success/error) */
    @FXML private Label lblStatus;

    /** Reference to the shared ClientController used for server communication */
    private ClientController client;

    /** Holds the verified subscriber code after successful validation */
    private int validatedSubscriberCode = -1;

    /**
     * Injects the ClientController and registers this controller instance inside it.
     *
     * @param client active ClientController
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setPickupController(this);
    }

    /**
     * Initializes the screen – hides the second step (parking code input)
     * until the subscriber is verified.
     */
    @FXML
    public void initialize() {
        txtParkingCode.setVisible(false);
        txtParkingCode.setManaged(false);
        btnCollectCar.setVisible(false);
        btnCollectCar.setManaged(false);
        btnLostCode.setVisible(false);
        btnLostCode.setManaged(false);
        lblParkingPrompt.setVisible(false);
        lblParkingPrompt.setManaged(false);
    }

    /**
     * @return the label used for showing system status messages
     */
    public Label getStatusLabel() {
        return lblStatus;
    }

    /**
     * Triggered when the user clicks "Next" to verify their subscriber code.
     * Performs local validation (numeric check) before sending the code to the server.
     */
    @FXML
    public void validateSubscriber() {
        if (client == null) {
            UiUtils.setStatus(lblStatus, "Client not initialized.", false);
            UiUtils.showAlert("System Error", "Client was not connected. Please try again.",
                    javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        try {
            int code = Integer.parseInt(txtSubscriberCode.getText());
            client.getRequestSender().validateSubscriber(code);
        } catch (NumberFormatException e) {
            UiUtils.setStatus(lblStatus, "Subscriber code must be a number.", false);
        }
    }

    /**
     * Triggered when the user clicks "Read Tag" to validate via RFID.
     * Sends the tag ID to the server after checking that it’s not empty.
     */
    @FXML
    public void handleReadTag() {
        String tagId = txtTagId.getText();
        if (tagId.isEmpty()) {
            UiUtils.setStatus(lblStatus, "Please enter a valid Tag ID.", false);
            return;
        }

        UiUtils.setStatus(lblStatus, "Reading tag... (" + tagId + ")", true);
        client.getRequestSender().validateSubscriberByTag(tagId);
    }

    /**
     * Called after the server confirms subscriber identity via manual code.
     * Locks the first step and reveals the second.
     */
    public void onSubscriberValidated() {
        try {
            validatedSubscriberCode = Integer.parseInt(txtSubscriberCode.getText());
        } catch (NumberFormatException ignored) { }
        lockAfterValidation();
    }

    /**
     * Called when the user clicks "Collect Car".
     * Validates the parking code input and sends the request to the server.
     *
     * Only 6-digit numeric codes (between 100000 and 999999) are allowed.
     * If the input is invalid or too long, we show a friendly message.
     */
    @FXML
    public void collectCar() {
        String input = txtParkingCode.getText().trim(); // remove spaces

        // make sure the input is only digits (regex), before parsing
        if (!input.matches("\\d+")) {
            UiUtils.setStatus(lblStatus, "Parking code must contain digits only.", false);
            txtParkingCode.clear();
            return;
        }

        try {
            int parkingCode = Integer.parseInt(input); // might still throw if input too big

            if (parkingCode < 100000 || parkingCode > 999999) {
                UiUtils.setStatus(lblStatus, "Parking code is incorrect.", false);
                txtParkingCode.clear();
                return;
            }

            client.getRequestSender().collectCar(validatedSubscriberCode, parkingCode);

        } catch (NumberFormatException e) {
            // input was numeric, but too large for int (e.g. more than 9 digits)
            UiUtils.setStatus(lblStatus, "Parking code is incorrect. Please try again.", false);
            txtParkingCode.clear();
        } catch (Exception ex) {
            UiUtils.setStatus(lblStatus,
                    "An error occurred while trying to collect the vehicle.", false);
        }
    }

    /**
     * Triggered when the user clicks "I Lost My Code".
     * Sends a request to the server to resend the confirmation code.
     */
    @FXML
    public void forgotMyCode() {
        try {
        	client.getRequestSender().forgotMyParkingCode(validatedSubscriberCode);
        } catch (Exception e) {
            UiUtils.setStatus(lblStatus,
                    "An error occurred while requesting your code.", false);
        }
    }

    /**
     * Called when the server confirms RFID-based validation.
     *
     * @param subscriberCode numeric code returned by the server
     */
    public void onSubscriberValidated(int subscriberCode) {
        validatedSubscriberCode = subscriberCode;
        txtSubscriberCode.setText(String.valueOf(subscriberCode));
        lockAfterValidation();
    }

    /**
     * Unlocks the second step and disables the first step (identity fields).
     */
    private void lockAfterValidation() {
        txtSubscriberCode.setDisable(true);
        txtTagId.setDisable(true);
        btnNext.setDisable(true);
        btnReadTag.setDisable(true);

        lblParkingPrompt.setVisible(true);
        lblParkingPrompt.setManaged(true);

        txtParkingCode.setVisible(true);
        txtParkingCode.setManaged(true);
        txtParkingCode.setPromptText("e.g. 654321");
        txtParkingCode.setTooltip(new Tooltip(
                "Enter the parking code you received after placing your order."));

        btnCollectCar.setVisible(true);
        btnCollectCar.setManaged(true);
        btnLostCode.setVisible(true);
        btnLostCode.setManaged(true);

        UiUtils.setStatus(lblStatus,
                "Subscriber verified. Please enter your parking code.", true);
    }

    /**
     * Called after successful vehicle pickup.
     * Disables all controls to prevent reuse.
     */
    public void disableAfterPickup() {
        txtParkingCode.setDisable(true);
        btnCollectCar.setDisable(true);
        btnLostCode.setDisable(true);
    }
    
    /**
     * Clears the parking code field and puts the cursor back inside it,
     * so the user can easily try again.
     */
    public void resetParkingCodeField() {
        txtParkingCode.clear();         // remove whatever the user typed
        txtParkingCode.requestFocus(); // put the blinking cursor back in the field
    }
}
