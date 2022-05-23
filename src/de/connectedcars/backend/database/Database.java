package de.connectedcars.backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Derby database connection provider.
 * @author David
 *
 */
public class Database {

	private static Connection connection;
	
	static {
		
		try {
			
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		
		String dbURL = "jdbc:derby:database;create=true";
		 
		try {
			
			//Get the connection (if db does not exist -> create)
			connection = DriverManager.getConnection(dbURL);
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Get a connection to the database.
	 * @return db connection
	 */
	public static Connection getConnection() {
		
		return connection;
		
	}
	
	public static void createTables() {
		
		Statement stmt = null;
		
		try {
			
			stmt = connection.createStatement();
			try {
			stmt.execute("DROP TABLE STATUS");
			
			stmt.execute("DROP TABLE USERS_CARS");
			
			stmt.execute("DROP TABLE ROUTES");
			
			stmt.execute("DROP TABLE CARS");
			
			stmt.execute("DROP TABLE USERS");
			} catch(Exception ex) {}
			
		
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
			
			e1.printStackTrace();
			
		}
	}
	
}
