package controllers;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import ui.UiUtils;

/**
 * Controller for BPARK's entry screen.
 * Lets the user choose between Guest mode and Login.
 */
public class MainController implements ClientAware {

    /* ------------------------------------------------------------------
     *  FXML-injected controls
     * ------------------------------------------------------------------ */
    @FXML private Button guestBtn;   // “Enter as Guest”
    @FXML private Button loginBtn;   // “Login”

    /* ------------------------------------------------------------------
     *  Runtime
     * ------------------------------------------------------------------ */
    private ClientController client;

    /** Injects the active client. */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /* ------------------------------------------------------------------
     *  Button handlers
     * ------------------------------------------------------------------ */

    /** Opens the guest interface. */
    @FXML
    public void handleGuest() {
        UiUtils.loadScreen(guestBtn,
                           "/client/GuestMainScreen.fxml",
                           "BPARK – Guest",
                           client);               // client may be null for guest flow
    }

    /** Opens the login screen. */
    @FXML
    public void handleLogin() {
        UiUtils.loadScreen(loginBtn,
                           "/client/LoginScreen.fxml",
                           "BPARK – Login",
                           client);
    }
    
    
    //this is good- need to improve UiUtilis
    public void handleGoToOrderSummarry() {
	    try {
	    	
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/GuestMainScreen.fxml")); //load the Placing_an_order_view.fxml after search on resources
	        Parent root = loader.load();

	        
	        GuestMainController controller = loader.getController(); //after loading the fxml- get the controller
	        controller.setClient(client);// move the client to the new controller
	        client.setGuestController(controller); //for act functions
	        
	        
	        Stage stage = (Stage) guestBtn.getScene().getWindow(); //get the stage
	        Scene scene = new Scene(root); //create new scene
	        
	        stage.setScene(scene);
	        stage.show();
	    } catch(Exception e) {
	    	System.out.println("Error!"+e.getMessage());
	    }
	}
}

