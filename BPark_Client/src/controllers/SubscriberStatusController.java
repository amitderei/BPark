package controllers;

import java.io.IOException;
import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;

import client.ClientController;
import common.SubscriberStatusRow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * UI controller for the "Subscriber Status" report screen.
 * Sends a request to the server and visualises the result.
 */
public class SubscriberStatusController implements ClientAware {

    /* ------------------------------------------------ FXML bindings */
    @FXML private ComboBox<Integer> cmbMonth;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private Button btnLoad;


    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    @FXML private TableView<SubscriberStatusRow> tblReport;
    @FXML private TableColumn<SubscriberStatusRow, Integer> colCode;
    @FXML private TableColumn<SubscriberStatusRow, String>  colName;
    @FXML private TableColumn<SubscriberStatusRow, Integer> colEntries;
    @FXML private TableColumn<SubscriberStatusRow, Integer> colExtends;
    @FXML private TableColumn<SubscriberStatusRow, Integer> colLates;
    @FXML private TableColumn<SubscriberStatusRow, Double> colHours;

    /* ------------------------------------------------ internal data */
    private final ObservableList<SubscriberStatusRow> rows =
            FXCollections.observableArrayList();

    private ClientController client;

    /* ---------- initialize (combo limits) ---------- */
    @FXML
    private void initialize() {
        int curMonth = java.time.LocalDate.now().getMonthValue();
        int curYear  = Year.now().getValue();

        /* year combo 2024..current */
        cmbYear.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(2024, curYear).boxed().toList()));

        /* adjust month list when year changes */
        cmbYear.getSelectionModel().selectedItemProperty().addListener((obs, oldY, newY) -> {
            int maxMonth = (newY == curYear) ? curMonth - 1 : 12;
            cmbMonth.setItems(FXCollections.observableArrayList(
                    IntStream.rangeClosed(1, maxMonth).boxed().toList()));
            if (!cmbMonth.getItems().isEmpty())
                cmbMonth.getSelectionModel().selectLast();
        });

        /* trigger initial fill */
        cmbYear.getSelectionModel().select(Integer.valueOf(curYear));

        /* table columns */
        colCode   .setCellValueFactory(new PropertyValueFactory<>("code"));
        colName   .setCellValueFactory(new PropertyValueFactory<>("name"));
        colEntries.setCellValueFactory(new PropertyValueFactory<>("totalEntries"));
        colExtends.setCellValueFactory(new PropertyValueFactory<>("totalExtends"));
        colLates .setCellValueFactory(new PropertyValueFactory<>("totalLates"));
        colHours .setCellValueFactory(new PropertyValueFactory<>("totalHours"));

        tblReport.setItems(rows);

        btnLoad.setOnAction(e -> sendRequest());
    }

    /* ------------------------------------------------ ClientAware */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setSubscriberStatusController(this);
        }
    }

    /* ---------- sendRequest ---------- */
    private void sendRequest() {
        Integer month = cmbMonth.getValue();
        Integer year  = cmbYear.getValue();
        if (month == null || year == null) return;      // nothing selected
        try {
            client.sendToServer(new Object[]{"get_subscriber_status", month, year});
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /* ------------------------------------------------ callback from ClientController */
    public void onReportReceived(List<SubscriberStatusRow> list) {
        Platform.runLater(() -> {
            rows.setAll(list);
            updateCharts();
        });
    }

    /* ------------------------------------------------ charts */
    private void updateCharts() {
        /* BarChart: top-10 by hours */
        var top = rows.stream()
                      .sorted((a, b) -> Double.compare(b.getTotalHours(), a.getTotalHours()))
                      .limit(10)
                      .toList();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        top.forEach(r -> series.getData()
                .add(new XYChart.Data<>(r.getName(), r.getTotalHours())));
        barChart.getData().setAll(series);
        barChart.setTitle("Top-10 by Hours");
        

        /* PieChart: Active vs Inactive */
        long active   = rows.stream().filter(r -> r.getTotalEntries() > 0).count();
        long inactive = rows.size() - active;
        pieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Active",   active),
                new PieChart.Data("Inactive", inactive)));
        pieChart.setTitle("Active vs Inactive");
    }
    
    
}
