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
 * Staff-side table that shows every subscriber together with
 * basic contact info and the number of late vehicle pickups.
 */
public class ViewSubscribersInfoController implements ClientAware {

    /* ---------- FXML controls ---------- */
    @FXML private TableView<Subscriber> subscriberTable;
    @FXML private TableColumn<Subscriber,Integer> colCode;
    @FXML private TableColumn<Subscriber,String>  colId;
    @FXML private TableColumn<Subscriber,String>  colName;
    @FXML private TableColumn<Subscriber,String>  colUsername;
    @FXML private TableColumn<Subscriber,String>  colPhone;
    @FXML private TableColumn<Subscriber,String>  colEmail;
    @FXML private TableColumn<Subscriber,Integer> colLate;

    /* ---------- runtime data ---------- */
    private ClientController client;
    private final ObservableList<Subscriber> data = FXCollections.observableArrayList();
    private Map<Subscriber,Integer> lateLookup = new HashMap<>();   // subscriber → late count


    /**
     * Saves the ClientController and lets it call back later.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null)
            client.setViewSubscribersInfoController(this);
    }

    /**
     * Triggered by the parent layout once setClient() is done.
     * Sends “get_all_subscribers” to the server.
     */
    public void requestSubscribers() {
        if (client != null)
            client.requestAllSubscribers();
    }

    // -----------------------------------------------------------------
    // server callback
    // -----------------------------------------------------------------

    /**
     * Fills the table after the server returns data.
     *
     * @param subs     list of Subscriber objects
     * @param lateMap  map subscriber → number of late pickups
     */
    public void onSubscribersReceived(List<Subscriber> subs,
                                      Map<Subscriber,Integer> lateMap) {
        Platform.runLater(() -> {
            lateLookup = lateMap;
            data.setAll(subs);
            subscriberTable.setItems(data);
        });
    }

    // -----------------------------------------------------------------
    // table column setup
    // -----------------------------------------------------------------

    /** Builds column value factories once the FXML is loaded. */
    @FXML
    private void initialize() {

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

        // late count looked up in a map populated by onSubscribersReceived()
        colLate.setCellValueFactory(c ->
            new ReadOnlyObjectWrapper<>(lateLookup.getOrDefault(c.getValue(), 0)));
    }
}

