package de.connectedcars.backend.webservice;

/**
 * Response that is send from the webservice to the client.<br>
 * It is send as json string.
 * @author David Schwarze
 *
 */
public class ResponseWS {

	/*
	 * All differnt response types
	 */
	public final static int OK = 0;
	
	public final static int AUTHENTICATION_FAILED = 1;
	
	public final static int FATAL_ERROR = 2;
	
	public final static int FAILED = 3;
	
	public final static int UP_TO_DATE = 4;
	
	public final static int DATABASE = 5;
	
	public final static int NOT_FOUND = 6;
	
	public final static int ALREADY_DONE = 7;
	
	public final static int INVAILD_DATA = 8;
	
	public final static int AUTHORIZATION_FAILED = 9;
	
	private ResponseWS() { }
	
	/**
	 * Build a new response
	 * @param status of the response
	 * @param jsonData of the response, has to be a java object that is serialized to json
	 * @return json
	 */
	public static String build(int status, String jsonData) {
		
		return "{ \"status\": " + status + ",\"data\":" + jsonData +   "}";
		
	}

	/**
	 * Build a new response without any data.
	 * @param status of the response
	 * @return json
	 */
	public static String build(int status) {
		
		return "{ \"status\": " + status + "}";
		
	}
	
	/**
	 * Build a new response
	 * @param status of the response
	 * @param msg of the response
	 * @return json
	 */
	public static String buildPlain(int status, String msg) {
		
		String data = "\"" + msg + "\"";
		
		return build(status, data);
		
	}
	
	/**
	 * Build an exception response
	 * @param msg of the exception
	 * @return json
	 */
	public static String buildError(String msg) {
		
		return buildPlain(FATAL_ERROR, msg);
		
	}
	
	
}
