package dat.patterns.requestor;

import dat.Message;
import dat.utils.TypedMap;
/**
 * This is a Tag class, used to identify response messages
 * 
 * @author Pablo Chacin
 *
 */
public class ResponseMessage extends Message {

	public ResponseMessage() {
		super();
	}

	public ResponseMessage(String id, String type, TypedMap attributes) {
		super(id, type, attributes);
	}


	public ResponseMessage(String id, String type) {
		super(id, type);
	}
 
}
