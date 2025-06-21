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

    // ------------------------ Identity step ------------------------

    /** Text field for entering the subscriber code manually */
    @FXML private TextField txtSubscriberCode;

    /** Text field for entering the tag ID (RFID-based authentication) */
    @FXML private TextField txtTagId;

    /** Button to proceed with subscriber code validation */
    @FXML private Button btnNext;

    /** Button to validate using tag ID */
    @FXML private Button btnReadTag;

    // ------------------------ Pickup step ------------------------

    /** Text field for entering the parking code to retrieve the vehicle */
    @FXML private TextField txtParkingCode;

    /** Button to collect the vehicle after entering a valid parking code */
    @FXML private Button btnCollectCar;

    /** Button to request a resend of the parking code if lost */
    @FXML private Button btnLostCode;

    // ------------------------ Misc UI ------------------------

    /** Label that prompts the user to enter the parking code (step 2) */
    @FXML private Label lblParkingPrompt;

    /** Label used to display live status messages (success/error) */
    @FXML private Label lblStatus;

    // ------------------------ Runtime ------------------------

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
            client.validateSubscriber(code);
        } catch (NumberFormatException e) {
            UiUtils.setStatus(lblStatus, "Subscriber code must be a number.", false);
            UiUtils.showAlert("BPARK - Error", "Subscriber code must be a number.",
                    javafx.scene.control.Alert.AlertType.ERROR);
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
        client.validateSubscriberByTag(tagId);
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
     * Triggered when the user enters the parking code and clicks "Collect Car".
     * Sends both the subscriber code and parking code to the server.
     */
    @FXML
    public void collectCar() {
        try {
            int parkingCode = Integer.parseInt(txtParkingCode.getText());
            client.collectCar(validatedSubscriberCode, parkingCode);
        } catch (NumberFormatException e) {
            UiUtils.setStatus(lblStatus, "Parking code must be numeric.", false);
            UiUtils.showAlert("BPARK - Error", "Parking code must be numeric.",
                    javafx.scene.control.Alert.AlertType.ERROR);
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
            client.forgotMyParkingCode(validatedSubscriberCode);
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
}
