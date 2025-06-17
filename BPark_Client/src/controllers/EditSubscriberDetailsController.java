package controllers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import client.ClientController;
import common.Subscriber;
import common.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import ui.UiUtils;

/**
 * Lets a subscriber edit his personal details.  
 * Locked fields: subscriber-code, user-ID and username  
 * Editable fields: first / last name, phone, email, password
 */
public class EditSubscriberDetailsController implements ClientAware {

    /* ---------- captions + input fields ---------- */
    @FXML private Label headline;

    @FXML private Label sunscriberCode;      @FXML private TextField subscriberCodeEdit;
    @FXML private Label id;                 @FXML private TextField idEdit;
    @FXML private Label firstName;          @FXML private TextField firstNameEdit;
    @FXML private Label lastName;           @FXML private TextField lastNameEdit;
    @FXML private Label username;           @FXML private TextField usernameEdit;
    @FXML private Label password;           @FXML private TextField passwordEdit;
    @FXML private Label email;              @FXML private TextField emailEdit;
    @FXML private Label phoneNumber;        @FXML private TextField phoneNumberEdit;

    @FXML private Button saveChangesBtn;

    /* ---------- runtime ---------- */
    private ClientController client;
    private Subscriber       subscriber;   // original data
    private String           passwordStr;  // original password
    private SubscriberMainLayoutController mainLayoutController;

    // ------------------------------------------------------------------
    // initial fill
    // ------------------------------------------------------------------

    /** Copies data into the text-fields and locks non-editable ones. */
    public void setTextOnField() {
        subscriberCodeEdit.setText(String.valueOf(subscriber.getSubscriberCode()));
        idEdit           .setText(subscriber.getUserId());
        firstNameEdit    .setText(subscriber.getFirstName());
        lastNameEdit     .setText(subscriber.getLastName());
        usernameEdit     .setText(subscriber.getUsername());
        passwordEdit     .setText(passwordStr);
        emailEdit        .setText(subscriber.getEmail());
        phoneNumberEdit  .setText(subscriber.getPhoneNum());

        subscriberCodeEdit.setDisable(true);
        usernameEdit     .setDisable(true);
        idEdit           .setDisable(true);
    }

    /** Pulls the latest Subscriber + password from ClientController. */
    public void setSubscriberAndPassword() {
        this.subscriber   = client.getSubscriber();
        this.passwordStr  = client.getPassword();
    }

    // ------------------------------------------------------------------
    // DI
    // ------------------------------------------------------------------

    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    // ------------------------------------------------------------------
    // save button logic
    // ------------------------------------------------------------------

    /**
     * Validates input, compares with old data and sends minimal update to server.
     * Empty field or invalid format -> popup + abort.
     */
    public void saveChanges() {

        // guard – no blanks
        if (isAnyFieldBlank()) {
            UiUtils.showAlert("Error", "One or more fields are empty.", AlertType.ERROR);
            return;
        }
        // guard – email format
        if (!isValidEmail(emailEdit.getText().trim())) {
            UiUtils.showAlert("Error", "Email format is not valid.", AlertType.ERROR);
            return;
        }
        // guard – phone format
        if (!isValidPhone(phoneNumberEdit.getText().trim())) {
            UiUtils.showAlert("Error", "Phone number format is not valid.", AlertType.ERROR);
            return;
        }

        /* build a fresh Subscriber with new values */
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

        // decide which parts actually changed
        if (!Subscriber.equals(updated, subscriber)) {              // personal details changed
            if (passwordChanged)
                client.updateDetailsOfSubscriber(updated, new User(subscriber.getUsername(), newPassword));
            else
                client.updateDetailsOfSubscriber(updated, null);
        } else if (passwordChanged) {                               // only password changed
            client.updateDetailsOfSubscriber(null, new User(usernameEdit.getText(), newPassword));
        } else {                                                    // nothing changed
            handleGoToView();
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** @return true if any editable text-field is blank */
    private boolean isAnyFieldBlank() {
        return firstNameEdit.getText().trim().isEmpty() ||
               lastNameEdit .getText().trim().isEmpty() ||
               passwordEdit .getText().trim().isEmpty() ||
               emailEdit    .getText().trim().isEmpty() ||
               phoneNumberEdit.getText().trim().isEmpty();
    }

    /** Simple regex for email validation. */
    private static boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(regex, email);
    }

    /** Accepts Israeli mobile numbers starting with 05 + 8 digits. */
    private static boolean isValidPhone(String phone) {
        return phone.matches("^05[0-9]{8}$");
    }

    /** Returns to the read-only "View Details" screen. */
    public void handleGoToView() {
        try {
            mainLayoutController = client.getMainLayoutController();
            mainLayoutController.loadScreen("/client/ViewSubscriberDetailsScreen.fxml");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

