package dat.network;

import dat.Message;


/**
 * 
 * Defines the interface of the handler of events related to a {@link Transport}
 * 
 * @author Pablo Chacin
 *
 */
public interface TransportHandler {

	/**
	 * Handles a message received from the transport
	 * 
	 * @param message
	 */
	public void dispatchMessage(Message message);
	
	/**
	 * Handles an exception occurred in the transport. Transport is no longer
	 * active (implicitly, it's disconnected)
	 * 
	 * @param e
	 */
	public void handleException(Throwable e);
}