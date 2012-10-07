package dat.algorithms.leader.bully;

import dat.Message;
import dat.network.NodeAddress;
import dat.utils.TypedMap;

public class ElectionRequestMessage extends Message {

	public ElectionRequestMessage() {
		super();
	}

	public ElectionRequestMessage(String id, String algorithm, String type,
			NodeAddress sender, NodeAddress destination, TypedMap attributes) {
		super(id, algorithm, type, sender, destination, attributes);
	}

	public ElectionRequestMessage(String id, String type, TypedMap attributes) {
		super(id, type, attributes);
	}

	public ElectionRequestMessage(String id, String type) {
		super(id, type);
	}


}
