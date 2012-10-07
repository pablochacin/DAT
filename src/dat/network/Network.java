package dat.network;

import org.apache.commons.configuration.Configuration;


/**
 * Abstracts from the underlaying network infrastructure
 * 
 * @author Pablo Chacin
 *
 */
public interface Network {

	/**
	 * Initializes the Nnetowk communications
	 */
	public void init(Configuration config) throws NetworkException;
	
	/**
	 * Returns the communication transport used to communicate nodes in the network
	 * @return
	 */
	public Transport getTransport();
	
		
}
