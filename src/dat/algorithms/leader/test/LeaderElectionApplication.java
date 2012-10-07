package dat.algorithms.leader.test;


import dat.core.DAT;
import dat.algorithms.leader.LeaderElection;
import dat.utils.Exponential;


public class LeaderElectionApplication implements Runnable {


	@Override
	public void run() {
		
		
		LeaderElection election = (LeaderElection)DAT.getAlgorithm("leader", LeaderElection.class);
		
		
		double delay = DAT.getAppParameters().getDouble("delay");
		
		try {
			while(true){
				Thread.sleep(Exponential.nextInt(1.0/delay)*1000);
				DAT.getLog().info("Leader:" + election.getLeader().getLocation());
			}
		} catch (InterruptedException e) {
			return;
		}
		
		

	}

}
