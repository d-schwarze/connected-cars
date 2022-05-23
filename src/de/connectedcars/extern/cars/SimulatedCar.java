package de.connectedcars.extern.cars;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import com.google.gson.Gson;

import de.connectedcars.backend.binding.BindingCode;
import de.connectedcars.backend.cars.Car;
import de.connectedcars.backend.cars.Route.RouteJSON;
import de.connectedcars.backend.messaging.Message;
import de.connectedcars.backend.messaging.MessageDeserializer;
import de.connectedcars.backend.messaging.MessageType;
import de.connectedcars.backend.messaging.data.BindingCodeData;
import de.connectedcars.backend.messaging.data.CarData;
import de.connectedcars.backend.messaging.data.CloseData;
import de.connectedcars.backend.messaging.data.FinishedData;
import de.connectedcars.backend.messaging.data.RegisterData;
import de.connectedcars.backend.messaging.data.RouteData;
import de.connectedcars.backend.messaging.data.SimpleRouteData;
import de.connectedcars.backend.messaging.data.StatusData;
import de.connectedcars.backend.messaging.util.ConfigeredGson;

/**
 * Simulated car that should model a real car. The simulated car is connected to the main server and communicates with the main server via a websocket.
 * @author David
 *
 */
@ClientEndpoint
public class SimulatedCar {
	
	private Car car;
	
	private Session server;
	
	private Gson gson = ConfigeredGson.get();
	
	private BindingCode bindingCode;
	
	public SimulatedCar(Car car) {
		
		this.car = car;
		
	}
	
	/**
	 * Called if the websocket connection was opened.
	 * @param session of the server ({@linkplain CarSocket})
	 */
	@OnOpen
    public void onOpen(Session session) {
		this.server = session;
		
		//Register at the server
		Message registerMessage = new Message(MessageType.REGISTER, new RegisterData(car.getID()));
		
		
		String registerJson = gson.toJson(registerMessage);
		
        try {
        	
            server.getBasicRemote().sendText(registerJson);
            
            
        } catch (IOException e) {
        	
            throw new RuntimeException(e);
            
        }
        
    }

	/**
	 * Called if data was send from the server to this simulated car.
	 * @param data
	 * @param session
	 */
    @OnMessage
    public void onMessage(String data, Session session) {
    	
    	try {
    		
    		//Get the message of the string
    		Message message = MessageDeserializer.deserialize(data);
    	
    		processMessage(message);
    		
    	} catch(Exception ex) {
    		
    		ex.printStackTrace();
    	}
    	
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    	
    	System.out.println("Client was closed.");
    }
    
    /**
     * Process the sent message.
     * @param message
     */
    private void processMessage(Message message) {
    	
    	switch(message.getType()) {
    	
    		case STATUS: //Status data is requested
    			server.getAsyncRemote().sendText(getStatusMessage());
    			break;
    		case CAR: //Car data is requested
    			server.getAsyncRemote().sendText(getCarMessage());
    			break;
    		case ADD_ROUTE: //New route is added
    			addRoute(message);
    			server.getAsyncRemote().sendText(getFinishedMessage(MessageType.ADD_ROUTE));
    			break;
    		case REMOVE_ROUTE: //Route is removed
    			removeRoute(message);
    			server.getAsyncRemote().sendText(getFinishedMessage(MessageType.REMOVE_ROUTE));
    			break;
    		case CURRENT_ROUTE: //New current route is set
    			setCurrentRoute(message);
    			server.getAsyncRemote().sendText(getFinishedMessage(MessageType.CURRENT_ROUTE));
    			break;
    		case BIND: //New binding code is requested
    			generateNewBindingCode();
    			break;
    		default:
    			break;
    	
    	}
    	
    }
    
    /**
     * Get a new Message with a new BindingCode in a {@linkplain de.connectedcars.messaging.data.BindingCodeData} as json.
     * @param code
     * @return message as json
     */
    private String getBindingCodeMessage(BindingCode code) {
    	
    	Message msg = new Message(MessageType.BIND, new BindingCodeData(code));
    	
    	return gson.toJson(msg);
    	
    }
    
    /**
     * Set the current route from the sent message.
     * @param message
     */
    private void setCurrentRoute(Message message) {
    	
    	long routeId = ((SimpleRouteData) message.getData()).getRouteId();
    	
    	this.car.getNavigationSystem().setCurrentRoute(routeId);
    	
    }
    
    /**
     * Add a new route from the sent message.
     * @param message
     */
    private void addRoute(Message message) {

    	RouteJSON route = ((RouteData) message.getData()).getRoute();

    	this.car.getNavigationSystem().addRoute(route);
    	
    }
    
    
    /**
     * Remove the route from the sent message.
     * @param message
     */
    private void removeRoute(Message message) {
    	
    	long routeId = ((SimpleRouteData) message.getData()).getRouteId();
    	
    	this.car.getNavigationSystem().removeRoute(routeId);

    }
	
    /**
     * Get a new Message with a the status in a {@linkplain de.connectedcars.backend.messaging.data.StatusData} as json.
     * @return message as json
     */
    private String getStatusMessage() {
    	
    	Message msg = new Message(MessageType.STATUS, new StatusData(this.car.getStatus()));
    	
    	return gson.toJson(msg);
    	
    }
    
    /**
     * Get a new Message with a the car data in a {@linkplain de.connectedcars.backend.messaging.data.CarData} as json.
     * @return message as json
     */
    private String getCarMessage() {
    	
    	Message msg = new Message(MessageType.CAR, new CarData(this.car));
    	
    	return gson.toJson(msg);
    	
    }
    
    /**
     * Get a new Message with only the MessageType as json.
     * @return message as json
     */
    private String getFinishedMessage(MessageType type) {
    	
    	Message msg = new Message(type, new FinishedData());
    	
    	return gson.toJson(msg);
    	
    }
    
    public Car getCar() {
    	
    	return car;
    	
    }
    
    /**
     * Generate a new binding code and send it to the server(/manager).
     */
    public void generateNewBindingCode() {
    	
    	
    	this.bindingCode = new BindingCode();
    	
    
		server.getAsyncRemote().sendText(getBindingCodeMessage(this.bindingCode));
		
    	
    }
    
    
    
}
