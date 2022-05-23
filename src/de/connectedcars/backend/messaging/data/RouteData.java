package de.connectedcars.backend.messaging.data;

import de.connectedcars.backend.cars.Route.RouteJSON;

/**
 * Data is used to send a new {@linkplain de.connectedcars.backend.cars.Route.RouteJSON} to a {@linkplain de.connectedcars.extern.cars.SimulatedCar}.
 * @author David
 *
 */
public class RouteData extends MessageData {

	private RouteJSON route;
	
	public RouteData(RouteJSON route) {
		
		this.route = route;
		
	}

	public RouteJSON getRoute() {
		return route;
	}

	public void setRoute(RouteJSON route) {
		this.route = route;
	}
}
