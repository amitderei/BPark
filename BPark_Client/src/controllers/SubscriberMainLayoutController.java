package controllers;

import java.io.IOException;

import client.ClientController;
import common.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import ui.UiUtils;

/**
 * Main container for every subscriber screen.  
 * Keeps the same top bar and side-menu while swapping the centre pane.
 */
public class SubscriberMainLayoutController implements ClientAware {

    /* ---------- top bar ---------- */
    @FXML private Button btnHome;
    @FXML private Button btnLogout;
    @FXML private Button btnExit;

    /* ---------- side-menu ---------- */
    @FXML private Button btnViewPersonalInfo;
    @FXML private Button btnViewParkingHistory;
    @FXML private Button btnViewActiveParkingInfo;
    @FXML private Button btnExtendParkingTime;
    @FXML private Button btnParkingReservation;
    @FXML private Button btnMyReservations;

    /* ---------- centre pane ---------- */
    @FXML private AnchorPane center;

    /* ---------- state ---------- */
    private ClientController client;     // shared socket handler
    private String subscriberName;       // used by home screen for greeting

    /* ======================================================
     *  simple setters
     * ====================================================== */

    /**
     * Injects the shared client.
     * @param client active client
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Stores the subscriber’s name so we can greet them on the home screen.
     * @param name subscriber first name
     */
    public void setSubscriberName(String name) {
        this.subscriberName = name;
    }

    /* ======================================================
     *  toolbar handlers
     * ====================================================== */

    /** Loads the subscriber home screen. */
    @FXML private void handleHomeClick() {
        loadScreen("/client/SubscriberMainScreen.fxml");
    }

    /** Logs out and returns to the entry screen. */
    @FXML private void handleLogoutClick() {
        UiUtils.loadScreen(btnLogout,
                "/client/MainScreen.fxml",
                "BPARK – Welcome",
                client);
    }

    /** Disconnects and quits. */
    @FXML private void handleExitClick() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(new Object[]{"disconnect"});
                client.closeConnection();
            }
        } catch (Exception ignored) { }
        javafx.application.Platform.exit();
        System.exit(0);
    }

    /* ======================================================
     *  side-menu handlers (one-liners)
     * ====================================================== */

    @FXML private void handleViewPersonalInfo()      { loadScreen("/client/ViewSubscriberDetailsScreen.fxml"); }
    @FXML private void handleViewParkingHistory()    { loadScreen("/client/ViewSubscriberHistoryScreen.fxml"); }
    @FXML private void handleViewActiveParkingInfo() { loadScreen("/client/ViewActiveParkingInfoScreen.fxml"); }
    @FXML private void handleExtendParkingTime()     { loadScreen("/client/ExtendParkingScreen.fxml"); }
    @FXML private void handleMyReservations()        { loadScreen("/client/WatchAndCancelOrdersScreen.fxml"); }

    /**
     * Opens the first step of the reservation wizard.
     * Connected to the button inside the reservation pane.
     */
    public void handleGoToCreateOrder() {
        loadScreen("/client/PlacingAnOrderView.fxml");
    }

    /* ======================================================
     *  screen loader helpers
     * ====================================================== */

    /**
     * Loads a child FXML into the centre pane
     * and wires its controller for server callbacks.
     *
     * @param fxml path to FXML inside resources
     */
    public void loadScreen(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = fxmlLoader.load();
            Object ctrl = fxmlLoader.getController();

            // welcome headline on the home screen
            if (ctrl instanceof SubscriberMainController c)
                c.setSubscriberName(subscriberName);

            // each child that needs the client gets it here
            if (ctrl instanceof ClientAware aware)
                aware.setClient(client);

            // extra initialisation for specific screens
            if (ctrl instanceof CreateNewOrderViewController c) {
                client.setNewOrderController(c);
                c.initializeCombo();
            }
            if (ctrl instanceof WatchAndCancelOrdersController c) {
                client.setWatchAndCancelOrdersController(c);
                c.defineTable();
            }
            if (ctrl instanceof ViewSubscriberDetailsController c) {
                client.setViewSubscriberDetailsController(c);
                c.setSubscriberAndPassword();
                c.setLabels();
            }
            if (ctrl instanceof EditSubscriberDetailsController c) {
                client.setEditSubscriberDetailsController(c);
                c.setSubscriberAndPassword();
                c.setTextOnField();
            }
            if (ctrl instanceof ViewParkingHistoryController c) {
                client.setViewParkingHistoryController(c);
                c.setTable();
            }
            if (ctrl instanceof ViewActiveParkingInfoController c) {
                client.setViewActiveParkingInfoController(c);
                c.getDetailsOfActiveInfo();
            }
            if (ctrl instanceof ExtendParkingController c) {
                client.setExtendParkingController(c);
            }

            center.getChildren().setAll(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a summary screen that needs the Order object.
     *
     * @param fxml  summary FXML
     * @param order reservation to display
     */
    public void loadScreen(String fxml, Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = loader.load();

            if (loader.getController() instanceof ParkingReservationSummaryController c) {
                client.setSummaryController(c);
                c.setLabels(order);
            }
            center.getChildren().setAll(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
