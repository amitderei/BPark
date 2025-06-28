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
 * Controller for displaying a monthly parking report using pie and bar charts.
 * 
 * This version uses two ComboBoxes (year and month) instead of one date picker.
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

	@FXML
	private PieChart parkingPieChart;

	@FXML
	private PieChart latesPieChart;

	@FXML
	private BarChart<String, Number> hoursParkingChart;

	private ClientController client;

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
			monthItems.add(String.format("%02d", m)); // format as "01", "02", ...
		}
		monthCombo.setItems(monthItems);
	}

	@Override
	public void setClient(ClientController client) {
		this.client = client;
	}

	public void setParkingReport(ParkingReport parkingReport) {
		this.parkingReport = parkingReport;
	}

	/**
	 * Populates all charts based on the current parking report data.
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
	 * Handles the "Load Report" button press.
	 * Builds the selected date from year and month combo boxes and sends request.
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
	 * Requests list of existing report dates from the server.
	 */
	public void getDatesOfReportsInDB() {
		client.getDatesOfReports();
	}
}
