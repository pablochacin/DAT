package dat.algorithms.gcast;


import java.util.List;

import dat.Message;
import dat.network.NodeAddress;

public interface GroupcastProtocol {
	
	/**
	 * Handle a message send to the group and targeted to this node
	 * 
	 * @param group
	 * @param handler
	 */
	public void registerHandler(String group,GroupcastHandler handler);
	
	/**
	 * Send a message to a group. 
	 * 
	 * If a list of targets nodes is specified, only those will receive it.
	 * If the list is empty, all member will receive it.
	 *  
	 * @param group
	 * @param message
	 * @param targets
	 */
	public void cast(String group,Message message,List<NodeAddress>targets);
	

}
