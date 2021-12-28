package Winsome.WinsomeExceptions;

public class WinsomeServerException extends Exception {
	String cause;

	public WinsomeServerException(String cause) {
		this.cause = cause;
	}

	@Override
	public String toString() {
		return "[SERVER ERROR]: " + cause;
	}
}
