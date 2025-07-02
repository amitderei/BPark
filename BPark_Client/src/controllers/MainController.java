package controllers;

import java.net.URL;

import client.ClientController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import ui.UiUtils;

/**
 * Entry screen of BPARK.
 * 
 * Allows the user to either continue as a guest or log in as a registered user.
 * Also includes navigation controls to go back or exit the application.
 */
public class MainController implements ClientAware {
	
    /** Button to enter the system as a guest (no login) */
    @FXML 
    private Button guestBtn;

    /** Button to navigate to the login screen for registered users */
    @FXML 
    private Button loginBtn;

    @FXML 
    private Button btnExit;

    @FXML 
    private Button btnBack;
    
    @FXML
    private MediaView automaticParkingVideo;
    
    @FXML
    private MediaView braudeVideo;
    
    private MediaPlayer mediaPlayerOfParkingVideo;
    private MediaPlayer mediaPlayerOfBraude;
    
    
    /** Shared ClientController used to send requests to the server */
    private ClientController client;
    
    /**
     * set the videos on MediaView
     */
    @FXML
    public void setVideos() {
    	//path to the videos
    	String pathOfParkingVideo="/client/AutomaticParking.mp4";
    	String pathOfBraude="/client/Braude.mp4";
    	
    	//URL of the videos
    	URL pathOfParkingVideoUrl= getClass().getResource(pathOfParkingVideo);
    	URL pathOfBraudeUrl= getClass().getResource(pathOfBraude);
    	if (pathOfParkingVideoUrl==null || pathOfBraudeUrl==null) {
    		System.out.println("Video not found");
    		return;
    	}
    	//create media objects that represents the video file
    	Media mediaOfParkingVideo= new Media(pathOfParkingVideoUrl.toExternalForm());
    	Media mediaOfBraude= new Media(pathOfBraudeUrl.toExternalForm());
    	
    	//create media player object that take control on media
    	mediaPlayerOfParkingVideo= new MediaPlayer(mediaOfParkingVideo);
    	mediaPlayerOfBraude= new MediaPlayer(mediaOfBraude);
    	
    	//connect between media player to media view
    	automaticParkingVideo.setMediaPlayer(mediaPlayerOfParkingVideo);
    	braudeVideo.setMediaPlayer(mediaPlayerOfBraude);
    }
    
    
    /**
     * play the video of automatic parking system
     */
    @FXML
    private void handleAutomaticParkingVideoClick(MouseEvent event) {
        if (mediaPlayerOfParkingVideo.getStatus() == MediaPlayer.Status.PLAYING) {
        	mediaPlayerOfParkingVideo.pause();
        } else {
        	mediaPlayerOfParkingVideo.play();
        }
    }
    
    /**
     * play the video of braude
     */
    @FXML
    private void handleMediaPlayerOfBraudeClick(MouseEvent event) {
        if (mediaPlayerOfBraude.getStatus() == MediaPlayer.Status.PLAYING) {
        	mediaPlayerOfBraude.pause();
        } else {
        	mediaPlayerOfBraude.play();
        }
    }

    /**
     * Injects the client controller used for server communication and screen transitions.
     *
     * @param client the active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Loads the Guest layout and opens the main interface
     * for unregistered users.
     */
    @FXML
    public void handleGuest() {
        UiUtils.loadScreen(guestBtn,
                "/client/GuestMainLayout.fxml",
                "BPARK – Guest",
                client);
    }

    /**
     * Loads the login screen for registered users
     * including Subscribers, Attendants, or Managers.
     */
    @FXML
    public void handleLogin() {
        UiUtils.loadScreen(loginBtn,
                "/client/LoginScreen.fxml",
                "BPARK – Login",
                client);
    }

    /**
     * Disconnects from the server (if connected) and exits the application.
     * Called when the user presses the Exit button.
     */
    @FXML
    private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[] { "disconnect" });
                client.closeConnection();
            }
        } catch (Exception ignored) { }
        Platform.exit();
        System.exit(0);
    }

    /**
     * Loads the Mode Selection screen, allowing the user to choose
     * between App mode and Terminal mode.
     */
    @FXML
    private void handleBackClick() {
        UiUtils.loadScreen(btnBack,
                "/client/SelectionScreen.fxml",
                "Select User Type",
                client);
    }
}
