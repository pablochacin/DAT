package dat.network;

import java.io.Serializable;
import java.util.UUID;



/**
 * 
 * Represents the address of a {@link NetworkNode} in the network.
 * 
 * @author Pablo Chacin
 *
 */
public interface NodeAddress extends Serializable, Comparable<NodeAddress>{
			
	
	/**
	 * Returns a printable representation of the node's address
	 * @return
	 */
	public String getLocation();	
	

	/**
	 * Returns the UUID of the node associated with this address.
	 * 
	 * @deprecated
	 * 
	 * @return
	 */
	public UUID getID();
	
}
