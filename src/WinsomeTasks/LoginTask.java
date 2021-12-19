package WinsomeTasks;

import java.util.concurrent.Callable;

import WinsomeServer.User;
import WinsomeServer.WinsomeServer;

public class LoginTask extends Task implements Callable<Integer> {
	private String username;
	private String password;
	private WinsomeServer servRef;

	public LoginTask(String user, String pwd, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Login");
		this.username = user;
		this.password = pwd;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nUsername: " + this.username
				+ "\nPassword: " + this.password;
	}

	public Integer call() {
		// Utente non esistente
		if (!this.servRef.getUsernames().contains(this.username)) {
			return 1;
		}
		// Recupero l'utente
		User u = this.servRef.getUser(this.username);
		// Password non corretta
		if (!u.getPassword().equals(this.password)) {
			return 2;
		}
		// Utente già loggato
		if (u.isLogged()) {
			return 3;
		}
		// Eseguo login
		u.login();
		return 0;
	}

}
