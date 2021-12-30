package Winsome.WinsomeTasks;

/**
 * Task che implementa la richesta di chiusura della connessione 
 * da parte di un WinsomeClient.
 * 
 * NOTA: una QuitTask viene processata direttamente dalla classe WinsomeServer
 * per praticit&agrave;, per cui in questo caso non implementa l'interfaccia
 * Callable (non restituisce alcuna risposta al client)
 */
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
