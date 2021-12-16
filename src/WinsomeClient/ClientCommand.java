package WinsomeClient;

/**
 * Classe che contiene un comando del client
 */
public class ClientCommand {
	/**
	 * Enumerazione dei possibili comandi che il client può eseguire
	 */
	public enum Command {
		REGISTER("register"), LOGIN("login"), LOGOUT("logout"), LIST("list"), FOLLOW("follow"), UNFOLLOW(
				"unfollow"), BLOG("blog"), FEED(
						"feed"), WALLET("wallet"), POST("post"), SHOW("show"), DELETE(
								"delete"), REWIN("rewin"), RATE(
										"rate"), COMMENT("comment"), UNKNOWN_COMMAND("unknown");

		private String text;

		private Command(String cmd) {
			this.text = cmd;
		}

		public static Command fromString(String s) {
			try {
				return valueOf(s.toUpperCase());
			} catch (IllegalArgumentException ill) {
				return Command.UNKNOWN_COMMAND;
			}
		}
	}

	private Command ClientCommand;
	private String[] args = null;

	public ClientCommand(String[] tokens) throws IllegalArgumentException {
		if (tokens == null || tokens.length == 0) {
			throw new IllegalArgumentException();
		}
		this.ClientCommand = Command.fromString(tokens[0]);
		this.args = new String[tokens.length - 1];
		for (int i = 1; i < tokens.length; i++) {
			this.args[i - 1] = tokens[i];
		}
	}

	public Command getCommand() {
		return this.ClientCommand;
	}

	public String[] getArgs() {
		return this.args.clone();
	}

	public String getArg(int idx) {
		if (idx < 0 || idx >= this.args.length) {
			return null;
		}
		return this.args[idx];
	}

}
