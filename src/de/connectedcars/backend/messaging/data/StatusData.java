package de.connectedcars.backend.messaging.data;

import de.connectedcars.backend.cars.Status;

/**
 * Used to send the {@linkplain de.connectedcars.backend.cars.Status} to the Websocket.
 * @author David Schwarze
 *
 */
public class StatusData extends MessageData {

	private Status status;
	
	public StatusData(Status status) {
		
		this.status = status;
		
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
