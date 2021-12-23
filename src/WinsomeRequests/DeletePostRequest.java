package WinsomeRequests;

public class DeletePostRequest extends Request {
	private long postID;

	public DeletePostRequest() {
		super.setKind("DeletePost");
		this.postID = -1;
	}

	public DeletePostRequest(long id) {
		super.setKind("CreatePost");
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
