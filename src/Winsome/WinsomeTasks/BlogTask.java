package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;

import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.WinsomeServer;

public class BlogTask extends Task implements Callable<String> {
	private String user;
	private String currentUser;
	private WinsomeServer servRef;

	public BlogTask(String requester, String loggedUser, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Blog");
		this.user = requester;
		this.currentUser = loggedUser;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nRequester: " + this.user
				+ "\nCurrentUser: " + this.currentUser;
	}

	/**
	 * Metodo che ritorna ad un client la lista di post dell'utente loggato
	 *
	 * @return La rappresentazione dei post, secondo il seguente formato:
	 * Id:<id>
	 * Autore:<author>
	 * Titolo:<titolo>
	 */
	public String call() {
		// Posso vederlo sse il client ha richiesto di visualizzare il proprio blog
		if (!this.user.equals(this.currentUser)) {
			return "NonAutorizzato";
		}
		ConcurrentLinkedDeque<Post> all_posts = servRef.getBlog(this.currentUser);
		// Costruisco la rappresentazione dei post
		StringBuffer blogView = new StringBuffer();
		for (Post p : all_posts) {
			blogView.append("Id:" + p.getPostID() + "\n");
			blogView.append("Autore:" + p.getAuthor() + "\n");
			blogView.append("Titolo:" + p.getTitle() + "\n");
		}
		if (blogView.length() == 0) {
			return "NessunPost";
		}
		return blogView.toString();
	}
}
