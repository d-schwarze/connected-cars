package de.connectedcars.backend.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

import de.connectedcars.backend.binding.BindingCode;
import de.connectedcars.backend.binding.BindingCodeManager;
import de.connectedcars.backend.cars.Car;
import de.connectedcars.backend.cars.CarManager;
import de.connectedcars.backend.messaging.Message;
import de.connectedcars.backend.messaging.MessageDeserializer;
import de.connectedcars.backend.messaging.MessageType;
import de.connectedcars.backend.messaging.data.BindingCodeData;
import de.connectedcars.backend.messaging.data.CarData;
import de.connectedcars.backend.messaging.data.ErrorData;
import de.connectedcars.backend.messaging.data.MessageData;
import de.connectedcars.backend.messaging.data.RegisterData;
import de.connectedcars.backend.messaging.util.ConfigeredGson;

/**
 * Websocket-Server for the {@linkplain de.connectedcars.extern.cars.SimulatedCar}. The simulated cars connect to this websocket endpoint.
 * Via this server, the rest of the services and managers can communicate directly with the cars and get/send real-time-data.<br>
 * If a new simulated car connects to the websocket, a new {@linkplain CarSession} is created. This session stores the car id and the {@linkplain Session}.<br>
 * With {@linkplain Request}´s data can send and get from the simulated cars ( {@link #requestData(Request)} ). 
 * {@linkplain de.connectedcars.backend.messaging.Message}`s are used to send data from the server to the client and the other way around.
 * The car then has 2 seconds time to response to the request. Otherwise the request will timeout + removed and nothing is returned.<br>
 * Users can check their binding codes with {@link #checkBindingCode(String)}.<br>
 * Singleton-Pattern
 * @author David Schwarze
 *
 */
@ServerEndpoint(value = "/cars")
public class CarSocket {
	
	//All carsession.
	private static final Set<CarSession> CARSESSIONS = ConcurrentHashMap.newKeySet();
	 
	private Gson gson = ConfigeredGson.get();
	
	//All requests
	private static final Set<Request> REQUESTS = ConcurrentHashMap.newKeySet();
	
	private static CarSocket instance;
	
	private CountDownLatch cdl;
	
	private CarManager carManager = CarManager.getInstance();
	
	private BindingCodeManager bindingCodeManager = BindingCodeManager.getInstance();
	
	public CarSocket() { 
		
		instance = this;
		
		
	}
	
	public static CarSocket getInstance() {
		
		if(instance == null)  {
			
			instance = new CarSocket();
			
		}
			
		return instance;
		
	}
	
    @OnOpen
    public void onOpen(Session session) {
    	
    	//Car sessions have to register first. -> onMessage()
    	
    }
 
    /**
     * One message from the client sessions (carsessions).
     * @param data form the client sessions
     * @param session of the client
     */
    @OnMessage
    public void onMessage(String data, Session session) {
    	
    	try {
    		
    		//Create a Message from the string.
    		Message message = MessageDeserializer.deserialize(data);
    	
    		//Process the message
    		processMessage(message, session);
    		
    	} catch(Exception ex) {
    		
    		ex.printStackTrace();
    		
    	}
    }
 
    /**
     * Called if a simulated car logs out.
     * @param session
     * @param closeReason
     */
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    	
    	//Get the carid of ther session
    	long carId = getCarSession(session).getCarID();
		
    	//Log out
		removeCarSession(session);
			
    }
    
    /**
     * Request new data from the simulated cars.
     * @param request for the car
     */
    public void requestData(Request request) {
    	
    	//Add request to active pool
    	REQUESTS.add(request);
    	
    	//Get the session of the car. Session is used to send text to the client endpoint (simulated cars).
    	Session session = getSession(request.getCarId());
    	
    	
    	//If a session was found
    	if(session != null) {
    		
    		//Get the message as json.
    		String json = gson.toJson(request.getRequestMessage());
    		
    		//Send the message to the simulated car.
    		session.getAsyncRemote().sendText(json);
    		
    		//Set timeout
    		Thread t = new Thread(() ->  {
    			
    			try {
    				
					Thread.sleep(2000);
					//Car has not responded -> Error is responded.
					//Is called everytime, but response(...) checks whether the request has not already been answered. If so the error is not send.
					response(request, new ErrorData(ErrorData.CAR_NOT_AVAILABLE));
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		});
    		t.start();
    		
    		
    	} else {
    		//Requested car is not connected or registered
    		response(request, new ErrorData(ErrorData.CAR_NOT_CONNECTED));
    		
    	}
    	
    }
    
    /**
     * Response of a {@linkplain Request}.
     * @param request that was requested by a client
     * @param data the response data
     */
    private void response(Request request, MessageData data) {
    	
    	//Check whether the request has not already been answered. Very important for the timeout (see: requestData(...))
    	if(!request.hasResponded()) {
    		//Set request to responded
    		request.setResponded(true);
    		
    		//Response
    		request.onResponse(data);
    		
    		//Request is removed, can not be answered again.
    		REQUESTS.remove(request);
    	
    	} 
    	
    	
    }
    
    /**
     * Get the {@linkplain Session} of a car.
     * @param carId of the car
     * @return the session or null, if not the car is not connected.
     */
    private Session getSession(long carId) {
    	
    	for(CarSession carSession : CARSESSIONS) {
    		
    		if(carSession.getCarID() == carId) {
    			
    			return carSession.getSession();
    			
    		}
    		
    	}
    	
    	return null;
    	
    }
    
    /**
     * Process a message.
     * @param message
     * @param session
     */
    private void processMessage(Message message, Session session) {
    	
    	switch(message.getType()) {
    	
    		case REGISTER:
    			//Register is only coming from the simulated car.
    			register((RegisterData) message.getData(), session);
    			break;
    		case CLOSE:
				try {
					
					session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Auto meldet sich ab"));
					
				} catch (IOException e) {
					e.printStackTrace();
				} 
					
				break;
    		default:
    			//Standard process
    			handleData(message, session);
    			break;
    	
    	}
    	
    }
    
    /**
     * Standard data process.
     * @param message
     * @param session
     */
    private synchronized void handleData(Message message, Session session) {
    	
    	CarSession carSession = getCarSession(session);
    	
    	//If the car is logged in
    	if(carSession != null) {

    		//Iterate through requests
    		for(Request request : REQUESTS) {
  
    			//Check if same car AND if the requested message type is equal -> if true, data is for this request.
    			if(request.getCarId() == carSession.getCarID() && message.getType() == request.getRequestMessage().getType()) {
    				
    				//Response the request
    				response(request, message.getData());
    				
    			}
    			
    		}
    		
    		/*
    		 * Some exception (not in processMessage(...), because a usersession is necessary)
    		 */
    		
    		//A simulated car create a new binding code
    		if(message.getType() == MessageType.BIND) {
    			
    			BindingCode code = ((BindingCodeData) message.getData()).getCode();
    			
    			//Add code to manager
    			bindingCodeManager.addCode(carSession.getCarID(), code);
    			
    		}
    		
    		//Case 1: A new car is registerd -> car data is requested (see: register(...)) -> car is added to database.
    		//Case 2: If the car data was requested and the response is handled now, the database is updated with the latest data.
    		if(message.getType() == MessageType.CAR) {
    			
    			Car car = ((CarData) message.getData()).getCar();
    			
    			if(carManager.containsCar(car.getID())) {
					
					carManager.updateCar(car);
					
				} else {
					
					carManager.createCar(car);
				}
    			
    		}
    		
    	} else {
    		
    		//Car is not registered -> no access to socket
    	}
    	
    }
    
    /**
     * Get the {@linkplain CarSession} of a {@linkplain Session}
     * @param session
     * @return the carsession, null if the carsession was not found
     */
    private CarSession getCarSession(Session session) {
    	
    	for(CarSession carSession : CARSESSIONS) {
    		
    		if(carSession.getSession().getId().equals(session.getId()))
    			return carSession;
    		
    	}
    	
    	return null;
    	
    }
	
    /**
     * Register a new simulated car.
     * @param data for the registration
     * @param session that is stored into the new usersession
     */
    private synchronized void register(RegisterData data, Session session) {
    	
    	//Login, new usersession
    	addCarSession(data.getCarID(), session);
    	
    	//Update/create db data.
    	session.getAsyncRemote().sendText(gson.toJson(new Message(MessageType.CAR)));
    	
    }
    
    /**
     * Check if the {@link #CARSESSIONS} contain a {@linkplain Session}.
     * @param session 
     * @return true if the session is contained<br>false is the session is not contained
     */
    public boolean containsSession(Session session) {
    	
    	for(CarSession carSession : CARSESSIONS) {
    		
    		if(carSession.getSession().getId().equals(session.getId())) {
    			
    			return true;
    			
    		}
    		
    	}
    	
    	return false;
    	
    }
    
    /**
     * Add a new {@linkplain CarSession}. Register a simulated car.
     * @param carId of the car
     * @param session the carsession
     */
    public void addCarSession(long carId, Session session) {
    	
    	if(!containsSession(session)) {
    		
    		CARSESSIONS.add(new CarSession(carId, session));
    		
    	}
    }
    
    /**
     * Remove a {@linkplain CarSession}. Log out a simulated car.
     * @param session of the carsession
     */
    public void removeCarSession(Session session) {
    	
    	for(CarSession carSession : CARSESSIONS) {
    		
    		if(carSession.getSession().getId().equals(session.getId())) {
    			
    			CARSESSIONS.remove(carSession);
    			break;
    			
    		}
    		
    	}
    	
    }
    
    /**
     * Get all directly connected simulated cars.
     * @return all all directly connected simulated cars.
     */
    public List<Car> getConnectedCars() {
    	
    	final List<Car> cars = new ArrayList<>();
    	
    	final List<Request> requestsTemp = new ArrayList<>();
    	
    	//Iterate through connected cars
    	for(CarSession carSession : CARSESSIONS) {
    		
    		//Create a new request the car data
    		Request request = new Request(carSession.getCarID(), new Message(MessageType.CAR)) {

				@Override
				public void onResponse(MessageData responseData) {
					
					if(!hasError(responseData)) {
					
						Car car = ((CarData) responseData).getCar();
						
						cars.add(car);
					
					}
					
					cdl.countDown();
					
					
					
				}
    			
    		};
    		
    		//Add request to temp list, because the CountDownLatch size can only be set in the constructor.
    		requestsTemp.add(request);
    	
    	}
    	
    	//Create a new CountDownLatch
    	cdl = new CountDownLatch(requestsTemp.size());
    	
    	for(Request request : requestsTemp) {
    		
    		//Request the car data
    		requestData(request);
    		
    	}
    	
    	try {
    		
    		//Wait for responses (max ca. 2 seconds)
			cdl.await();
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
			
		}
    	
    	return cars;
    	
    }
    
    /**
     * Check if a {@linkplain de.connectedcars.backend.messaging.data.MessageData} is a {@linkplain de.connectedcars.backend.messaging.data.ErrorData}.
     * Very important for the requests.
     * @param data to be checked
     * @return true: has error<br>false: has no error, data can be processed normally
     */
    public static boolean hasError(MessageData data) {
    	
    	try {
    		
			ErrorData error = (ErrorData) data;
    		
    	} catch(Exception ex) {
    		
    		return false;
    		
    	}
    	
    	return true;
    	
    }
    
    /**
     * Check a binding code.
     * @param code to be checked
     * @return the carid if the code is valid<br>-1 if the code is not valid
     */
    public long checkBindingCode(String code) {
    	
    	return bindingCodeManager.checkCode(code);
    	
    }
    
}
