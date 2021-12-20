package WinsomeClient;

/**
 * Classe che incapsula lo stato del client Winsome
 */
public class WinsomeClientState {
	private String currentUser;
	private boolean isQuitting;

	public WinsomeClientState() {
		this.currentUser = "";
		this.isQuitting = false;
	}

	public String getCurrentUser() {
		return this.currentUser;
	}

	public boolean isTerminating() {
		return this.isQuitting;
	}

	public void setUser(String newUser) {
		this.currentUser = newUser;
	}

	public void setTermination() {
		this.isQuitting = true;
	}

	public void unsetTermination() {
		this.isQuitting = false;
	}
}
