package controllers;

import java.io.IOException;
import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;

import client.ClientController;
import common.SubscriberStatusReport;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.UiUtils;

/**
 * This controller handles the Subscriber Status report screen.
 * It lets the user pick a month and year, sends a request to the server,
 * and shows the data in a table, a bar chart, and a pie chart.
 */
public class SubscriberStatusController implements ClientAware {
    /** Combo box to select the month */
    @FXML 
    private ComboBox<Integer> cmbMonth;

    /** Combo box to select the year */
    @FXML 
    private ComboBox<Integer> cmbYear;

    /** Button to load the report */
    @FXML 
    private Button btnLoad;

    /** Bar chart to show top subscribers by total hours */
    @FXML 
    private BarChart<String, Number> barChart;

    /** Pie chart to show active vs inactive subscribers */
    @FXML 
    private PieChart pieChart;

    /** Table that displays all subscriber data */
    @FXML 
    private TableView<SubscriberStatusReport> tblReport;

    /** Table column: subscriber code */
    @FXML 
    private TableColumn<SubscriberStatusReport, Integer> colCode;

    /** Table column: subscriber name */
    @FXML 
    private TableColumn<SubscriberStatusReport, String> colName;

    /** Table column: number of entries */
    @FXML 
    private TableColumn<SubscriberStatusReport, Integer> colEntries;

    /** Table column: number of parking extensions */
    @FXML 
    private TableColumn<SubscriberStatusReport, Integer> colExtends;

    /** Table column: number of late exits */
    @FXML 
    private TableColumn<SubscriberStatusReport, Integer> colLates;

    /** Table column: total hours parked */
    @FXML 
    private TableColumn<SubscriberStatusReport, Double> colHours;

    /** The list that holds the report rows to show in the table */
    private final ObservableList<SubscriberStatusReport> rows = FXCollections.observableArrayList();

    /** Reference to the active client (used to send requests) */
    private ClientController client;

    /**
     * Called automatically when the screen is loaded.
     * Sets up the combo boxes, table columns, and button listener.
     */
    @FXML
    private void initialize() {
        int curMonth = java.time.LocalDate.now().getMonthValue();
        int curYear  = Year.now().getValue();

        // Fill the year combo with 2025 up to current year
        cmbYear.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(2025, curYear).boxed().toList()));

        // When the user picks a year, limit the months (if it's the current year)
        cmbYear.getSelectionModel().selectedItemProperty().addListener((obs, oldY, newY) -> {
            int maxMonth = (newY == curYear) ? curMonth - 1 : 12;
            cmbMonth.setItems(FXCollections.observableArrayList(
                    IntStream.rangeClosed(1, maxMonth).boxed().toList()));
            if (!cmbMonth.getItems().isEmpty())
                cmbMonth.getSelectionModel().selectLast(); // select latest month
        });

        cmbYear.getSelectionModel().clearSelection();

        // Link table columns to SubscriberStatusRow fields
        colCode   .setCellValueFactory(new PropertyValueFactory<>("code"));
        colName   .setCellValueFactory(new PropertyValueFactory<>("name"));
        colEntries.setCellValueFactory(new PropertyValueFactory<>("totalEntries"));
        colExtends.setCellValueFactory(new PropertyValueFactory<>("totalExtends"));
        colLates .setCellValueFactory(new PropertyValueFactory<>("totalLates"));
        colHours .setCellValueFactory(new PropertyValueFactory<>("totalHours"));

        // Bind data to table
        tblReport.setItems(rows);

        // When the button is clicked, send request
        btnLoad.setOnAction(e -> sendRequest());
    }

    /**
     * This method is called by the client to give this controller access.
     * It also registers this controller inside the client for future use.
     *
     * @param client the active ClientController
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setSubscriberStatusController(this);
        }
    }

    /**
     * Sends a request to the server to load the report
     * for the selected month and year.
     */
    public void sendRequest() {
        Integer month = cmbMonth.getValue();
        Integer year  = cmbYear.getValue();

        if (month == null || year == null) {
            UiUtils.showAlert("Missing Selection", "Please select both month and year before loading the report.", AlertType.WARNING);

            return;
        }

        System.out.println("[DEBUG] Sending request for " + month + "/" + year); 
        client.getSubscriberReport(month, year);
    }



    /**
     * Called by the client when the report data arrives from the server.
     * Updates the table and both charts with the new data.
     *
     * @param list the list of rows received from the server
     */
    public void onReportReceived(List<SubscriberStatusReport> list) {
        Platform.runLater(() -> {
            System.out.println("[DEBUG] Received report with " + list.size() + " rows");   
            rows.setAll(list);
            updateCharts();
        });
    }


    /**
     * Refreshes both the bar chart and the pie chart
     * using the current data inside `rows`.
     */
    private void updateCharts() {
        // BarChart: top 10 subscribers with most hours
        var top = rows.stream()
                      .sorted((a, b) -> Double.compare(b.getTotalHours(), a.getTotalHours()))
                      .limit(10)
                      .toList();
        
        System.out.println("[DEBUG] Top-10 list size: " + top.size());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        top.forEach(r -> series.getData()
                .add(new XYChart.Data<>(r.getName(), r.getTotalHours())));
        barChart.getData().setAll(series);
        ((CategoryAxis) barChart.getXAxis()).setTickLabelRotation(45);
        barChart.setTitle("Top-10 by Hours");

        // PieChart: number of active vs inactive subscribers
        long active   = rows.stream().filter(r -> r.getTotalEntries() > 0).count();
        long inactive = rows.size() - active;
        
        System.out.println("[DEBUG] Pie chart — Active: " + active + ", Inactive: " + inactive);
        
        pieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Active",   active),
                new PieChart.Data("Inactive", inactive)));
        pieChart.setTitle("Active vs Inactive");
        
        
    }
}

