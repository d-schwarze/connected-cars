package de.connectedcars.backend.user.session;


import de.connectedcars.backend.user.User;

/**
 * UserSession for a user. It is used to authenticate the user. Only if a usersession is created, the user has full access to webservice.<br>
 * Managed by {@linkplain UserSessionManager}
 * @author David Schwarze
 *
 */
public class UserSession {

	//Unique Usersessionid, created by the manager.
	private String sessionID;
	
	//Linked user
	private User user;
	
	public UserSession(String sessionID, User user) {
		
		this.sessionID = sessionID;
		this.user = user;
		
	}
	
	public User getUser() {
		
		return user;
		
	}

	public String getSessionID() {
		return sessionID;
	}
	
	
}
