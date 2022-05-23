package de.connectedcars.backend.messaging.data;

import de.connectedcars.backend.cars.Car;

/**
 * Data is used to send the {@linkplain Car} of a {@linkplain SimulatedCar} to the {@linkplain CarSocket}.
 * @author David
 *
 */
public class CarData extends MessageData {

	private Car car;
	
	public CarData(Car car) {
		
		this.car = car;
		
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}
	
}
