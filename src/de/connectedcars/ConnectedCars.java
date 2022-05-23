package de.connectedcars;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import javax.websocket.DeploymentException;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.tyrus.server.Server;

import com.sun.net.httpserver.HttpServer;

import de.connectedcars.backend.binding.BindingCode;
import de.connectedcars.backend.database.Database;
import de.connectedcars.backend.messaging.Message;
import de.connectedcars.backend.messaging.MessageDataContainer;
import de.connectedcars.backend.messaging.MessageType;
import de.connectedcars.backend.messaging.data.BindingCodeData;
import de.connectedcars.backend.messaging.data.MessageData;
import de.connectedcars.backend.socket.CarSocket;
import de.connectedcars.backend.socket.Request;
import de.connectedcars.config.CORSResponseFilter;
import de.connectedcars.extern.cars.SimulatedCar;
import de.connectedcars.extern.cars.SimulatedCarFactory;

public class ConnectedCars {
	
	private static Server server;

	private static HttpServer webservice;
	
	private static Scanner scanner;
	
	private static boolean stop = false;
	
	private static long nextCarId = 0;
	
	public static void main(String[] args) {
		
		initialize();
		
		scanner = new Scanner(System.in);
		
		while(!stop) {
			
			System.out.println("Enter command (h for Help)");
			
			String input = scanner.nextLine();
			
			handleInput(input);
			
		}
		
		stop();
		
	}
	
	public static void initialize() {
		
		try {
			
			System.out.println("Inizializing...");
			
			startWebservice();
			
			startWebsocket();
			
			
			
			System.out.println("Finished initializing.");
			
		} catch (Exception e) {
			
			System.err.println("Exception:");
			System.err.println(e.getMessage());
			
		}
		
	}
	
	public static void stop() {
		
		try {
			
			System.out.println("Stopping...");
			
			server.stop();
			webservice.stop(0);
			
			
			
			System.out.println("Stopped.");
			
		} catch (Exception e) {
			
			System.err.println("Exception:");
			System.err.println(e.getMessage());
			
		}
	}
	
	public static void startWebservice() throws Exception {
		
			
		ResourceConfig rc = new ResourceConfig().register(CORSResponseFilter.class).packages("de.connectedcars.backend.webservice");
		
		webservice = JdkHttpServerFactory.createHttpServer(URI.create("http://localhost:8080/rest"), rc);
		
		System.out.println("Webservice started at http://localhost:8080/rest");
			
	
	}
	
	public static void startWebsocket() throws DeploymentException {
		
		server = new Server("localhost", 8025, "/websocket", CarSocket.class);
		
		server.start();
		
		System.out.println("Websocket started at ws://localhost:8025/websocket");
		
	}
	
	public static void handleInput(String input) {
		
		String[] data = input.split(":");
		
		if(data.length > 0) {
			
			switch(data[0]) {
			
				case "h":
					returnHelp();
					break;
				case "stop":
					stop = true;
					break;
				case "createdb":
					createDbTables();
					break;
				case "createsc":
					if(data.length > 1) {
						try {
							
							createNewRandomizedSimulatedCar(Integer.valueOf(data[1]));
							break;
							
						} catch(NumberFormatException ex) {
							
							System.err.println("Amount of cars has be to an integer!");
							
						}
					
					}
				case "loadsc":
					if(data.length > 1) {
						
						try {
							
							loadCar(Long.valueOf(data[1]));
							break;
							
						} catch(NumberFormatException ex) {
							
							System.err.println("Car id has to be a long!");
							
						}
						
					} else {
						
						loadAllCars();
						break;
						
					}
				case "createbc":
					if(data.length > 1) {
						
						try {
							
							createNewBindingCode(Long.valueOf(data[1]));
							break;
							
						} catch(NumberFormatException ex) {
							
							System.err.println("Car id has to be a long!");
							
						}
						
					}
				default:
					returnCommandNotFound();
					
			
			}
		
		}
		
	}
	
	public static void createNewBindingCode(long carid) {
		
		CountDownLatch cdl = new CountDownLatch(1);
		
		final MessageDataContainer mdc = new MessageDataContainer();
		
		CarSocket.getInstance().requestData(new Request(carid, new Message(MessageType.BIND)) {

			@Override
			public void onResponse(MessageData responseData) {
				
				if(!CarSocket.hasError(responseData)) {
					
					mdc.data = responseData;
					
				} else {
					
					System.err.println("Car did not response.");
				}
				
				cdl.countDown();
				
			}
			
			
			
		});
		
		try {
			
			cdl.await();
			
			if(mdc.data != null) {
			
				BindingCode code = ((BindingCodeData) mdc.data).getCode();
				
				System.out.println("New code: " + code.getCode());
			
			}
		} catch (InterruptedException e) {
			
			System.err.println("Exception: ");
			System.err.println(e.getMessage());
			
		}
		
	}
	
	public static void loadCar(long carid) {
		
		try {
			
			SimulatedCar car = SimulatedCarFactory.loadSimulatedCar(carid);
			
			if(car != null) {
				
				System.out.println("Car was loaded.");
				
			} else {
				
				System.out.println("Car was not found.");
				
			}
			
		} catch (DeploymentException | URISyntaxException e) {
			
			System.err.println("Exception: ");
			System.err.println(e.getMessage());
			
		}
	}
	
	public static void loadAllCars() {
		
		try {
			
			SimulatedCarFactory.loadSimulatedCars();
			
			System.out.println("All cars are loaded.");
			
		} catch (DeploymentException | URISyntaxException e) {
			
			System.err.println("Exception: ");
			System.err.println(e.getMessage());
			
		}
		
	}
	
	public static void returnHelp() {
		
		System.out.println("Syntax: <command>[:<data>]");
		System.out.println("---Commands---");
		System.out.println("Help: h");
		System.out.println("Create simulated car: createsc:<amountOfCars>");
		System.out.println("Load simulated car from database: loadsc:<carid>");
		System.out.println("Load all simulated cars from database: loadsc");
		System.out.println("Create new binding code: createbc:<carid>");
		System.out.println("Create database tables (old ones are dropped): createdb");
		
	}
	
	public static void returnCommandNotFound() {
		
		System.out.println("Command was not found.");
		
	}
	
	public static void createDbTables() {
		
		Database.createTables();
		
	}
	
	public static void createNewRandomizedSimulatedCar(int amount) {
		
		if(nextCarId < SimulatedCarFactory.getNewID()) {
			
			nextCarId = SimulatedCarFactory.getNewID();
			
		}
		
		for(int i = 0; i < amount; i++) {
			
			try {
				
				SimulatedCarFactory.createRandomizedSimulatedCar(nextCarId);
			
			} catch (DeploymentException | URISyntaxException e) {
				
				System.err.println("Exception: ");
				System.err.println(e.getMessage());
				
			} finally {
			
				nextCarId++;
				
			}
		}
		
		
	}
	
	
}
