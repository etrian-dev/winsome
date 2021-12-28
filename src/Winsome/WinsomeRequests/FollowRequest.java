package Winsome.WinsomeRequests;

/**
 * Classe che implementa una richiesta di follow/unfollow
 * dall'utente followed verso l'utente followed
 */
public class FollowRequest extends Request {
	String follower;
	String followed;
	/** true se richiesta di following, false altrimenti */
	boolean type;

	public FollowRequest() {
		super.setKind("Follow");
		this.follower = null;
		this.followed = null;
		this.type = true;
	}

	public FollowRequest(String from, String to, boolean follow_unfollow) {
		super.setKind("Follow");
		this.follower = from;
		this.followed = to;
		this.type = follow_unfollow;
	}

	public String getFollower() {
		return this.follower;
	}

	public String getFollowed() {
		return this.followed;
	}

	public boolean getType() {
		return this.type;
	}

	public boolean setFollower(String user) {
		if (user == null) {
			return false;
		}
		this.follower = user;
		return true;
	}

	public boolean setFollowed(String user) {
		if (user == null) {
			return false;
		}
		this.followed = user;
		return true;
	}

	public void setType(boolean follow_unfollow) {
		this.type = follow_unfollow;
	}
}
