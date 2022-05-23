package de.connectedcars.backend.messaging.data;

/**
 * Data is used if a {@linkplain de.connectedcars.extern.cars.SimulatedCar} disconnects from {@linkplain de.connectedcars.backend.socket.CarSocket}.<br>
 * Data is empty because there is no data send and it is only used for {@linkplain de.connectedcars.backend.messaging.MessageDeserializer}.
 * @author David
 *
 */
public class CloseData extends MessageData { }
