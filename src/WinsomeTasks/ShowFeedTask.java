package WinsomeTasks;

import java.util.concurrent.Callable;

import WinsomeServer.WinsomeServer;

public class ShowFeedTask extends Task implements Callable<String> {
	private String currentUser;
	private WinsomeServer servRef;

	public ShowFeedTask(String loggedUser, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("ShowFeed");
		this.currentUser = loggedUser;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * Metodo per ottenere il feed di post di un utente
	 * 
	 * @return Il risultato dell'operazione richiesta è una stringa:
	 */
	public String call() {
		return "";
	}
}
