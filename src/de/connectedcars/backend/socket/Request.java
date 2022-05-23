package de.connectedcars.backend.socket;

import de.connectedcars.backend.messaging.Message;
import de.connectedcars.backend.messaging.data.MessageData;

/**
 * Request for the {@linkplain CarSocket} to get data from the {@linkplain de.connectedcars.extern.cars.SimulatedCar}.
 * @author David Schwarze
 *
 */
public abstract class Request {
	
	//Id of the, from which data is requested
	private long carId;
	
	//Message
	private Message requestMessage;
	
	//Creation time of the request
	private long creationTime;
	
	//Requests was responded by the simulated car
	private boolean responded;
	
	public Request(long carId, Message requestMessage) {
		
		this.carId = carId;
		this.requestMessage = requestMessage;
		this.creationTime = System.currentTimeMillis();
		
	}
	
	/**
	 * Is called when the simulated cars has responded to the request or a timeout was raised after 2 seconds.<br>
	 * Returns a ErrorData if a timeout was raised.
	 * @param responseData
	 */
	public abstract void onResponse(MessageData responseData);


	public long getCarId() {
		return carId;
	}

	public void setCarId(long carId) {
		this.carId = carId;
	}

	public Message getRequestMessage() {
		return requestMessage;
	}

	public void setRequestMessage(Message requestMessage) {
		this.requestMessage = requestMessage;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public boolean hasResponded() {
		return responded;
	}

	public void setResponded(boolean responded) {
		this.responded = responded;
	}
	
	
}
