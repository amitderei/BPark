package controllers;

import java.util.regex.Pattern;

import client.ClientController;
import common.Subscriber;
import common.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import ui.UiUtils;

/**
 * Controller for editing a subscriber’s personal details.
 * 
 * Locked fields (cannot be edited): subscriber code, ID, username.
 * Editable fields: first name, last name, phone number, email, password.
 */
public class EditSubscriberDetailsController implements ClientAware {

	/**
	 * FXML UI elements for the subscriber details edit screen.
	 * Includes labels, text fields, and the save button.
	 */
	@FXML 
	private Label headline;
	@FXML 
	private Label subscriberCode;  
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

	/** Reference to the active client used for server communication */
	private ClientController client;

	/** Current subscriber object holding original (before editing) details */
	private Subscriber subscriber;
	
	/** Original password value of the subscriber */
	private String passwordStr;
	
	/** Reference to main layout controller, used for screen navigation */
	private SubscriberMainLayoutController mainLayoutController;
	
	/**
	 * Injects the shared ClientController instance.
	 *
	 * @param client active client controller instance
	 */
	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}

	/**
	 * Copies subscriber details into text fields and disables fields that cannot be edited.
	 */
	public void setTextOnField() {
		subscriberCodeEdit.setText(String.valueOf(subscriber.getSubscriberCode()));
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
	 * Fetches the latest subscriber details and password from ClientController.
	 */
	public void setSubscriberAndPassword() {
		this.subscriber = client.getSubscriber();
		this.passwordStr = client.getPassword();
	}

	/**
	 * Validates user inputs, detects changes compared to original data,
	 * and sends minimal updates to the server. Shows validation errors as alerts.
	 */
	public void saveChanges() {
		if (isAnyFieldBlank()) {
			UiUtils.showAlert("Error", "One or more fields are empty.", AlertType.ERROR);
			return;
		}

		if(!isValidName(firstNameEdit.getText().trim(), lastNameEdit.getText().trim())) {
			UiUtils.showAlert("Error", "First and last name must contain only letters.", AlertType.ERROR);
			return;
		}
		
		if(!isValidPassword(passwordEdit.getText().trim())) {
			UiUtils.showAlert("Error", "Password must be between 6 to 10 chars.", AlertType.ERROR);
			return;
		}
		
		if (!isValidEmail(emailEdit.getText().trim())) {
			UiUtils.showAlert("Error", "Email format is not valid.", AlertType.ERROR);
			return;
		}

		if (!isValidPhone(phoneNumberEdit.getText().trim())) {
			UiUtils.showAlert("Error", "Phone number format is not valid.", AlertType.ERROR);
			return;
		}

		Subscriber updated = new Subscriber(
				subscriber.getSubscriberCode(),
				subscriber.getUserId(),
				firstNameEdit.getText(),
				lastNameEdit.getText(),
				phoneNumberEdit.getText(),
				emailEdit.getText(),
				subscriber.getUsername(),
				subscriber.getTagId());

		String newPassword = passwordEdit.getText();
		boolean passwordChanged = !passwordStr.equals(newPassword);

		if (!Subscriber.equals(updated, subscriber)) {
			if (passwordChanged)
				client.getRequestSender().updateDetailsOfSubscriber(updated, new User(subscriber.getUsername(), newPassword));
			else
				client.getRequestSender().updateDetailsOfSubscriber(updated, null);
		} else if (passwordChanged) {
			client.getRequestSender().updateDetailsOfSubscriber(null, new User(usernameEdit.getText(), newPassword));
		} else {
			handleGoToView();
		}
	}

	
	/**
	 * Checks if both first name and last name contain only English letters.
	 *
	 * @return true if both names are valid, false otherwise
	 */
	private boolean isValidName(String firstName, String lastName) {
	    return firstName.matches("[a-zA-Z]+") && lastName.matches("[a-zA-Z]+");		
	}
	
	/**
	 * Checks if the password is between 6-10 chars
	 *
	 * @return true if the passwords length is 6-10, false otherwise
	 */
	private boolean isValidPassword(String password) {
	    return password.length() >= 6 && password.length() <= 10;		
	}
	
	/**
	 * Checks whether any editable text field is left blank.
	 *
	 * @return true if at least one editable field is blank
	 */
	private boolean isAnyFieldBlank() {
		return firstNameEdit.getText().trim().isEmpty()
				|| lastNameEdit.getText().trim().isEmpty()
				|| passwordEdit.getText().trim().isEmpty()
				|| emailEdit.getText().trim().isEmpty()
				|| phoneNumberEdit.getText().trim().isEmpty();
	}

	/**
	 * Validates the format of an email address.
	 *
	 * @param email the email to check
	 * @return true if valid, false otherwise
	 */
	private static boolean isValidEmail(String email) {
		String regex = "^[A-Za-z0-9_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		return Pattern.matches(regex, email);
	}

	/**
	 * Validates Israeli mobile number format (must start with '05' followed by 8 digits).
	 *
	 * @param phone the phone number to validate
	 * @return true if phone number format is valid
	 */
	private static boolean isValidPhone(String phone) {
		return phone.matches("^05[0-9]{8}$");
	}

	/**
	 * Navigates back to the read-only subscriber details view.
	 */
	public void handleGoToView() {
		try {
			mainLayoutController = client.getMainLayoutController();
			mainLayoutController.loadScreen("/client/ViewSubscriberDetailsScreen.fxml");
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}

