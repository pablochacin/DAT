package dat.patterns.requestor;

import dat.Event;
import dat.utils.TypedMap;

public class RequestTimeout extends Event {

	public RequestTimeout() {
		super();
	}

	public RequestTimeout(long time, String type, TypedMap attributes) {
		super(time, type, attributes);
	}

	public RequestTimeout(long time, String type) {
		super(time, type);
	}

	public RequestTimeout(String id, long time, String type, TypedMap attributes) {
		super(id, time, type, attributes);
	}

	public RequestTimeout(String id, long time, String type) {
		super(id, time, type);
	}

}
