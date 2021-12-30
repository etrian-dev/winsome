package Winsome.WinsomeServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe che contiene i dati di un utente di Winsome
 */
public class User {
	/**
	 * Massimo numero di tag per utente (non serializzabile)
	 */
	public transient static int MAX_TAGCOUNT = 5;

	/** Flag per indicare se l'utente è loggato o meno (non serializzata) */
	private transient boolean loggedIn;

	private String username;
	private String password;
	private Set<String> tags;
	private Set<String> followers;
	private Set<String> following;
	/** valore, in wincoin, delle ricompense accumulate dall'utente */
	private double wallet;
	private List<Transaction> transactions;
	/** Numero di commenti totale effettuati dall'utente */
	private int totalComments;

	/**
	 * crea un oggetto utente vuoto (per deserializzazione)
	 */
	public User() {
		this.loggedIn = false;
		this.username = null;
		this.password = null;
		this.tags = new HashSet<>();
		this.followers = new HashSet<>();
		this.following = new HashSet<>();
		this.wallet = 0L;
		this.transactions = new ArrayList<>();
		this.totalComments = 0;
	}

	@Override
	public String toString() {
		StringBuilder sbuf = new StringBuilder("=== USER ===");
		sbuf.append("\nusername: " + this.username);
		sbuf.append("\npassword: " + this.password);
		sbuf.append("\ntags: " + this.tags.toString());
		sbuf.append("\nfollowers: " + this.followers.toString());
		sbuf.append("\nfollowing: " + this.following.toString());
		sbuf.append("\nwallet: " + this.wallet + " wincoin");
		sbuf.append("\ntransactions: " + this.transactions.toString());
		sbuf.append("\n# of comments: " + this.totalComments);
		return sbuf.toString();
	}

	// Operazioni di modifica dello stato dell'utente

	public boolean isLogged() {
		return this.loggedIn;
	}

	public synchronized void login() {
		this.loggedIn = true;
	}

	public synchronized void logout() {
		this.loggedIn = false;
	}

	public synchronized boolean removeFollowing(String user) {
		return this.following.remove(user);
	}

	public synchronized boolean removeFollower(String user) {
		return this.followers.remove(user);
	}

	/**
	 * Aggiunge la somma reward (espressa in wincoin) al wallet dell'utente.
	 * 
	 * La somma da aggiungere può anche avere un valore negativo: in tal caso 
	 * viene sottratta al valore del wallet, previo controllo che il saldo
	 * risultante non sia negativo
	 * @param reward la somma (misurata in Wincoin) da aggiungere al wallet
	 */
	public boolean addReward(double reward) {
		if (this.wallet + reward < 0) {
			// saldo sarebbe negativo: operazione non consentita
			return false;
		}
		this.wallet += reward;
		// inserisce la nuova transazione autorizzata nella lista
		this.transactions.add(new Transaction(System.currentTimeMillis(), reward));
		return true;
	}

	/**
	 * Meotodo per incrementare il numero di commenti fatti dall'utente
	 */
	public void addComment() {
		this.totalComments += 1;
	}

	// Getters
	public String getUsername() {
		return (this.username == null ? null : new String(this.username));
	}

	public String getPassword() {
		return (this.password == null ? null : new String(this.password));
	}

	public Set<String> getTags() {
		return Set.copyOf(this.tags);
	}

	public Set<String> getFollowers() {
		return Set.copyOf(this.followers);
	}

	public Set<String> getFollowing() {
		return Set.copyOf(this.following);
	}

	public double getWallet() {
		return this.wallet;
	}

	public List<Transaction> getTransactions() {
		return List.copyOf(this.transactions);
	}

	public int getTotalComments() {
		return this.totalComments;
	}

	// Setters
	public boolean setUsername(String user) {
		if (user == null) {
			return false;
		}
		this.username = user;
		return true;
	}

	public boolean setPassword(String pwd) {
		if (pwd == null) {
			return false;
		}
		this.password = pwd;
		return true;
	}

	public boolean setTag(String newTag) {
		if (newTag == null
				|| (this.tags.size() == User.MAX_TAGCOUNT && !this.tags.contains(newTag))) {
			return false;
		}
		return this.tags.add(newTag);
	}

	public boolean setFollower(String newFollower) {
		if (newFollower == null) {
			return false;
		}
		return this.followers.add(newFollower);
	}

	public boolean setFollowing(String newFollowing) {
		if (newFollowing == null) {
			return false;
		}
		return this.following.add(newFollowing);
	}

	public boolean setWallet(double value) {
		if (value < 0L) {
			return false;
		}
		this.wallet = value;
		return true;
	}

	public void setTransactions(List<Transaction> tList) {
		this.transactions = tList;
	}

	public boolean setTotalComments(int value) {
		if (value < 0) {
			return false;
		}
		this.totalComments = value;
		return true;
	}
}
