package controllers;

import java.sql.Date;
import java.util.ArrayList;

import client.ClientController;
import common.ParkingReport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import ui.UiUtils;

/**
 * Controller for displaying a monthly parking report using two pie charts.
 * 
 * Shows statistics about parking extensions and late pickups. This screen is
 * only accessible to staff roles such as attendant or manager.
 */
public class ParkingReportController implements ClientAware {
	/** Headline label displayed above the charts */
	@FXML
	private Label headline;

	@FXML
	private Label month;

	@FXML
	private ComboBox<Date> monthCombo;

	@FXML
	private Button sumbit;

	/** Pie chart showing how many users extended their parking sessions */
	@FXML
	private PieChart parkingPieChart;

	/** Pie chart showing how many users were late to pick up their vehicles */
	@FXML
	private PieChart latesPieChart;

	@FXML
	private BarChart<String, Number> hoursParkingChart;

	/** Shared socket handler used to communicate with the server */
	private ClientController client;

	/** Holds the report data received from the server */
	private ParkingReport parkingReport;

	private ArrayList<Date> dates;

	public void setDates(ArrayList<Date> dates) {
		ObservableList<Date> observableDates = FXCollections.observableArrayList(dates);
		monthCombo.setItems(observableDates);
	}

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

	/**
	 * Populates the pie charts based on the data in the parking report.
	 * 
	 * Chart 1: Extended vs. non-extended parking sessions. Chart 2: Late pickups
	 * vs. on-time pickups.
	 */
	public void setChart() {
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName("Distribution of Parking Durations");
		series.getData().add(new XYChart.Data<>("0-4", parkingReport.getLessThanFour()));
		series.getData().add(new XYChart.Data<>("4-8", parkingReport.getBetweenFourToEight()));
		series.getData().add(new XYChart.Data<>("8+", parkingReport.getMoreThanEight()));

		ObservableList<PieChart.Data> extendsChartData = FXCollections.observableArrayList(
				new PieChart.Data("Extends parking time", parkingReport.getTotalExtends()), new PieChart.Data(
						"Not extends parking time", parkingReport.getTotalEntries() - parkingReport.getTotalExtends()));

		ObservableList<PieChart.Data> latesChartData = FXCollections.observableArrayList(
				new PieChart.Data("Late pickups", parkingReport.getTotalLates()),
				new PieChart.Data("On-time pickups", parkingReport.getTotalEntries() - parkingReport.getTotalLates()));

		hoursParkingChart.getData().add(series);
		CategoryAxis xAxis = (CategoryAxis) hoursParkingChart.getXAxis();
		xAxis.setCategories(FXCollections.observableArrayList("0-4", "4-8", "8+"));

		parkingPieChart.setData(extendsChartData);
		parkingPieChart.setTitle("Monthly Extends Pie");

		latesPieChart.setData(latesChartData);
		latesPieChart.setTitle("Monthly Lates Pie");
	}

	/**
	 * Sends a request to the server for the parking report of May 2025.
	 * 
	 * This date is currently hardcoded for demonstration purposes, but it can be
	 * made dynamic in future versions.
	 */
	public void getParkingReportFromServer() {
		if (monthCombo.getValue() == null) {
			UiUtils.showAlert("Error", "Choose a date.", AlertType.ERROR);
			return;
		}
		hoursParkingChart.getData().clear();
		Date dateStr = monthCombo.getValue();
		client.getParkingReport(dateStr);
	}

	public void getDatesOfReportsInDB() {
		client.getDatesOfReports();

	}
}
