package dat.algorithms.voting;

import java.io.Serializable;

/**
 * A participant in a voting process  
 * 
 * @author Pablo Chacin
 *
 */
public interface VotingHandler {

	/**
	 * Vote on a subject
	 * @param subject
	 * @return the participant's vote (true or false)
	 */
	public boolean vote(Serializable subject);
}
