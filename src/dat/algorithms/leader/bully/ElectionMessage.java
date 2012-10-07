package dat.algorithms.leader.bully;

import dat.Message;
import dat.network.NodeAddress;
import dat.utils.TypedMap;

public class ElectionMessage extends Message {

	public ElectionMessage() {
		super();
	}

	public ElectionMessage(String id, String algorithm, String type,
			NodeAddress sender, NodeAddress destination, TypedMap attributes) {
		super(id, algorithm, type, sender, destination, attributes);
	}

	public ElectionMessage(String id, String type, TypedMap attributes) {
		super(id, type, attributes);
	}

	public ElectionMessage(String id, String type) {
		super(id, type);
	}


	
}
