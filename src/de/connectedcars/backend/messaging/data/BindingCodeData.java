package de.connectedcars.backend.messaging.data;

import de.connectedcars.backend.binding.BindingCode;

/**
 * Data is used to send a new {@linkplain BindingCode} to the {@linkplain CarSocket}, where it is added to {@linkplain BindingCodeManager}.<br>
 * @author David Schwarze
 *
 */
public class BindingCodeData extends MessageData {

	private BindingCode code;
	
	public BindingCodeData(BindingCode code) {
		
		this.code = code;
		
	}

	public BindingCode getCode() {
		return code;
	}

	public void setCode(BindingCode code) {
		this.code = code;
	}
	
	
}
