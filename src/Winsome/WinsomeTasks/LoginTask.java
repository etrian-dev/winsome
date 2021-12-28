package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;

import Winsome.WinsomeServer.ClientData;
import Winsome.WinsomeServer.User;
import Winsome.WinsomeServer.WinsomeServer;

public class LoginTask extends Task implements Callable<Integer> {
	private String username;
	private String password;
	private ClientData cData;
	private WinsomeServer servRef;

	public LoginTask(String user, String pwd, ClientData client, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Login");
		this.username = user;
		this.password = pwd;
		this.cData = client;
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
		User u = this.servRef.getUser(this.username);
		// Password non corretta
		if (!u.getPassword().equals(this.password)) {
			return 2;
		}
		// Utente già loggato
		if (u.isLogged()) {
			return 3;
		}
		// Utente non autorizzato
		if (!(this.cData.getCurrentUser() == null
				|| this.cData.getCurrentUser().equals(this.username))) {
			return -1;
		}
		// Associo (se possibile) l'username di questo utente al socket
		if (!cData.setCurrentUser(this.servRef.getUsernames(), this.username)) {
			return -1;
		}
		// Eseguo login
		u.login();
		return 0;
	}

}
