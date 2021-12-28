package Winsome.WinsomeExceptions;

public class WinsomeConfigException extends Exception {
	String cause;

	public WinsomeConfigException(String cause) {
		this.cause = cause;
	}

	@Override
	public String toString() {
		return "[CONFIG ERROR]: " + cause;
	}
}
