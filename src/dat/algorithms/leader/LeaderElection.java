package dat.algorithms.leader;


import dat.network.NodeAddress;



public interface LeaderElection {

	/**
	 * Returns  the currently elected leader. If none exists, an election process
	 * is started and the method waits until one node is elected.
	 * 
	 * 
	 * There's no guaranty that the leader node is still currently active.
	 * 
	 * @return the NodeAddress of the currently elected leader
	 */
	public NodeAddress getLeader();
	
	
	/**
	 * Forces the election of a leader. Used when the application suspects the
	 * leader is no longer active.
	 * 
	 * @return the NodeAddress of the currently elected leader.
	 */
	public NodeAddress electLeader();
	
	/**
	 * 
	 * Determines if this node is the currently elected leader. If no leader has been elected, a
	 * leader election process is started.
	 * 
	 * @return a boolean indicating id this node is the currently elected leader.
	 */
	public boolean isLeader();
		

}
