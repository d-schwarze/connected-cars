package de.connectedcars.backend.user;

import java.util.ArrayList;
import java.util.List;

import de.connectedcars.backend.cars.Car;


/**
 * POJO for the user account. Is stored in the database.<br>
 * Users are created and accessed via the {@linkplain UserManager}.
 * @author David Schwarze
 *
 */
public class User {
	
	//Unique email, for the registration of the user.
	private String email;
	
	//Can be used multiple times.
	private String name;
	
	//Not serialized into a json
	private transient String password;
	
	//Own binded cars.
	private List<Car> ownCars = new ArrayList<>();
	
	public User(String email, String name, String password) {
		
		this.email = email;
		this.name = name;
		this.password = password;
		
	}
	
	/**
	 * Check whether the user contains the car.
	 * @param car to be checked
	 * @return true, if the user contains the car<br>
	 * 		   false, if the usr does not contain the car		
	 */
	public boolean containsCar(Car car) {
		
		for(Car c : ownCars) {
			
			if(c.getID() == car.getID())
				return true;
			
		}
		
		return false;
		
	}
	
	/**
	 * Removes a car, if it was binded.
	 * @param car to be removed
	 */
	public void removeCar(Car car) {
		
		for(Car c : ownCars) {
			
			if(c.getID() == car.getID()) {
				ownCars.remove(c);
				break;
			}
			
		}
		
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Car> getOwnCars() {
		return ownCars;
	}

	public void setOwnCars(List<Car> ownCars) {
		this.ownCars = ownCars;
	}
	
	@Override
	public String toString() {
		
		return "Email: " + email + " Name: " + name + " Password: " + password;
		
	}
	
	@Override
	public boolean equals(Object o ) {
		
		if(o == this)
			return true;
		
		if(o instanceof User) {
			
			User u = (User) o;
			
			if(u.getEmail().equals(email) && u.getName().equals(name) && u.getPassword().equals(password))
				return true;
			
		}
		
		return false;
		
	}
	
	
}
