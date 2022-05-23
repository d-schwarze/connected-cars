package de.connectedcars.extern.cars;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.client.ClientManager;

import de.connectedcars.backend.cars.Car;
import de.connectedcars.backend.cars.CarManager;
import de.connectedcars.backend.cars.Location;
import de.connectedcars.backend.cars.Status;
import de.connectedcars.backend.cars.StatusType;

/**
 * Create or load {@linkplain SimulatedCar}`s.
 * @author David Schwarze
 *
 */
public class SimulatedCarFactory {

	//Brands that can be used.
	private static String[] brands = { "VW", "Mercedes-Benz", "Audi", "BMW", "Porsche", "Opel", "Toyota", "Tesla", "Ford"  };
	
	//Types that can be used -> linked to the brands (2D-Array).
	private static String[][] types = {
			{ "Golf;normal", "Polo;normal", "Touran;normal", "Sharan;normal", "Käfer;normal", "Passat;normal", "Tiguan;normal", "e-Golf;electric" },
			{ "A-Klasse;normal", "E-Klasse;normal", "S-Klasse;normal", "C-Klasse;normal", "CLS;normal", "SL Roadster;sport" },
			{ "A1 Sportback;normal", "RS 3 Sportback;normal", "S4 Avant;normal", "A5 Sportback g-tron;normal", "A6 Limousine;normal", "Q8;normal", "R8 Spyder V10 RWS;sport" },
			{"1er 5-Türer;normal", "3er Gran Turismo;normal", "7er Limousine;normal", "X1;normal", "X3;normal", "M4 Coué;sport", "BMW i8 Roadster;sport" },
			{ "718 Cayman S;sport", "911 Carrea 4;sport", "Cayenne;sport", "911 GT3;sport" },
			{ "Insignia Country Tourer;normal", "Crossland X;normal", "Astra Sports Tourer;normal" },
			{ "Corolla;normal", "Auris;normal" },
			{ "Model 3;electric", "Model S;electric", "Model X;electric" },
			{ "Focus;normal", "Mustang;sport", "GT;sport", "Ranger;normal" }
	};
	
	/**
	 * Create a new simulated car with own parameters. The new simulated car will be connected to the server immediately.
	 * @param id can be set to -1 if the id should be get automatically from the db. Problem for the automatically id is, 
	 * that it could be, that many simulated cars are created and they will get all the same id, 
	 * because they are not stored into the database before the next simulated car is created.
	 * @param location
	 * @param nextInspection
	 * @param fuelLevel
	 * @param mileage
	 * @param brand
	 * @param type
	 * @param manufactureYear
	 * @param fuelConsumption
	 * @param fuelCapacity
	 * @param status
	 * @param electrical
	 * @return the new simulated car
	 * @throws DeploymentException
	 * @throws URISyntaxException
	 */
	public static SimulatedCar createSimulatedCar(long id, Location location, LocalDate nextInspection, double fuelLevel, double mileage, String brand, String type, short manufactureYear, double fuelConsumption, double fuelCapacity, Status status, boolean electrical) throws DeploymentException, URISyntaxException {
		
		Car car = null;
		
		if(id == -1) {
			
			car = new Car(getNewID(), location, fuelLevel, mileage, nextInspection, brand, type, manufactureYear, fuelConsumption, fuelCapacity, electrical);
			car.setStatus(status);
			
		} else {
			
			car = new Car(id, location, fuelLevel, mileage, nextInspection, brand, type, manufactureYear, fuelConsumption, fuelCapacity, electrical);
			car.setStatus(status);
			
		}
        
        SimulatedCar simulatedCar = new SimulatedCar(car);
        
        ClientManager client = ClientManager.createClient();
        
        client.connectToServer(simulatedCar, new URI("ws://localhost:8025/websocket/cars"));
        
        return simulatedCar;
		
	}
	
	/**
	 * Create a new randomized simulated car. The new simulated car will be connected to the server immediately.
	 * @return the new simulated car
	 * @throws DeploymentException
	 * @throws URISyntaxException
	 */
	public static SimulatedCar createRandomizedSimulatedCar(long id) throws DeploymentException, URISyntaxException {
		
		Location location = getRandomLocation();
		LocalDate nextInspection = getRandomNextInspection();
		double fuelLevel = getRandomFuelLevel();		
		double mileage = getRandomMileage();
		int index = getRandomBrand();
		String brand = brands[index];
		
		String[] typeData = getRandomType(index);
		String type = typeData[0];
		
		short manufactureYear = getRandomManufactureYear();
		
		double fuelConsumption = getRandomFuelConsumption(typeData[1]);
		
		double fuelCapacity = getRandomFuelCapacity(typeData[1]);
		
		Status status = getRandomStatus(typeData[1]);
		
		boolean electircal = typeData[1].equals("electric") ? true : false;
		
		return createSimulatedCar(id, location, nextInspection, fuelLevel, mileage, brand, type, manufactureYear, fuelConsumption, fuelCapacity, status, electircal);
		
	}
	
	/**
	 * Load a simulated car from the database. The new simulated car will be connected to the server immediately.
	 * @param id of the car
	 * @throws DeploymentException
	 * @throws URISyntaxException
	 */
	public static SimulatedCar loadSimulatedCar(long id) throws DeploymentException, URISyntaxException {
		
		CarManager carManager = CarManager.getInstance();
		
		Car car = carManager.findCar(id);
		
		if(car != null) {
			
			ClientManager client = ClientManager.createClient();
			
			SimulatedCar sc = new SimulatedCar(car);
			
			client.connectToServer(sc, new URI("ws://localhost:8025/websocket/cars"));
			
			return sc;
			
		}
		
		return null;
		
		
	}
	
	/**
	 * Load all simulated cars from the database. The new simulated cars will be connected to the server immediately.
	 * @throws DeploymentException
	 * @throws URISyntaxException
	 */
	public static void loadSimulatedCars() throws DeploymentException, URISyntaxException {
		
		CarManager carManager = CarManager.getInstance();
		
		ClientManager client = ClientManager.createClient();
		
		for(Car car : carManager.getCars()) {
	        
	        client.connectToServer(new SimulatedCar(car), new URI("ws://localhost:8025/websocket/cars"));
			
				
		}
	}
	
	/**
	 * Get a new car id, that is not used in the database.
	 * @return
	 */
	public static long getNewID() {
		
		CarManager carManager = CarManager.getInstance();
		
		long id = 0;
		
		for(Car car : carManager.getCars()) {
			
			if(car.getID() > id) {
				
				id = car.getID();
				
			}
			
		}
		
		id++;
		
		return id;
		
	}
	
	/*
	 * Methods to generate random values.
	 */
	
	private static Location getRandomLocation() {
		
		//Lng: From 8.0 to 27.0
		double lng = 8.0 + Math.random() * 9.0;
				
		//Lat: From 46.0 to 52
		double lat = 46.0 + Math.random() * 6.0;
		
		//Position is somewhere in europe
		return new Location(lat, lng);
		
	}
	
	private static LocalDate getRandomNextInspection() {
		
		LocalDate inspection = LocalDate.now();
		
		//Somewhere in the next 365 days, (copy)
		return inspection.plusDays((long)(Math.random() * 365));
		
	}
	
	private static int getRandomBrand() {
		
		return (int) Math.round((Math.random() * (brands.length - 1)));
		
	}
	
	private static String[] getRandomType(int brand) {
		
		String[] brandTypes = types[brand]; 
		
		String type = brandTypes[(int) Math.round((Math.random() * (brandTypes.length - 1)))];
		
		String[] data = type.split(";");
		
		return data;
		
	}
	
	private static double getRandomFuelLevel() {
		
		return Math.random();
		
	}
	
	private static short getRandomManufactureYear() {
		
		//1990 + max. 28 years.
		return (short) (1990 + Math.round(Math.random() * 28));
		
	}
	
	private static double getRandomFuelCapacity(String special) {
		
		if(special.equals("electric")) {
			//Electic cars have a greate fuel capacity (other unit of measurement)
			return 50 + Math.random() * 100;
			
		} else {
			
			return 30 + Math.random() * 60;
			
		}
	}
	
	private static double getRandomFuelConsumption(String special) {
		//Different car kits (electic or sport), need different fuel consumptions.
		switch(special) {
		
			case "sport":
				return 8 + Math.random() * 8;
			case "electirc":
				return 160 + Math.random() * 80;
			default:
				return 2 + Math.random() * 5;
		
		}
	}
	
	private static Status getRandomStatus(String special) {
		
		if(special.equals("electric")) {
			//Electic cars do not need all status components (for example transmission)
			return new Status(
					null, 
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom(),
					null,
					StatusType.getRandom(),
					null);
			
		} else {
		
			return new Status(
					StatusType.getRandom(), 
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom(),
					StatusType.getRandom());
			
		}
	}
	
	private static double getRandomMileage() {
		
		//Max mileage 200000km
		return Math.random() * 200000;
		
	}
}
