package WinsomeTasks;

/**
 * Classe che implementa una task che il server Winsome può eseguire
 */
public class Task<T> {
	private String kind;
	private String state;
	private String msg;

	public Task() {
		this.kind = null;
		this.state = "Invalid";
		this.msg = null;
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
