package WinsomeClient;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Classe che incapsula lo stato del client Winsome
 */
public class WinsomeClientState {
	private String currentUser;
	private boolean isQuitting;
	/** 
	 * Socket TCP sul quale sono effettuate 
	 * la maggior parte delle comunicazioni tra client e server 
	 */
	private SocketChannel tpcConnection;
	/**
	 * Struttura dati lato client che memorizza i follower del client connesso
	 */
	private List<String> followers;

	public WinsomeClientState() {
		this.currentUser = "";
		this.isQuitting = false;
		this.tpcConnection = null;
		this.followers = null;
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

	public List<String> getFollowers() {
		return List.copyOf(this.followers);
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

	public void setFollowers(List<String> update) {
		this.followers = update;
	}
}
