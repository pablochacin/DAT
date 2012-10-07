package dat.network;

public class TransportException extends Exception {

	public TransportException() {
		super();
	}

	public TransportException(String message) {
		super(message);
	}

	public TransportException(Throwable cause) {
		super(cause);
	}

	public TransportException(String message, Throwable cause) {
		super(message, cause);
	}

}
