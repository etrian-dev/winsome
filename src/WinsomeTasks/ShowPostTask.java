package WinsomeTasks;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

import WinsomeServer.Comment;
import WinsomeServer.Post;
import WinsomeServer.Vote;
import WinsomeServer.WinsomeServer;

public class ShowPostTask extends Task implements Callable<String> {
	private long postID;
	private String currentUser;
	private WinsomeServer servRef;

	public ShowPostTask(Long id, String loggedUser, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("ShowPost");
		this.postID = id;
		this.currentUser = loggedUser;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString() + "\nId: " + this.postID;
	}

	/**
	 * Metodo che ritorna ad un client una rappresentazione del post richiesto, 
	 * oppure un errore se il post non esiste.
	 * 
	 * @return La rappresentazione del post, secondo il seguente formato:
	 * Title:<titolo>
	 * Contenuto:<cont>
	 * Voti:<num> positivi, <num> negativi
	 * Commenti:<user1>:<comment1>:<user2>:<comment2>:[...]
	 */
	public String call() {
		// Recupero il post dalla mappa globale
		Post p = this.servRef.getPost(this.postID);
		if (p == null) {
			return "Errore";
		}
		// Posso vederlo sse sono l'autore o seguo l'autore del post
		if (!(p.getAuthor().equals(this.currentUser)
				|| servRef.getUser(this.currentUser).getFollowing().contains(p.getAuthor()))) {
			return "NonAutorizzato";
		}
		// Costruisco la rappresentazione del post con il seguente formato
		StringBuffer pView = new StringBuffer();
		pView.append("Titolo:" + p.getTitle() + "\n");
		pView.append("Contenuto:" + p.getContent() + "\n");
		pView.append("Voti:");
		int countPos = 0;
		for (Vote v : p.getVotes()) {
			if (v.getIsLike()) {
				countPos++;
			}
		}
		pView.append(countPos + " positivi, " + (p.getVotes().size() - countPos) + " negativi\n");
		// Scrivo il numero di commenti sulla risposta
		pView.append("Commenti:" + p.getComments().size() + "\n");
		// Commenti sono ordinati per timestamp: dal più recente al meno recente
		for (Comment c : p.getComments()) {
			LocalDateTime dateTm = LocalDateTime.ofInstant(Instant.ofEpochMilli(c.getTimestamp()),
					ZoneId.systemDefault());
			String dateString = dateTm.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			pView.append(c.getAuthor() + "|" + c.getContent() + " (" + dateString + ")" + "\n");
		}
		return pView.toString();
	}
}
