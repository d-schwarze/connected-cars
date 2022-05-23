package de.connectedcars.backend.messaging;

import de.connectedcars.backend.messaging.data.MessageData;

/**
 * Message for the communication between {@linkplain SimulatedCar} and {@linkplain CarSocket}.<br>
 * The message is serialized by {@linkplain com.google.gson.Gson} and is send as a string to the client or server.
 * @author David Schwarze
 *
 */
public class Message {

	//Different actions require different types.
	private MessageType type;
	
	//Different actions require different data. The MessageData is linked to the MessageType (examples: MessageDeseriaizer).
	private MessageData data;
	
	public Message() {
		
	}
	
	public Message(MessageType type) {
		
		this.type = type;
		
	}
	
	public Message(MessageType type, MessageData data) {
		this(type);
		
		this.data = data;
		
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public MessageData getData() {
		return data;
	}

	public void setData(MessageData data) {
		this.data = data;
	}
}
