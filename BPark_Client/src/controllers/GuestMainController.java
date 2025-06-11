package controllers;


import javafx.scene.control.Label;
import client.ClientController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.util.Duration;
import ui.UiUtils;

/**
 * Controller for the guest main screen.
 * Allows basic guest functionality like viewing availability,
 * and returning to the entry screen.
 */
public class GuestMainController implements ClientAware {

    /** Reference to the client controller (may be null for guests) */
    private ClientController client;

    /** Button to return to the main welcome screen */
    @FXML
    private Button btnBack;
    
    @FXML
    private Button btnExit;
    
  //Author: Ravid changes for autmoaticly availability
  	@FXML
  	private Label availableSpotsLabel;

  	private Timeline updateTimeline;

  	
  	private void startAutoUpdate() {
  		updateTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> client.requestAvailableSpots()));
  		updateTimeline.setCycleCount(Timeline.INDEFINITE); //run in infinity loop
  		updateTimeline.play(); //start timer
  	}
  	
  	//server answer and this is update the label
  	public void updateAvailableSpots(int count) {
  	    availableSpotsLabel.setText("Free spots: " + count);
  	}

    /**
     * Injects the active ClientController (even for guests, may be needed for back navigation).
     *
     * @param client the client instance (may be null)
     */
  	@Override
  	public void setClient(ClientController client) {
  	    this.client = client;
  	    client.setGuestMainController(this);
  	    startAutoUpdate();
  	}

    /**
     * Triggered when the "Home" button is clicked.
     * This is already the home screen for guests.
     */
    @FXML
    private void handleHomeClick() {
        System.out.println("Home button clicked (guest already on home screen).");
    }

    /**
     * Triggered when the "Back" button is clicked.
     * Returns to the user type selection screen.
     */
    @FXML
    private void handleBackClick() {
    	if (updateTimeline != null) {
    		updateTimeline.stop();
        }
        UiUtils.loadScreen(btnBack,
                           "/client/MainScreen.fxml",
                           "Select User Type",
                           client); 
    }
    
    /**
	 * Disconnects from the server and exits the application.
	 */
	@FXML
	private void handleExitClick() {

		try {
			if (client != null && client.isConnected()) {
				client.sendToServer(new Object[] { "disconnect" });
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
