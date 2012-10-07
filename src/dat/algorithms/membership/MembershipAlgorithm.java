package dat.algorithms.membership;

import java.util.List;

import dat.Algorithm;
import dat.network.NodeAddress;

/**
 * Allows nodes to find other nodes in the network.
 * 
 * @author Pablo Chacin
 *
 */
public interface MembershipAlgorithm extends Algorithm {

	/**
	 * Make this node visible to other nodes running the same 
	 * discovery algorithm. This method should be identpotent and
	 * multiple calls should be allowed
	 */
	public void join();

	/**
	 * Make this node no longer visible to other nodes running the same
	 * discovery algorithm. 
	 * 
	 * Notice that the node will still be available for other algorithms.
	 */
	public void leave();

	
	/**
	 * Gets a list of currently known nodes. There's no warranty those nodes are still
	 * alive.
	 * 
	 * @return a list of NodeAddress for known nodes.
	 */
	public List<NodeAddress>getKnownNodes();

	
	/**
	 * Informs the algorithm that the given node is suspected of been
	 * failed. 
	 * 
	 * @param node
	 */
	public void supectedFail(NodeAddress node);
	
	/**
	 * Informs the algorithm that a node with the given NodeAddress has been
	 * detected and is candidate to be include in the list of known nodes.
	 * Algorithm can ignore this information and therefore there's no warranty this
	 * NodeAddres will be returned by getKnownNodes method.
	 * @param node
	 */
	public void candidate(NodeAddress node);
}
