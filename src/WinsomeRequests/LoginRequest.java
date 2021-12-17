package WinsomeRequests;

public class LoginRequest extends Request {
	private String username;
	private String password;

	public LoginRequest(String user, String pwd) {
		super.setKind("Login");
		this.username = user;
		this.password = pwd;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setUsername(String user) {
		this.username = user;
	}

	public void setPassword(String pwd) {
		this.password = pwd;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nUsername: " + this.username + "\nPassword: " + this.password;
	}
}
