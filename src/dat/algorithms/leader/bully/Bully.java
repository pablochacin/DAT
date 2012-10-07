package dat.algorithms.leader.bully;


import dat.Algorithm;
import dat.DatException;
import dat.Event;
import dat.Message;
import dat.Node;
import dat.network.NodeAddress;
import dat.algorithms.membership.MembershipAlgorithm;
import dat.algorithms.leader.LeaderElection;


public class Bully implements Algorithm,LeaderElection {

	/**
	 * Default timeout
	 */
	protected static long TIMEOUT=1000;
	
	/**
	 * Algorithm used to find participants for the leader election
	 */
	protected MembershipAlgorithm membership;
	
	/**
	 * Address of the leader
	 */
	protected NodeAddress leader;
	
	/**
	 * Node on which this algorithms executes
	 */
	protected Node node;
		
	/**	
	 * Number of pending responses for the last issue election request
	 */
	protected int pendingResponses=0;
	
	/**
	 * Current's node id
	 */
	protected String location;
	
	/**
	 * Time to wait for responses
	 */
	protected long timeout;
	
	/**
	 * Indicates that this node already started an election
	 */
	protected boolean inElection;
		
	/**
	 * Timeout event for receiving election responses
	 */
	protected ElectionRequestTimeoutEvent responseTimeout;
	
	/**
	 * Timeout event for election process
	 */
	protected ElectionTimeoutEvent electionTimeout;
	
	
	/**
	 * Constructor
	 */
	public Bully(){
	
		inElection = false;
	
	}
	
	
	/**
	 * Get the current leader. If current leader is null, elects one.
	 * 
	 * @return
	 */
	public NodeAddress getLeader(){
		if(leader !=null){
			return leader;
		}
		else{
			return electLeader();
		}
		
	}
	
	
	public boolean isLeader(){
		NodeAddress currentLeader = getLeader();
		return node.getAddress().equals(currentLeader);
	}

	
	/**
	 * Request a new leader. The application may suspect the current leader is not
	 * longer valid (for example, it is unresponsive ) 
	 */
	public NodeAddress electLeader(){
						
		startElection();
		
		while(leader == null){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		return leader;
	}
	
	
	
	protected synchronized void  startElection(){
				
		if(inElection)
			return;
		
		inElection = true;
		leader = null;
				
		ElectionRequestMessage message = new ElectionRequestMessage();
				
		pendingResponses = 0;
		
		for(NodeAddress neighbor: membership.getKnownNodes()){			

			if( neighbor.compareTo(node.getAddress()) > 0){
				try {
					node.sendMessage(neighbor,message);
					pendingResponses++;
				} catch (DatException e) {
					membership.supectedFail(neighbor);
				}
			}
		}
		
		responseTimeout = new ElectionRequestTimeoutEvent();
		responseTimeout.setTime(timeout);
		responseTimeout.setType("bully.response.timeout");
		node.scheduleEvent(responseTimeout);
		
	}
	
	/**
	 * Handle unexpected events
	 */
	@Override
	public void handleEvent(Event event) {
		node.getLog().error("Unexpected event:" + event.toString());
	}
	
	
	
	public void handleEvent(ElectionTimeoutEvent event) {
		
		startElection();
	}
	
	
	/**
	 * Time of the wait for answers to the election request. If this event
	 * is triggered, it is assumed that no other node has rejected the election
	 * therefore the node elects itself as leader.
	 * @param event
	 */
	public void handleEvent(ElectionRequestTimeoutEvent event) {
		
		ElectionMessage message = new ElectionMessage();
		
		for(NodeAddress neighbor: membership.getKnownNodes()){
			if(neighbor.compareTo(node.getAddress()) < 0){
				try {
					node.sendMessage(neighbor, message);
				} catch (DatException e) {
					node.getLog().trace("Exception sending message" + message.toString());
				}
			}
		}
		leader = node.getAddress();
		inElection = false;

	}

	
	/**
	 * Handle unexpected messages
	 */
	@Override
	public void handleMessage(Message message) {
		
		node.getLog().error("Receiving unexpected message" + message.toString());

	}

	
	public void handleMessage(ElectionRequestMessage request){
				
		//current leader is no longer valid
		leader = null;
		
		membership.candidate(request.getSender());
		
		ElectionResponseMessage response = new ElectionResponseMessage();
		
		if(request.getSender().compareTo(node.getAddress()) > 0){
			response.getAttributes().putBoolean("response", true);			
		}
		else{	
			response.getAttributes().putBoolean("response",false);	
			startElection();
		}
		
		
		try {
			node.sendMessage(request.getSender(), response);
		} catch (DatException e) {
			node.getLog().warn("Exception sending response to " + request.getSender());
		}
		
	}
	
	/**
	 * Handle a response from an election started by this node.
	 * 
	 * @param response
	 */
	public void handleMessage(ElectionResponseMessage response){
		
		
		if(!response.getAttributes().getBoolean("response"))	{
			//inElection = false;
			node.cancelEvent(responseTimeout.getId());
			electionTimeout = new ElectionTimeoutEvent();
			electionTimeout.setTime(timeout);
			electionTimeout.setType("bully.election.timeout");
			node.scheduleEvent(electionTimeout);
		}
	}
	
	public void handleMessage(ElectionMessage message){
		
		if(message.getSender().compareTo(node.getAddress()) > 0){
			
			if(responseTimeout != null)
				node.cancelEvent(responseTimeout.getId());
			
			if(electionTimeout != null)
				node.cancelEvent(electionTimeout.getId());
			
			leader =  message.getSender();
			inElection = false;
		}
		

	}
	
	@Override
	public void init(Node node) {
		this.node = node;
		this.timeout = node.getParameters().getLong("timeout",TIMEOUT);
		this.membership = (MembershipAlgorithm) node.getAlgorithm(node.getParameters().getString("membership"), 
				                            MembershipAlgorithm.class);
		this.membership.join();
	}

}
