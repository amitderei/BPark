package controllers;

import client.ClientController;
import common.Order;
import common.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ViewSubscriberDetailsController implements ClientAware{
	@FXML
	private Label headline;
	
	@FXML
	private Label sunscriberCode;
	
	@FXML
	private Label subscriberCodeDetail;
	
	@FXML
	private Label id;
	
	@FXML
	private Label idDetail;
	
	@FXML
	private Label firstName;
	
	@FXML
	private Label firstNameDetail;
	
	@FXML
	private Label lastName;
	
	@FXML
	private Label lastNameDetail;
	
	@FXML
	private Label username;
	
	@FXML
	private Label usernameDetail;
	
	@FXML
	private Label password;
	
	@FXML
	private Label passwordDetail;
	
	@FXML
	private Label email;
	
	@FXML
	private Label emailDetail;
	
	@FXML
	private Label phoneNumber;
	
	@FXML
	private Label phoneNumberDetail;
	
	@FXML
	private Button editBtn;
	
	private ClientController client; 
	
	private Subscriber subscriber;
	private String passwordStr;
	private SubscriberMainLayoutController mainLayoutController; 
	
	public void setLabels() {
		subscriberCodeDetail.setText(((Integer)subscriber.getSubscriberCode()).toString());
		idDetail.setText(subscriber.getUserId());
		firstNameDetail.setText(subscriber.getFirstName());
		lastNameDetail.setText(subscriber.getLastName());
		usernameDetail.setText(subscriber.getUsername());
		passwordDetail.setText(passwordStr);
		emailDetail.setText(subscriber.getEmail());
		phoneNumberDetail.setText(subscriber.getPhoneNum());
	}
	
	public void setSubscriberAndPassword() {
		this.subscriber=client.getSubscriber();
		this.passwordStr=client.getPassword();
	}
	
	public void setClient(ClientController client) {
		this.client=client;
	}
	
	public void handleGoToEdit() {
		  try {
		    	mainLayoutController=client.getMainLayoutController();
		    	mainLayoutController.loadScreen("/client/EditSubscriberDetailsScreen.fxml");
		    	
		    } catch (Exception e) {
		        System.out.println("Error:"+ e.getMessage());
		    }
	}
}
