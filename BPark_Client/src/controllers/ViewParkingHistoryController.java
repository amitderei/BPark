package controllers;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
	private TableColumn<ParkingEvent, LocalDate> entryDateColumn;
	
	@FXML
	private TableColumn<ParkingEvent, LocalTime> entryHourColumn;
	
	@FXML
	private TableColumn<ParkingEvent, LocalDate> exitDateColumn;
	
	@FXML
	private TableColumn<ParkingEvent, LocalTime> exitHourColumn;
	
	@FXML
	private TableColumn<ParkingEvent, String> status;
	
	private ClientController client;
	

	
	public void setTable() {
		eventNumberColumn.setCellValueFactory(new PropertyValueFactory<>("eventId"));
		entryDateColumn.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
		entryHourColumn.setCellValueFactory(new PropertyValueFactory<>("entryHour"));
		entryHourColumn.setCellFactory(column->new TableCell<ParkingEvent, LocalTime>(){
			@Override
			protected void updateItem(LocalTime time, boolean empty) {
				super.updateItem(time, empty);
				if(!empty && !(time==null)) {
					setText(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
				}
			}
		});
		exitDateColumn.setCellValueFactory(new PropertyValueFactory<>("exitDate"));
		exitDateColumn.setCellFactory(column->new TableCell<ParkingEvent, LocalDate>(){
			@Override
			protected void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				if(empty) {
					setText(null);
				} else if(date==null) {
					setText("Active");
				}else {
					setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
				}
			}
		});
		exitHourColumn.setCellValueFactory(new PropertyValueFactory<>("exitHour"));
		exitHourColumn.setCellFactory(column->new TableCell<ParkingEvent, LocalTime>(){
			@Override
			protected void updateItem(LocalTime time, boolean empty) {
				super.updateItem(time, empty);
				if(empty) {
					setText(null);
				} else if(time==null) {
					setText("Active");
				}else {
					setText(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
				}
			}
		});
		status.setCellValueFactory(new PropertyValueFactory<>("status"));
		client.updateParkingHistoryOfSubscriber();
	}
	
	/**
	 * display the orders on the table
	 * @param orders
	 */
	public void displayHistory(ArrayList<ParkingEvent> parkingEventsHistory) {
		ObservableList<ParkingEvent> observableList = FXCollections.observableArrayList(parkingEventsHistory);
		parkingHistoryTable.setItems(observableList);
	}
	
	public void setClient (ClientController client) {
		this.client=client;
	}
}
