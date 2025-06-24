package controllers;

import client.ClientController;
import common.Subscriber;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the staff screen that displays a full list of subscribers.
 * Each subscriber row includes contact info and the number of times
 * they were late in picking up their vehicle.
 */
public class ViewSubscribersInfoController implements ClientAware {
    /** Table showing all subscribers */
    @FXML private TableView<Subscriber> subscriberTable;

    /** Column: subscriber code (unique ID in system) */
    @FXML private TableColumn<Subscriber, Integer> colCode;

    /** Column: Israeli national ID */
    @FXML private TableColumn<Subscriber, String> colId;

    /** Column: full name (first + last) */
    @FXML private TableColumn<Subscriber, String> colName;

    /** Column: username used for login */
    @FXML private TableColumn<Subscriber, String> colUsername;

    /** Column: mobile phone number */
    @FXML private TableColumn<Subscriber, String> colPhone;

    /** Column: email address */
    @FXML private TableColumn<Subscriber, String> colEmail;

    /** Column: number of late vehicle pickups */
    @FXML private TableColumn<Subscriber, Integer> colLate;

    /** Main client instance used for communication with the server */
    private ClientController client;

    /** Observable list of subscriber rows */
    private final ObservableList<Subscriber> data = FXCollections.observableArrayList();

    /** Lookup table that holds how many times each subscriber was late */
    private Map<Subscriber, Integer> lateLookup = new HashMap<>();

    /**
     * Injects the client and registers this controller in it for callbacks.
     *
     * @param client the connected ClientController
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null)
            client.setViewSubscribersInfoController(this);
    }

    /**
     * Sends a request to the server to get the full list of subscribers.
     * This is triggered externally by the parent layout after setClient().
     */
    public void requestSubscribers() {
        if (client != null)
            client.requestAllSubscribers();
    }

    /**
     * Called by the client when the server returns the subscriber data.
     * Updates the UI table and stores the late-count lookup map.
     *
     * @param subs    list of Subscriber objects
     * @param lateMap map of Subscriber → number of late pickups
     */
    public void onSubscribersReceived(List<Subscriber> subs,
                                      Map<Subscriber, Integer> lateMap) {
        Platform.runLater(() -> {
            lateLookup = lateMap;
            data.setAll(subs); // replace current list
            subscriberTable.setItems(data);
        });
    }

    /**
     * Initializes the column mappings after FXML is loaded.
     * This method is automatically called by JavaFX.
     */
    @FXML
    private void initialize() {
        // Bind each column to the relevant field in Subscriber

        colCode.setCellValueFactory(c ->
            new ReadOnlyObjectWrapper<>(c.getValue().getSubscriberCode()));

        colId.setCellValueFactory(c ->
            new ReadOnlyStringWrapper(c.getValue().getUserId()));

        colName.setCellValueFactory(c -> {
            Subscriber s = c.getValue();
            return new ReadOnlyStringWrapper(s.getFirstName() + " " + s.getLastName());
        });

        colUsername.setCellValueFactory(c ->
            new ReadOnlyStringWrapper(c.getValue().getUsername()));

        colPhone.setCellValueFactory(c ->
            new ReadOnlyStringWrapper(c.getValue().getPhoneNum()));

        colEmail.setCellValueFactory(c ->
            new ReadOnlyStringWrapper(c.getValue().getEmail()));

        // Number of times the subscriber was late (from lookup map)
        colLate.setCellValueFactory(c ->
            new ReadOnlyObjectWrapper<>(lateLookup.getOrDefault(c.getValue(), 0)));
    }
}
