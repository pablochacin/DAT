package dat.network;

import java.util.UUID;

import dat.DatException;
import dat.Message;


public interface Transport {
	
	/**
	 * Connects to the network, making it accessible to other nodes.
	 * 
	 * @throws DatException
	 */
	public void connect() throws DatException;
	
	
	/**
	 * Disconnect from the network, making it unaccessible for other nodes.
	 * 
	 * @throws DatException
	 */
	public void disconnect() throws DatException;
	
	
	/**
	 * Initializes the transport. Used to acquire resources.
	 * 
	 * @throws DatException if there is a problem during the initialization.
	 */
	
	/**
	 * Sends message over the wire 
	 */
	public void sendNetworkMessage(NodeAddress destination, Message message) throws TransportException ;

	/**
	 * Get a node address that allows contacting the Node with the given id using this transport.
	 * 
	 * @param  id and {@link UUID} that identifies the node
	 * 
	 * @return
	 */
	public NodeAddress getAddress();
		
	
	/**
	 * Resolves a String containing a transport specific externalization of a NodeAddress,
	 * to the corresponding NodeAddress
	 *  
	 * @param address a String with an externalized form of an Address
	 * @return the NodeAddress 
	 * 
	 * @throws TransportException if the String can't be resolved (for example, 
	 *         it has an invalid format)
	 */
	public NodeAddress resolve(String address) throws TransportException;
	
	/**
	 * Sets a handler to handle transport related events
	 * @param handler
	 */
	public void setHandler(TransportHandler handler);
}