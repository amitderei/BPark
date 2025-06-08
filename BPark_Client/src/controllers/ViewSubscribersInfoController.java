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
 * Lists all subscribers with contact details and late-pickup count.
 */
public class ViewSubscribersInfoController implements ClientAware {

    /* ---------- FXML-injected controls ---------- */
    @FXML private TableView<Subscriber> subscriberTable;
    @FXML private TableColumn<Subscriber, Integer> colCode;
    @FXML private TableColumn<Subscriber, String>  colId;
    @FXML private TableColumn<Subscriber, String>  colName;
    @FXML private TableColumn<Subscriber, String>  colUsername;
    @FXML private TableColumn<Subscriber, String>  colPhone;
    @FXML private TableColumn<Subscriber, String>  colEmail;
    @FXML private TableColumn<Subscriber, Integer> colLate;

    /* ---------- Runtime fields ---------- */
    private ClientController client;
    private final ObservableList<Subscriber> data = FXCollections.observableArrayList();
    private Map<Subscriber,Integer> lateLookup = new HashMap<>();

    /* ---------- ClientAware implementation ---------- */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        client.setViewSubscribersInfoController(this);
        requestSubscribers();
    }

    /**
     * Called by ClientController once the server response arrives.
     * Receives a list of Subscriber objects and a map of late-counts.
     */
    public void onSubscribersReceived(List<Subscriber> subs,
                                      Map<Subscriber,Integer> lateMap) {
    	System.out.println("[DEBUG] onSubscribersReceived called. Total subs = " + subs.size());
    	System.out.println("[DEBUG] lateLookup map size = " + lateMap.size());

        Platform.runLater(() -> {
            this.lateLookup = lateMap;
            data.setAll(subs);
            subscriberTable.setItems(data);
        });
    }


    private void requestSubscribers() {
        System.out.println("[DEBUG] Sending requestAllSubscribers() to server...");
        client.requestAllSubscribers();
    }


    @FXML
    private void initialize() {
        // Column “Code” → subscriberCode getter
        colCode.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().getSubscriberCode()));

        // Column “ID” → userId getter
        colId.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getUserId()));

        // Column “Name” → firstName + " " + lastName
        colName.setCellValueFactory(cell -> {
            Subscriber s = cell.getValue();
            String fullName = s.getFirstName() + " " + s.getLastName();
            return new ReadOnlyStringWrapper(fullName);
        });

        // Column “Username” → username getter
        colUsername.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getUsername()));

        // Column “Phone” → phoneNum getter
        colPhone.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getPhoneNum()));

        // Column “Email” → email getter
        colEmail.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getEmail()));

        // Column “Late #” → lookup in lateLookup map
        colLate.setCellValueFactory(cell -> {
            int count = lateLookup.getOrDefault(cell.getValue(), 0);
            return new ReadOnlyObjectWrapper<>(count);
        });
    }
}