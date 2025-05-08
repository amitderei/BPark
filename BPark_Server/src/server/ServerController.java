package server;

import server.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServerController {

	@FXML
	private Button btnSend = null;
	
	@FXML
	private TextField txtPort;
	
	@FXML
	private Label lblConnection;
	
	@FXML
	private Label lblEx;
	
	
	private String getport() {
		return txtPort.getText();			
	}
	
	public void connect(ActionEvent event) throws Exception {
		String p;
		
		p=getport();
		if(p.trim().isEmpty()) {
			System.out.println("You must enter a port number");
					
		}
		else
		{
			((Node)event.getSource()).getScene().getWindow().hide(); //hiding primary window
			Stage primaryStage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			BparkServerApp.runServer(p);
		}
	}


	

}
