package dat;


/**
 * A distributed algorithm.
 * 
 * @author Pablo Chacin
 *
 */
public interface Algorithm {

	/**
	 * Handle the reception of a message. 
	 * 
	 * Algorithms can also implement methods to handle specialized message types that extend
	 * the Message class, like for example:
	 * 
	 * public void handleMessage(MyMessage message);
	 * 
	 * where MyMessage extends Message.
	 * 
	 * @param message
	 */
	public void handleMessage(Message message);
	
	/**
	 * Handle the occurrence of an scheduled event
	 * 
	 * Algorithms can also implement methods to handle specialized even types that extend
	 * the Event class, like for example:
	 * 
	 * public void handleEvent(MyEvent event)
	 * 
	 * where MyEvent extends Event. 
	 * 
	 * 
	 * @param name
	 * @param attributes
	 */
	public void handleEvent(Event event);
	
	/**
	 * Initiates the algorithm. Called when the algorithm is registered in a node.
	 * @param node
	 */
	public void init(Node node);
}
