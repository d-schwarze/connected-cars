package de.connectedcars.backend.messaging.data;

/**
 * Data is used to indicate, that a {@linkplain de.connectedcars.extern.cars.SimulatedCar} is not connected or available.<br>
 * Every {@linkplain de.connectedcars.backend.socket.Request#onResponse(MessageData)} should check with {@linkplain de.connectedcars.backend.socket.CarSocket#hasError(MessageData)}
 * if the responded {@linkplain Message} is an error.
 * @author David Schwarze
 *
 */
public class ErrorData extends MessageData {
	
	public final static int CAR_NOT_CONNECTED = 1;
	
	public final static int CAR_NOT_AVAILABLE = 2;
	
	private int code;
	
	public ErrorData(int code) {
		
		this.code = code;
		
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
