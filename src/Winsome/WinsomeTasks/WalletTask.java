package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;

import Winsome.WinsomeServer.ClientData;
import Winsome.WinsomeServer.User;
import Winsome.WinsomeServer.WinsomeServer;

/**
 * Task che implementa l'ottenimento del valore corrente del wallet, 
 * oppure la sua conversione in BTC
 */
public class WalletTask extends Task implements Callable<Double> {
	private String username;
	private boolean convertToBTC;
	private ClientData cData;
	private WinsomeServer servRef;

	public WalletTask(String user, boolean resultInBTC, ClientData cd, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Wallet");
		this.username = user;
		this.convertToBTC = resultInBTC;
		this.cData = cd;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nUsername: " + this.username
				+ "\nInBTC:" + this.convertToBTC;
	}

	/**
	 * Metodo con il quale ottenere il valore corrente del wallet, 
	 * oppure convertire in BTC il suo valore
	 */
	public Double call() {
		// Utente non esistente
		// NOTA: nel caso di WinsomeClient ciò accade solo se è richiesto il logout
		// prima di essere loggati (currentUser="")
		if (!this.servRef.getUsernames().contains(this.username)) {
			return -2.0;
		}
		// Recupero l'utente
		User u = this.servRef.getUser(this.username);
		if (!u.isLogged()) {
			return -3.0;
		}
		// Utente non autorizzato
		if (!(this.cData.getCurrentUser() == null
				|| this.cData.getCurrentUser().equals(this.username))) {
			return -1.0;
		}
		// Ritorno il valore del wallet corrente
		// TODO: btc conversion
		return u.getWallet();
	}
}
