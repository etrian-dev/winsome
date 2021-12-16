package WinsomeClient;

/**
 * Classe che implementa il parser dei comandi letti da standard input
 */
public class CommandParser {
	public static ClientCommand parseCommand(String[] command_tokens) {

		ClientCommand comm = null;
		try {
			comm = new ClientCommand(command_tokens);
		} catch (IllegalArgumentException e) {
			return null;
		}
		String[] args = null;
		switch (comm.getCommand()) {
			// Comando register <username> <pwd> <tags>
			case REGISTER:
				return (comm.getArgs().length >= 3 ? comm : null);
			// Comando login <username> <pwd>
			case LOGIN:
				return (comm.getArgs().length == 2 ? comm : null);
			// Comando logout
			case LOGOUT:
				return (comm.getArgs().length == 0 ? comm : null);
			// Comando list <followers|following>
			case LIST:
				args = comm.getArgs();
				if (args != null && args.length == 1
						&& (args[0].equals("users")
								|| args[0].equals("followers")
								|| args[0].equals("following"))) {
					return comm;
				}
				return null;
			// Comando follow <username>
			// Comando unfollow <username>
			case FOLLOW:
			case UNFOLLOW:
				return (comm.getArgs().length == 1 ? comm : null);
			// Comando blog
			// Comando feed
			case BLOG:
			case FEED:
				return (comm.getArgs() == null ? comm : null);
			// Comando post <title> <content>
			case POST:
				return (comm.getArgs().length == 2 ? comm : null);
			// Comando show post <postID>
			case SHOW:
				args = comm.getArgs();
				if (args != null && args.length == 2 && args[0].equals("post")) {
					try {
						Long.valueOf(args[1]);
						return comm;
					} catch (NumberFormatException e) {
						return null;
					}
				}
				return null;
			// Comando delete <postID>
			// Comando rewin <postID>
			case DELETE:
			case REWIN:
				args = comm.getArgs();
				if (args != null && args.length == 1) {
					try {
						Long.valueOf(args[0]);
						return comm;
					} catch (NumberFormatException e) {
						return null;
					}
				}
				return null;
			// Comando rate <postID> <vote>
			case RATE:
				args = comm.getArgs();
				if (args != null && args.length == 2) {
					try {
						Long.valueOf(args[0]);
						int vote = Integer.valueOf(args[1]);
						if (vote == 1 || vote == -1) {
							return comm;
						}
						return null;
					} catch (NumberFormatException e) {
						return null;
					}
				}
				return null;
			// Comando comment <postID> <comment>
			case COMMENT:
				args = comm.getArgs();
				if (args != null && args.length == 2) {
					try {
						Long.valueOf(args[0]);
						int vote = Integer.valueOf(args[1]);
						if (vote == 1 || vote == -1) {
							return comm;
						}
						return null;
					} catch (NumberFormatException e) {
						return null;
					}
				}
				return null;
			// Comando wallet [btc]
			case WALLET:
				args = comm.getArgs();
				if (args == null || (args.length == 1 && args[1].equals("btc"))) {
					return comm;
				}
				return null;
			default:
				// comando non riconosciuto
				return null;
		}
	}
}
