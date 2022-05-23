package de.connectedcars.backend.cars;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * POJO for a location. Used for routes or car positions.<br>Format: latlng
 * @author David
 *
 */
public class Location {

	private double lat;
	
	private double lng;
	
	//Is not serialized with Gson.
	private transient NumberFormat nf;
	
	public Location(double lat, double lng) {
		//Database format
		nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setMaximumFractionDigits(6);
		
		this.setLat(lat);
		this.setLng(lng);
		
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = Double.valueOf(nf.format(lat));
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = Double.valueOf(nf.format(lng));
	}
	
	
	
}
