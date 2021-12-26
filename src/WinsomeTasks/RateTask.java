package WinsomeTasks;

import java.util.concurrent.Callable;

import WinsomeServer.Post;
import WinsomeServer.Vote;
import WinsomeServer.WinsomeServer;

public class RateTask extends Task implements Callable<Integer> {
	private long postID;
	private int vote;
	private String currentUser;
	private WinsomeServer servRef;

	public RateTask(long id, int newvote, String loggedUser, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("RatePost");
		this.postID = id;
		this.vote = newvote;
		this.currentUser = loggedUser;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nId: " + this.postID
				+ "\nVote: " + this.vote;
	}

	/**
	 * Metodo per votare un post (solo se autorizzato)
	 * 
	 * @return Il risultato dell'operazione richiesta è un intero:
	 * <ul>
	 * <li>0: sse il voto &egrave; stato registrato con successo</li>
	 * <li>1: l'autore del post ha tentato di votarlo: operazione non consentita</li>
	 * <li>2: il votante aveva gi&agrave; votato il post in precedenza</li>
	 * <li>-1: l'utente non &egrave; autorizzato a votare il post</li>
	 * <li>-2: all'ID specificato nella richiesta non corrisponde alcun post in Winsome</li>
	 * </ul>
	 */
	public Integer call() {
		// Recupero post e autorizzazione alla rimozione
		Post p = this.servRef.getPost(this.postID);
		if (p == null) {
			return -2;
		}
		// Controllo che il votante non sia l'autore del post
		if (p.getAuthor().equals(this.currentUser)) {
			return 1;
		}
		for (Vote v : p.getVotes()) {
			if (v.getVoter().equals(this.currentUser)) {
				// Il post era stato già votato da chi ha fatto la richiesta: operazione negata
				return 2;
			}
		}
		// Utente non autorizzato perché non segue l'autore del post
		if (!this.servRef.getUser(this.currentUser).getFollowing().contains(p.getAuthor())) {
			// Unauthorized
			return -1;
		}
		// Aggiunta del voto al post in questione
		p.setVote(new Vote(this.currentUser, (this.vote > 0 ? true : false)));
		return 0;
	}
}
