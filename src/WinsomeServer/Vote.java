package WinsomeServer;

public class Vote {
	private long timestamp;
	private String voter;
	private boolean isLike;

	public Vote() {
		this.timestamp = 0;
		this.voter = null;
		this.isLike = true; // voto positivo di default
	}

	public Vote(String liker, boolean like_dislike) {
		this.timestamp = System.currentTimeMillis();
		this.voter = liker;
		this.isLike = like_dislike;
	}

	// Getters & setters
	public long getTimestamp() {
		return this.timestamp;
	}

	public String getVoter() {
		return this.voter;
	}

	public boolean getIsLike() {
		return this.isLike;
	}

	public boolean setTimestamp(long tstamp) {
		if (tstamp < 0 || tstamp > System.currentTimeMillis()) {
			return false;
		}
		this.timestamp = tstamp;
		return true;
	}

	public void setVoter(String liker) {
		this.voter = liker;
	}

	public void setIsLike(boolean like_dislike) {
		this.isLike = like_dislike;
	}
}
