package de.connectedcars.backend.binding;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Manager for {@linkplain BindingCode}. Through this manager binding codes from the client can be checked with those of the {@linkplain de.connectedcars.extern.cars.SimulatedCar}.
 * SimulatedCars have to register their codes at this manager.<br>
 * Singleton-Pattern
 * @author David Schwarze
 *
 */
public class BindingCodeManager {
	
	//The carid is linked to the binding code, a car can only have one code.
	private HashMap<Long, BindingCode> codes;
	
	private static BindingCodeManager instance;
	
	private BindingCodeManager() {
	
		codes = new HashMap<>();
		
	}
	
	public static BindingCodeManager getInstance() {
		
		if(instance == null) {
			
			instance = new BindingCodeManager();
		}
		
		return instance;
		
	}
	
	/**
	 * Add a new {@linkplain BindingCode} to the manager.
	 * @param carId of the car
	 * @param code new generated binding code
	 */
	public void addCode(long carId, BindingCode code) {
		
		codes.put(carId, code);
	}

	/**
	 * Check a code (string) from a client-
	 * @param code
	 * @return the id of the car, which is linked to the {@linkplain BindingCode}<br>
	 * 		   -1, if the code is not valid/not found
	 */
	public long checkCode(String code) {
		
		//HashMap can not be iterated -> keySet
		Iterator<Long> it = codes.keySet().iterator();
		
		while(it.hasNext()) {
			
			long id = it.next();
			
			//Compate the code of the BindingCode with the given parameter code
			int ret = codes.get(id).compare(code);
			
			
			if(ret == 0) {
				//If code is equal
				return id;
				
			} else if(ret == -2) {
				//Code expried (more informations: BindingCode.class) -> remove
				codes.remove(id);
				
			}
			
		}
		//No code was found
		return -1;
		
	}
	
}
