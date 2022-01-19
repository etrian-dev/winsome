package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;

import Winsome.WinsomeServer.ClientData;
import Winsome.WinsomeServer.User;
import Winsome.WinsomeServer.WinsomeServer;

/** 
 * Task che implementa il logout di un utente
 */
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

	/**
	 * Metodo per il logout di un utente
	 * 
	 * @return Uno tra i seguenti valori:
	 * <ul>
	 * <li>-1: se l'utente non &egrave; autorizzato</li>
	 * <li>0: sse il logout ha avuto successo</li>
	 * <li>1: se l'utente specificato nella richiesta non esiste in Winsome</li>
	 * <li>2: se l'utente che ha effettuato la richiesta non era loggato</li>
	 * </ul>
	 */
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
		// Deregistro, se necessario, dal servizio di callback
		if (this.servRef.rmCallback(this.username)) {
			System.out.println("Utente " + this.username
					+ " deregistrato dal servizio di callback");
		}
		return 0;
	}
}
