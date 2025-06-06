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

public class WatchAndCancelOrdersController implements ClientAware{

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

	/**
	 * define the table and columns of tables, call for the function that bring the order to table,
	 * add button for cancel order only where has row. in event call for delete order from SQL
	 */
	public void defineTable() {
		orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
		timeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
		confirmationCodeColumn.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
		//update reservations on table
		client.askForReservations();
		
		cancelOrderColumn.setCellFactory(column->new TableCell<>(){
			//define button
			private Button deleteBtn= new Button("Cancel");
			//define action of button
			{
				deleteBtn.setOnAction(e->{
				Order orderToDelete=getTableView().getItems().get(getIndex());
				int orderNumberToDelete= orderToDelete.getOrderNumber();
				client.deleteOrder(orderNumberToDelete);
			});
			}
			
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty? null: deleteBtn);
			}
		});
	}

	/**
	 * display the orders on the table
	 * @param orders
	 */
	public void displayOrders(ArrayList<Order> orders) {
		ObservableList<Order> observableList = FXCollections.observableArrayList(orders);
		reservationTable.setItems(observableList);
	}

	/**
	 * set client on this screen
	 * @param client
	 */
	public void setClient(ClientController client) {
		this.client = client;
	}

}
