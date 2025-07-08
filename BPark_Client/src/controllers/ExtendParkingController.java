package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

/**
 * Controller for the "Extend Parking Time" screen.
 * Allows a subscriber (or a terminal user) to extend an active parking session by 4 hours.
 * The extension is allowed only if:
 * - The session is still active (exitDate and exitHour are null)
 * - It hasn't been extended before
 * - If a subscriber is logged in, the session must belong to them
 *
 * The request is sent to the server and handled via onExtensionResponse().
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
     * Called when the "Extend" button is clicked.
     * Validates the parking code and sends an extension request to the server.
     * - If a subscriber is logged in, the request includes their subscriber code.
     * - If used from a terminal, only the parking code is sent.
     *
     * Displays an error if input is missing or invalid.
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
            UiUtils.setStatus(lblStatus, "Invalid parking code. Must be a number (6 digits)", false);
            return;
        }

        // Case 1: if a subscriber is logged in (subscriber screen)
        if (client.getSubscriber() != null) {
            String subscriberCode = String.valueOf(client.getSubscriber().getSubscriberCode());
            client.getRequestSender().extendParking(parkingCode, subscriberCode);
        }

        // Case 2: no subscriber is logged in (terminal screen)
        else {
            // Send empty subscriber code – the server will handle validation
        	client.getRequestSender().extendParking(parkingCode, null);
        }
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

