package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.WinsomeServer;

/**
 * Task che implementa il rewin di un post
 */
public class RewinTask extends Task implements Callable<Long> {
	long postID;
	String currentUser;
	WinsomeServer servRef;

	public RewinTask(long post, String loggedUser, WinsomeServer serv) {
		super.setKind("RewinPost");
		this.postID = post;
		this.currentUser = loggedUser;
		this.servRef = serv;
	}

	/**
	 * Metodo per effettuare il rewin di un post (solo se autorizzato)
	 *
	 * @return Il risultato dell'operazione richiesta è un intero:
	 * <ul>
	 * <li>-2: all'ID specificato nella richiesta non corrisponde alcun post in Winsome</li>
	 * <li>-1: l'utente che ha effettuato la richiesta non esiste</li>
	 * <li>&ge; 0: sse il rewin &egrave; stato effettuato con successo (restituito id del rewin)</li>
	 * </ul>
	 */
	public Long call() {
		if (!this.servRef.getUsernames().contains(this.currentUser)) {
			return -1L;
		}
		Post p = this.servRef.getPost(postID);
		if (p == null) {
			return -2L;
		}
		long rewinPostID = 0;
		do {
			rewinPostID = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
		} while (servRef.getPost(rewinPostID) != null);
		// creazione post
		Post rewinPost = new Post(rewinPostID, true, this.currentUser, p.getTitle(), p.getContent());
		// Setto ID del post originale
		rewinPost.setOriginalID(p.getPostID());
		// Il post creato deve essere inserito nel blog dell'autore
		// e nella mappa globale dei post
		this.servRef.addPost(rewinPost);
		return rewinPost.getPostID();
	}
}
