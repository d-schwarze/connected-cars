package de.connectedcars.backend.messaging;

/**
 * Type of the {@linkplain Message}. Is used to deserialize the data of a {@linkplain Message} ({@linkplain MessageDeserializer}).
 * @author David Schwarze
 *
 */
public enum MessageType {

	REGISTER, STATUS, ERROR, CAR, ADD_ROUTE, REMOVE_ROUTE, CURRENT_ROUTE, BIND, CLOSE
	
}
