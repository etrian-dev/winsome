package Winsome.WinsomeTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.User;
import Winsome.WinsomeServer.WinsomeServer;

/**
 * Task che implementa la restituzione del feed di un utente Winsome
 */
public class ShowFeedTask extends Task implements Callable<String> {
	private String currentUser;
	private ObjectMapper mapper;
	private WinsomeServer servRef;

	public ShowFeedTask(String loggedUser, ObjectMapper objMapper, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("ShowFeed");
		this.currentUser = loggedUser;
		this.mapper = objMapper;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * Metodo per ottenere il feed di post di un utente
	 *
	 * @return Il risultato dell'operazione richiesta è una stringa contenente
	 * la lista di post (ordinati per timestamp decrescente) degli utenti seguiti
	 * dal richiedente (quello loggato da questa istanza del client), oppure un errore
	 */
	public String call() {
		User u = this.servRef.getUser(this.currentUser);
		if (u == null) {
			return "Errore:utente inesistente";
		}
		// Aggiungo tutti i post del blog, poi ordino per timestamp crescente
		List<Post> feed = new ArrayList<>();
		for (String follow : u.getFollowing()) {
			feed.addAll(this.servRef.getBlog(follow));
		}
		// L'ordinamento effettuato tramite una lambda che implementa Comparator
		Collections.sort(feed, (a, b) -> ((Long) (a.getTimestamp() - b.getTimestamp())).intValue());
		try {
			String feedStr = this.mapper.writeValueAsString(feed);
			return feedStr;
		} catch (JsonProcessingException e) {
			System.err.println("Impossibile completare " + this.getKind() + ": " + e.getMessage());
			return "Errore:impossibile completare la richiesta";
		}
	}
}
