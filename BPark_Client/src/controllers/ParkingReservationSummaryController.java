package controllers;

import client.ClientController;
import common.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ParkingReservationSummaryController {

	@FXML
	private Label headline;
	
	@FXML
	private Label reservationNumber;
	
	@FXML
	private Label reservationNumberOfOrder;
	
	@FXML
	private Label confirmationCode;
	
	@FXML
	private Label confirmationCodeOfOrder;
	
	@FXML
	private Label subscriberCode;
	
	@FXML
	private Label subscriberCodeOfOrder;
	
	@FXML
	private Label orderSubmissionDate;
	
	@FXML
	private Label orderSubmissionDateOfOrder;
	
	@FXML
	private Label reservationDate;
	
	@FXML
	private Label reservationDateOfOrder;
	
	@FXML
	private Label reservationTime;
	
	@FXML
	private Label reservationTimeOfOrder;
	
	@FXML
	private Label parkingSpace;
	
	@FXML
	private Label parkingSpaceOfOrder;
	
	@FXML
	private Label thankU;
	
	@FXML
	private Label support;
	
	@FXML
	private Button backToHome;
	
	private ClientController client;

	/**
	 * set client
	 * 
	 * @param client
	 */
	public void setClient(ClientController client) {
		this.client = client;
	}

	
	public void setLabels(Order order) {
		reservationNumberOfOrder.setText(((Integer)order.getOrderNumber()).toString());
		confirmationCodeOfOrder.setText(order.getConfirmationCode());
		subscriberCodeOfOrder.setText(((Integer) order.getSubscriberId()).toString());
		orderSubmissionDateOfOrder.setText(order.getDateOfPlacingAnOrder().toString());
		reservationDateOfOrder.setText(order.getOrderDate().toString());
		reservationTimeOfOrder.setText(order.getArrivalTime().toString());
		parkingSpaceOfOrder.setText(((Integer)order.getParkingSpace()).toString());
	}
	
	public void handleGoToMainScreen() {
	    try {
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/SubscriberMainScreen.fxml")); //load the Placing_an_order_view.fxml after search on resources
	        Parent root = loader.load();

	        
	        SubscriberMainController controller = loader.getController(); //after loading the fxml- get the controller
	        controller.setClient(client);// move the client to the new controller
	        client.setSubscriberMainController(controller); //for act functions
	        

	        
	        Stage stage = (Stage) backToHome.getScene().getWindow(); //get the stage
	        Scene scene = new Scene(root); //create new scene
	        stage.setScene(scene);
	        stage.show();
	    } catch (Exception e) {
	        System.out.println("Error:"+ e.getMessage());
	    }
	}
	
}
