package controllers;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import client.ClientController;
import common.Order;
import common.ParkingEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ViewParkingHistoryController {
	@FXML
	private Label headline;
	
	@FXML
	private TableView<ParkingEvent> parkingHistoryTable;
	
	@FXML
	private TableColumn<ParkingEvent, Integer> eventNumberColumn;
	
	@FXML
	private TableColumn<ParkingEvent, Date> entryDateColumn;
	
	@FXML
	private TableColumn<ParkingEvent, Time> entryHourColumn;
	
	@FXML
	private TableColumn<ParkingEvent, Date> exitDateColumn;
	
	@FXML
	private TableColumn<ParkingEvent, Time> exitHourColumn;
	
	private ClientController client;
	
	public void setTable() {
		eventNumberColumn.setCellValueFactory(new PropertyValueFactory<>("eventId"));
		entryDateColumn.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
		entryHourColumn.setCellValueFactory(new PropertyValueFactory<>("entryHour"));
		exitDateColumn.setCellValueFactory(new PropertyValueFactory<>("exitDate"));
		exitHourColumn.setCellValueFactory(new PropertyValueFactory<>("exitHour"));
		client.updateParkingHistoryOfSubscriber();
	}
	
	/**
	 * display the orders on the table
	 * @param orders
	 */
	public void displayOrders(ArrayList<ParkingEvent> parkingEventsHistory) {
		System.out.println("display");
		ObservableList<ParkingEvent> observableList = FXCollections.observableArrayList(parkingEventsHistory);
		parkingHistoryTable.setItems(observableList);
	}
	
	public void setClient (ClientController client) {
		this.client=client;
	}
}
