package dat.algorithms.gcast.test;

import java.util.Random;

import dat.algorithms.gcast.GroupcastHandler;
import dat.algorithms.gcast.GroupcastProtocol;
import dat.algorithms.membership.MembershipAlgorithm;
import dat.core.DAT;
import dat.Message;

public class GcastApp implements Runnable, GroupcastHandler {

	private static int id = 0;
		
	private static String getId(){
		return String.valueOf(id++);
	}
		
	
	private Random rand = new Random();

	private GroupcastProtocol gcast;
	
	private MembershipAlgorithm membership;
	
	@Override
	public void run() {
		
		gcast = (GroupcastProtocol) DAT.getAlgorithm("bcast", GroupcastProtocol.class);
		
		membership = (MembershipAlgorithm) DAT.getAlgorithm("memebership", MembershipAlgorithm.class);
		
		gcast.registerHandler(this.getClass().getName(),this);
		
		while(true){
			
			try {
				Thread.currentThread().sleep(1000);
				Message message = new Message();
				message.setId(getId());
				
				DAT.getLog().info("Sending message: " +message.getId());
				gcast.cast(GcastApp.class.getName(),message,membership.getKnownNodes());

			} catch (InterruptedException e) {
				System.exit(1);
			}
		}
	}

	@Override
	public void handleCast(String group,Message message) {

		System.out.println("Receiving message: " + message.toString());
	}

}
