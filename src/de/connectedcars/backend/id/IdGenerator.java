package de.connectedcars.backend.id;

import java.util.List;

/**
 * Generates unique id's for a list.
 * @author David Schwarze
 *
 * @param <T> class that implements {@linkplain ID}
 */
public class IdGenerator<T extends ID> {

	private List<T> ids;
	
	public IdGenerator(List<T> ids) {
		
		this.ids = ids;
		
	}
	
	/**
	 * Generates a new unique id.
	 * @return unique id
	 */
	public long generateNewID() {
		
		long biggestID = 0;
		
		for(T id : ids) {
			
			if(id.getID() > biggestID) 
				biggestID = id.getID();
		}
		
		return biggestID + 1;
		
	}
	
}
