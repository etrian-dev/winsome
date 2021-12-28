package Winsome.WinsomeRequests;

public class LogoutRequest extends Request {
	String username;

	public LogoutRequest() {
		super.setKind("Logout");
		this.username = null;
	}

	public LogoutRequest(String user) {
		super.setKind("Logout");
		this.username = user;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String user) {
		this.username = user;
	}
}
