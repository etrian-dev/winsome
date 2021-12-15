package WinsomeClient;

public enum Command {
	REGISTER("register"), LOGIN("login"), LOGOUT("logout"), LIST("list");

	private String command;

	private Command(String newCommand) {
		this.command = newCommand;
	}

	public static Command fromString(String comm) {
		return valueOf(comm);
	}
}
