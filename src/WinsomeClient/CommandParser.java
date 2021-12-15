package WinsomeClient;

/**
 * Classe che implementa il parser dei comandi letti da standard input
 */
public class CommandParser {
	public static boolean parseCommand(String[] command_tokens) {
		Command comm = Command.fromString(command_tokens[0]);
		switch (comm) {
			case REGISTER:
				return true;
			case LOGIN:
				return true;
			case LOGOUT:
				return true;
			case LIST:
				return true;
			default:
				// comando non riconosciuto
				return false;
		}
	}
}
