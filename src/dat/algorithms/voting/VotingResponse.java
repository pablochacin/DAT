package dat.algorithms.voting;

import java.io.Serializable;

import dat.patterns.requestor.ResponseMessage;
import dat.utils.TypedMap;

public class VotingResponse extends ResponseMessage {

	private Serializable subject;
	
	private Boolean vote;
	
	public VotingResponse() {
		super();
	}

	public VotingResponse(String id, String type, Serializable subject, Boolean vote,TypedMap attributes) {
		super(id, type, attributes);
		this.subject = subject;
		this.vote = vote;
	}


	public VotingResponse(String id, String type,Serializable subject,Boolean vote) {
		super(id, type);
		this.subject = subject;
		this.vote = vote;
	}

	public Serializable getSubject() {
		return subject;
	}

	public void setSubject(Serializable subject) {
		this.subject = subject;
	}

	public Boolean getVote() {
		return vote;
	}

	public void setVote(Boolean vote) {
		this.vote = vote;
	}
	
	

}
