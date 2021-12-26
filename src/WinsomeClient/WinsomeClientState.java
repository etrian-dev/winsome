package WinsomeClient;

import java.nio.channels.SocketChannel;

/**
 * Classe che incapsula lo stato del client Winsome
 */
public class WinsomeClientState {
	private String currentUser;
	private boolean isQuitting;
	/** Socket TCP sul quale sono effettuate 
	 * la maggior parte delle comunicazioni tra client e server */
	private SocketChannel tpcConnection;

	public WinsomeClientState() {
		this.currentUser = "";
		this.isQuitting = false;
		this.tpcConnection = null;
	}

	public String getCurrentUser() {
		return this.currentUser;
	}

	public boolean isTerminating() {
		return this.isQuitting;
	}

	public SocketChannel getSocket() {
		return this.tpcConnection;
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

	public void setSocket(SocketChannel sc) {
		this.tpcConnection = sc;
	}
}
