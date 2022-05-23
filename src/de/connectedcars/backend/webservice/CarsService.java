package de.connectedcars.backend.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


import com.google.gson.Gson;

import de.connectedcars.backend.cars.Car;
import de.connectedcars.backend.cars.CarManager;
import de.connectedcars.backend.cars.Route.RouteJSON;
import de.connectedcars.backend.messaging.Message;
import de.connectedcars.backend.messaging.MessageDataContainer;
import de.connectedcars.backend.messaging.MessageType;
import de.connectedcars.backend.messaging.data.CarData;
import de.connectedcars.backend.messaging.data.MessageData;
import de.connectedcars.backend.messaging.data.RouteData;
import de.connectedcars.backend.messaging.data.SimpleRouteData;
import de.connectedcars.backend.messaging.data.StatusData;
import de.connectedcars.backend.messaging.util.ConfigeredGson;
import de.connectedcars.backend.socket.CarSocket;
import de.connectedcars.backend.socket.Request;
import de.connectedcars.backend.user.User;
import de.connectedcars.backend.user.UserManager;
import de.connectedcars.backend.user.session.UserSession;
import de.connectedcars.backend.user.session.UserSessionManager;

/**
 * Is used to to get data from the cars or send data to them.<br>
 * Part of the webservice.
 * @author David Schwarze
 *
 */
@Path("cars")
public class CarsService {

	private UserSessionManager userSessionManager = UserSessionManager.getInstance();
	
	private CarManager carManager = CarManager.getInstance();
	
	private UserManager userManager = UserManager.getInstance();
	
	private CarSocket carSocket = CarSocket.getInstance();
	
	
	//Standard gson
	private Gson gson = ConfigeredGson.get();
	
	/**
	 * Get status of a car.
	 * @param carId of the car
	 * @return {@linkplain ResponseWS} in form of a json string.<br>
	 * 		   If the car is directly available via the websocket, the latets status is returned, if not, the status from the database is returned.
	 */
	@GET
	@Path("status")
	public String getStatus(@QueryParam("id") long carId) {
		
		try {
			//Countdown to wait until the simulated car has responded
			CountDownLatch cdl = new CountDownLatch(1);
			
			//Container to get the data of the response(work-around, because of the anonymous class request)
			final MessageDataContainer mdc = new MessageDataContainer();
			
			CarSocket.getInstance().requestData(new Request(carId, new Message(MessageType.STATUS)) {
	
				@Override
				public void onResponse(MessageData responseData) {
					
					//If error: Car is not connected or does not response in 2 seconds.
					if(!CarSocket.hasError(responseData)) {
					
						mdc.data = responseData;
					
					}
					
					//Cound down -> cdl.await() can continue
					cdl.countDown();
					
				}
				
			});
			
			
			String jsonData = "";
			
			int status;
			
				
			//Waiting for only two seconds. Due to connection issues this time is necessary.
			cdl.await();
			
			
			//If data was directly found
			if(mdc.data != null) {
				
				jsonData =  gson.toJson(((StatusData) mdc.data).getStatus());
				
				status = ResponseWS.UP_TO_DATE;
			
			//If data was not directly found
			} else {
				
				Car car = carManager.findCar(carId);
				
				if(car != null) {
				
					jsonData = gson.toJson(car.getStatus());
				
					status = ResponseWS.DATABASE;
				
				} else {
					
					return ResponseWS.build(ResponseWS.NOT_FOUND);
					
				}
				
			}
			
			return ResponseWS.build(status, jsonData);
		
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
				
           
	}
	
	
	/**
	 * Get all cars that are binded to the user. The list can be filtered.
	 * @param sessionId of the user
	 * @param filter to filter the list
	 * @return {@linkplain ResponseWS} in form of a json string.<br>
	 */
	@GET
	@Path("my")
	public String getMyCars(@QueryParam("sessionid") String sessionId, @QueryParam("filter") String filter) {
	
		try {
		
			UserSession session = userSessionManager.getSession(sessionId);
			
			//If user is logged in
			if(session != null) {
				
				//If a filter is available
				if(filter != null && filter.trim().length() > 0) {
					
					List<Car> filteredCars = new ArrayList<>();
					
					//Iterate through binded cars
					for(Car c : session.getUser().getOwnCars()) {
						
						//Filter car
						if(c.getBrand().contains(filter) || c.getType().contains(filter)) {
							
							filteredCars.add(c);
							
						}
						
					}
					
					//Return filterd cars
					return ResponseWS.build(ResponseWS.OK, gson.toJson(filteredCars));
				
				} else {
					
					//Return all cars
					return ResponseWS.build(ResponseWS.OK, gson.toJson(session.getUser().getOwnCars()));
				
				}
				
			} else {
				
				// User is not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
		
	}
	
	/**
	 * Get all cars.
	 * @param location of the cars: DB (database), WEBSOCKET (directly connected), ALL (combined)
	 * @param sessionId of the user
	 * @return {@linkplain ResponseWS} in form of a json string.
	 */
	@GET
	@Path("all/{location}")
	public String getAllCars(@PathParam("location") String location, @QueryParam("sessionid") String sessionId) {
		
		try {
		
			UserSession session = userSessionManager.getSession(sessionId);
			
			String json = "";
			
			//If user is logged in
			if(session != null) {
			
				//Get cars from database
				if(location.equals("db")) {
				
						
					json = gson.toJson(carManager.getCars());
						
					return ResponseWS.build(ResponseWS.OK, json);
				
				//Get cars from websocket
				} else if(location.equals("websocket")) {
					
					json = gson.toJson(carSocket.getConnectedCars());
					
					return ResponseWS.build(ResponseWS.OK, json);
				
				//Get combined cars
				} else {
					
					List<Car> carsFromDB = carManager.getCars();
					
					List<Car> cars = carSocket.getConnectedCars();
					
					for(Car car : carsFromDB) {
						
						//List of the directly connected cars does not contain the car from the database, the car is added
						if(cars.stream().filter(c -> c.getID() == car.getID()).count() == 0) {
							
							cars.add(car);
							
						}
						
					}
					
					json = gson.toJson(cars);
					
					return ResponseWS.build(ResponseWS.OK, json);
					
				}
			} else {
				
				//User is not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage()); 
			
		}
	}
	
	/**
	 * Get a car.
	 * @param carId of the car, if 0 -> return the first car that is found
	 * @param sessionId of the user
	 * @return {@linkplain ResponseWS} in form of a json string.<br>
	 *         If the car is directly available via the websocket, the latets status is returned, if not, the status from the database is returned.
	 */
	@GET
	@Path("/get")
	@Produces(MediaType.TEXT_HTML)
	public String car(@QueryParam("carid") long carId, @QueryParam("sessionid") String sessionId) {
		
		try {
		
			UserSession session = userSessionManager.getSession(sessionId);
			
			//Container to get the data of the response(work-around, because of the anonymous class request)
			final MessageDataContainer mdc = new MessageDataContainer();
			
			//User is logged in
			if(session != null) {
				
				User user = session.getUser();
				
				//Iterate through binded cars and check if the user contains the car
				for(Car car : user.getOwnCars()) {
					
					//Check if the car is the searched one, if no carid -> return first
					if(car.getID() == carId || carId == 0) {
						
						//Countdown to wait until the simulated car has responded
						final CountDownLatch cdl = new CountDownLatch(1);
						
						CarSocket.getInstance().requestData(new Request(car.getID(), new Message(MessageType.CAR)) {
	
							@Override
							public void onResponse(MessageData responseData) {
								
								if(!CarSocket.hasError(responseData)) {

									mdc.data = responseData;
									
									
								} 
								
								//Cound down, cdl.await() can continue
								cdl.countDown();
								
							}
							
						});
						
						String jsonData = "";
						
						int status;
							
						//Wait for response
						cdl.await();
						
						//If data was directly found
						if(mdc.data != null) {
							
							jsonData =  gson.toJson(((CarData) mdc.data).getCar());
							
							status = ResponseWS.UP_TO_DATE;
							
						} else {
							
							Car c = carManager.findCar(car.getID());
							
							if(c != null) {
							
								jsonData = gson.toJson(c);
							
								status = ResponseWS.DATABASE;
							
							} else {
								
								return ResponseWS.build(ResponseWS.NOT_FOUND);
								
							}
							
						}
						
						return ResponseWS.build(status, jsonData);
						
					}
					
				}
				
				//Car was not found
				return ResponseWS.build(ResponseWS.NOT_FOUND);
				
			} else {
				
				//User is not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			} 
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
		
		
	}
	
	/**
	 * Remove a car
	 * @param id
	 * @return
	 */
	@POST
	@Path("remove")
	public String removeCar(String id) {
		
		try {
			
			long carid = Long.valueOf(id);
			
			boolean removed = carManager.removeCar(carid);
		
			if(removed)
				return ResponseWS.build(ResponseWS.OK);
			else
				return ResponseWS.build(ResponseWS.FAILED);
		
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
	}
	
	/**
	 * Unbind a car from a user
	 * @param carId of the car
	 * @param sessionid of the usersession
	 * @return {@linkplain ResponseWS} in form of a json string.<br>
	 */
	@GET
	@Path("unbind")
	public String unbindCar(@QueryParam("carid") long carId, @QueryParam("sessionid") String sessionid) {
		
		try {
			
			
			UserSession session = userSessionManager.getSession(sessionid);
			
			Car car = carManager.findCar(carId);
			
			//If the user is logged in
			if(session != null) {
			
				//If the car was found in the database
				if(car != null) {
				
					//If the car is binded to the user
					if(session.getUser().containsCar(car)) {
						
						//Remove binding
						session.getUser().removeCar(car);
						
						//Update user
						userManager.updateUser(session.getUser());
						
						return ResponseWS.build(ResponseWS.OK);
						
					} else {
						
						//Car is not binded
						return ResponseWS.build(ResponseWS.ALREADY_DONE);
						
					}
					
				} else {
					
					//Car not found
					return ResponseWS.build(ResponseWS.NOT_FOUND);
					
				}
				
			} else {
				
				//User is not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
		
		} catch(Exception ex) {
			ex.printStackTrace();
			return ResponseWS.buildError(ex.getMessage());
			
		}
	}
	
	/**
	 * Bind a car to a user
	 * @param carId of the car
	 * @param sessionid of the usersession
	 * @return {@linkplain ResponseWS} in form of a json string.<br>
	 */
	@GET
	@Path("bind")
	public String bindCar(@QueryParam("code") String code, @QueryParam("sessionid") String sessionid) {
		
		try {
			
			//Container to get the data of the response(work-around, because of the anonymous class request)
			final MessageDataContainer mdc = new MessageDataContainer();
		
			UserSession session = userSessionManager.getSession(sessionid);
			
			
			//User is logged in
			if(session != null) {
				
				//Check the binding code, if true, the carid is returned
				long carId = CarSocket.getInstance().checkBindingCode(code);
				
				//If a carid was returned -> code is valid
				if(carId != -1) {
				
					Car car = carManager.findCar(carId);
					
					//If car was found
					if(car != null) {
					
						//If car is not binded to car
						if(!session.getUser().containsCar(car)) {
									
							//Bind car
							session.getUser().getOwnCars().add(car);
							
							
							//Update car
							userManager.updateUser(session.getUser());
							
							return ResponseWS.build(ResponseWS.OK);
								
						} else {
							
							//Car already binded
							return ResponseWS.build(ResponseWS.ALREADY_DONE);
							
						}
						
					} else {
						
						//Car not found
						return ResponseWS.build(ResponseWS.NOT_FOUND);
						
					}
									
				} else {
					
					//Code is not valid
					return ResponseWS.build(ResponseWS.AUTHORIZATION_FAILED);
					
				}	
				
			} else {
				
				//User not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
		
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
	}
	
	/**
	 * Add a route to a car/navi. Route can only be added if the car is connected directly to the websocket.
	 * @param carId of the car
	 * @param sessionid of the usersession
	 * @param routeJson route in form of {@linkplain RouteJSON}
	 * @return {@linkplain ResponseWS} in form of a json string.<br>
	 *         Only used to send the status.
	 */
	@POST
	@Path("route/add")
	public String addRoute(@QueryParam("carid") long carId, @QueryParam("sessionid") String sessionid, String routeJson) {
		
		try {
		
			UserSession session = userSessionManager.getSession(sessionid);
			
			Car car = carManager.findCar(carId);
			
			//Container to get the data of the response(work-around, because of the anonymous class request)
			final MessageDataContainer mdc = new MessageDataContainer();
			
			//If user is logged in
			if(session != null) {
				
				//If car was found and user contains car
				System.out.println(car != null);
				System.out.println(session.getUser().containsCar(car));
				if(car != null && session.getUser().containsCar(car)) {
					
					RouteJSON newRoute = null;
					
					newRoute = gson.fromJson(routeJson, RouteJSON.class);
					
					//Countdown to wait until the simulated car has responded
					final CountDownLatch cdl = new CountDownLatch(1);
					
					CarSocket.getInstance().requestData(new Request(carId, new Message(MessageType.ADD_ROUTE, new RouteData(newRoute))) {
	
						@Override
						public void onResponse(MessageData responseData) {
							//Has Error if the car is not connected or the response took over 2sec (timeout)
							
							if(!CarSocket.hasError(responseData)) { 
								
								mdc.data = responseData;
								
							} 
							
							//Count down, cdl.await() can continue
							cdl.countDown();
							
						}
						
					});
					
					//Wait for response
					cdl.await();
					
					//If route was added
					if(mdc.data != null)
						return ResponseWS.build(ResponseWS.OK);
					else
						return ResponseWS.build(ResponseWS.FAILED);
					
				} else {
					
					//Car not found
					return ResponseWS.build(ResponseWS.NOT_FOUND);
					
				}
				
			} else {
				
				//User not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
	}
	
	/**
	 * Remove a route from a car/navi. Route can only be removed if the car is connected directly.
	 * @param carId of the car
	 * @param routeId of the route (not unique)
	 * @param sessionid of the userssion
	 * @return{@linkplain ResponseWS} in form of a json string.<br>
	 *         
	 */
	@GET
	@Path("route/remove")
	public String removeRoute(@QueryParam("carid") long carId, @QueryParam("routeid") long routeId, @QueryParam("sessionid") String sessionid) {
		
		try {
		
			UserSession session = userSessionManager.getSession(sessionid);
			
			Car car = carManager.findCar(carId);
			
			//Container to get the data of the response(work-around, because of the anonymous class request)
			final MessageDataContainer mdc = new MessageDataContainer();
			
			//If user is logged in
			if(session != null) {
				
				//If car was found and the car is binded to the car
				if(car != null && session.getUser().containsCar(car)) {
						
					//Countdown to wait until the simulated car has responded
					final CountDownLatch cdl = new CountDownLatch(1);
					
					carSocket.requestData(new Request(carId, new Message(MessageType.REMOVE_ROUTE, new SimpleRouteData(routeId))) {
	
						@Override
						public void onResponse(MessageData responseData) {
							//Has Error if the car is not connected or the response took over 2sec (timeout)
							
							if(!CarSocket.hasError(responseData)) { 
								
								mdc.data = responseData;
								
							} 
							
							//Coutn down, cdl.await() can continue
							cdl.countDown();
							
						}
						
					});
					
					//Wait for response
					cdl.await();
					
					if(mdc.data != null) {
						
						return ResponseWS.build(ResponseWS.OK);
						
					} else {
						
						return ResponseWS.build(ResponseWS.FAILED);
						
					}
					
				} else {
					
					//Car not found/not binded
					return ResponseWS.build(ResponseWS.NOT_FOUND);
					
				}
				
			} else {
				
				//User is not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
		
	}
	
	/**
	 * Set the current route of a car/navi. Can only be set, if the car is directly connected to the car.
	 * @param carId of the car
	 * @param routeId of the route (not unique)
	 * @param sessionid of the usersession
	 * @return {@linkplain ResponseWS} in form of a json string.<br>
	 */
	@GET
	@Path("route/current")
	public String setCurrentRoute(@QueryParam("carid") long carId, @QueryParam("routeid") long routeId, @QueryParam("sessionid") String sessionid) {
		
		try {
		
			UserSession session = userSessionManager.getSession(sessionid);
			
			Car car = carManager.findCar(carId);
			
			//Container to get the data of the response(work-around, because of the anonymous class request)
			final MessageDataContainer mdc = new MessageDataContainer();
			
			//If user is logged in
			if(session != null) {
				
				//If car was found and car is binded to user
				if(car != null && session.getUser().containsCar(car)) {
					
					//Countdown to wait until the simulated car has responded
					final CountDownLatch cdl = new CountDownLatch(1);
					
					carSocket.requestData(new Request(carId, new Message(MessageType.CURRENT_ROUTE, new SimpleRouteData(routeId))) {
	
						@Override
						public void onResponse(MessageData responseData) {
							//Has Error if the car is not connected or the response took over 2sec (timeout)
							
							if(!CarSocket.hasError(responseData)) { 
								
								mdc.data = responseData;
								
							} 
							
							//Count down, cdl.await() can continue
							cdl.countDown();
							
						}
						
					});
					
					//Wait for response
					cdl.await();
					
					if(mdc.data != null) {
						
						return ResponseWS.build(ResponseWS.OK);
						
					} else {
						
						return ResponseWS.build(ResponseWS.FAILED);
						
					}
					
					
				} else {
					
					//Car not found/not binded
					return ResponseWS.build(ResponseWS.NOT_FOUND);
					
				}
				
			} else {
				
				//Car not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
		
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
	} 
	
}
