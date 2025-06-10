package controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import client.ClientController;
import common.ParkingEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ViewActiveParkingInfoController implements ClientAware{
	@FXML
	private Label headline;
	
	@FXML
	private Label eventId;
	
	@FXML
	private Label eventIdInfo;
	
	@FXML
	private Label vehicleId;
	
	@FXML
	private Label vehicleIdInfo;
	
	@FXML
	private Label entryDate;
	
	@FXML
	private Label entryDateInfo;
	
	@FXML
	private Label entryHour;
	
	@FXML
	private Label entryHourInfo;
	
	@FXML
	private Label extended;
	
	@FXML
	private Label extendedInfo;
	
	@FXML
	private Label expectedExitDate;
	
	@FXML
	private Label expectedExitDateInfo;
	
	@FXML
	private Label expectedExitHour;
	
	@FXML
	private Label expectedExitHourInfo;
	
	@FXML
	private Label parkingSpace;
	
	@FXML
	private Label parkingSpaceInfo;
	
	@FXML
	private Label parkingCode;
	
	@FXML
	private Label parkingCodeInfo;
	
	private ClientController client;
	
	private ParkingEvent parkingEvent;

	@Override
	public void setClient(ClientController client) {
		this.client=client;
	}
	
	public void setParkingEvent(ParkingEvent parkingEvent) {
		this.parkingEvent=parkingEvent;
	}
	
	public void getDetailsOfActiveInfo() {
		client.getDetailsOfActiveInfo();
	}
	
	public void setTexts() {
		if(!(parkingEvent==null)) {
			eventIdInfo.setText(((Integer)(parkingEvent.getEventId())).toString());
			vehicleIdInfo.setText(parkingEvent.getVehicleId());
			entryDateInfo.setText(parkingEvent.getEntryDate().toString());
			entryHourInfo.setText(parkingEvent.getEntryHour().format(DateTimeFormatter.ofPattern("HH:mm")));
			LocalDateTime dateTimeEntry=parkingEvent.getEntryDate().atTime(parkingEvent.getEntryHour());
			if (parkingEvent.isWasExtended()) {
				extendedInfo.setText("Yes");
				dateTimeEntry=dateTimeEntry.plusHours(8);
				
			}
			else {
				extendedInfo.setText("No");
				dateTimeEntry=dateTimeEntry.plusHours(4);
			}
			expectedExitDateInfo.setText(dateTimeEntry.toLocalDate().toString());
			expectedExitHourInfo.setText(dateTimeEntry.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
			parkingSpaceInfo.setText(((Integer)parkingEvent.getParkingSpace()).toString());
			parkingCodeInfo.setText(parkingEvent.getParkingCode());
		}
	}
	
}
