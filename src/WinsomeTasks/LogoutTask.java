package WinsomeTasks;

import java.util.concurrent.Callable;

import WinsomeServer.User;
import WinsomeServer.WinsomeServer;

public class LogoutTask extends Task implements Callable<Integer> {
	private String username;
	private WinsomeServer servRef;

	public LogoutTask(String user, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Logout");
		this.username = user;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString() + "\nUsername: " + this.username;
	}

	public Integer call() {
		// Utente non esistente
		// NOTA: nel caso di WinsomeClient ciò accade solo se è richiesto il logout
		// prima di essere loggati (currentUser="")
		if (!this.servRef.getUsers().contains(this.username)) {
			return 2;
		}
		// Recupero l'utente
		User u = this.servRef.getUser(this.username);
		if (u.isLogged()) {
			u.logout();
			return 0;
		} else {
			return 1;
		}
	}
}
