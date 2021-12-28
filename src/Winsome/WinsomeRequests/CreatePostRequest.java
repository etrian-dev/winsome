package Winsome.WinsomeRequests;

public class CreatePostRequest extends Request {
	private String author;
	private String title;
	private String content;

	public CreatePostRequest() {
		super.setKind("CreatePost");
		this.author = null;
		this.title = null;
		this.content = null;
	}

	public CreatePostRequest(String author, String postTitle, String postContent) {
		super.setKind("CreatePost");
		this.author = author;
		this.title = postTitle;
		this.content = postContent;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nAuthor: " + this.author
				+ "\nTitle: " + this.title
				+ "\nContent: " + this.content;
	}

	// Getters & setters
	public String getAuthor() {
		return this.author;
	}

	public String getTitle() {
		return this.title;
	}

	public String getContent() {
		return this.content;
	}

	public void setAuthor(String auth) {
		this.author = auth;
	}

	public void setTitle(String postTitle) {
		this.title = postTitle;
	}

	public void setContent(String postContent) {
		this.content = postContent;
	}
}
