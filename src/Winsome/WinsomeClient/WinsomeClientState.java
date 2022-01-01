package Winsome.WinsomeClient;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import Winsome.WinsomeServer.Signup;

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
	/**
	 * Indirizzo UDP multicast su cui si ricevono messaggi di update
	 */
	private InetAddress mcastAddr;
	private int mcastPort;
	private MulticastSocket mcastSock;
	private Thread mcastThread;
	private AtomicBoolean run_thread;

	public WinsomeClientState() {
		this.signupStub = null;
		this.currentUser = "";
		this.isQuitting = false;
		this.tpcConnection = null;
		this.callbackRef = null;
		this.mcastAddr = null;
		this.mcastPort = 0;
		this.mcastSock = null;
		this.mcastThread = null;
		this.run_thread = new AtomicBoolean();
	}

	public void startMcastThread(String netif) throws SocketException {
		// Crea e fa partire un thread demone per la ricezione di messaggi su un gruppo multicast
		McastListener listener = new McastListener(netif, this.run_thread, this);
		this.run_thread.set(true);
		this.mcastThread = new Thread(listener);
		this.mcastThread.setDaemon(true);
		this.mcastThread.setPriority(Thread.MIN_PRIORITY);
		this.mcastThread.setName("Wallet-notifications");
		this.mcastThread.start();
	}

	public void stopMcastThread() {
		this.run_thread.set(false);
		try {
			this.mcastThread.join();
		} catch (InterruptedException e) {
			System.err.println("Impossibile effettuare join del thread " + this.mcastThread.getName());
		}
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

	public FollowerCallbackImpl getCallback() {
		return this.callbackRef;
	}

	public InetAddress getMcastAddr() {
		return this.mcastAddr;
	}

	public int getMcastPort() {
		return this.mcastPort;
	}

	public MulticastSocket getMcastSocket() {
		return this.mcastSock;
	}

	public Thread getMcastThread() {
		return this.mcastThread;
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

	public void setMcastAddress(InetAddress addr) {
		this.mcastAddr = addr;
	}

	public void setMcastPort(int port) {
		this.mcastPort = port;
	}

	public void setMcastSocket(MulticastSocket sock) {
		this.mcastSock = sock;
	}

}
