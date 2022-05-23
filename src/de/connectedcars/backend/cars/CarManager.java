package de.connectedcars.backend.cars;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.connectedcars.backend.database.Database;
import de.connectedcars.backend.user.User;

/**
 * Manager for database access for all {@linkplain Car}.<br>
 * Database connection via: {@linkplain Database#getConnection()}<br>
 * Singleton-Pattern
 * @author David Schwarze
 */
public class CarManager {
	
	//singleton instance
	private static CarManager instance;
	
	//database connection
	private Connection connection = Database.getConnection();
	
	private CarManager() { }
	
	public static CarManager getInstance() {
		
		if(instance == null)
			instance = new CarManager();
		
		return instance;
		
	}
	
	/**
	 * Find a {@linkplain Car} in the database.
	 * @param carId unique identifier
	 * @return POJO of the car.<br>
	 * 		   Can return null, if the car was not found.
	 */
	public Car findCar(long carId) {
		
		Statement stmt = null;
		
		//SQL query to select the car
		String query = "SELECT * FROM CARS WHERE id=" + carId;
		
		//SQL query to select the status
		String query2 = "SELECT * FROM STATUS WHERE car_id=" + carId;
		
		Car car = null;
		
		try {
			
			stmt = connection.createStatement();
        
			ResultSet rs = stmt.executeQuery(query);
        	
			//If a car was found
			if(rs.next()) {
				
				//Create car with the data from the database
				car = new Car(rs.getLong("id"), 
							new Location(rs.getDouble("lat"), rs.getDouble("lng")), 
							rs.getDouble("fuellevel"), 
							rs.getDouble("mileage"), 
							rs.getDate("inspection").toLocalDate(), 
							rs.getString("brand"), 
							rs.getString("type"), 
							rs.getShort("manufacture_year"), 
							rs.getDouble("fuel_consumption"), 
							rs.getDouble("fuel_capacity"), 
							rs.getBoolean("electrical"));
				
				
				rs = stmt.executeQuery(query2);
				
				//If a status was found
				if(rs.next()) {
					
					//Create new status and set it to the new car.
					car.setStatus(new Status(
										StatusType.ofNumber(rs.getInt("oil_level")),
										StatusType.ofNumber(rs.getInt("disk_wiping_water")),
										StatusType.ofNumber(rs.getInt("battery")),
										StatusType.ofNumber(rs.getInt("brake_pads")),
										StatusType.ofNumber(rs.getInt("antifreeze")),
										StatusType.ofNumber(rs.getInt("engine")),
										StatusType.ofNumber(rs.getInt("transmission")),
										StatusType.ofNumber(rs.getInt("brakes")),
										StatusType.ofNumber(rs.getInt("coolant"))));
					
				}
					
				NavigationSystem navi = new NavigationSystem();
				
				//SQL query to select all routes of the car
				query = "SELECT * FROM ROUTES WHERE car_id=" + car.getID();
				
				rs = stmt.executeQuery(query);
				
				
				//Iterate through routes
				while(rs.next()) {
					
					Route route = new Route(
										rs.getLong("internal_id"),
										new Location(rs.getDouble("start_lat"), rs.getDouble("start_lng")), 
										new Location(rs.getDouble("destination_lat"), rs.getDouble("destination_lng")), 
										LocalDateTime.parse(rs.getString("creation_time"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.n]"))
									);
					
					boolean isCurrentRoute = rs.getBoolean("is_current_route");
					
					//Add route to navigation system
					navi.getRoutes().add(route);
					
					
					//Check if the route is the current route -> if yes: setcurrentroute
					if(isCurrentRoute)
						navi.setCurrentRoute(route);
					
				}
				
				
				//Add the filled navigation system to the new car.
				car.setNavigationSystem(navi);
					
				
				
				
			} 
			
		} catch (SQLException e ) {
	
			e.printStackTrace();
    
		} finally {
	
			if (stmt != null) { 
    	
				try {
    	
					stmt.close();
		
				} catch (SQLException e) {
		
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
    
		}
		
		return car;
		
	}
	
	/**
	 * Create a new car.
	 * @see #createCar(long, Location, double, double, LocalDate, String, String, short, double, double, NavigationSystem, Status, boolean)
	 */
	public void createCar(Car car) {
		
		createCar(car.getID(), car.getLocation(), car.getFuellevel(), car.getMileage(), car.getNextInspection(), car.getBrand(), car.getType(), car.getManufactureYear(), car.getFuelConsumption(), car.getFuelCapacity(), car.getNavigationSystem(), car.getStatus(), car.isElectrical());
		
	}
	
	/**
	 * Create a new car and add it to the database.
	 * @param id
	 * @param location
	 * @param fuellevel
	 * @param mileage
	 * @param nextInspection
	 * @param brand
	 * @param type
	 * @param manufactureYear
	 * @param fuelConsumption
	 * @param fuelCapacity
	 * @param navi
	 * @param status
	 * @param electrical
	 */
	public void createCar(long id, Location location, double fuellevel, double mileage, LocalDate nextInspection, String brand,
			String type, short manufactureYear, double fuelConsumption, double fuelCapacity, NavigationSystem navi, Status status, boolean electrical) {
		
		//Check if the data is valid
		if(dataValid(location, fuellevel, mileage, nextInspection, brand, type, manufactureYear, fuelConsumption, fuelCapacity)) {
			
			//Create a new sql query
			try (PreparedStatement statement = connection.prepareStatement("INSERT INTO CARS (id,lat,lng,fuellevel,mileage,inspection,brand,type,manufacture_year,fuel_consumption,fuel_capacity,electrical) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")) {
				
				java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse(nextInspection.toString());
				
				//Fill the sql query
				statement.setDouble(1, id);
		        statement.setDouble(2, location.getLat());
		        statement.setDouble(3, location.getLng());
		        statement.setDouble(4, fuellevel);
		        statement.setDouble(5, mileage);
		        statement.setDate(6, new  Date(date.getTime()));
		        statement.setString(7, brand);
		        statement.setString(8, type);
		        statement.setInt(9, manufactureYear);
		        statement.setDouble(10, fuelConsumption);
		        statement.setDouble(11, fuelCapacity);
		        statement.setBoolean(12, electrical);
		        
	
		        statement.executeUpdate();
 
                Statement stmt = connection.createStatement();
                
                
                //SQL query to add the routes, first part
                String query = "INSERT INTO ROUTES (internal_id,car_id,start_lat,start_lng, destination_lat,destination_lng,creation_time,is_current_route) VALUES ";
	            
               
                if(navi.getRoutes().size() > 0) {
    				
                	//Add the routes to the sql query.
    				for(int i = 0; i < navi.getRoutes().size(); i++) {
    					
    					Route route = navi.getRoutes().get(i);
    					
    					if(navi.getCurrentRoute() != null && navi.getCurrentRoute().getID() == route.getID()) {

    						query += "(" + route.getID() + ", " + id + ", " + route.getStart().getLat() + ", " + route.getStart().getLng() + ", " +  route.getDestination().getLat() + ", " + route.getDestination().getLng() + ", '" + route.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "', true)";
    					
    					} else {
    						
    						query += "(" + route.getID() + ", " + id + ", "  + route.getStart().getLat() + ", " + route.getStart().getLng() + ", " +  route.getDestination().getLat() + ", " + route.getDestination().getLng() + ", '" + route.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "', false)";
    						
    					}
    						
    					if(i + 1 < navi.getRoutes().size()) {
    						
    						query += ", ";
    						
    					} else {
    						
    						query += ";";
    						
    					}
    					
    				}
    				
    				stmt.executeUpdate(query);
    				
    			}
                
                //SQL query to add the status
                query = "INSERT INTO STATUS VALUES (" + id + ", " + 
                		StatusType.getNumber(status.getOilLevel()) + ", " + 
                		StatusType.getNumber(status.getDiskWipingWater()) + ", " + 
                		StatusType.getNumber(status.getBattery()) + ", " + 
                		StatusType.getNumber(status.getBrakePads()) + ", " + 
                		StatusType.getNumber(status.getAntifreeze()) + ", " + 
                		StatusType.getNumber(status.getEngine()) + ", " + 
                		StatusType.getNumber(status.getTransmission()) + ", " + 
                		StatusType.getNumber(status.getBrakes()) + ", " +
                		StatusType.getNumber(status.getCoolant()) + ")";
                
                stmt.executeUpdate(query);
		        
		        
		    } catch (SQLException | ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		}
		
	}
	
	/**
	 * Update an existing car.
	 * @param car the changed car
	 */
	public void updateCar(Car car) {
		
		Statement stmt = null;
		
		//SQL query to update the car
		String query = "UPDATE CARS SET fuel_capacity=" + car.getFuelCapacity() + ", fuel_consumption=" + car.getFuelConsumption() + ", manufacture_year=" + car.getManufactureYear() + ", mileage=" + car.getMileage() + ", brand='" + car.getBrand() + "', type='" + car.getType() + "', lat=" + car.getLocation().getLat() + ", lng=" + car.getLocation().getLng() + ", fuellevel=" + car.getFuellevel() + ", electrical=" + car.isElectrical() + " WHERE id=" + car.getID();

		//SQL query to remove the routes of the car
		String query3 = "DELETE FROM ROUTES WHERE car_id=" + car.getID();
		
		//SQL query to add the routes of the car, first part.
		String query4 = "INSERT INTO ROUTES (internal_id,car_id,start_lat,start_lng, destination_lat,destination_lng,creation_time,is_current_route) VALUES ";
		
		//SQL query to update the car
		String query2 = "UPDATE STATUS SET " + 
				"oil_level=" + StatusType.getNumber(car.getStatus().getOilLevel()) + ", " + 
				"disk_wiping_water=" + StatusType.getNumber(car.getStatus().getDiskWipingWater()) + ", " + 
				"battery=" + StatusType.getNumber(car.getStatus().getBattery()) + ", " + 
				"brake_pads=" + StatusType.getNumber(car.getStatus().getBrakePads()) + ", " + 
				"antifreeze=" + StatusType.getNumber(car.getStatus().getAntifreeze()) + ", " + 
				"engine=" + StatusType.getNumber(car.getStatus().getEngine()) + ", " + 
				"transmission=" + StatusType.getNumber(car.getStatus().getTransmission()) + ", " + 
				"brakes=" + StatusType.getNumber(car.getStatus().getBrakes()) + ", " +
				"coolant=" + StatusType.getNumber(car.getStatus().getCoolant()) +
				" WHERE car_id=" + car.getID();

		
		try {
			
			stmt = connection.createStatement();
        
			stmt.executeUpdate(query);
			
			stmt.executeUpdate(query2);
			
			stmt.executeUpdate(query3);
			
			if(car.getNavigationSystem().getRoutes().size() > 0) {
				
				//Add all routes to the sql query
				for(int i = 0; i < car.getNavigationSystem().getRoutes().size(); i++) {
					
					Route route = car.getNavigationSystem().getRoutes().get(i);
					
					if(car.getNavigationSystem().getCurrentRoute() != null && car.getNavigationSystem().getCurrentRoute().getID() == route.getID()) {

						query4 += "(" + route.getID() + ", " + car.getID() + ", " + route.getStart().getLat() + ", " + route.getStart().getLng() + ", " +  route.getDestination().getLat() + ", " + route.getDestination().getLng() + ", '" + route.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "', true)";
					
					} else {
						
						query4 += "(" + route.getID() + ", " + car.getID() + ", "  + route.getStart().getLat() + ", " + route.getStart().getLng() + ", " +  route.getDestination().getLat() + ", " + route.getDestination().getLng() + ", '" + route.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "', false)";
						
					}
						
					if(i + 1 < car.getNavigationSystem().getRoutes().size()) {
						
						query4 += ", ";
						
					} 
					
				}
				
				stmt.executeUpdate(query4);
				
			}
	        
			
		} catch (SQLException e ) {
	
			e.printStackTrace();
    
		} finally {
	
			if (stmt != null) { 
    	
				try {
    	
					stmt.close();
		
				} catch (SQLException e) {
		
					e.printStackTrace();
				} 
			}
    
		}
		
	}
	
	/**
	 * Remove a car from the database
	 * @param carId of the car, which should be removed
	 * @return true, if the car was removed<br>
	 * 		   false, if the car could not be removed
	 */
	public boolean removeCar(long carId) {
		
		Statement stmt = null;
		
		//SQL query to remove the car
		String query = "DELETE FROM CARS WHERE id=" + carId;
		
		
		try {
			
			stmt = connection.createStatement();
			
			//Affected rows
			int rows = stmt.executeUpdate(query);
			
			//If no rows were affected -> car was not removed -> return false
			if(rows == 0)
				return false;
			else
				return true;
			
		} catch (SQLException e ) {
	
			return false;
    
		} finally {
	
			if (stmt != null) { 
    	
				try {
    	
					stmt.close();
		
				} catch (SQLException e) {
		
					e.printStackTrace();
				} 
			}
    
		}
		
	}
	
	/**
	 * Check if the database contains the car.
	 * @param id of the car
	 * @return true, if the car is available
	 * 		   false, if the car is not available
	 */
	public boolean containsCar(long id) {
		
		Statement stmt = null;
		
		//SQL query to select the number of rows that contain the carid. Normally it should be 0 or 1.
		String query = "SELECT COUNT(*) AS total FROM CARS WHERE id=" + id;
		
		int total = 0;

		try {
			
			stmt = connection.createStatement();
        
			ResultSet rs = stmt.executeQuery(query);
			
			//Get the number of rows that contains the carid.
			rs.next();
			total = rs.getInt("total");
			
			
		} catch (SQLException e ) {
	
			e.printStackTrace();
    
		} finally {
	
			if (stmt != null) { 
    	
				try {
    	
					stmt.close();
		
				} catch (SQLException e) {
		
					e.printStackTrace();
				} 
			}
    
		}
		
		return (total > 0) ? true : false;
		
	}
	
	/**
	 * Get all cars of the database.
	 * @return list with all cars
	 */
	public List<Car> getCars() {
		
		Statement stmt = null;
		
		//SQL query to select all car.
		String query = "SELECT * FROM CARS";
		
		List<Car> cars = new ArrayList<>();
		
		try {
			
			stmt = connection.createStatement();
        
			ResultSet rs = stmt.executeQuery(query);
        	
			//Iterate through cars
			while(rs.next()) {
				
				//Fill the new car with the db data
				Car car = new Car(rs.getLong("id"), 
								new Location(rs.getDouble("lat"), rs.getDouble("lng")), 
								rs.getDouble("fuellevel"), rs.getDouble("mileage"), 
								rs.getDate("inspection").toLocalDate(), 
								rs.getString("brand"), 
								rs.getString("type"), 
								rs.getShort("manufacture_year"), 
								rs.getDouble("fuel_consumption"), 
								rs.getDouble("fuel_capacity"), 
								rs.getBoolean("electrical"));
				
				//Add car to return list
				cars.add(car);
				
				NavigationSystem navi = new NavigationSystem();
				
				//SQL query to select all routes of the current car
				query = "SELECT * FROM ROUTES WHERE car_id=" + car.getID();
				
				ResultSet rs2 = connection.createStatement().executeQuery(query);
				
				//Iterate through the routes
				while(rs2.next()) {
					
					//Fill the route with the db data
					Route route = new Route(
										rs2.getLong("internal_id"),
										new Location(rs2.getDouble("start_lat"), rs2.getDouble("start_lng")), 
										new Location(rs2.getDouble("destination_lat"), rs2.getDouble("destination_lng")), 
										LocalDateTime.parse(rs2.getString("creation_time"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.n]"))
									);
					
					boolean isCurrentRoute = rs2.getBoolean("is_current_route");
					
					//Add route to navigation system
					navi.getRoutes().add(route);
					
					//Check if the car is the current route, if yes -> setcurrentroute
					if(isCurrentRoute)
						navi.setCurrentRoute(route);
					
				}
				
				//SQL query to select the status of the current car
				query = "SELECT * FROM STATUS WHERE car_id=" + car.getID();
				
				rs2 = connection.createStatement().executeQuery(query);
				
				//If status was found
				if(rs2.next()) {
					
					//Create status and set it to the current car
					car.setStatus(new Status(
										StatusType.ofNumber(rs2.getInt("oil_level")),
										StatusType.ofNumber(rs2.getInt("disk_wiping_water")),
										StatusType.ofNumber(rs2.getInt("battery")),
										StatusType.ofNumber(rs2.getInt("brake_pads")),
										StatusType.ofNumber(rs2.getInt("antifreeze")),
										StatusType.ofNumber(rs2.getInt("engine")),
										StatusType.ofNumber(rs2.getInt("transmission")),
										StatusType.ofNumber(rs2.getInt("brakes")),
										StatusType.ofNumber(rs2.getInt("coolant"))));
						
					
				}
				
				//Set the navigation sytem to the current car.
				car.setNavigationSystem(navi);
					
				
				
				
			} 
			
		} catch (SQLException e ) {
	
			e.printStackTrace();
    
		} finally {
	
			if (stmt != null) { 
    	
				try {
    	
					stmt.close();
		
				} catch (SQLException e) {
		
					e.printStackTrace();
				} 
			}
    
		}
		
		return cars;
		
	}
	
	
	
	public boolean dataValid(Location location, double fuellevel, double mileage, LocalDate nextInspection, String brand,
			String type, short manufactureYear, double fuelConsumption, double fuelCapacity) {
		
		if(location != null && nextInspection != null && !brand.isEmpty() && !type.isEmpty() && manufactureYear >= 1885 && fuelConsumption > 0.0 && fuelCapacity > 0.0)
			return true;
		
		return false;
		
	}
	
}
