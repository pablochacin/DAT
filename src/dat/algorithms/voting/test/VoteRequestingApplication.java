package dat.algorithms.voting.test;

import dat.algorithms.voting.Voting;
import dat.core.DAT;


/**
 * 
 * @author pchacin
 *
 */
public class VoteRequestingApplication implements Runnable{

	protected Voting voting;
	
	@Override
	public void run() {

		voting = (Voting) DAT.getAlgorithm("voting", Voting.class);
		
		Boolean result = voting.requestVoting("voting-test",0,5000);
		
		DAT.getLog().info("Result of votation: " + result);
	}

}
