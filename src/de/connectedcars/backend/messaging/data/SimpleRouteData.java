package de.connectedcars.backend.messaging.data;

/**
 * Data is used to send a routeid to a {@linkplain de.connectedcars.extern.cars.SimulatedCar} and remove the route or change the current route.
 * @author David Schwarze
 *
 */
public class SimpleRouteData extends MessageData {

	private long routeId;
	
	public SimpleRouteData(long routeId) {
		
		this.routeId = routeId;
		
		
	}

	public long getRouteId() {
		return routeId;
	}

	public void setRouteId(long routeId) {
		this.routeId = routeId;
	}
	
	
	
}
