package de.connectedcars.backend.cars;

import java.time.LocalDateTime;

import de.connectedcars.backend.id.ID;

/**
 * POJO of a route.<br>
 * Routes are saved in a {@linkplain NavigationSystem}.
 * Route id's are not unique in the global context and are generated via {@linkplain IdGenerator}.
 * @author David Schwarze
 *
 */
public class Route implements ID {

	private long id;
	
	private Location start;
	
	private Location destination;
	
	private LocalDateTime creationTime;
	
	public Route(long id, Location start, Location destination, LocalDateTime creationTime) {
		
		this.id = id;
		this.start = start;
		this.destination = destination;
		this.creationTime = creationTime;
		
	}

	
	public Route(long id, RouteJSON data) {
		
		this.start = data.start;
		this.destination = data.destination;
		this.creationTime = data.creationTime;
		this.id = id;
		
	}
	public Location getStart() {
		return start;
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public Location getDestination() {
		return destination;
	}

	public void setDestination(Location destination) {
		this.destination = destination;
	}

	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(LocalDateTime creationTime) {
		this.creationTime = creationTime;
	}
	
	/**
	 * Object of a route from a json format. Deserialized by Gson.
	 * @author David Schwarze
	 *
	 */
	public class RouteJSON {
		
		public Location start;
		
		public Location destination;
		
		public LocalDateTime creationTime;
		
	}

	@Override
	public long getID() {
		// TODO Auto-generated method stub
		return id;
	}
	
	public void setID(long id) {
		this.id = id;
	}
	
}
