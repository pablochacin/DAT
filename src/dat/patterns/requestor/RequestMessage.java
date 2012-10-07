package dat.patterns.requestor;

import dat.Message;
import dat.network.NodeAddress;
import dat.utils.TypedMap;

/**
 * This class is a Tag used to identify Request messages
 * 
 * @author Pablo Chacin
 *
 */
public abstract class RequestMessage extends Message {
	
	protected NodeAddress requestor;

	public RequestMessage() {
		super();

	}

	public RequestMessage(NodeAddress requestor) {
		super();
		this.requestor = requestor;
	}
	
	public RequestMessage(NodeAddress requestor,String id, String type, TypedMap attributes) {
		super(id, type, attributes);
	}
 
	public RequestMessage(NodeAddress requestor,String id, String type) {
		super(id, type);
	}

	public NodeAddress getRequestor() {
		if(requestor == null){
			requestor = getSender();
		}
		return requestor;
	}

	public void setRequestor(NodeAddress requestor) {
		this.requestor = requestor;
	}
	
	
}
