package de.connectedcars.backend.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import de.connectedcars.backend.messaging.util.ConfigeredGson;
import de.connectedcars.backend.user.User;
import de.connectedcars.backend.user.UserManager;
import de.connectedcars.backend.user.session.UserSession;
import de.connectedcars.backend.user.session.UserSessionManager;

/**
 * Is used to authenticate users.<br>
 * Part of the webservice.
 * 
 * @author David Schwarze
 *
 */
@Path("auth")
public class AuthenticationService {

	private UserSessionManager userSessionManager = UserSessionManager.getInstance();
	
	private UserManager userManager = UserManager.getInstance();
	
	private Gson gson = ConfigeredGson.get();
	
	/**
	 * Login to the server, to get access to all services.
	 * 
	 * @param email of the user
	 * @param password of the user
	 * @return {@linkplain ResponseWS} in form of a json string.
	 */
	@GET
	@Path("login")
	@Produces(MediaType.TEXT_PLAIN)
	public String login(@QueryParam("email") String email, @QueryParam("password") String password) {
		
		try {
		
			User user = userManager.findUser(email);
			
			//If user was found
			if(user != null) {
				
				//If password is valid
				if(user.getPassword().equals(password)) {
					
					//Create new user session.
					UserSession session = userSessionManager.createSession(user);
					
					//Return ok-staus and the new sessionid
					return ResponseWS.buildPlain(ResponseWS.OK, session.getSessionID());
				
				} else {
					
					return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
					
				}
				
			} else {
				
				return ResponseWS.build(ResponseWS.NOT_FOUND);
				
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
		
	}
	
	/**
	 * Logout from the server. UserSession will be destoryed.
	 * @param sessionID
	 * @return {@linkplain ResponseWS} in form of a json string.
	 */
	@GET
	@Path("logout")
	public String logout(@QueryParam("sessionid") String sessionID) {
		
		try {
			
			//Remove user session
			userSessionManager.removeSession(sessionID);
			
			return ResponseWS.build(ResponseWS.OK);
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
		
	}
	
	/**
	 * Register new user and create a new user session.
	 * @param email of the user
	 * @param name of the user
	 * @param password of the user
	 * 
	 * @return {@linkplain ResponseWS} in form of a json string.
	 */
	@GET
	@Path("register")
	@Produces(MediaType.TEXT_PLAIN)
	public String register(@QueryParam("email") String email, @QueryParam("name") String name, @QueryParam("password") String password) {
		
		try {
		
			User user = userManager.createUser(email, name, password);
			
			//If user could be crated
			if(user != null)  {
				
				UserSession session = userSessionManager.createSession(user);
				
				//Returns new sessionid
				return ResponseWS.buildPlain(ResponseWS.OK, session.getSessionID());
				
			} else {
				
				//Only return already done, because the validation of the data is checked on the frontend side.
				return ResponseWS.build(ResponseWS.ALREADY_DONE);
				
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
	}
	
	/**
	 * Get all registered users.
	 * @return
	 */
	@GET
	@Path("users")
	public String getUsers() {
		
		try {
			
			return ResponseWS.build(ResponseWS.OK, gson.toJson(userManager.getUsers()));
			
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
	}
	
}
