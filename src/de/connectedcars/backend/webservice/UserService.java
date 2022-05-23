package de.connectedcars.backend.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;

import de.connectedcars.backend.messaging.util.ConfigeredGson;
import de.connectedcars.backend.user.User;
import de.connectedcars.backend.user.UserManager;
import de.connectedcars.backend.user.session.UserSession;
import de.connectedcars.backend.user.session.UserSessionManager;

/**
 * Is used to to get data from the user(s) or change him.<br>
 * Part of the webservice.
 * @author David Schwarze
 *
 */
@Path("users")
public class UserService {

	private UserSessionManager userSessionManager = UserSessionManager.getInstance();
	
	private UserManager userManager = UserManager.getInstance();
	
	private Gson gson = ConfigeredGson.get();
	
	/**
	 * Get a user by his session.
	 * @param sessionid of the usersession
	 * @return {@linkplain ResponseWS} in form of a json string.
	 */
	@GET
	@Path("get")
	public String getUser(@QueryParam("sessionid") String sessionid) {
		
		try {
			
			UserSession session = userSessionManager.getSession(sessionid);
			
			//User is logged in
			if(session != null) {
				
				//Get the user of the usersession
				User user = session.getUser();
				
				return ResponseWS.build(ResponseWS.OK, gson.toJson(user));
				
			} else {
				
				//User not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
		
	}
	
	/**
	 * Update a user. Only his password and his name can be updated.
	 * @param pw new password, can be null
	 * @param name new name, can be null
	 * @param sessionid of the usersession
	 * @return {@linkplain ResponseWS} in form of a json string.
	 */
	@POST
	@Path("update")
	public String update(@QueryParam("pw") String pw, @QueryParam("name") String name, @QueryParam("sessionid") String sessionid) {
		
		try {
			
			UserSession session = userSessionManager.getSession(sessionid);
			
			//User is logged in
			if(session != null) {
				
				User user = session.getUser();
				
				//If new password is available
				if(pw != null) {
					
					//Password can not be empty
					if(pw.trim().length() > 0) {
						
						user.setPassword(pw);
					
						userManager.updateUser(user);
						
						return ResponseWS.build(ResponseWS.OK);
						
					} else {
						
						return ResponseWS.build(ResponseWS.INVAILD_DATA);
						
					}
					
				} 
				
				//If new name is available
				if(name != null) {
					
					//Name can not be empty
					if(name.trim().length() > 0) {
						
						user.setName(name);
					
						userManager.updateUser(user);
						
						return ResponseWS.build(ResponseWS.OK);
						
					} else {
						
						return ResponseWS.build(ResponseWS.INVAILD_DATA);
						
					}
					
				}  
				
				return ResponseWS.build(ResponseWS.OK);
				
			} else {
				
				//Not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
	}
	
	/**
	 * Remove a user from the database.
	 * @param sessionid of ther usersession
	 * @return {@linkplain ResponseWS} in form of a json string.
	 */
	@POST
	@Path("remove")
	public String removeUser(@QueryParam("sessionid") String sessionid) {
		
		try {
			
			UserSession session = userSessionManager.getSession(sessionid);
			
			//If user is logged in
			if(session != null) {
				
				User user = session.getUser();
				
				//Remove usersession (logout)
				userSessionManager.removeSession(sessionid);
				
				//Remove user from database
				userManager.removeUser(user.getEmail());
				
				return ResponseWS.build(ResponseWS.OK);
				
			} else {
				
				//User not logged in
				return ResponseWS.build(ResponseWS.AUTHENTICATION_FAILED);
				
			}
			
		} catch(Exception ex) {
			
			return ResponseWS.buildError(ex.getMessage());
			
		}
		
	}
}
