package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;

import Winsome.WinsomeServer.ClientData;
import Winsome.WinsomeServer.User;
import Winsome.WinsomeServer.WinsomeServer;

public class LogoutTask extends Task implements Callable<Integer> {
	private String username;
	private ClientData cData;
	private WinsomeServer servRef;

	public LogoutTask(String user, ClientData client, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Logout");
		this.username = user;
		this.cData = client;
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
		if (!this.servRef.getUsernames().contains(this.username)) {
			return 1;
		}
		// Recupero l'utente
		User u = this.servRef.getUser(this.username);
		if (!u.isLogged()) {
			return 2;
		}
		// Utente non autorizzato
		if (!(this.cData.getCurrentUser() == null
				|| this.cData.getCurrentUser().equals(this.username))) {
			return -1;
		}
		// Dissocio (se possibile) l'username di questo utente dal socket
		if (!cData.unsetCurrentUser(this.username)) {
			return -1;
		}
		u.logout();
		return 0;
	}
}
