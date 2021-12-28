package WinsomeClient;

import java.nio.channels.SocketChannel;
import java.util.List;

import WinsomeServer.Signup;

/**
 * Classe che incapsula lo stato del client Winsome
 */
public class WinsomeClientState {
	/** Stub per la registrazione tramite RMI */
	private Signup signupStub;
	private String currentUser;
	private boolean isQuitting;
	/** 
	 * Socket TCP sul quale sono effettuate 
	 * la maggior parte delle comunicazioni tra client e server 
	 */
	private SocketChannel tpcConnection;
	/**
	 * Riferimento all'oggetto utilizzato per il callback
	 */
	private FollowerCallbackImpl callbackRef;

	public WinsomeClientState() {
		this.signupStub = null;
		this.currentUser = "";
		this.isQuitting = false;
		this.tpcConnection = null;
		this.callbackRef = null;
	}

	// Getters

	public Signup getStub() {
		return this.signupStub;
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
		return List.copyOf(this.callbackRef.getCurrentFollowers());
	}

	public long getLastFollowerUpdate() {
		return this.callbackRef.getLastUpdate();
	}

	// Setters

	public void setStub(Signup stub) {
		this.signupStub = stub;
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

	public void setCallback(FollowerCallbackImpl callback) {
		this.callbackRef = callback;
	}

}
