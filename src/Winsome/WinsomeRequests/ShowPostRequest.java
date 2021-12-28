package Winsome.WinsomeRequests;

public class ShowPostRequest extends Request {
	private long postID;

	public ShowPostRequest() {
		super.setKind("ShowPost");
		this.postID = -1;
	}

	public ShowPostRequest(long id) {
		super.setKind("ShowPost");
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
