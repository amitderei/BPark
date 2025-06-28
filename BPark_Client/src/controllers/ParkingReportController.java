package controllers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import client.ClientController;
import common.ParkingReport;
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
import javafx.scene.control.Label;
import ui.UiUtils;

/**
 * Controller for the parking report screen.
 *
 * Displays statistics related to user behavior using two pie charts and one bar chart:
 * - Number of users who extended their parking
 * - Number of users who arrived late
 * - Duration of parking sessions (0–4, 4–8, 8+ hours)
 *
 * This screen is only available to staff roles such as attendants and managers.
 */
public class ParkingReportController implements ClientAware {

	@FXML
	private Label headline;

	@FXML
	private ComboBox<Integer> yearCombo;

	@FXML
	private ComboBox<String> monthCombo;

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

	/**
	 * Populates the year and month combo boxes based on available report dates.
	 * 
	 * @param dates list of dates in the format yyyy-MM-01 received from server
	 */
	public void setDates(ArrayList<Date> dates) {
		Set<Integer> years = new LinkedHashSet<>();
		Set<Integer> months = new LinkedHashSet<>();

		for (Date d : dates) {
			LocalDate localDate = d.toLocalDate();
			years.add(localDate.getYear());
			months.add(localDate.getMonthValue());
		}

		yearCombo.setItems(FXCollections.observableArrayList(years));

		ObservableList<String> monthItems = FXCollections.observableArrayList();
		for (Integer m : months) {
			monthItems.add(String.format("%02d", m));
		}
		monthCombo.setItems(monthItems);
	}

	/**
	 * Injects the client controller used for server communication and screen transitions.
	 *
	 * @param client the active client instance
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
	 * Populates the charts using the data stored in the parking report.
	 * Generates:
	 * - Bar chart for parking durations
	 * - Pie chart for extensions
	 * - Pie chart for late pickups
	 */
	public void setChart() {
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName("Distribution of Parking Durations");
		series.getData().add(new XYChart.Data<>("0-4", parkingReport.getLessThanFour()));
		series.getData().add(new XYChart.Data<>("4-8", parkingReport.getBetweenFourToEight()));
		series.getData().add(new XYChart.Data<>("8+", parkingReport.getMoreThanEight()));

		hoursParkingChart.getData().add(series);
		CategoryAxis xAxis = (CategoryAxis) hoursParkingChart.getXAxis();
		xAxis.setCategories(FXCollections.observableArrayList("0-4", "4-8", "8+"));

		ObservableList<PieChart.Data> extendsChartData = FXCollections.observableArrayList(
			new PieChart.Data("Extends parking time", parkingReport.getTotalExtends()),
			new PieChart.Data("Not extends parking time", parkingReport.getTotalEntries() - parkingReport.getTotalExtends()));

		ObservableList<PieChart.Data> latesChartData = FXCollections.observableArrayList(
			new PieChart.Data("Late pickups", parkingReport.getTotalLates()),
			new PieChart.Data("On-time pickups", parkingReport.getTotalEntries() - parkingReport.getTotalLates()));

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
		if (yearCombo.getValue() == null || monthCombo.getValue() == null) {
			UiUtils.showAlert("Error", "Choose both year and month.", AlertType.ERROR);
			return;
		}

		int year = yearCombo.getValue();
		int month = Integer.parseInt(monthCombo.getValue());

		LocalDate date = LocalDate.of(year, month, 1);
		Date sqlDate = Date.valueOf(date);

		hoursParkingChart.getData().clear();
		client.getParkingReport(sqlDate);
	}

	/**
	 * Requests from the server the list of months that have parking reports stored.
	 */
	public void getDatesOfReportsInDB() {
		client.getDatesOfReports();
	}
}
