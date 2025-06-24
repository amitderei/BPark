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
 * <p>
 * Locked fields (cannot be edited): subscriber code, ID, username.<br>
 * Editable fields: first name, last name, phone number, email, password.
 */
public class EditSubscriberDetailsController implements ClientAware {
    /** Headline label at the top of the edit details screen */
    @FXML private Label headline;

    /** Caption label for subscriber code (locked) */
    @FXML private Label sunscriberCode;
    /** Input field for subscriber code (locked) */
    @FXML private TextField subscriberCodeEdit;

    /** Caption label for user ID (locked) */
    @FXML private Label id;
    /** Input field for user ID (locked) */
    @FXML private TextField idEdit;

    /** Caption label for first name */
    @FXML private Label firstName;
    /** Editable input field for first name */
    @FXML private TextField firstNameEdit;

    /** Caption label for last name */
    @FXML private Label lastName;
    /** Editable input field for last name */
    @FXML private TextField lastNameEdit;

    /** Caption label for username (locked) */
    @FXML private Label username;
    /** Input field for username (locked) */
    @FXML private TextField usernameEdit;

    /** Caption label for password */
    @FXML private Label password;
    /** Editable input field for password */
    @FXML private TextField passwordEdit;

    /** Caption label for email address */
    @FXML private Label email;
    /** Editable input field for email address */
    @FXML private TextField emailEdit;

    /** Caption label for phone number */
    @FXML private Label phoneNumber;
    /** Editable input field for phone number */
    @FXML private TextField phoneNumberEdit;

    /** Button that triggers saving of edited details */
    @FXML private Button saveChangesBtn;

    /** Reference to the central ClientController */
    private ClientController client;

    /** Current subscriber object holding original (pre-edit) details */
    private Subscriber subscriber;

    /** Original subscriber’s password */
    private String passwordStr;

    /** Parent layout controller to navigate back to the detail view */
    private SubscriberMainLayoutController mainLayoutController;

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
     * Injects the shared ClientController instance.
     *
     * @param client active client controller instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
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
                client.updateDetailsOfSubscriber(updated, new User(subscriber.getUsername(), newPassword));
            else
                client.updateDetailsOfSubscriber(updated, null);
        } else if (passwordChanged) {
            client.updateDetailsOfSubscriber(null, new User(usernameEdit.getText(), newPassword));
        } else {
            handleGoToView();
        }
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
     * Validates email address format using a regex.
     *
     * @param email the email address to validate
     * @return true if email format is valid
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

