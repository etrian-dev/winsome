package WinsomeTasks;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import WinsomeServer.Post;
import WinsomeServer.WinsomeServer;

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

	/** Metodo per effettuare il rewin di un post */
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
		Post rewinPost = new Post(rewinPostID, true, p.getAuthor(), p.getTitle(), p.getContent());
		// Setto ID del post originale
		rewinPost.setOriginalID(p.getPostID());
		// Il post creato deve essere inserito nel blog dell'autore
		this.servRef.addPostToBlog(this.currentUser, rewinPost);
		// e nella mappa globale dei post
		this.servRef.addPost(rewinPost);
		return rewinPost.getPostID();
	}
}
