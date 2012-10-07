package dat.algorithms.voting;

import java.io.Serializable;

public interface Voting{

	public abstract void registerVoter(Serializable subject, VotingHandler voter);

	/**
	 * Makes a new voting. 
	 * 
	 * @return true if the majority votes true, false otherwise
	 */
	public abstract Boolean requestVoting(Serializable subject,int num,long timeout);

}