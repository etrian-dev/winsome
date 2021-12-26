package WinsomeTasks;

import java.util.concurrent.Callable;

import WinsomeServer.User;
import WinsomeServer.WinsomeServer;

/**
 * Task che implementa l'operazione di follow/unfollow tra utenti Winsome
 */
public class FollowTask extends Task implements Callable<Integer> {
	private String follower;
	private String followed;
	private boolean type;
	private WinsomeServer servRef;

	public FollowTask(String from, String to, boolean follow_unfollow, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Follow");
		this.follower = from;
		this.followed = to;
		this.type = follow_unfollow;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nFollower: " + this.follower
				+ "\nFollowed: " + this.followed
				+ "\nType: " + (this.type ? "Follow" : "Unfollow");
	}

	/**
	 * Metodo che esegue l'operazione di follow/unfollow, ritornandone l'esito
	 * 
	 * @return Il risultato dell'operazione richiesta è un intero:
	 * <ul>
	 * <li>0: l'operazione ha avuto successo (modificato lo stato dell'utente richiedente)</li>
	 * <li>1: l'utente richiedente non esiste in Winsome</li>
	 * <li>2: l'utente oggetto dell'operazione non esiste in Winsome</li>
	 * <li>3: l'utente richiedente [non] seguiva gi&agrave; l'oggetto della richiesta</li>
	 * </ul>
	 */
	public Integer call() {
		if (!this.servRef.getUsernames().contains(this.follower)) {
			return 1; // Utente follower non esistente
		}
		if (!this.servRef.getUsernames().contains(this.followed)) {
			return 2; // Utente followed non esistente
		}
		// Recupero l'utente
		User u = this.servRef.getUser(this.follower);
		User followed_user = this.servRef.getUser(followed);
		boolean ok = false;
		if (this.type) {
			// Richiesta di following: aggiungo utente da seguire ai following di u
			ok = u.setFollowing(followed);
			// e l'utente che ha effettuato la richiesta ai seguiti di followed

			ok = followed_user.setFollower(this.follower);
			return (ok ? 0 : 3);
		} else {
			// Richiesta di unfollow: rimuovo followed dai seguiti di follower
			ok = u.removeFollowing(followed);
			// e follower dalla lista di utenti che seguono followed
			ok = followed_user.removeFollower(this.follower);
			return (ok ? 0 : 3);
		}
	}
}
