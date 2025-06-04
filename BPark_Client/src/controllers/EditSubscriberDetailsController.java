package controllers;

import client.ClientController;
import common.Subscriber;
import common.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

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
	private MainLayoutController mainLayoutController; 

	/**
	 * initialize the field with text and disable some of them
	 */
	public void setTextOnField() {
		subscriberCodeEdit.setText(((Integer) subscriber.getSubscriberCode()).toString());
		idEdit.setText(subscriber.getUserId());
		firstNameEdit.setText(subscriber.getFirstName());
		lastNameEdit.setText(subscriber.getLastName());
		usernameEdit.setText(subscriber.getUsername());
		passwordEdit.setText(passwordStr);
		emailEdit.setText(subscriber.getEmail());
		phoneNumberEdit.setText(subscriber.getPhoneNum());

		subscriberCodeEdit.setDisable(true);
		usernameEdit.setDisable(true);
		idEdit.setDisable(true);
	}

	/**
	 * set subscriber details and password from client
	 */
	public void setSubscriberAndPassword() {
		this.subscriber = client.getSubscriber();
		this.passwordStr = client.getPassword();
	}

	public void setClient(ClientController client) {
		this.client = client;
	}

	/**
	 * check if there is a different between existed details to the details that user entered
	 * if there is at least one field empty- it would show alert and the action doesn't success
	 */
	public void saveChanges() {
		if(subscriberCodeEdit.getText().trim().isEmpty() || subscriberCodeEdit.getText().trim().isEmpty() || firstNameEdit.getText().trim().isEmpty() || lastNameEdit.getText().trim().isEmpty()|| passwordEdit.getText().trim().isEmpty() || emailEdit.getText().trim().isEmpty() || phoneNumberEdit.getText().trim().isEmpty()) {
			UiUtils.showAlert("Error", "There is empty field", AlertType.ERROR);
			return;
		}
		Subscriber newDetails= new Subscriber(Integer.parseInt(subscriberCodeEdit.getText()), subscriber.getUserId(), firstNameEdit.getText(), lastNameEdit.getText(), phoneNumberEdit.getText(), emailEdit.getText(), usernameEdit.getText(), subscriber.getTagId());
		String newPassword=passwordEdit.getText();
		boolean passwordChange= passwordStr.equals(newPassword);
		if(!Subscriber.equals(newDetails, subscriber)) {
			if (!passwordChange) {
				client.updateDetailsOfSubscriber(newDetails, new User(subscriber.getUsername(), newPassword));
			}
			else {
				client.updateDetailsOfSubscriber(newDetails, null);
			}
		}
		else if (!passwordChange) {
			client.updateDetailsOfSubscriber(null, new User(usernameEdit.getText(), newPassword));
		}	
	}
	
	public void handleGoToView() {
		  try {
		    	mainLayoutController=client.getMainLayoutController();
		    	mainLayoutController.loadScreen("/client/ViewSubscriberDetailsScreen.fxml");
		    	
		    } catch (Exception e) {
		        System.out.println("Error:"+ e.getMessage());
		    }
	}
}
