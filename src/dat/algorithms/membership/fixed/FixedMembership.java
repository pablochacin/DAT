package dat.algorithms.membership.fixed;

import java.util.ArrayList;
import java.util.List;

import dat.DatException;
import dat.Event;
import dat.Message;
import dat.Node;
import dat.algorithms.membership.MembershipAlgorithm;
import dat.network.NodeAddress;

/**
 * A MembershipAlgorithm that uses a fixed set of members taken from the configuration.
 * The addresses for other nodes are specified as 
 *   host:port[-range]
 * where: host is the host name or host address
 *        port is the port
 *        range, if specified, indicates the upper limit for a port range, starting with port
 * 
 * @author Pablo Chacin
 *
 */
public class FixedMembership implements MembershipAlgorithm {

	protected List<NodeAddress> knownNodes;
	
	@Override
	public void candidate(NodeAddress node) {
	
		knownNodes.add(node);
	}

	@Override
	public List<NodeAddress> getKnownNodes() {
		return new ArrayList<NodeAddress>(knownNodes);
	}

	@Override
	public void join() {
		
	}

	@Override
	public void leave() {

	}

	@Override
	public void supectedFail(NodeAddress node) {
		knownNodes.remove(node);
	}

	@Override
	public void handleEvent(Event event) {

	}

	@Override
	public void handleMessage(Message message) {

	}

	@Override
	public void init(Node node) {

		knownNodes = new ArrayList<NodeAddress>();
		String[] seedList = node.getParameters().getStringArray("seeds");
		for(String s: seedList){
			
			
			try{
				String[] address = s.split("[:-]");
				String host = address[0];
				int port = Integer.parseInt(address[1]);
				int range=port;
				if(address.length >2 ){
					range = Integer.parseInt(address[2]);
				} 
			
				for(int p = port;p <= range;p++){
					NodeAddress a = node.resolve(host+":"+p);
					if(!a.equals(node.getAddress())){
						knownNodes.add(a);
					}
				}
				
			}catch(DatException e){
					//discart exception resolving address
				}
					continue;
				}
		}
		
	}

