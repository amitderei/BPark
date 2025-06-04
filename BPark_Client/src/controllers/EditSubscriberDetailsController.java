package controllers;

import client.ClientController;
import common.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EditSubscriberDetailsController {
	@FXML
	private Label headline;

	@FXML
	private Label sunscriberCode;

	@FXML
	private TextField subscriberCodeEdit;

	@FXML
	private Label id;

	@FXML
	private TextField idEdit;

	@FXML
	private Label firstName;

	@FXML
	private TextField firstNameEdit;

	@FXML
	private Label lastName;

	@FXML
	private TextField lastNameEdit;

	@FXML
	private Label username;

	@FXML
	private TextField usernameEdit;

	@FXML
	private Label password;

	@FXML
	private TextField passwordEdit;

	@FXML
	private Label email;

	@FXML
	private TextField emailEdit;

	@FXML
	private Label phoneNumber;

	@FXML
	private TextField phoneNumberEdit;

	@FXML
	private Button saveChangesBtn;

	private ClientController client;

	private Subscriber subscriber;
	private String passwordStr;
	
	/**
	 * initialize the field with text and disable some of them
	 */
	public void setTextOnField() {
		subscriberCodeEdit.setText(((Integer)subscriber.getSubscriberCode()).toString());
		idEdit.setText(subscriber.getUserId());
		firstNameEdit.setText(subscriber.getFirstName());
		lastNameEdit.setText(subscriber.getLastName());
		usernameEdit.setText(subscriber.getUsername());
		passwordEdit.setText(passwordStr);
		emailEdit.setText(subscriber.getEmail());
		phoneNumberEdit.setText(subscriber.getPhoneNum());
		
		subscriberCodeEdit.setDisable(true);
		idEdit.setDisable(true);
	}
	
	/**
	 * set subscriber details and password from client
	 */
	public void setSubscriberAndPassword() {
		this.subscriber=client.getSubscriber();
		this.passwordStr=client.getPassword();
	}
	
	public void setClient(ClientController client) {
		this.client=client;
	}
	
	

}
