package dat.network;

public class NetworkException extends Exception {

	public NetworkException() {
		super();
	}

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(Throwable cause) {
		super(cause);
	}

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}

}
