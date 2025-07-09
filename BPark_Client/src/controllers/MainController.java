package controllers;

import java.net.URL;

import client.ClientController;
import common.Operation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import ui.DragUtilServer;
import ui.StageAware;
import ui.UiUtils;

/**
 * Entry screen of BPARK.
 * 
 * Allows the user to either continue as a guest or log in as a registered user.
 * Also includes navigation controls to go back or exit the application.
 */
public class MainController implements ClientAware, StageAware {
	
	//FXML UI elements for the BPARK entry screen
	
    @FXML 
    private Button guestBtn;

    @FXML 
    private Button loginBtn;

    @FXML 
    private Button btnExit;

    @FXML 
    private Button btnBack;
    
    @FXML
    private MediaView automaticParkingVideo;
    
    @FXML
    private MediaView parkingSystemVideo;
    
    /** The toolbar used to drag the undecorated main window. */
    @FXML
    private ToolBar dragArea;

    
    /** Media player for the automatic parking video preview. */
    private MediaPlayer mediaPlayerOfParkingVideo;
    
    /** Media player for the parking system video preview. */
    private MediaPlayer mediaPlayerOfParkingSystem;
    
    /** Shared ClientController used to send requests to the server */
    private ClientController client;
    
    /**
     * Enables drag-to-move functionality for the main window
     * using the top toolbar as the drag handle.
     *
     * @param stage the main JavaFX window
     */
    @Override
    public void setStage(Stage stage) {
        DragUtilServer.enableDrag(dragArea, stage);
    }

    
    /**
     * set the videos on MediaView
     */
    @FXML
    public void setVideos() {
    	//path to the videos
    	String pathOfParkingVideo="/media/AutomaticParking.mp4";
    	String pathOfParkingSystem="/media/parkingSystem.mp4";
    	
    	//URL of the videos
    	URL pathOfParkingVideoUrl= getClass().getResource(pathOfParkingVideo);
    	URL pathOfParkingSystemUrl= getClass().getResource(pathOfParkingSystem);
    	if (pathOfParkingVideoUrl==null || pathOfParkingSystemUrl==null) {
    		System.out.println("Video not found");
    		return;
    	}
    	//create media objects that represents the video file
    	Media mediaOfParkingVideo= new Media(pathOfParkingVideoUrl.toExternalForm());
    	Media mediaOfParkingSystem= new Media(pathOfParkingSystemUrl.toExternalForm());
    	
    	//create media player object that take control on media
    	mediaPlayerOfParkingVideo= new MediaPlayer(mediaOfParkingVideo);
    	mediaPlayerOfParkingSystem= new MediaPlayer(mediaOfParkingSystem);
    	
    	mediaPlayerOfParkingVideo.setMute(true);
    	mediaPlayerOfParkingSystem.setMute(true);
    	
    	//connect between media player to media view
    	automaticParkingVideo.setMediaPlayer(mediaPlayerOfParkingVideo);
    	parkingSystemVideo.setMediaPlayer(mediaPlayerOfParkingSystem);
    	
    	mediaPlayerOfParkingVideo.setAutoPlay(true);
    	mediaPlayerOfParkingSystem.setAutoPlay(true);
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
     * play the video of parking system
     */
    @FXML
    private void handleMediaPlayerOfBraudeClick(MouseEvent event) {
        if (mediaPlayerOfParkingSystem.getStatus() == MediaPlayer.Status.PLAYING) {
        	mediaPlayerOfParkingSystem.pause();
        } else {
        	mediaPlayerOfParkingSystem.play();
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
                client.sendToServer(new Object[] { Operation.EXIT });
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
