package controllers;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import client.ClientController;
import common.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class WatchAndCancelOrdersController {

	@FXML
	private Label headline;

	@FXML
	private TableView<Order> reservationTable;

	@FXML
	private TableColumn<Order, Integer> orderNumberColumn;

	@FXML
	private TableColumn<Order, Date> dateColumn;

	@FXML
	private TableColumn<Order, Time> timeColumn;

	@FXML
	private TableColumn<Order, String> confirmationCodeColumn;

	@FXML
	private TableColumn<Order, Void> cancelOrderColumn;

	private ClientController client;

	
	public void defineTable() {
		System.out.println("here10");
		orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
		timeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
		confirmationCodeColumn.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));

		client.askForReservations();
	}

	public void displayOrders(ArrayList<Order> orders) {
		System.out.println("here");
		ObservableList<Order> observableList = FXCollections.observableArrayList(orders);
		reservationTable.setItems(observableList);
	}

	public void setClient(ClientController client) {
		this.client = client;
	}
	
	

}
