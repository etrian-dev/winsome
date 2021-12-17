package WinsomeTasks;

import java.util.Map;
import java.util.concurrent.Callable;

import WinsomeServer.User;

public class LoginTask<T> extends Task<T> implements Callable<Integer> {
	private String username;
	private String password;
	private Map<String, User> usersMap = null;

	public LoginTask(String user, String pwd, Map<String, User> users) {
		super.setInvalid();
		this.username = user;
		this.password = pwd;
		this.usersMap = users;
	}

	public Integer call() {
		User u = this.usersMap.get(this.username);
		// Utente non esistente
		if (u == null) {
			return Integer.valueOf(1);
		}
		// Password non corretta
		if (!u.getPassword().equals(this.password)) {
			return Integer.valueOf(2);
		}
		// Utente già loggato
		if (u.isLogged()) {
			return Integer.valueOf(3);
		}
		// Eseguo login
		u.login();
		return Integer.valueOf(0);
	}

}
