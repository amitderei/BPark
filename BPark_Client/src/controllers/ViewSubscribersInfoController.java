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

    /**
     * Injects the ClientController instance.
     * Note: actual DB call is deferred until explicitly requested.
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setViewSubscribersInfoController(this);
        }
    }

    /**
     * Called by StaffMainLayoutController AFTER setClient, to initiate request.
     */
    public void requestSubscribers() {
        if (client != null) {
            System.out.println("[DEBUG] Sending requestAllSubscribers() to server...");
            client.requestAllSubscribers();
        }
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

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().getSubscriberCode()));

        colId.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getUserId()));

        colName.setCellValueFactory(cell -> {
            Subscriber s = cell.getValue();
            String fullName = s.getFirstName() + " " + s.getLastName();
            return new ReadOnlyStringWrapper(fullName);
        });

        colUsername.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getUsername()));

        colPhone.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getPhoneNum()));

        colEmail.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getEmail()));

        colLate.setCellValueFactory(cell -> {
            int count = lateLookup.getOrDefault(cell.getValue(), 0);
            return new ReadOnlyObjectWrapper<>(count);
        });
    }
}
