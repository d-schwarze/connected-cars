package de.connectedcars.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Properties;
import java.util.concurrent.Future;

import javax.websocket.DeploymentException;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import de.connectedcars.backend.cars.Car;
import de.connectedcars.backend.cars.Location;
import de.connectedcars.backend.cars.Status;
import de.connectedcars.backend.cars.StatusType;
import de.connectedcars.backend.messaging.Message;
import de.connectedcars.backend.messaging.MessageType;
import de.connectedcars.backend.messaging.data.MessageData;
import de.connectedcars.backend.socket.CarSocket;
import de.connectedcars.backend.socket.Request;
import de.connectedcars.backend.user.User;
import de.connectedcars.backend.user.UserManager;
import de.connectedcars.extern.cars.SimulatedCar;
import de.connectedcars.extern.cars.SimulatedCarFactory;


public class TestServer {

	public static void main(String[] args) {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String dbURL = "jdbc:derby:database;create=true";
		
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dbURL);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			stmt.execute("CREATE TABLE USERS(\r\n" + 
					"    email VARCHAR(255) NOT NULL PRIMARY KEY,\r\n" + 
					"    name VARCHAR(255) NOT NULL,\r\n" + 
					"    password VARCHAR(255) NOT NULL\r\n" +  
					")");
			
			
			stmt.execute("CREATE TABLE CARS(\r\n" + 
					"    id INT NOT NULL PRIMARY KEY,\r\n" + 
					"    lat DECIMAL(9,6) NOT NULL,\r\n" + 
					"    lng DECIMAL(9,6) NOT NULL,\r\n" + 
					"    fuellevel DECIMAL(3,2) DEFAULT 1.0,\r\n" + 
					"    mileage DECIMAL(12,3) DEFAULT 0.0,\r\n" + 
					"    inspection DATE,\r\n" + 
					"    brand VARCHAR(255),\r\n" + 
					"    type VARCHAR(255),\r\n" + 
					"    manufacture_year INT,\r\n" + 
					"    fuel_consumption DECIMAL(4,1) NOT NULL,\r\n" + 
					"    fuel_capacity DECIMAL(4,1) NOT NULL,\r\n" + 
					"    electrical BOOLEAN NOT NULL\r\n" + 
					")");
			
			
			stmt.execute("CREATE TABLE ROUTES(\r\n" + 
					"    id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\r\n" + 
					"	 internal_id INT NOT NULL,\r\n" +
					"    car_id INT NOT NULL REFERENCES CARS(id) ON DELETE CASCADE,\r\n" + 
					"    start_lat DECIMAL(9,6) NOT NULL,\r\n" + 
	 				"    start_lng DECIMAL(9,6) NOT NULL,\r\n" + 
					"    destination_lat DECIMAL(9,6) NOT NULL,\r\n" + 
					"    destination_lng DECIMAL(9,6) NOT NULL,\r\n" + 
					"    creation_time TIMESTAMP NOT NULL,\r\n" + 
					"    is_current_route BOOLEAN DEFAULT false\r\n" + 
					")");
			
			stmt.execute("CREATE TABLE USERS_CARS(\r\n" + 
					"    user_email VARCHAR(255) NOT NULL REFERENCES USERS(email) ON DELETE CASCADE,\r\n" + 
					"    car_id int NOT NULL REFERENCES CARS(id) ON DELETE CASCADE\r\n" + 
					")");
					
			stmt.execute("CREATE TABLE STATUS(\r\n" + 
					"    car_id INT NOT NULL REFERENCES CARS(id) ON DELETE CASCADE,\r\n" + 
					"    oil_level INT,\r\n" + 
					"    disk_wiping_water INT,\r\n" + 
					"    battery INT,\r\n" + 
					"    brake_pads INT,\r\n" + 
					"    antifreeze INT,\r\n" + 
					"    engine INT,\r\n" + 
					"    transmission INT,\r\n" + 
					"    brakes INT,\r\n" + 
					"    coolant INT\r\n" + 
					")");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/

		
		ResourceConfig rc = new ResourceConfig().register(CORSResponseFilter.class).packages("de.connectedcars.backend.webservice");
	
		HttpServer webservice = JdkHttpServerFactory.createHttpServer(URI.create("http://localhost:8080/rest"), rc);
		/*
		UserManager um = UserManager.getInstance();
		um.createUser("tim.schwarze@gmail.com", "Tim Schwarze", "geheim1233");
		um.createUser("david.schwarze@gmail.com", "David Schwarze", "geheim1233");
		*/
		
		Server server = new Server("localhost", 8025, "/websockets", CarSocket.class);

        try {
            server.start();
            
            WebTarget targe = ClientBuilder.newClient().target("http://localhost:8080/rest");
            
            Car car = new Car(1, new Location(49.169173,10.080199), 0.09, 1345.5, LocalDate.now(), "VW", "Passat", (short) 2007, 5.6, 67.2, true);
            car.setStatus(new Status(null, StatusType.CRITICAL, StatusType.OK, StatusType.SUFFICIENT, StatusType.CRITICAL, StatusType.OK, StatusType.OK, StatusType.OK, StatusType.OK));
            ClientManager client = ClientManager.createClient();
            //SimulatedCar car2 = SimulatedCarFactory.createRandomizedSimulatedCar();
            //car2 = SimulatedCarFactory.createRandomizedSimulatedCar();
            //System.out.println(car2.getCar().getID());
            try {
                //client.connectToServer(new SimulatedCar(car), new URI("ws://localhost:8025/websockets/cars"));
            	
                SimulatedCarFactory.loadSimulatedCar(1);
                SimulatedCarFactory.loadSimulatedCar(2);
                SimulatedCarFactory.loadSimulatedCar(3);
                SimulatedCarFactory.loadSimulatedCar(4);;
                /*
                if(c2 != null)
                	client.connectToServer(c2, new URI("ws://localhost:8025/websockets/cars"));
                */
                //client.connectToServer(car2, new URI("ws://localhost:8025/websockets/cars"));
                /*new Thread(new Runnable() {

        			@Override
        			public void run() {
        				try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        				CarSocket.getInstance().requestData(new Request(1, new Message(MessageType.STATUS)) {

        					@Override
        					public void onResponse(MessageData responseData) {
        						
        						Gson gson = new GsonBuilder().create();
        						
        						System.out.println(gson.toJson(responseData));
        						
        					}
        					
        				});
        				
        			}
        		}).start();*/
                
                Thread.sleep(3000);
            
            /*String test = targe.path("cars/status").request().
                    accept(MediaType.TEXT_PLAIN).
                    get(Response.class)
                    .toString();
            // get() waits for the response to be ready
            System.out.println("Response received : " + test);
            */
                

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Please press a key to stop the server.");
            reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//server.stop(0);
	}
	
}
