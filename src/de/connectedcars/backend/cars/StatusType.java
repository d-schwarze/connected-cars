package de.connectedcars.backend.cars;

/**
 * Status of a component of {@linkplain Status}.
 * @author David
 *
 */
public enum StatusType {

	//Three types (+ 1: null, if the status is not used -> transmission for an electirc car).
	OK, SUFFICIENT, CRITICAL;
	
	/**
	 * Get the type from a number. Is used for the database, because only numbers are stored into the database.
	 * @param number
	 * @return
	 */
	public static StatusType ofNumber(int number) {
		
		switch(number) {
		
			case 0:
				return OK;
			case 1:
				return SUFFICIENT;
			case 2:
				return CRITICAL;
			default:
				//If status is not set (normally -1)
				return null;
		
		}
		
	}
	
	/**
	 * Get the number of a status type.
	 * @return corresponding number to the type
	 */
	public int getNumber() {
		
		switch(this) {
		
			case OK:
				return 0;
			case SUFFICIENT:
				return 1;
			case CRITICAL:
				return 2;
			default:
				//If null (not set)
				return -1;
		
		}
		
	}
	
	/**
	 * Get the number of a status type.<br>
	 * Static because a status type can null and {@linkplain #getNumber()} does not work for that case.
	 * @param type status type
	 * @return corresponding number to the type
	 */
	public static int getNumber(StatusType type) {
		
		if(type != null) {
			
			switch(type) {
			
				case OK:
					return 0;
				case SUFFICIENT:
					return 1;
				case CRITICAL:
					return 2;
				default:
					return -1;
			
			}
			
		} else {
			
			return -1;
			
		}
	}
	
	/**
	 * Create a random status type.
	 * 
	 * @return random status type, not null
	 */
	public static StatusType getRandom() {
		
		return StatusType.ofNumber((int) Math.round(Math.random() * 2));
		
	}
	
}
