package WinsomeServer;

import java.util.HashSet;
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
	// TODO: add wallet
	private Set<String> followers;
	private Set<String> following;

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
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("username: " + this.username + "\n");
		sbuf.append("password: " + this.password + "\n");
		sbuf.append("tags: ");
		sbuf.append(this.tags.toString());
		return sbuf.toString();
	}

	public boolean isLogged() {
		return this.loggedIn;
	}

	public synchronized void login() {
		this.loggedIn = true;
	}

	public synchronized void logout() {
		this.loggedIn = false;
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
		this.tags.add(newTag);
		return true;
	}

	public boolean setFollower(String newFollower) {
		if (newFollower == null) {
			return false;
		}
		this.followers.add(newFollower);
		return true;
	}

	public boolean setFollowing(String newFollowing) {
		if (newFollowing == null) {
			return false;
		}
		this.following.add(newFollowing);
		return true;
	}
}
