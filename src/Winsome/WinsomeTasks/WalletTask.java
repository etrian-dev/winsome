package Winsome.WinsomeTasks;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
	//public static final HttpClient httpRandomClient = HttpClient.newHttpClient();
	public static final Lock httpClientLock = new ReentrantLock();

	public static final String REQUEST_METHOD = "GET";
	public static final String REQUEST_URL = "/decimal-fractions/?num=1&dec=10&col=1&format=plain&rnd=new";
	public static final String[][] HEADERS = {
			{ "Host", " www.random.org" },
			{ "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8" },
			{ "Accept-Language", "en-GB,en;q=0.5" },
			{ "Accept-Encoding", "gzip, deflate, br" },
			{ "Connection", "keep-alive" } };

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
		try {
			HttpURLConnection.setFollowRedirects(true);
			URL url = new URL("https", "www.random.org", 443, REQUEST_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(REQUEST_METHOD);
			for (int i = 0; i < HEADERS.length; i++) {
				conn.addRequestProperty(HEADERS[i][0], HEADERS[i][1]);
			}

			StringBuffer req = new StringBuffer("=== HTTP Request ===");
			req.append("\nMethod: " + REQUEST_METHOD)
					.append("\nURL: " + REQUEST_URL);
			for (int i = 0; i < HEADERS.length; i++) {
				req.append(HEADERS[i][0] + ": " + HEADERS[i][1] + "\n");
			}
			System.out.println(req.toString());

			// Sincronizzazione delle richieste tramite una lock statica
			WalletTask.httpClientLock.lock();

			conn.connect();
			InputStream is = conn.getInputStream();
			// Il body della risposta è soltanto un reale, per cui occupa pochi byte
			// e può essere fatta l'assunzione ragionevole di non bloccare il thread 
			// per troppo tempo su questa chiamata
			byte[] bb = is.readAllBytes();
			conn.disconnect();

			// Estraggo varie informazioni
			int bytes_read = bb.length;
			int statusCode = conn.getResponseCode();
			int contentLenght = conn.getContentLength();
			String ctype = conn.getContentType();
			WalletTask.httpClientLock.unlock();

			String content = new String(bb);

			StringBuffer sbuf = new StringBuffer();
			sbuf.append("=== HTTP Response ===")
					.append("\nResp code: " + statusCode)
					.append("\nLenght: " + contentLenght)
					.append("\nContent-Type: " + ctype)
					.append("\nContent: " + content + " (read " + bytes_read + " bytes)");
			System.out.println(sbuf.toString());
			Double exchange_rate = 0.0;
			if (statusCode == HttpURLConnection.HTTP_OK
					&& contentLenght == bytes_read
					&& ctype.contains("text/plain")) {
				exchange_rate = Double.valueOf(content);

			} else {
				return "-1.0";
			}

			double val = wallet * exchange_rate;
			// FIXME: debug print
			System.out.printf("wallet * exchange_rate = %f * %f = %f BTC\n", wallet, exchange_rate, val);
			return String.valueOf(val);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Impossibile completare la richiesta: " + e.getMessage());
			return "0.0";
		}
	}
}
