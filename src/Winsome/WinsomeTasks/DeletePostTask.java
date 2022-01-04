package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;

import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.WinsomeServer;

/**
 * Task che implementa la cancellazione di un post dalla rete sociale
 */
public class DeletePostTask extends Task implements Callable<Integer> {
	private long postID;
	private String currentUser;
	private WinsomeServer servRef;

	public DeletePostTask(Long id, String loggedUser, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("DeletePost");
		this.postID = id;
		this.currentUser = loggedUser;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString() + "\nId: " + this.postID;
	}

	/**
	 * Metodo che esegue l'operazione di rimozione di un post (solo l'utente &egrave; autorizzato)
	 *
	 * @return Il risultato dell'operazione richiesta è un intero:
	 * <ul>
	 * <li>0 sse il post è stato rimosso con successo</li>
	 * <li>-1: l'utente non &egrave; autorizzato a rimuovere il post (non ne &egrave; l'autore)</li>
	 * <li>-2: all'ID specificato nella richiesta non corrisponde alcun post in Winsome</li>
	 * </ul>
	 */
	public Integer call() {
		// Recupero post e autorizzazione alla rimozione
		Post p = this.servRef.getPost(this.postID);
		if (p == null) {
			return -2;
		}
		// L'autore del post deve essere lo stesso che rimuove il post
		if (!p.getAuthor().equals(this.currentUser)) {
			// Unauthorized
			return -1;
		}
		// rimozione post
		// Il post creato deve essere rimosso dal blog dell'autore
		// e dalla mappa globale dei post
		servRef.rmPost(p);
		return 0;
	}
}
