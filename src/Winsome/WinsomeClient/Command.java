package Winsome.WinsomeClient;

/**
	 * Enumerazione dei possibili comandi che il client può eseguire
	 */
public enum Command {
	REGISTER("register"), LOGIN("login"), LOGOUT("logout"), LIST("list"), FOLLOW("follow"), UNFOLLOW(
			"unfollow"), BLOG("blog"), WALLET("wallet"), POST("post"), SHOW("show"), DELETE(
					"delete"), REWIN("rewin"), RATE(
							"rate"), COMMENT("comment"), QUIT("quit"), HELP("help"), UNKNOWN("unknown");

	private String text;

	private Command(String cmd) {
		this.text = cmd;
	}

	public static Command fromString(String s) {
		try {
			return valueOf(s.toUpperCase());
		} catch (IllegalArgumentException ill) {
			return Command.UNKNOWN;
		}
	}

	@Override
	public String toString() {
		return "=== Command ===\nText: " + this.text;
	}
}