package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;

import Winsome.WinsomeServer.Comment;
import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.WinsomeServer;

/**
 * Task che implementa la funzione di aggiunta di un commento ad un post in Winsome
 */
public class CommentTask extends Task implements Callable<Integer> {
	private long postID;
	private String comment;
	private String currentUser;
	private WinsomeServer servRef;

	public CommentTask(long id, String newComment, String loggedUser, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("CommentPost");
		this.postID = id;
		this.comment = newComment;
		this.currentUser = loggedUser;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nId: " + this.postID
				+ "\nComment: " + this.comment;
	}

	/**
	 * Metodo per eseguire il commento ad un post (solo se autorizzato)
	 *
	 * @return Il risultato dell'operazione richiesta è un intero:
	 * <ul>
	 * <li>0: sse il commento &egrave; stato registrato con successo</li>
	 * <li>1: l'autore del post ha tentato di commentarlo: operazione non consentita</li>
	 * <li>-1: l'utente non &egrave; autorizzato ad aggiungere un commento</li>
	 * <li>-2: all'ID specificato nella richiesta non corrisponde alcun post in Winsome</li>
	 * </ul>
	 */
	public Integer call() {
		// Recupero post e autorizzazione alla rimozione
		Post p = this.servRef.getPost(this.postID);
		if (p == null) {
			return -2;
		}
		// Controllo che il commentatore non sia l'autore del post
		if (p.getAuthor().equals(this.currentUser)) {
			return 1;
		}
		// Utente non autorizzato perché non segue l'autore del post
		if (!this.servRef.getUser(this.currentUser).getFollowing().contains(p.getAuthor())) {
			// Unauthorized
			return -1;
		}
		// Aggiunta del commento al post in questione
		p.setComment(new Comment(this.currentUser, this.comment));
		// Incremento il numero di commenti effettuati dall'utente
		this.servRef.getUser(this.currentUser).addComment();
		return 0;
	}
}
