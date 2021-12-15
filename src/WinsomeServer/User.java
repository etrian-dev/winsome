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

	private String username;
	private String password;
	private List<String> tags;

	/**
	 * crea un oggetto utente vuoto (per deserializzazione)
	 */
	public User() {
		this.username = null;
		this.password = null;
		this.tags = null;
	}

	/**
	 * 
	 */
	public User(String username, String password, List<String> tagList) {
		// TODO: error checking params + throw exception on illegal values
		// username trasformato in minuscolo
		this.username = username.toLowerCase();
		this.password = password;
		// tag trasformati in minuscolo
		this.tags = new ArrayList<>();
		for (String tag : tagList) {
			this.tags.add(tag.toLowerCase());
		}
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
}
