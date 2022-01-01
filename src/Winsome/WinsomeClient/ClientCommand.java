package Winsome.WinsomeClient;

/**
 * Classe che contiene un comando del client
 */
public class ClientCommand {
	private Command ClientCommand;
	private String[] args = null;

	public ClientCommand(String[] tokens) throws IllegalArgumentException {
		if (tokens == null || tokens.length == 0) {
			throw new IllegalArgumentException();
		}
		this.ClientCommand = Command.fromString(tokens[0].toLowerCase());
		this.args = (tokens.length > 1 ? new String[tokens.length - 1] : null);
		for (int i = 1; i < tokens.length; i++) {
			this.args[i - 1] = tokens[i];
		}
	}

	public Command getCommand() {
		return this.ClientCommand;
	}

	public String[] getArgs() {
		return this.args;
	}

	public String getArg(int idx) {
		if (idx < 0 || idx >= this.args.length) {
			return null;
		}
		return this.args[idx];
	}

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
				return (comm.getArgs() != null && comm.getArgs().length >= 3 ? comm : null);
			// Comando login <username> <pwd>
			case LOGIN:
				return (comm.getArgs() != null && comm.getArgs().length == 2 ? comm : null);
			// Comando logout
			case LOGOUT:
				return (comm.getArgs() == null ? comm : null);
			// Comando list <followers|following|users>
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
				return (comm.getArgs() != null && comm.getArgs().length == 1 ? comm : null);
			// Comando blog
			case BLOG:
				return (comm.getArgs() == null ? comm : null);
			// Comando post <title> <content>
			case POST:
				return (comm.getArgs() != null && comm.getArgs().length == 2 ? comm : null);
			// Comando show feed
			// Comando show post <postID>
			case SHOW:
				args = comm.getArgs();
				if (args != null && (args.length == 2 && args[0].equals("post")
						|| (args.length == 1 && args[0].equals("feed")))) {
					try {
						if (args[0].equals("post")) {
							Long.valueOf(args[1]);
						}
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
						System.err.println(e + "\narg: " + comm.getArgs());
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
						return comm;
					} catch (NumberFormatException e) {
						return null;
					}
				}
				return null;
			// Comando wallet [btc]
			case WALLET:
				args = comm.getArgs();
				if (args == null || (args.length == 1 && args[0].equals("btc"))) {
					return comm;
				}
				return null;
			// Comando quit
			// Comando help
			case QUIT:
			case HELP:
				return (comm.getArgs() == null ? comm : null);
			default:
				// comando non riconosciuto
				String[] dummy = { "unknown" };
				return new ClientCommand(dummy);
		}
	}

	public static void getHelp() {
		StringBuilder buf = new StringBuilder("=== HELP ===\n");
		// Register
		buf.append("register <username> <password> <tags>\n");
		buf.append("\tRegistra un nuovo utente Winsome (RMI)\n");
		// Login & logout
		buf.append("login <username> <password>\n");
		buf.append("\tEsegue il login dell'utente specificato\n");
		buf.append("logout\n");
		buf.append("\tEsegue il logout dell'utente corrente\n");
		// list
		buf.append("list users\n");
		buf.append("\tStampa la lista degli utenti Winsome che hanno"
				+ "\n\talmeno un tag in comune con l'utente loggato\n");
		buf.append("list following\n");
		buf.append("\tStampa la lista degli utenti Winsome seguiti "
				+ "\n\tdall'utente loggato\n");
		buf.append("list followers\n");
		buf.append("\tStampa la lista degli utenti Winsome che seguono l'utente loggato"
				+ "\n\t(struttura dati del client, con RMI callback per aggiornamento)\n");
		// Follow & unfollow
		buf.append("follow <username>\n");
		buf.append("\tAggiunge l'utente username, se possibile, alla lista degli "
				+ "\n\tutenti seguiti dall'utente loggato\n");
		buf.append("unfollow <username>\n");
		buf.append("\tRimuove l'utente username, se possibile, alla lista degli "
				+ "\n\tutenti seguiti dall'utente loggato\n");
		// Blog & feed
		buf.append("blog\n");
		buf.append("\tStampa la lista di post presenti nel blog dell'utente loggato,"
				+ "\n\tordinati per timestamp decrescente\n");
		buf.append("show feed\n");
		buf.append("\tStampa la lista dei post pubblicati dagli utenti seguiti "
				+ "\n\tdall'utente loggato, ordinati per timestamp decrescente\n");
		buf.append("show post <postID>\n");
		buf.append("\tStampa l'id, titolo e contenuto del post con id specificato, "
				+ "\n\tse presente\n");
		// Manipolazione dei post
		buf.append("post <title> <content>\n");
		buf.append("\tPubblica nel blog dell'utente loggato il post con titolo e "
				+ "\n\tcontenuto indicati. Il titolo deve essere lungo al più 20 "
				+ "\n\tcaratteri ed il contenuto deve essere al più di 500 caratteri\n");
		buf.append("delete <postID>\n");
		buf.append("\tRimuove dal blog dell'utente loggato il post con id specificato,"
				+ "\n\tse possibile\n");
		buf.append("rewin <postID>\n");
		buf.append("\tEffettua il rewin di un post nel feed dell'utente loggato\n");
		buf.append("rate <postID> <vote>\n");
		buf.append("\tAggiunge un voto (1 o -1) al post con id specificato, "
				+ "\n\tse possibile\n");
		buf.append("comment <postID> <comment>\n");
		buf.append("\tAggiunge un commento al post con id specificato, se possibile\n");
		// Wallet
		buf.append("wallet [btc]\n");
		buf.append("\tSe non viene specificato alcun argomento stampa lo storico "
				+ "\n\tdel wallet dell'utente loggato ed il saldo corrente in Wincoin."
				+ "\n\tSe viene passato il parametro btc viene mostrata la conversione"
				+ "\n\tin bitcoin, secondo un tasso di cambio random, del valore del "
				+ "\n\tproprio wallet\n");
		buf.append("quit\n");
		buf.append("\tEffettua la richesta di logout dell'utente, "
				+ "\n\tse necessario, e termina il client\n");

		System.out.println(buf.toString());
	}
}
