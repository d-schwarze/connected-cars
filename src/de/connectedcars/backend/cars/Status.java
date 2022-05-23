package de.connectedcars.backend.cars;

/**
 * Status of a {@linkplain Car}.<br>
 * Contains a variety of different components.<br> 
 * Not all components have to be set. For instance an electric car does not need  a transmission-status.
 * @author David Schwarze
 *
 */
public class Status {

	private StatusType oilLevel;
	
	private StatusType diskWipingWater;
	
	private StatusType battery;
	
	private StatusType brakePads;
	
	private StatusType antifreeze;
	
	private StatusType engine;
	
	private StatusType transmission;
	
	private StatusType brakes;
	
	private StatusType coolant;

	public Status(StatusType oilLevel, StatusType diskWipingWater, StatusType battery, StatusType brakePads,
			StatusType antifreeze, StatusType engine, StatusType transmission, StatusType brakes, StatusType coolant) {
		
		this.oilLevel = oilLevel;
		this.diskWipingWater = diskWipingWater;
		this.battery = battery;
		this.brakePads = brakePads;
		this.antifreeze = antifreeze;
		this.engine = engine;
		this.transmission = transmission;
		this.brakes = brakes;
		this.coolant = coolant;
	}

	public StatusType getOilLevel() {
		return oilLevel;
	}

	public void setOilLevel(StatusType oilLevel) {
		this.oilLevel = oilLevel;
	}

	public StatusType getDiskWipingWater() {
		return diskWipingWater;
	}

	public void setDiskWipingWater(StatusType diskWipingWater) {
		this.diskWipingWater = diskWipingWater;
	}

	public StatusType getBattery() {
		return battery;
	}

	public void setBattery(StatusType battery) {
		this.battery = battery;
	}

	public StatusType getBrakePads() {
		return brakePads;
	}

	public void setBrakePads(StatusType brakePads) {
		this.brakePads = brakePads;
	}

	public StatusType getAntifreeze() {
		return antifreeze;
	}

	public void setAntifreeze(StatusType antifreeze) {
		this.antifreeze = antifreeze;
	}

	public StatusType getEngine() {
		return engine;
	}

	public void setEngine(StatusType engine) {
		this.engine = engine;
	}

	public StatusType getTransmission() {
		return transmission;
	}

	public void setTransmission(StatusType transmission) {
		this.transmission = transmission;
	}

	public StatusType getBrakes() {
		return brakes;
	}

	public void setBrakes(StatusType brakes) {
		this.brakes = brakes;
	}

	public StatusType getCoolant() {
		return coolant;
	}

	public void setCoolant(StatusType coolant) {
		this.coolant = coolant;
	}
}
