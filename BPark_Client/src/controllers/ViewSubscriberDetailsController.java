package controllers;

import client.ClientController;
import common.Subscriber;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Read-only view of the subscriber’s personal details.  
 * Values are pulled from ClientController (which already
 * stores the logged-in Subscriber and the password).
 */
public class ViewSubscriberDetailsController implements ClientAware {

    /* ---------- headline ---------- */
    @FXML private Label headline;

    /* ---------- left column captions + right column values ---------- */
    @FXML private Label sunscriberCode;        @FXML private Label subscriberCodeDetail;
    @FXML private Label id;                    @FXML private Label idDetail;
    @FXML private Label firstName;             @FXML private Label firstNameDetail;
    @FXML private Label lastName;              @FXML private Label lastNameDetail;
    @FXML private Label username;              @FXML private Label usernameDetail;
    @FXML private Label password;              @FXML private Label passwordDetail;
    @FXML private Label email;                 @FXML private Label emailDetail;
    @FXML private Label phoneNumber;           @FXML private Label phoneNumberDetail;

    /* ---------- edit button ---------- */
    @FXML private Button editBtn;

    /* ---------- runtime ---------- */
    private ClientController client;
    private Subscriber subscriber;
    private String passwordStr;
    private SubscriberMainLayoutController mainLayoutController;

    /* =====================================================
     *  data wiring
     * ===================================================== */

    /**
     * Saves client ref so we can reach the cached subscriber
     * and later switch screens.
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Pulls Subscriber object and password from ClientController.
     * Call once before setLabels().
     */
    public void setSubscriberAndPassword() {
        this.subscriber   = client.getSubscriber();
        this.passwordStr  = client.getPassword();
    }

    /**
     * Fills all value labels with the subscriber’s details.
     * Assumes setSubscriberAndPassword() was called first.
     */
    public void setLabels() {
        subscriberCodeDetail.setText(String.valueOf(subscriber.getSubscriberCode()));
        idDetail       .setText(subscriber.getUserId());
        firstNameDetail.setText(subscriber.getFirstName());
        lastNameDetail .setText(subscriber.getLastName());
        usernameDetail .setText(subscriber.getUsername());
        passwordDetail .setText(passwordStr);
        emailDetail    .setText(subscriber.getEmail());
        phoneNumberDetail.setText(subscriber.getPhoneNum());
    }

    /* =====================================================
     *  navigation
     * ===================================================== */

    /** Loads the “Edit Details” screen via the parent layout. */
    public void handleGoToEdit() {
        try {
            mainLayoutController = client.getMainLayoutController();
            mainLayoutController.loadScreen("/client/EditSubscriberDetailsScreen.fxml");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
