package de.connectedcars.backend.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.connectedcars.backend.cars.Car;
import de.connectedcars.backend.cars.CarManager;
import de.connectedcars.backend.database.Database;

/**
 * Manager for database access for all {@linkplain User}.<br>
 * Database connection via: {@linkplain Database#getConnection()}<br>
 * Singleton-Pattern
 * @author David Schwarze
 */
public class UserManager {
	
	//Singleton reference
	private static UserManager instance;
	
	//Database connection
	private Connection connection = Database.getConnection();
	
	private CarManager carManager = CarManager.getInstance();
	
	private UserManager() { }
	
	//Get singleton-instance
	public static UserManager getInstance() {
		
		if(instance == null)
			instance = new UserManager();
		
		return instance;
		
	}
	
	/**
	 * Find a {@linkplain User} in the database.
	 * @param email unique identifier
	 * @return POJO of the user.<br>
	 * 		   Can return null, if the user was not found.
	 */
	public User findUser(String email) {
		
		Statement stmt = null;
		
		//SQL query for the user.
		String query = "SELECT * FROM USERS WHERE email='" + email + "'";
		
		User user = null;
		
		if(email != null) {
		
			try {
				
				stmt = connection.createStatement();
	        
				ResultSet rs = stmt.executeQuery(query);
	        
	        	
				//If the user was found true, if not false.
				if(rs.next()) {
					
					//Fill the pojo with the database data.
					user = new User(rs.getString("email"), rs.getString("name"), rs.getString("password"));
					
					//SQL query for the binded cars.
					query = "SELECT car_id FROM USERS_CARS WHERE user_email='" + email + "'";
					
					rs = stmt.executeQuery(query);
					
					//Iterate through founded cars.
					while(rs.next()) {
						
						//Get the Car-POJO via the carManager (USERS_CARS only contains the car ids).
						Car car = carManager.findCar(rs.getLong("car_id"));
						
						//If the car was found.
						if(car != null) {
							
							user.getOwnCars().add(car);
							
						}
						
					}
					
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

        return user;
		
	}
	
	/**
	 * Check if the database contains a user with the given email.
	 * @param email
	 * @return true, if the email is already in use<br>
	 * 		   false, if the email is available
	 */
	public boolean containsUser(String email) {
		
		Statement stmt = null;
		
		//SQL query to select the number of rows where the email is found. Normally it should be 1 or 0.
		String query = "SELECT COUNT(*) AS total FROM USERS WHERE email='" + email + "'";
		
		int total = 0;

		try {
			
			stmt = connection.createStatement();
        
			ResultSet rs = stmt.executeQuery(query);
        
			//Get the number of rows where the email appears.
			rs.next();
			total = rs.getInt("total");
			
			
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
		
		return (total > 0) ? true : false;
		
	}
	
	/**
	 * Creates a new user.
	 * @param email unique identifier of the user
	 * @param name must not be empty
	 * @param password must not be empty
	 * @return new {@linkplain User}<br>
	 * 		   null, if the email is already in use or the data is not valid
	 */
	public User createUser(String email, String name, String password) {
		
		if(!containsUser(email)) {
			
			User user = new User(email, name, password);
			
			Statement stmt = null;
			
			String query = "INSERT INTO USERS VALUES ('" + email + "', '" + name  + "', '" + password + "')";
			

			try {
				
				stmt = connection.createStatement();
	        
				stmt.executeUpdate(query);
				
				
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
			
			return user;
			
		} else {
			
			return null;
			
		}
	}
	
	/**
	 * Removes a user from the database.
	 * @param email of the user
	 */
	public void removeUser(String email) {
		
		Statement stmt = null;
		
		String query = "DELETE FROM USERS WHERE email='" + email + "'";
		
		String query2 = "DELETE FROM USERS_CARS WHERE user_email='" + email + "'";
		
		try {
			
			stmt = connection.createStatement();
        
			stmt.executeUpdate(query);
			
			stmt.executeUpdate(query2);
			
			
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
		
	}
	
	/**
	 * Updates the user data in the database.
	 * @param user with the new data
	 */
	public void updateUser(User user) {
		
		Statement stmt = null;
		
		//Update USERS-table
		String query1 = "UPDATE USERS SET name='" + user.getName() + "', password='" + user.getPassword() + "' WHERE email='" + user.getEmail() + "'";
		
		//Remove all binded cars
		String query2 = "DELETE FROM USERS_CARS WHERE user_email='" + user.getEmail() + "'";

		//Add binded cars; first part
		String query3 = "INSERT INTO USERS_CARS VALUES ";
		
		try {
			
			stmt = connection.createStatement();
        
			stmt.executeUpdate(query1);
	        
			stmt.executeUpdate(query2);
			
			
			//Complete query3
			if(user.getOwnCars().size() > 0) {
				
				for(int i = 0; i < user.getOwnCars().size(); i++) {
					
					Car car = user.getOwnCars().get(i);
					
					query3 += "('" + user.getEmail() + "', " + car.getID() + ")";
					
					if(i + 1 < user.getOwnCars().size()) {
						
						query3 += ", ";
						
					}
					
				}
				
				stmt.executeUpdate(query3);
				
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
	 * Returns all users of the database.
	 * @return list with all users
	 */
	public List<User> getUsers() {
		
		List<User> users = new ArrayList<>();
		
		Statement stmt = null;
		
		String query = "SELECT * FROM USERS";
		
		try {
			
			stmt = connection.createStatement();
        
			ResultSet rs = stmt.executeQuery(query);
        
			//Iterate through results
			while(rs.next()) {
				
				User user = new User(rs.getString("email"), rs.getString("name"), rs.getString("password"));
				
				users.add(user);
				
				query = "SELECT car_id FROM USERS_CARS WHERE user_email='" + user.getEmail() + "'";
				
				ResultSet rs2 = connection.createStatement().executeQuery(query);
				
				//Iterate through the binded cars of the current user.
				while(rs2.next()) {
					
					Car car = carManager.findCar(rs2.getLong("car_id"));
					
					if(car != null) {
						
						user.getOwnCars().add(car);
						
					}
					
				}
				
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
		
		return users;
		
	}
	
	/**
	 * Check if the user data is valid
	 * @param email for example: max.mustermann@gmail.com
	 * @param name must be not empty
	 * @param password must be not empty
	 * @return true, if the data is valid<br>
	 * 		   false, if the data is not valid
	 */
	public boolean dataValid(String email, String name, String password) {
		
		if(email != null && email.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$") && name != null && name.trim().length() > 0 && password != null && password.trim().length() > 0) {
			
			return true;
			
		}
		
		return false;
		
	}
}
