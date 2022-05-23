package de.connectedcars.backend.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.connectedcars.backend.messaging.data.BindingCodeData;
import de.connectedcars.backend.messaging.data.CarData;
import de.connectedcars.backend.messaging.data.CloseData;
import de.connectedcars.backend.messaging.data.RegisterData;
import de.connectedcars.backend.messaging.data.RouteData;
import de.connectedcars.backend.messaging.data.SimpleRouteData;
import de.connectedcars.backend.messaging.data.StatusData;
import de.connectedcars.backend.messaging.util.ConfigeredGson;

public class MessageDeserializer {

	private static Gson gson = ConfigeredGson.get();
	
	public static Message deserialize(String jsonMsg) {
		
		JsonObject jsonObject = gson.fromJson(jsonMsg, JsonObject.class);
		
		MessageType type = gson.fromJson(jsonObject.get("type"), MessageType.class);
		
		Message message;
		
		switch(type) {
		
			case REGISTER:
				message = new Message(type, gson.fromJson(jsonObject.get("data"), RegisterData.class));
				
				break;
			case STATUS:
				message = new Message(type, gson.fromJson(jsonObject.get("data"), StatusData.class));
				
				break;
				
			case CAR:
				message = new Message(type, gson.fromJson(jsonObject.get("data"), CarData.class));
				
				break;
				
			case ADD_ROUTE:
				message = new Message(type, gson.fromJson(jsonObject.get("data"), RouteData.class));
				
				break;
			
			case REMOVE_ROUTE:
				message = new Message(type, gson.fromJson(jsonObject.get("data"), SimpleRouteData.class));
				
				break;
				
			case CURRENT_ROUTE:
				message = new Message(type, gson.fromJson(jsonObject.get("data"), SimpleRouteData.class));
				
				break;	
			
			case BIND:
				message = new Message(type, gson.fromJson(jsonObject.get("data"), BindingCodeData.class));
				
				break;
			case CLOSE:
				message = new Message(type, gson.fromJson(jsonObject.get("data"), CloseData.class));
				
				break;
			default:
				throw new DeserializerException("MessageType is not valid.");
				
			
		}
		
		return message;
		
	}
	
}
