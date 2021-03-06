package Winsome.WinsomeTasks;

/**
 * Classe che implementa una task che il server Winsome può eseguire
 */
public class Task {
	private String state;
	private String kind;
	private String msg;

	public Task() {
		this.state = "Invalid";
		this.kind = null;
		this.msg = null;
	}

	@Override
	public String toString() {
		return "=== Task ==="
				+ "\nState: " + this.state
				+ "\nKind: " + this.kind
				+ "\nMessage: " + this.msg;
	}

	public String getKind() {
		return this.kind;
	}

	public void setKind(String tKind) {
		this.kind = tKind;
	}

	public String getState() {
		return this.state;
	}

	public void setInvalid() {
		this.state = "Invalid";
	}

	public void setValid() {
		this.state = "Valid";
	}

	public String getMessage() {
		return this.msg;
	}

	public void setMessage(String newMsg) {
		this.state = newMsg;
	}
}
