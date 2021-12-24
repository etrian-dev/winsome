package WinsomeRequests;

public class CommentRequest extends Request {
	private long postID;
	private String comment;

	public CommentRequest() {
		super.setKind("CommentPost");
		this.postID = -1;
	}

	public CommentRequest(long id, String newComment) {
		super.setKind("CommentPost");
		this.postID = id;
		this.comment = newComment;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nId: " + this.postID
				+ "\nComment: " + this.comment;
	}

	// Getters & setters
	public long getPostID() {
		return this.postID;
	}

	public String getComment() {
		return this.comment;
	}

	public void setPostID(long id) {
		this.postID = id;
	}

	public void setComment(String comm) {
		this.comment = comm;
	}
}
