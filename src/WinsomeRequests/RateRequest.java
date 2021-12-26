package WinsomeRequests;

public class RateRequest extends Request {
	private long postID;
	private int vote;

	public RateRequest() {
		super.setKind("RatePost");
		this.postID = -1;
		this.vote = 0;
	}

	public RateRequest(long id, int newvote) {
		super.setKind("RatePost");
		this.postID = id;
		this.vote = newvote;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nId: " + this.postID
				+ "\nVote: " + (this.vote > 0 ? "+1" : (this.vote == 0 ? "-" : "-1"));
	}

	// Getters & setters
	public long getPostID() {
		return this.postID;
	}

	public int getVote() {
		return this.vote;
	}

	public void setPostID(long id) {
		this.postID = id;
	}

	public void setVote(int value) {
		if (value < 0) {
			this.vote = -1;
		}
		if (value > 0) {
			this.vote = 1;
		}
	}
}
