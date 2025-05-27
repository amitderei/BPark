package controllers;


import javafx.scene.control.Label;
import client.ClientController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
    
  //Author: Ravid changes for autmoaticly availability
  	@FXML
  	private Label availableSpotsLabel;

  	private Timeline updateTimeline;

  	//start initilize for the 
  	public void initialize() {
  		startAutoUpdate();
  	}
  	private void startAutoUpdate() {
  		updateTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> client.requestAvailableSpots()));
  		updateTimeline.setCycleCount(Timeline.INDEFINITE); //run in infinity loop
  		updateTimeline.play(); //start timer
  	}
  	
  	//server answer and this is update the label
  	public void updateAvailableSpots(int count) {
  		System.out.println("here we are in the center of town");
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
        UiUtils.loadScreen(btnBack,
                           "/client/MainScreen.fxml",
                           "Select User Type",
                           client); 
    }

    /**
     * Triggered when the "Check Parking Availability" button is clicked.
     */
    @FXML
    private void handleCheckAvailability() {
        System.out.println("Checking parking availability...");
    }
}
