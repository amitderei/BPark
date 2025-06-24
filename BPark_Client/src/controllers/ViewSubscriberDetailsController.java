package controllers;

import client.ClientController;
import common.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for the screen that displays the subscriber's personal details.
 * All fields are shown in read-only mode and populated using the
 * ClientController, which holds the logged-in subscriber's data.
 * The screen also provides a button that lets the user navigate to the
 * "Edit Details" screen.
 */
public class ViewSubscriberDetailsController implements ClientAware {
    /** Headline label ("Your Personal Details") */
    @FXML private Label headline;

    /** Caption: "Subscriber Code" */          @FXML private Label sunscriberCode;
    /** Value: subscriberCode from DB */       @FXML private Label subscriberCodeDetail;

    /** Caption: "ID" */                        @FXML private Label id;
    /** Value: Israeli national ID */           @FXML private Label idDetail;

    /** Caption: "First Name" */                @FXML private Label firstName;
    /** Value: subscriber’s first name */       @FXML private Label firstNameDetail;

    /** Caption: "Last Name" */                 @FXML private Label lastName;
    /** Value: subscriber’s last name */        @FXML private Label lastNameDetail;

    /** Caption: "Username" */                  @FXML private Label username;
    /** Value: username used for login */       @FXML private Label usernameDetail;

    /** Caption: "Password" */                  @FXML private Label password;
    /** Value: password retrieved from client */@FXML private Label passwordDetail;

    /** Caption: "Email" */                     @FXML private Label email;
    /** Value: subscriber’s email address */    @FXML private Label emailDetail;

    /** Caption: "Phone Number" */              @FXML private Label phoneNumber;
    /** Value: mobile phone number */           @FXML private Label phoneNumberDetail;

    /** Button to switch to the edit screen */
    @FXML private Button editBtn;

    /** Shared client used to access subscriber and screen switching */
    private ClientController client;

    /** Logged-in subscriber */
    private Subscriber subscriber;

    /** Password for display only (read from client) */
    private String passwordStr;

    /** Layout controller used to switch to the edit screen */
    private SubscriberMainLayoutController mainLayoutController;

    /**
     * Injects the central ClientController used for retrieving
     * subscriber data and navigating to other screens.
     *
     * @param client the connected controller instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Loads the Subscriber object and password string from the client.
     * This method must be called before calling #setLabels().
     */
    public void setSubscriberAndPassword() {
        this.subscriber  = client.getSubscriber();
        this.passwordStr = client.getPassword();
    }

    /**
     * Updates the screen labels with the subscriber's current details.
     * Assumes #setSubscriberAndPassword() was called beforehand.
     */
    public void setLabels() {
        subscriberCodeDetail.setText(String.valueOf(subscriber.getSubscriberCode()));
        idDetail            .setText(subscriber.getUserId());
        firstNameDetail     .setText(subscriber.getFirstName());
        lastNameDetail      .setText(subscriber.getLastName());
        usernameDetail      .setText(subscriber.getUsername());
        passwordDetail      .setText(passwordStr);
        emailDetail         .setText(subscriber.getEmail());
        phoneNumberDetail   .setText(subscriber.getPhoneNum());
    }

    /**
     * Triggered when the user clicks the "Edit Details" button.
     * Navigates to the EditSubscriberDetails screen via the
     * SubscriberMainLayoutController.
     */
    public void handleGoToEdit() {
        try {
            mainLayoutController = client.getMainLayoutController();
            mainLayoutController.loadScreen("/client/EditSubscriberDetailsScreen.fxml");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
