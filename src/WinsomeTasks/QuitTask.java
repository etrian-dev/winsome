package WinsomeTasks;

public class QuitTask extends Task {
	private String username;

	public QuitTask(String user) {
		super.setInvalid();
		super.setKind("Quit");
		this.username = user;
	}

	@Override
	public String toString() {
		return super.toString() + "\nUsername: " + this.username;
	}

	public String getUsername() {
		return this.username;
	}
}
