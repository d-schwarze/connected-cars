package de.connectedcars.backend.messaging.data;

/**
 * Data is used to register a new {@linkplain de.connectedcars.extern.cars.SimulatedCar} to the Websocket.
 * @author David Schwarze
 *
 */
public class RegisterData extends MessageData {

	private long carID;
	
	public RegisterData() {
		super();
		
	}
	
	public RegisterData(long carID) {
		super();
		this.carID = carID;
		
	}

	public long getCarID() {
		return carID;
	}

	public void setCarID(long carID) {
		this.carID = carID;
	}
	
	
}
