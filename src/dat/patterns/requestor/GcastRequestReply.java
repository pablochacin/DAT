package dat.patterns.requestor;


import java.util.List;

import dat.Message;
import dat.Node;
import dat.algorithms.gcast.GroupcastHandler;
import dat.algorithms.gcast.GroupcastProtocol;
import dat.network.NodeAddress;


/**
 * Extends the RequestReply algorithm 
 * 
 * @author Pablo Chacin
 *
 */
public abstract class GcastRequestReply extends RequestReply implements GroupcastHandler {
	
	public GcastRequestReply() {
		super();
	}


	protected GroupcastProtocol gcast;
	
	protected String group;
	
	protected String gcastProtocol;
	
				
	public void init(Node node){
		super.init(node);
		
		//get the group name, use the algorithm's class name by default.
		this.group = node.getParameters().getString("group",this.getClass().getName());
		this.gcastProtocol = node.getParameters().getString("gcast","gcast");
		this.gcast = (GroupcastProtocol)node.getAlgorithm(gcastProtocol, GroupcastProtocol.class);
		this.gcast.registerHandler(group, this);
	}
	
	
	@Override
	protected void sendRequests(RequestMessage request,List<NodeAddress> targets){
		gcast.cast(group, request,targets);
	}

	
	@Override
	public void handleCast(String group,Message message){
		dispatchRequest((RequestMessage)message);
	}
	
		

}
