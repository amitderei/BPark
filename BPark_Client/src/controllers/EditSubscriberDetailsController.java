package controllers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import client.ClientController;
import common.Subscriber;
import common.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ui.UiUtils;

public class EditSubscriberDetailsController implements ClientAware{
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
	private SubscriberMainLayoutController mainLayoutController; 

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
			UiUtils.showAlert("Error", "There is empty field.", AlertType.ERROR);
			return;
		}
		if(!isValidEmail(emailEdit.getText().trim())) {
			UiUtils.showAlert("Error", "Email format is not valid.", AlertType.ERROR);
			return;
		}
		if(!isValidPhone(phoneNumberEdit.getText().trim())) {
			UiUtils.showAlert("Error", "Phone number format is not valid.", AlertType.ERROR);
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
		else {
			handleGoToView();
		}
	}
	
	/**
	 * check if the email is valid by regular expression
	 * @param email (the email we need to check)
	 * @return true if it belongs to language, else return false
	 */
	private static boolean isValidEmail(String email) {
		String regulerExpression="^[A-Za-z0-9_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		Pattern pattern=Pattern.compile(regulerExpression); //check the regular expression
		Matcher matcher= pattern.matcher(email); //check is email match to regular expression(if email belongs to the language of regularExpression 
		return matcher.matches(); 
	}
	
	/**
	 * check if the phone is valid by regular expression
	 * @param phone (the phone we need to check)
	 * @return true if it belongs to language, else return false
	 */
	private static boolean isValidPhone(String phone) {
		String regularExpression="^05[0-9]{8}$";
		Pattern pattern=Pattern.compile(regularExpression);
		Matcher matcher=pattern.matcher(phone);
		return matcher.matches();
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
