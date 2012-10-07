package dat;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import dat.Algorithm;
import dat.Message;
import dat.network.NodeAddress;
import dat.utils.TypedMap;

/**
 * 
 * A network node where one or more {@link Algorithm} execute.
 * 
 * @author Pablo Chacin
 *
 */
public interface Node {

	/**
	 * Constructs and sends a {@link Message}. The id and source are added by
	 * the node to the resulting node.
	 * 
	 * @param destination
	 * @param id
	 * @param type
	 * @param attributes
	 * @throws DatException
	 */
	public void sendMessage(NodeAddress destination, String id,String type,TypedMap attributes) throws DatException;
	
	/**
	 * Sends a {@link Message} constructed by the algorithm to a destination {@link NodeAddress}.
	 * 
	 * The sender is responsible for constructing and providing a Message or a subclass of Message.
	 * 
	 * If the algorithms sets the algorithm or sender or destination fields of the Message, they
	 * will be ignored and overridden before sending the message.
     *	
	 * @param destination
	 * @param message
	 * @throws DatException
	 */
	public void sendMessage(NodeAddress destination, Message message) throws DatException;
	
	/**
	 * Convenience method to send a Message subclass to an group of nodes. 
	 * 
	 * See {@link #sendMessage(NodeAddress, Message)} for details.
	 *  
	 * @param destination
	 * @param id
	 * @param type
	 * @param attributes
	 * @throws DatException
	 */
	public void sendMessage(List<NodeAddress> destination, String id,String type, TypedMap attributes) throws DatException;
	
	/**
	 * Convenience method to send a custom Message 
	 * @param destination
	 * @param message
	 * @throws DatException
	 */
	public void sendMessage(List<NodeAddress>  destination, Message message) throws DatException;

	
	/**
	 * Returns the {@link InetAddress} of the node.
	 * 
	 * @return
	 */
	
	public NodeAddress getAddress();
	
	
	/**
	 * Resolves the given String to a NodeAddress
	 * 
	 * @param address
	 * @return
	 * @throws DatException
	 */
	public NodeAddress resolve(String address) throws DatException;	
	
	/**
	 * Returns the id associated with this node
	 * 
	 * @return the UUID of the node
	 */
	public UUID getID();
	
	/**
	 * Schedules the triggering of an event after a given delay. When that time comes, the 
	 * {@link Algorithm#handleEvent(Event event)} method is invoked, passing the attributes
	 * and the event type.
	 * 
	 * @param id
	 * @param delay
	 * @param type
	 * @param attributes
	 */
	public void scheduleEvent(String id,long delay,String type,TypedMap attributes);
	
	/**
	 * Convenience method to allow the algorithm to schedule a custom Event subclass
	 * 
	 * @param event
	 */
	public void scheduleEvent(Event event);
	
	/**
	 * Convenience method to schedule an event without Attributes (see {@link #scheduleEvent(String, long, String, TypedMap)})
	 * 
	 * @param id
	 * @param delay
	 * @param type
	 */
	public void scheduleEvent(String id,long delay,String type);
	
	
	/**
	 * Cancels an event given its id
	 * 
	 * @param id
	 */
	public void cancelEvent(String id);
	
	
	/**
	 * Gets the current time of the node. This method must be used instead of Java's 
	 * {@link System#currentTimeMillis()} method because the actual execution time may
	 * be affected by tracing, debugging, etc.
	 * 
	 * @return the current local time;
	 */
	public long getTime();
	
	
	/**
	 * Returns the configuration parameters passed to this algorithm. 
	 * 
	 * @return a TypedMap with the configuration parameters
	 */
	public Configuration getParameters();
	
	
		
	/**
	 * Returns a previously registered algorithm casted to a given interface
	 * @param name the name under
	 * @param interfaces
	 * @return the {@link Algorithm} or null, in none has been registered under this name
	 */
	public Object getAlgorithm(String name,Class...interfaces);
		
	
	/**
	 * Returns the Logger associated with this algorithm's instance
	 * 
	 * @return a reference to a Logger
	 */
	public Logger getLog();
}
