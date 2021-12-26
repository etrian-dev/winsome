package WinsomeTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import WinsomeServer.Post;
import WinsomeServer.User;
import WinsomeServer.WinsomeServer;

public class ShowFeedTask extends Task implements Callable<String> {
	private String currentUser;
	private WinsomeServer servRef;

	public ShowFeedTask(String loggedUser, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("ShowFeed");
		this.currentUser = loggedUser;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * Metodo per ottenere il feed di post di un utente
	 * 
	 * @return Il risultato dell'operazione richiesta è una stringa:
	 */
	public String call() {
		User u = this.servRef.getUser(this.currentUser);
		if (u == null) {
			return "NoUser";
		}
		StringBuffer sbuf = new StringBuffer();
		// Aggiungo tutti i post del blog, poi ordino per timestamp decrescente
		List<Post> feed = new ArrayList<>();
		for (String follow : u.getFollowing()) {
			feed.addAll(this.servRef.getBlog(follow));
		}
		// L'ordinamento effettuato tramite una lambda che implementa Comparator
		Collections.sort(feed, (a, b) -> ((Long) (b.getTimestamp() - a.getTimestamp())).intValue());
		for (Post p : feed) {
			sbuf.append("Id:" + p.getPostID() + "\n");
			sbuf.append("Autore:" + p.getAuthor() + "\n");
			sbuf.append("Titolo:" + p.getTitle() + "\n");
		}
		if (sbuf.length() == 0) {
			return "NessunPost";
		}
		return sbuf.toString();
	}
}
