package WinsomeServer;

import java.io.Serializable;
import java.util.List;

/**
 * Classe che contiene i dati di un utente di Winsome
 */
public class User implements Serializable {
	private long userID;
	private String username;
	private String password;
	private List<String> tags;

	// Getters
	public long getUserID() {
		return this.userID;
	}

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
	public boolean setUserID(Long uid) {
		if (uid == null) {
			return false;
		}
		this.userID = uid;
		return true;
	}

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

	public boolean setTags(List<String> tags) {
		if (tags == null) {
			return false;
		}
		for (String tag : tags) {
			this.tags.add(tag);
		}
		return true;
	}
}
