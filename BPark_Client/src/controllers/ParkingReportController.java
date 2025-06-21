package controllers;

import java.sql.Date;

import client.ClientController;
import common.ParkingReport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

/**
 * Controller for displaying a monthly parking report using two pie charts.
 * 
 * Shows statistics about parking extensions and late pickups.
 * This screen is only accessible to staff roles such as attendant or manager.
 */
public class ParkingReportController implements ClientAware {

    // ==========================
    // FXML UI Components
    // ==========================

    /** Headline label displayed above the charts */
    @FXML private Label headline;

    /** Pie chart showing how many users extended their parking sessions */
    @FXML private PieChart parkingPieChart;

    /** Pie chart showing how many users were late to pick up their vehicles */
    @FXML private PieChart latesPieChart;

    // ==========================
    // Runtime state
    // ==========================

    /** Shared socket handler used to communicate with the server */
    private ClientController client;

    /** Holds the report data received from the server */
    private ParkingReport parkingReport;

    // ==========================
    // Dependency Injection
    // ==========================

    /**
     * Stores the shared ClientController instance for use in server requests.
     *
     * @param client active client instance
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Stores the ParkingReport object after receiving it from the server.
     *
     * @param parkingReport the report object containing statistical data
     */
    public void setParkingReport(ParkingReport parkingReport) {
        this.parkingReport = parkingReport;
    }

    // ==========================
    // Chart Logic
    // ==========================

    /**
     * Populates the pie charts based on the data in the parking report.
     * 
     * Chart 1: Extended vs. non-extended parking sessions.  
     * Chart 2: Late pickups vs. on-time pickups.
     */
    public void setChart() {
        ObservableList<PieChart.Data> extendsChartData = FXCollections.observableArrayList(
                new PieChart.Data("Extends parking time", parkingReport.getTotalExtends()),
                new PieChart.Data("Not extends parking time", parkingReport.getTotalEntries() - parkingReport.getTotalExtends())
        );

        ObservableList<PieChart.Data> latesChartData = FXCollections.observableArrayList(
                new PieChart.Data("Late pickups", parkingReport.getTotalLates()),
                new PieChart.Data("On-time pickups", parkingReport.getTotalEntries() - parkingReport.getTotalLates())
        );

        parkingPieChart.setData(extendsChartData);
        parkingPieChart.setTitle("Monthly Extends Pie");
        parkingPieChart.setLegendVisible(true);

        latesPieChart.setData(latesChartData);
        latesPieChart.setTitle("Monthly Lates Pie");
        latesPieChart.setLegendVisible(true);
    }

    /**
     * Sends a request to the server for the parking report of May 2025.
     * 
     * This date is currently hardcoded for demonstration purposes,
     * but it can be made dynamic in future versions.
     */
    public void getParkingReportFromServer() {
        String dateStr = "2025-05-01"; // hardcoded for demo purposes
        Date sqlDate = Date.valueOf(dateStr);
        client.getParkingReport(sqlDate);
    }
}
