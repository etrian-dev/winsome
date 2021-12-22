package WinsomeExceptions;

public class BlogException extends Exception {
	String user;
	String path;
	String cause;

	public BlogException(String username, String blogPath, String cause) {
		this.user = username;
		this.path = blogPath;
		this.cause = cause;
	}

	@Override
	public String toString() {
		return "[BLOG ERROR]: Impossibile caricare il blog dell\'utente \""
				+ this.user + "\" (" + this.path + "): "
				+ this.cause;
	}
}
