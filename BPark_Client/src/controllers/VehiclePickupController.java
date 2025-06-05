package controllers;
import javafx.fxml.FXML;
import javafx.application.Platform;

import javafx.application.Platform;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import ui.UiUtils;

/**
 * Controller for the vehicle pickup screen.
 * Handles subscriber authentication and car retrieval logic.
 */
public class VehiclePickupController implements ClientAware {

    // -------------------- FXML fields --------------------

    @FXML private TextField txtSubscriberCode;
    @FXML private TextField txtTagId;
    @FXML private Button btnNext;
    @FXML private Button btnReadTag;
    @FXML private TextField txtParkingCode;
    @FXML private Button btnCollectCar;
    @FXML private Button btnLostCode;
    @FXML private Label lblParkingPrompt;
    @FXML private Label lblStatus;

    // -------------------- runtime --------------------
    private ClientController client;
    private int validatedSubscriberCode = -1; // stores verified subscriberCode

    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setPickupController(this);
    }

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

    public Label getStatusLabel() {
        return lblStatus;
    }

    /**
     * Called when "Next" is clicked. Validate by subscriber code.
     */
    @FXML
    public void validateSubscriber() {
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
     * Called when "Identify by Tag" is clicked.
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
     * Called after successful subscriber validation by manual code.
     */
    public void onSubscriberValidated() {
        try {
            validatedSubscriberCode = Integer.parseInt(txtSubscriberCode.getText());
        } catch (NumberFormatException ignored) {
            // shouldn't happen if flow is correct
        }

        lockAfterValidation();
    }

    /**
     * Called after successful validation via Tag.
     * Subscriber code is injected from server response.
     */
    public void onSubscriberValidated(int subscriberCode) {
        validatedSubscriberCode = subscriberCode;
        txtSubscriberCode.setText(String.valueOf(subscriberCode));
        lockAfterValidation();
    }

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
        txtParkingCode.setTooltip(new Tooltip("Enter the parking code you received after placing your parking order."));

        btnCollectCar.setVisible(true);
        btnCollectCar.setManaged(true);
        btnLostCode.setVisible(true);
        btnLostCode.setManaged(true);

        UiUtils.setStatus(lblStatus, "Subscriber verified. Please enter your parking code.", true);
    }

    /**
     * Collect car logic using subscriberCode + parkingCode
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
            UiUtils.setStatus(lblStatus, "An error occurred while trying to collect the vehicle.", false);
        }
    }

    /**
     * Sends a request to resend the confirmation code.
     */
    @FXML 
    public void sendLostCode() {
        try {
            client.sendLostParkingCode(validatedSubscriberCode);
        } catch (Exception e) {
            UiUtils.setStatus(lblStatus, "An error occurred while requesting your code.", false);
        }
    }

    /**
     * Disables controls after successful vehicle pickup.
     */
    public void disableAfterPickup() {
        txtParkingCode.setDisable(true);
        btnCollectCar.setDisable(true);
        btnLostCode.setDisable(true);
    }
    
    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnCollectCar, "/client/TerminalMainLayout.fxml", "BPARK – Terminal", client);
    }
    
    @FXML
    private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[]{"disconnect"});
                client.closeConnection();
                System.out.println("Client disconnected successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to disconnect client: " + e.getMessage());
        }
        Platform.exit();
        System.exit(0);
    }

}

