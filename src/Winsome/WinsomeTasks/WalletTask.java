package Winsome.WinsomeTasks;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Winsome.WinsomeServer.ClientData;
import Winsome.WinsomeServer.User;
import Winsome.WinsomeServer.WinsomeServer;

/**
 * Task che implementa l'ottenimento della storia delle transazioni del wallet, 
 * oppure la sua conversione in BTC
 */
public class WalletTask extends Task implements Callable<String> {
	public static final HttpClient httpRandomClient = HttpClient.newHttpClient();
	public static final Lock httpClientLock = new ReentrantLock();

	private String username;
	private boolean convertToBTC;
	private ClientData cData;
	private ObjectMapper mapper;
	private WinsomeServer servRef;

	public WalletTask(String user, boolean resultInBTC, ClientData cd,
			ObjectMapper objMapper, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Wallet");
		this.username = user;
		this.convertToBTC = resultInBTC;
		this.cData = cd;
		this.mapper = objMapper;
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
	public String call() {
		// Utente non esistente
		// NOTA: nel caso di WinsomeClient ciò accade solo se è richiesto il logout
		// prima di essere loggati (currentUser="")
		if (!this.servRef.getUsernames().contains(this.username)) {
			return "Errore:utente richiedente non esistente";
		}
		// Recupero l'utente
		User u = this.servRef.getUser(this.username);
		if (!u.isLogged()) {
			return "Errore:utente non loggato";
		}
		// Utente non autorizzato
		if (!(this.cData.getCurrentUser() == null
				|| this.cData.getCurrentUser().equals(this.username))) {
			return "Errore:operazione non consentita";
		}

		// Storia delle transazioni o conversione in BTC
		if (this.convertToBTC) {
			return convertToBTC(u.getWallet());
		} else {
			// Ritorno il valore del wallet corrente
			try {
				String history = mapper.writeValueAsString(u.getTransactions());
				return history;
			} catch (JsonProcessingException e) {
				System.err.println("Impossibile completare " + this.getKind() + ": " + e.getMessage());
				return "Errore:impossibile completare la richiesta";
			}
		}
	}

	/**
	 * Metodo per la conversione del valore wallet in BTC, 
	 * con un servizio web per il tasso di conversione.
	 * 
	 * Per la conversione si invia una richiesta HTTP al servizio RANDOM.org
	 * e si esegue il parsing della risposta. Si noti che l'invio di una richiesta e l'attesa
	 * (bloccante) di una risposta dal server contattato pu&ograve; introdurre
	 * un ritardo apprezzabile
	 * @param wallet
	 * @return
	 */
	private String convertToBTC(double wallet) {
		// Per la conversione in BTC ci si appoggia al servizio RANDOM.org
		// per la generazione di un reale casuale
		HttpRequest req = HttpRequest.newBuilder()
				.GET()
				.uri(URI
						.create("https://www.random.org/decimal-fractions/?num=1&dec=10&col=1&format=plain&rnd=new"))
				.headers("Accept", "text/plain,text/html")
				.build();
		try {
			// Sincronizzazione delle richieste tramite una lock statica
			WalletTask.httpClientLock.lock();
			HttpResponse<String> reply = WalletTask.httpRandomClient.send(req,
					HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
			WalletTask.httpClientLock.unlock();

			if (reply.statusCode() == 200) {
				Double exchange_rate = Double.valueOf(reply.body());
				double val = wallet * exchange_rate;
				// FIXME: debug print
				System.out.printf("wallet * exchange_rate = %f * %f = %f BTC\n", wallet, exchange_rate, val);
				return String.valueOf(val);
			} else {
				return "-1.0";
			}
		} catch (Exception e) {
			System.err.println("Impossibile completare la richiesta: " + e.getMessage());
			return "0.0";
		}
	}
}
