package dat.algorithms.voting;

import java.io.Serializable;

import dat.network.NodeAddress;
import dat.patterns.requestor.RequestMessage;
import dat.utils.TypedMap;


public class VotingRequest extends RequestMessage {

	/**
	 * Subject of the voting
	 */
	private Serializable subject;

	public VotingRequest() {
		super();
	}
	
	public VotingRequest(NodeAddress requestor,Serializable subject) {
		super(requestor);
		this.subject = subject;
	}
	
	
	public VotingRequest(NodeAddress requestor,String id, String type, Serializable subject, TypedMap attributes) {
		super(requestor,id, type, attributes);
		this.subject = subject;
	}
	
	public VotingRequest(NodeAddress requestor,String id, String type, Serializable subject) {
		super(requestor,id, type);
		this.subject = subject;
	}

	public Serializable getSubject() {
		return subject;
	}

	public void setSubject(Serializable subject) {
		this.subject = subject;
	}
	
	
	
}
