package dat;

public class DatException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DatException(String message) {
		super(message);
	}

	public DatException(Throwable cause) {
		super(cause);
	}

	public DatException(String message, Throwable cause) {
		super(message, cause);
	}

}
