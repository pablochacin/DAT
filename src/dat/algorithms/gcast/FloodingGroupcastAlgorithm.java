package dat.algorithms.gcast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dat.Algorithm;
import dat.DatException;
import dat.Event;
import dat.Message;
import dat.Node;
import dat.algorithms.membership.MembershipAlgorithm;
import dat.network.NodeAddress;


public class FloodingGroupcastAlgorithm implements GroupcastProtocol, Algorithm {

	private Set<String> messages;

	private Map<String,GroupcastHandler> handlers;

	private Node node;
	
	private MembershipAlgorithm membership;

	public FloodingGroupcastAlgorithm(){
		this.messages = new HashSet<String>();

		//initialize with a dummy handler
		this.handlers = new HashMap<String,GroupcastHandler>();
	}

	@Override
	public void cast(String group, Message message,List<NodeAddress> targets) {

		message.setString("group", group);
		message.setObject("targets",targets);
		messages.add(message.getId());


		try {
			node.sendMessage(membership.getKnownNodes(), message);
		} catch (DatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void registerHandler(String group,GroupcastHandler handler) {

		handlers.put(group, handler);

	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(Message message) {

		String id = message.getId();
		NodeAddress[] targets = (NodeAddress[]) message.getObject("targets");

		if(!messages.contains(id) && isTarget(targets)){
			messages.add(id);

			String group = message.getAttributes().getString("group");

			GroupcastHandler handler = handlers.get(group);
			if(handler == null){
				return ;
			}

			handler.handleCast(group,message);


		}

		try {

			//send to all neighbors, but the one the message came from
			for(NodeAddress n: membership.getKnownNodes()){
				if(!n.equals(message.getSender()))
					node.sendMessage(n, message);
			}
		} catch (DatException e) {
			node.getLog().debug("Exception disseminating message " + message.toString(),e);
		}

	}


	/**
	 * Checks if this node is part of the targets for the message.
	 * A message with no targets is targeted to all members of the
	 * group.
	 * 
	 * @param targets
	 * @return
	 */
	private boolean isTarget(NodeAddress[] targets){
		if(targets.length == 0){
			return true;
		}

		for(NodeAddress a: targets){
			if(a.equals(node.getAddress())){
				return true;
			}
		}

		return false;
	}

	@Override
	public void init(Node node){
		this.node = node;
	}


}
