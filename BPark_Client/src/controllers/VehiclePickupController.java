package controllers;

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

    @FXML
    private TextField txtSubscriberCode; // Input for subscriber code

    @FXML
    private Button btnNext; // Button to validate subscriber

    @FXML
    private Label lblStatus; // Displays status messages

    @FXML
    private TextField txtParkingCode; // Input for parking code (shown after validation)

    @FXML
    private Button btnCollectCar; // Button to collect car

    @FXML
    private Button btnLostCode; // Button to retrieve parking code

    @FXML
    private Button btnExtend; // Button to extend parking  ←  נוסף כדי להתאים ל-FXML

    @FXML
    private Label lblParkingPrompt;

    // -------------------- runtime --------------------

    private ClientController client; // Will be injected by setClient()

    /**
     * Returns the status label used for displaying messages to the user.
     *
     * @return the Label instance used for success/error feedback in the vehicle pickup screen.
     */
    public Label getStatusLabel() {
        return lblStatus;
    }

    // ----------------------------------------------------
    //  Initialization
    // ----------------------------------------------------

    /**
     * Called after the FXML is loaded. Hides elements until subscriber is validated.
     */
    @FXML
    public void initialize() {
        txtParkingCode.setVisible(false);
        txtParkingCode.setManaged(false);
        btnCollectCar.setVisible(false);
        btnCollectCar.setManaged(false);
        btnLostCode.setVisible(false);
        btnLostCode.setManaged(false);
        btnExtend.setVisible(false);
        btnExtend.setManaged(false);
        lblParkingPrompt.setVisible(false);
        lblParkingPrompt.setManaged(false);
    }

    // ----------------------------------------------------
    //  Client injection (implements ClientAware)
    // ----------------------------------------------------

    /**
     * Injects the connected ClientController instance.
     * This method is called automatically by the screen loader.
     *
     * @param client the connected ClientController instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setPickupController(this); // link back
    }

    // ----------------------------------------------------
    //  Stage 1 – subscriber verification
    // ----------------------------------------------------

    /**
     * Called when the "Next" button is clicked.
     * Sends a request to validate the subscriber code.
     */
    @FXML
    public void validateSubscriber() {
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
     * Called when the subscriber is validated successfully.
     * Reveals the confirmation code input and related action buttons.
     * Locks the subscriber code field and disables the "Next" button.
     */
    public void onSubscriberValidated() {
        // Lock subscriber code field to prevent further edits
        txtSubscriberCode.setDisable(true);

        // Disable the "Next" button to prevent re-click
        btnNext.setDisable(true);

        // Show and configure confirmation code label
        lblParkingPrompt.setVisible(true);
        lblParkingPrompt.setManaged(true);
        lblParkingPrompt.setText("Enter your parking code:");

        // Show and configure confirmation code field
        txtParkingCode.setVisible(true);
        txtParkingCode.setManaged(true);
        txtParkingCode.setPromptText("e.g. 654321");
        txtParkingCode.setTooltip(new Tooltip(
                "Enter the confirmation code you received after placing your parking order."));

        // Show action buttons
        btnCollectCar.setVisible(true);
        btnCollectCar.setManaged(true);
        btnLostCode.setVisible(true);
        btnLostCode.setManaged(true);
        btnExtend.setVisible(true);
        btnExtend.setManaged(true);

        // Show success message
        UiUtils.setStatus(lblStatus,
                "Subscriber verified. Please enter your confirmation code.", true);
    }

    // ----------------------------------------------------
    //  Stage 2 – actions
    // ----------------------------------------------------

    /**
     * Called when the "Collect Car" button is clicked.
     * Sends a request to the server to process vehicle pickup
     * based on subscriber code and parking code (entered by the user).
     */
    @FXML
    public void collectCar() {
        try {
            // Parse subscriber code from the input field
            int subscriberCode = Integer.parseInt(txtSubscriberCode.getText());

            // Parse parking confirmation code (a.k.a parking code)
            int parkingCode = Integer.parseInt(txtParkingCode.getText());

            // Send the pickup request to the server
            client.collectCar(subscriberCode, parkingCode);

        } catch (NumberFormatException e) {
            // Input was not a valid integer
            UiUtils.setStatus(lblStatus,
                    "Subscriber code and confirmation code must be numeric.", false);
            UiUtils.showAlert("BPARK - Error",
                    "Subscriber code and confirmation code must be numeric.",
                    javafx.scene.control.Alert.AlertType.ERROR);

        } catch (Exception ex) {
            // Catch any unexpected error
            UiUtils.setStatus(lblStatus,
                    "An error occurred while trying to collect the vehicle.", false);
        }
    }

    /**
     * Called when the "Extend Parking" button is clicked.
     * Sends a request to the server to mark this parking event as extended.
     */
    @FXML
    public void extendParking() {
        try {
            int subscriberCode = Integer.parseInt(txtSubscriberCode.getText().trim());
            client.requestExtension(subscriberCode); // send request to server
        } catch (NumberFormatException e) {
            UiUtils.setStatus(lblStatus, "Subscriber code must be numeric.", false);
        }
    }

    /**
     * Called when the "Lost Code" button is clicked.
     * Sends a request to the server to resend the parking code to the subscriber
     * via email and SMS.
     */
    @FXML
    public void sendLostCode() {
        try {
            // Parse subscriber code from input field
            int subscriberCode = Integer.parseInt(txtSubscriberCode.getText());

            // Send request to server
            client.sendLostParkingCode(subscriberCode);

        } catch (NumberFormatException e) {
            // Input was not a valid integer
            UiUtils.setStatus(lblStatus, "Subscriber code must be numeric.", false);
            UiUtils.showAlert("BPARK - Error",
                    "Subscriber code must be numeric.",
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    // ----------------------------------------------------
    //  Callbacks
    // ----------------------------------------------------

    /**
     * Disables all interactive controls after a successful pickup so the
     * user cannot trigger the action twice.
     */
    public void disableAfterPickup() {
        txtParkingCode.setDisable(true); // lock confirmation code field
        btnCollectCar.setDisable(true);  // prevent extra pickup clicks
        btnLostCode.setDisable(true);    // block “lost code”
        btnExtend.setDisable(true);      // block extension after pickup
    }

}

