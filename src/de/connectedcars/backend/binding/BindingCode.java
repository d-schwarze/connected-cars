package de.connectedcars.backend.binding;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BindingCode that is used to verify a binding between a car and a user.
 * @author David Schwarze
 *
 */
public class BindingCode {
	
	//Creation time of the code, CODE IS ONLY AVAILABLE FOR 15 MINUTES!
	private LocalDateTime creationTime;
	
	//Actual code (unique)
	private String code;
	
	public BindingCode() {
		
		code = UUID.randomUUID().toString();
		creationTime = LocalDateTime.now();
		
	}
	
	/**
	 * Compare a code (string) with the code of this BindingCode.
	 * @param code to be compared
	 * @return 0: equal<br>
	 *         -1: not equal<br>
	 *         -2: code expired
	 */
	public int compare(String code) {
		
		//If the code is not expired
		if(creationTime.plusMinutes(15).isAfter(LocalDateTime.now())) {
			
			if(this.code.equals(code)) {
				//Code is equal
				return 0;
				
			} else {
				//Code is not equal
				return -1;
				
			}
			
		} else {
			//Code expired
			return -2;
			
		}
		
	}
	
	/**
	 * String get the code.
	 * @return code
	 */
	public String getCode() {
		
		return code;
		
	}
}
