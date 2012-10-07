package dat.algorithms.voting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dat.DatException;
import dat.Node;
import dat.algorithms.membership.MembershipAlgorithm;
import dat.core.DAT;
import dat.patterns.requestor.RequestReply;

public class VotingAlgorithm extends RequestReply implements Voting{

	private Map<Serializable,VotingHandler> voters;
	
	private MembershipAlgorithm membership;
			
	
	public VotingAlgorithm() {
		super();
		this.voters = new HashMap<Serializable,VotingHandler>();
	}


	/* (non-Javadoc)
	 * @see dat.samples.voting.VotingAlgorithm#registerVoter(java.io.Serializable, dat.samples.voting.VotingHandler)
	 */
	public void registerVoter(Serializable subject, VotingHandler voter){
		this.voters.put(subject, voter);
	}
	
	
	/* (non-Javadoc)
	 * @see dat.samples.voting.VotingAlgorithm#requestVoting(java.io.Serializable)
	 */
	public Boolean requestVoting(Serializable subject,int num, long timeout){
		VotingRequest request = new VotingRequest();
		request.setRequestor(DAT.getNode().getAddress());
		request.setSubject(subject);
		request.setType("vote.resquest");
		
		try {
			List<VotingResponse> results = (List<VotingResponse>) makeRequest(request, num,timeout,membership.getKnownNodes());
			int trueVotes= 0;
			int falseVotes = 0;
			for(VotingResponse r: results){
				if(r.getVote()){
					trueVotes++;
				}else{
					falseVotes++;
				}
			}
			
			return trueVotes > falseVotes;
			
		} catch (DatException e) {
			DAT.getNode().getLog().error("Exception during votation", e);
			return null;
		}
	
	}
	
	
	public void processRequest(VotingRequest request) {
		
		Serializable subject  = request.getSubject();
		
		VotingHandler voter = voters.get(subject);

		if(voter != null){
			Boolean vote = voter.vote(subject);
			
			VotingResponse response = new VotingResponse();
			response.setType("vote.response");
			response.setId(request.getId());
			
			try {
				response.setVote(vote);
				DAT.getNode().sendMessage(request.getRequestor(), response);
			} catch (DatException e) {
				DAT.getNode().getLog().error("Can't contact requestor", e);
			}
		}
	}


	@Override
	public void init(Node node) {
		super.init(node);
		
		membership = (MembershipAlgorithm)node.getAlgorithm("membership", MembershipAlgorithm.class);
	}



}
