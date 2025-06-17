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

    /* ---------- identity step ---------- */
    @FXML private TextField txtSubscriberCode;
    @FXML private TextField txtTagId;
    @FXML private Button btnNext;
    @FXML private Button btnReadTag;

    /* ---------- pickup step ---------- */
    @FXML private TextField txtParkingCode;
    @FXML private Button btnCollectCar;
    @FXML private Button btnLostCode;

    /* ---------- misc UI ---------- */
    @FXML private Label lblParkingPrompt;
    @FXML private Label lblStatus;

    /* ---------- runtime ---------- */
    private ClientController client;
    /** Stores the subscriber code once the user is verified. */
    private int validatedSubscriberCode = -1;

    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setPickupController(this);
    }

    /** Hides step-2 controls until the subscriber is verified. */
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

    /** @return the label used for live status messages */
    public Label getStatusLabel() {
        return lblStatus;
    }

    /**
     * User pressed "Next" – validate by manual subscriber code.
     * Shows local error if the field is empty or not numeric.
     */
    @FXML
    public void validateSubscriber() {
        if (client == null) {
            UiUtils.setStatus(lblStatus, "Client not initialized.", false);
            UiUtils.showAlert("System Error",
                    "Client was not connected. Please try again.",
                    javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        try {
            int code = Integer.parseInt(txtSubscriberCode.getText());
            client.validateSubscriber(code);
        } catch (NumberFormatException e) {
            UiUtils.setStatus(lblStatus, "Subscriber code must be a number.", false);
            UiUtils.showAlert("BPARK - Error",
                    "Subscriber code must be a number.",
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    /**
     * Reads the tag ID typed by the operator and sends it for validation.
     * Shows a red status if the field is empty.
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

    /** Called by ClientController after manual subscriber code is accepted. */
    public void onSubscriberValidated() {
        try {
            validatedSubscriberCode = Integer.parseInt(txtSubscriberCode.getText());
        } catch (NumberFormatException ignored) { }
        lockAfterValidation();
    }

    /**
     * Sends the subscriber-code + parking-code combo to the server.
     * Local validation: parking code must be numeric.
     */
    @FXML
    public void collectCar() {
        try {
            int parkingCode = Integer.parseInt(txtParkingCode.getText());
            client.collectCar(validatedSubscriberCode, parkingCode);
        } catch (NumberFormatException e) {
            UiUtils.setStatus(lblStatus, "Parking code must be numeric.", false);
            UiUtils.showAlert("BPARK - Error",
                    "Parking code must be numeric.",
                    javafx.scene.control.Alert.AlertType.ERROR);
        } catch (Exception ex) {
            UiUtils.setStatus(lblStatus,
                    "An error occurred while trying to collect the vehicle.",
                    false);
        }
    }

    /**
     * Requests the system to resend the confirmation code
     * (used when the subscriber forgot the parking code).
     */
    @FXML
    public void forgotMyCode() {
        try {
            client.forgotMyParkingCode(validatedSubscriberCode);
        } catch (Exception e) {
            UiUtils.setStatus(lblStatus,
                    "An error occurred while requesting your code.",
                    false);
        }
    }

    /**
     * Server confirmed tag-based validation – subscriber code arrives as arg.
     *
     * @param subscriberCode numeric code returned by the server
     */
    public void onSubscriberValidated(int subscriberCode) {
        validatedSubscriberCode = subscriberCode;
        txtSubscriberCode.setText(String.valueOf(subscriberCode));
        lockAfterValidation();
    }

    /** Reveals step-2 controls and locks the identity fields. */
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

    /** Called when the server confirms a successful pickup. */
    public void disableAfterPickup() {
        txtParkingCode.setDisable(true);
        btnCollectCar.setDisable(true);
        btnLostCode.setDisable(true);
    }
}

