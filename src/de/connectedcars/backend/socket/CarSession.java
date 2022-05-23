package de.connectedcars.backend.socket;

import javax.websocket.Session;

import de.connectedcars.backend.cars.Car;

/**
 * Contains the Websocket session of the {@linkplain SimulatedCar} and the id of the {@linkplain Car}.<br>
 * Is used in {@linkplain CarSocket}. It is created if a {@linkplain SimulatedCar} connects to {@linkplain CarSocket}.
 * @author David Schwarze
 *
 */
public class CarSession {

	private long carID;
	
	private Session session;
	
	public CarSession(long carID, Session session) {
		
		this.carID = carID;
		this.session = session;
		
	}

	public long getCarID() {
		return carID;
	}

	public void setCarID(long carID) {
		this.carID = carID;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
	
	
	
}
