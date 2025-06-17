package controllers;

import java.io.IOException;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import ui.UiUtils;

/**
 * Layout controller for guest users.
 * Puts the regular toolbar on top and a tiny side-menu with one action:
 * checking spot availability.
 */
public class GuestMainLayoutController implements ClientAware {

    // ---------- FXML ----------

    @FXML private Button btnExit;
    @FXML private Button btnBack;
    @FXML private Button btnHome;
    @FXML private Button btnCheckAvailability;
    @FXML private AnchorPane center;

    // ---------- runtime ----------

    private ClientController client;

    /**
     * Called by the framework right after the FXML is loaded.
     * We show the guest home screen straight away.
     */
    @FXML
    public void initialize() {
        handleHomeClick();
    }

    /**
     * Gives the controller a live reference to the client object
     * so we can talk to the server when needed.
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    // ---------- toolbar buttons ----------

    @FXML
    private void handleHomeClick() {
        loadScreen("/client/GuestMainScreen.fxml");
    }

    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnBack,
                           "/client/SelectionScreen.fxml",
                           "BPARK – Welcome",
                           client);
    }

    @FXML
    private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[] { "disconnect" });
                client.closeConnection();
            }
        } catch (Exception ignored) {
            // not critical – we're quitting anyway
        }
        Platform.exit();
        System.exit(0);
    }

    // ---------- side-menu button ----------

    @FXML
    private void handleCheckAvailability() {
        loadScreen("/client/AvailabilityScreen.fxml");
    }

    // ---------- helpers ----------

    /**
     * Loads the given FXML into the center pane.
     *
     * @param fxml path to the FXML resource
     */
    private void loadScreen(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = loader.load();

            // if the loaded controller needs a client, pass it on
            Object ctrl = loader.getController();
            if (ctrl instanceof ClientAware aware) {
                aware.setClient(client);
            }
            
            if (ctrl instanceof AvailabilityController controller) {
                controller.setClient(client); 
                client.setAvailabilityController(controller);
                client.requestParkingAvailability(); 
            }

            center.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
