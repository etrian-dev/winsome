package Winsome.WinsomeServer;

import java.util.Date;

public class Comment {
	private long timestamp;
	private String author;
	private String content;

	public Comment() {
		this.timestamp = 0;
		this.author = null;
		this.content = null;
	}

	public Comment(String commenter, String text) {
		this.timestamp = System.currentTimeMillis();
		this.author = commenter;
		this.content = text;
	}

	@Override
	public String toString() {
		return "=== Comment ==="
				+ "\ndate: " + new Date(this.timestamp)
				+ "\nvoter: " + this.author
				+ "\ncontent: " + this.content;
	}

	// Getters & setters
	public long getTimestamp() {
		return this.timestamp;
	}

	public String getAuthor() {
		return this.author;
	}

	public String getContent() {
		return this.content;
	}

	public boolean setTimestamp(long tstamp) {
		if (tstamp < 0 || tstamp > System.currentTimeMillis()) {
			return false;
		}
		this.timestamp = tstamp;
		return true;
	}

	public void setAuthor(String commenter) {
		this.author = commenter;
	}

	public void setContent(String text) {
		this.content = text;
	}
}
