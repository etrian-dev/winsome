package WinsomeServer;

import java.util.ArrayList;
import java.util.List;

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
	private List<String> tags;
	// TODO: add wallet
	private List<String> followers;
	private List<String> following;

	/**
	 * crea un oggetto utente vuoto (per deserializzazione)
	 */
	public User() {
		this.loggedIn = false;
		this.username = null;
		this.password = null;
		this.tags = new ArrayList<>();
		this.followers = new ArrayList<>();
		this.following = new ArrayList<>();
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("username: " + this.username + "\n");
		sbuf.append("password: " + this.password + "\n");
		sbuf.append("tags: {");
		int i = 0;
		for (i = 0; i < this.tags.size() - 1; i++) {
			sbuf.append(this.tags.get(i) + ", ");
		}
		sbuf.append(this.tags.get(i) + "}\n");
		return sbuf.toString();
	}

	public boolean isLogged() {
		return this.loggedIn;
	}

	public synchronized void login() {
		this.loggedIn = true;
	}

	// Getters
	public String getUsername() {
		return (this.username == null ? null : new String(this.username));
	}

	public String getPassword() {
		return (this.password == null ? null : new String(this.password));
	}

	public List<String> getTags() {
		return List.copyOf(this.tags);
	}

	public List<String> getFollowers() {
		return List.copyOf(this.followers);
	}

	public List<String> getFollowing() {
		return List.copyOf(this.following);
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
		if (newFollower == null
				|| (this.tags.size() == User.MAX_TAGCOUNT && !this.tags.contains(newFollower))) {
			return false;
		}
		this.followers.add(newFollower);
		return true;
	}

	public boolean setFollowing(String newFollowing) {
		if (newFollowing == null
				|| (this.tags.size() == User.MAX_TAGCOUNT && !this.tags.contains(newFollowing))) {
			return false;
		}
		this.following.add(newFollowing);
		return true;
	}
}
