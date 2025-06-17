package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

/**
 * Screen that lets a subscriber extend an active parking session
 * (adds four hours) as long as the session has not been extended before.
 *
 * Flow:
 *  1. User types the parking code.
 *  2. Controller validates that the field is not empty and is numeric.
 *  3. A request is sent to the server.  The server replies through
 *     ClientController -> onExtensionResponse().
 */
public class ExtendParkingController implements ClientAware {

    /** Input field for the numeric parking code */
    @FXML private TextField txtParkingCode;

    /** "Extend" button */
    @FXML private Button btnExtend;

    /** Label used for success / error feedback */
    @FXML private Label lblStatus;

    /** Shared client used to talk with the server */
    private ClientController client;

    /**
     * Injects the ClientController so this screen can send requests.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Fired by the "Extend" button.
     * Validates the text, converts to int, and sends the request.
     * Shows a local error if the code is missing or not numeric.
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
        client.extendParking(parkingCode);
    }

    /**
     * Callback used by ClientController after the server responds.
     *
     * @param success true if the extension was successful
     * @param message explanatory text returned by the server
     */
    public void onExtensionResponse(boolean success, String message) {
        UiUtils.setStatus(lblStatus, message, success);
    }
}

