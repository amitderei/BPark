package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

/**
 * Screen that lets a subscriber extend an active parking session
 * (adds 4 hours) as long as the session has not been extended before.
 *
 * Flow:
 *  1. User types the parking code.
 *  2. Controller validates that the field is not empty and is numeric.
 *  3. A request is sent to the server.  The server replies through
 *     ClientController -> onExtensionResponse().
 */
public class ExtendParkingController implements ClientAware {

    /** Input field for the numeric parking code */
    @FXML 
    private TextField txtParkingCode;

    @FXML 
    private Button btnExtend;

    /** Label used for success / error feedback */
    @FXML 
    private Label lblStatus;

    /** Reference to the active client used for server communication */
    private ClientController client;

	/**
	 * Injects the shared ClientController instance.
	 *
	 * @param client active client controller instance
	 */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Triggered when the "Extend" button is clicked.
     * Validates the parking code and sends an extension request to the server.
     * If input is invalid, shows an appropriate error message.
     */
    @FXML
    private void handleExtendClick() {

        String codeText = txtParkingCode.getText().trim();

        if (codeText.isEmpty()) {
            UiUtils.setStatus(lblStatus, "Please enter your parking code.", false);
            return;
        }

        int parkingCode;
        try {
            parkingCode = Integer.parseInt(codeText);
        } catch (NumberFormatException e) {
            UiUtils.setStatus(lblStatus, "Invalid parking code. Must be a number.", false);
            return;
        }

        // Forward the request to the server
        String subscriberCode = String.valueOf(client.getSubscriber().getSubscriberCode());
        client.extendParking(parkingCode, subscriberCode);
    }

    /**
     * Called after the server responds to the extension request.
     * Updates the screen with a success or error message.
     *
     * @param success true if the extension was processed successfully
     * @param message feedback message returned from the server
     */
    public void onExtensionResponse(boolean success, String message) {
        UiUtils.setStatus(lblStatus, message, success);
    }
}

