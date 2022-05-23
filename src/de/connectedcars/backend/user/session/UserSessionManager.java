package de.connectedcars.backend.user.session;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.connectedcars.backend.user.User;

/**
 * Manager for the {@linkplain UserSession}. A user has only access to the full webservice, if he is registered with a session at this manager.<br>
 * Singleton-Pattern
 * @author David Schwarze
 *
 */
public class UserSessionManager {
	//Singleton instance
	private static UserSessionManager instance;
	
	private List<UserSession> sessions = new ArrayList<>();
	
	private UserSessionManager() {
		
	}
	
	public static UserSessionManager getInstance() {
		
		if(instance == null)
			instance = new UserSessionManager();
		
		return instance;
		
	}
	
	/**
	 * Get the usersession of a specific sessionid.
	 * @param sessionID of the session
	 * @return usersesseion with the same id<br>
	 * 		   null, if the session was not found
	 */
	public UserSession getSession(String sessionID) {
		
		for(UserSession session : sessions) {
			
			if(session.getSessionID().equals(sessionID))
				return session;
			
		}
		
		return null;
		
	}
 	
	/**
	 * Create a new usersession (login).
	 * @param user who is linked to the session
	 * @return the new usersession
	 */
	public UserSession createSession(User user) {
		
		//User is not registered
		if(!hasSession(user)) {
			
			//Create a new usersession
			UserSession session = new UserSession(UUID.randomUUID().toString(), user);
			
			sessions.add(session);
			
			return session;
			
		} else {
			//If the user is already registered, return his session.
			return getSession(user);
			
		}
		
	}
	
	/**
	 * Check whether the user has already a session.
	 * @param user who should be checked
	 * @return true: session available<br>
	 * 		   false: session not available
	 */
	public boolean hasSession(User user) {
		
		for(UserSession session : sessions) {
			
			if(session.getUser().equals(user))
				return true;
			
		}
		
		return false;
		
	}
	
	/**
	 * Remove a usersession from the manager (logout)
	 * @param sessionID of the usersession
	 */
	public void removeSession(String sessionID) {
		
		//Get the session
		UserSession session = getSession(sessionID);
		
		//If the session is available
		if(session != null) {
			
			this.sessions.remove(session);
			
		}
	}
	
	/**
	 * Get the usersession of a user.
	 * @param user
	 * @return the usersession of the user<br>
	 * 		   null, if no usersession was found
	 */
	private UserSession getSession(User user) {
		
		for(UserSession session : sessions) {
			
			if(session.getUser().equals(user))
				return session;
			
		}
		
		return null;
		
	}
	
}
