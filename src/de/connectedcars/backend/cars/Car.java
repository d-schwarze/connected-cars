package de.connectedcars.backend.cars;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import de.connectedcars.backend.id.ID;

/**
 * POJO of the cars, which are stored in the database.
 * 
 * @author David
 *
 */
public class Car implements ID {

	private long carID;
	
	private Location location;
	
	private double fuellevel;
	
	private double mileage;
	
	private LocalDate nextInspection;
	
	private String brand;
	
	private String type;
	
	private short manufactureYear;
	
	private double fuelConsumption;
	
	private double fuelCapacity;
	
	private NavigationSystem navigationSystem;
	
	private Status status;
	
	private boolean electrical;
	
	//Transient, because it is not stored by Gson in a json format.
	private transient NumberFormat nf;
	
	
	public Car(long carID, Location location, double fuellevel, double mileage, LocalDate nextInspection, String brand,
			String type, short manufactureYear, double fuelConsumption, double fuelCapacity, boolean electrical) {
		this(carID, location, fuellevel);
		
		this.mileage = mileage;
		this.nextInspection = nextInspection;
		this.brand = brand;
		this.type = type;
		this.manufactureYear = manufactureYear;
		this.fuelConsumption = fuelConsumption;
		this.fuelCapacity = fuelCapacity;
		this.electrical = electrical;
	}

	public Car(long carID, Location location, double fuellevel) {
		this.nf = NumberFormat.getNumberInstance(Locale.US);
		
		this.carID = carID;
		this.location = location;
		this.navigationSystem = new NavigationSystem();
		this.setFuellevel(fuellevel);
	}

	@Override
	public long getID() {
		return carID;
	}

	public void setID(long carID) {
		this.carID = carID;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public double getFuellevel() {
		return fuellevel;
	}

	public void setFuellevel(double fuellevel) {
		//Database format.
		nf.setMaximumFractionDigits(2);
		
		this.fuellevel = Double.valueOf(nf.format(fuellevel));
		
	}
	
	public double getMileage() {
		return mileage;
	}

	public void setMileage(double mileage) {
		//Database format.
		nf.setMaximumFractionDigits(3);
		
		this.mileage = Double.valueOf(nf.format(mileage));
	}

	public LocalDate getNextInspection() {
		return nextInspection;
	}

	public void setNextInspection(LocalDate nextInspection) {
		this.nextInspection = nextInspection;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public short getManufactureYear() {
		return manufactureYear;
	}

	public void setManufactureYear(short manufactureYear) {
		//Manufacture year has to be in the past.
		if(manufactureYear <= LocalDate.now().getYear())
			this.manufactureYear = manufactureYear;
	}

	public double getFuelConsumption() {
		return fuelConsumption;
	}

	public void setFuelConsumption(double fuelConsumption) {
		//Database format.
		nf.setMaximumFractionDigits(1);
		
		this.fuelConsumption = Double.valueOf(nf.format(fuelConsumption));
	}

	public double getFuelCapacity() {
		return fuelCapacity;
	}

	public void setFuelCapacity(double fuelCapacity) {
		//Database format.
		nf.setMaximumFractionDigits(1);
		
		this.fuelCapacity = Double.valueOf(nf.format(fuelCapacity));
	}

	public NavigationSystem getNavigationSystem() {
		return navigationSystem;
	}

	public void setNavigationSystem(NavigationSystem navigationSystem) {
		this.navigationSystem = navigationSystem;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public boolean isElectrical() {
		return electrical;
	}

	public void setElectrical(boolean electrical) {
		this.electrical = electrical;
	}

	@Override
	public String toString() {
		return "Car [carID=" + carID + ", location=" + location + ", fuellevel=" + fuellevel + ", mileage=" + mileage
				+ ", nextInspection=" + nextInspection + ", brand=" + brand + ", type=" + type + ", manufactureYear="
				+ manufactureYear + ", fuelConsumption=" + fuelConsumption + ", fuelCapacity=" + fuelCapacity
				+ ", navigationSystem=" + navigationSystem + ", status=" + status + ", electircal=" + electrical + "]";
	}
}
