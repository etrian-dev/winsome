package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.User;
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
	 * <li>-3: il post fornito non fa parte del proprio feed 
	 * (l'autore del post non &egrave; tra i propri following)</li>
	 * <li>-2: all'ID specificato nella richiesta non corrisponde alcun post in Winsome</li>
	 * <li>-1: l'utente che ha effettuato la richiesta non esiste</li>
	 * <li>&ge; 0: sse il rewin &egrave; stato effettuato con successo (restituito id del rewin)</li>
	 * </ul>
	 */
	public Long call() {
		// Username inesistente
		if (!this.servRef.getUsernames().contains(this.currentUser)) {
			return -1L;
		}
		// Post inesistente
		Post p = this.servRef.getPost(postID);
		if (p == null) {
			return -2L;
		}
		// Post non fa parte del propio feeed
		// NOTA: data la gestione degli autori ed id dei rewin svolta in seguito
		// questo controllo considera p.getAuthor come l'autore del post, anche se è un rewin
		// (altrimenti chiunque voglia effettuare un rewin dovrebbe seguire l'autore del post originale)
		User u = this.servRef.getUser(currentUser);
		if (!u.getFollowing().contains(p.getAuthor())) {
			return -3L;
		}
		// Creo il post rewin: ha un id univoco e l'autore del post diventa
		// chi effettua il rewin, ma vengono settati i campi
		// originalID e originalAuthor a quelli del post originale
		long rewinPostID = 0;
		do {
			rewinPostID = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
		} while (servRef.getPost(rewinPostID) != null);
		Post rewinPost = new Post(rewinPostID, true, this.currentUser, p.getTitle(), p.getContent());
		// Setto ID del post originale: se il post di cui faccio il rewin è anch'esso un rewin devo
		// recuperare l'id del post originale per propagarlo, altrimenti considererei originale
		// il rewin di un post, assegnando quindi eventuali ricompense a chi ha effettuato il rewin
		// e non all'autore originale
		Long originalID = p.getPostID();
		String originalAuthor = p.getAuthor();
		if (p.getIsRewin()) {
			originalID = p.getOriginalID();
			originalAuthor = p.getOriginalAuthor();
		}
		rewinPost.setOriginalID(originalID);
		rewinPost.setOriginalAuthor(originalAuthor);
		// Il post creato deve essere inserito nel blog dell'autore
		// e nella mappa globale dei post
		this.servRef.addPost(rewinPost);
		return rewinPost.getPostID();
	}
}
