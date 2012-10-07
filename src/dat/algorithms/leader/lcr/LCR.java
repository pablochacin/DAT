package dat.algorithms.leader.lcr;

import java.util.Collections;
import java.util.List;

import dat.Algorithm;
import dat.DatException;
import dat.Event;
import dat.Message;
import dat.Node;
import dat.algorithms.leader.LeaderElection;
import dat.algorithms.membership.MembershipAlgorithm;
import dat.network.NodeAddress;

/**
 * 
 * @author Pablo Chacin
 *
 */
public class LCR implements LeaderElection, Algorithm {

	/**
	 * Higest ID seen by this node
	 */
	private String higestId;

	protected NodeAddress leader;

	protected Node node;

	protected boolean inElection;

	protected MembershipAlgorithm membership;

	@Override
	public NodeAddress getLeader() {
		if(leader !=null){
			return leader;
		}
		else{
			return electLeader();
		}
	}

	@Override
	public boolean isLeader() {

		NodeAddress currentLeader = getLeader();
		return node.getAddress().equals(currentLeader);

	}


	/**
	 * Request a new leader. The application may suspect the current leader is not
	 * longer valid (for example, it is unresponsive ) 
	 */
	public NodeAddress electLeader(){

		if(!inElection)
			startElection();

		while(leader == null){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		return leader;
	}

	protected synchronized void  startElection(){


		inElection = true;
		leader = null;

		Message message = new ElectionRequestMessage();
		message.setString("candidate", higestId);

		NodeAddress base = node.getAddress();
		for(int i=0;i< membership.getKnownNodes().size();i++){

			NodeAddress neighbor = getNextNeighbor(base);
			try {
				node.sendMessage(neighbor,message);
				return;
			}catch (DatException e) {
				node.getLog().debug("Exception sending election message: " +e.getMessage());
				//set failed neighbor as next, try again 
				base = neighbor;
			}		
		}	

		//none of the nodes was available, therefore
		//current node is the leader
		leader = node.getAddress();
		inElection = false;

	}
	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(Message message) {


			node.getLog().warn("Invalid Message Received " +message.toString() );
			

	}

	/**
	 * Handle a request for electing a new leader.
	 * 
	 * The request message has the attribute "candidate" which has id of the node
	 * with the highest id found so far in the process.
	 * 
	 * On receiving this message, the node updates its highest known id with the highest
	 * between the current known and the candidate and passes this information to the next
	 * neighbor in the virtual ring.
	 * 
	 * @param request
	 */
	public void handleMessage(ElectionRequestMessage request) {
		
		String candidate = request.getString("candidate");
		
		if(candidate.equals(node.getAddress().getLocation())){
			leader = node.getAddress();
			ElectionResultMessage result = new ElectionResultMessage();
			
			for(NodeAddress n: membership.getKnownNodes()){
				try {
					node.sendMessage(n,result);
				} catch (DatException e) {
					node.getLog().debug("Unable to contact Neighbor " + n.getLocation());
				}
			}
			inElection = false;
		
		}
		else{
			
			if(candidate.compareTo(higestId) > 0){
				higestId = candidate;
			}

			startElection();
		}

	}

	
	public void handleMessage(ElectionResultMessage message) {
		leader = message.getSender();
		higestId = leader.getLocation();
		inElection = false;
	}

	@Override
	public void init(Node node) {
		this.node = node;
		this.higestId = node.getAddress().getLocation();
		this.membership = (MembershipAlgorithm) node.getAlgorithm(node.getParameters().getString("membership"), 
                MembershipAlgorithm.class);
		
	}


	/**
	 * Selects the next neighbor according to their IDs.
	 * 
	 * 
	 * 
	 * @return
	 */
	private NodeAddress getNextNeighbor(NodeAddress base){
		
		List<NodeAddress> neighbors = membership.getKnownNodes();

		Collections.sort(neighbors);
		
		for(NodeAddress n: neighbors){
			if(n.compareTo(base) > 0){
				return n;
			}
		}

		return neighbors.get(0);

	}


}
