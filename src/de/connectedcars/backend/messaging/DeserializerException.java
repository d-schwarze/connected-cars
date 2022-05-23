package de.connectedcars.backend.messaging;

/**
 * Is thrown by {@linkplain MessageDeserializer} if a {@linkplain Message} could not be deserialized.
 * @author David Schwarze
 *
 */
public class DeserializerException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public DeserializerException(String msg) {
		super(msg);
		
	}

}
