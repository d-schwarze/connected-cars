package de.connectedcars.backend.id;

/**
 * Interface to register the objects which implement this for an {@linkplain IdGenerator}.
 * @author David
 *
 */
public interface ID {

	/**
	 * For the {@linkplain IdGenerator}.
	 * @return the generated id by an {@linkplain IdGenerator}
	 */
	long getID();
	
}
