package controllers;

import java.sql.Date;

import client.ClientController;
import common.ParkingReport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

public class ParkingReportController implements ClientAware{

	@FXML
	private Label headline;
	
	@FXML
	private PieChart parkingPieChart;
	
	@FXML
	private PieChart latesPieChart;
		
	private ClientController client;
	private ParkingReport parkingReport;

	public void setClient(ClientController client) {
		this.client = client;
	}

	public void setParkingReport(ParkingReport parkingReport) {
		this.parkingReport = parkingReport;
	}
	
	public void setChart() {
		ObservableList<PieChart.Data> extendsChartData = FXCollections.observableArrayList(
		        new PieChart.Data("Extends parking time", parkingReport.getTotalExtends()),
		        new PieChart.Data("Not extends parking time", parkingReport.getTotalEntries()-parkingReport.getTotalExtends())
		        );
		ObservableList<PieChart.Data> latesChartData = FXCollections.observableArrayList(
		        new PieChart.Data("Extends parking time", parkingReport.getTotalLates()),
		        new PieChart.Data("Not extends parking time", parkingReport.getTotalEntries()-parkingReport.getTotalLates())
		        );
		parkingPieChart.setData(extendsChartData);
		parkingPieChart.setTitle("Monthly Extends Pie");
		latesPieChart.setData(latesChartData);
		latesPieChart.setTitle("Monthly Lates Pie");
		parkingPieChart.setLegendVisible(true);
		latesPieChart.setLegendVisible(true);
	}
	
	public void getParkingReportFromServer() {
		String dateStr = "2025-05-01";
		Date sqlDate = Date.valueOf(dateStr);
		client.getParkingReport(sqlDate);
	}
	
}
