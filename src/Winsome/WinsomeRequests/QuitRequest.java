package Winsome.WinsomeRequests;

public class QuitRequest extends Request {

	String username;

	public QuitRequest() {
		super.setKind("Quit");
		this.username = null;
	}

	public QuitRequest(String user) {
		super.setKind("Quit");
		this.username = user;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String user) {
		this.username = user;
	}
}
