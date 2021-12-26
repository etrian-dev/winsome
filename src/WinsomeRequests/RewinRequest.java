package WinsomeRequests;

public class RewinRequest extends Request {
	private long postID;

	public RewinRequest() {
		super.setKind("RewinPost");
		this.postID = -1;
	}

	public RewinRequest(long id) {
		super.setKind("RewinPost");
		this.postID = id;
	}

	@Override
	public String toString() {
		return super.toString() + "\nId: " + this.postID;
	}

	// Getters & setters
	public long getPostID() {
		return this.postID;
	}

	public void setPostID(long id) {
		this.postID = id;
	}
}
