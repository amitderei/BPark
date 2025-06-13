package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

/**
 * Controller for the "Extend Parking Time" screen.
 * Allows subscribers to extend their active parking session by 4 hours,
 * provided they have an ongoing parking event and haven't extended before.
 *
 * Expected input: subscriber enters their parking code.
 * The system validates the code and attempts the extension via the server.
 */
public class ExtendParkingController implements ClientAware {

    @FXML private TextField txtParkingCode;
    @FXML private Button btnExtend;
    @FXML private Label lblStatus;

    private ClientController client;

    /**
     * Sets the client controller reference, allowing communication with the server.
     *
     * @param client the main ClientController instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Called when the "Extend" button is clicked.
     * Sends a request to the server to extend the parking session
     * corresponding to the entered parking code.
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

        // Send extension request to server
        client.extendParking(parkingCode);
    }

    /**
     * Called by the ClientController after receiving server response.
     * Displays success/failure message on screen.
     *
     * @param success true if the extension was applied, false otherwise
     * @param message message from the server to display to the user
     */
    public void onExtensionResponse(boolean success, String message) {
        UiUtils.setStatus(lblStatus, message, success);
    }
}
