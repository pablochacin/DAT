package dat.algorithms.voting.test;

import java.io.Serializable;
import java.util.Random;

import dat.algorithms.voting.Voting;
import dat.algorithms.voting.VotingHandler;
import dat.core.DAT;



public class VoterApplication implements Runnable, VotingHandler {

	protected static Random rand = new Random();
	
	protected Voting voting;
	
	@Override
	public void run() {
					
		voting = (Voting) DAT.getAlgorithm("voting", Voting.class);
		voting.registerVoter("voting-test", this);


	}

	@Override
	public boolean vote(Serializable subject) {
		
		Boolean vote = rand.nextBoolean();

		DAT.getLog().info("vote:"+vote);

		return vote;
	}

}
