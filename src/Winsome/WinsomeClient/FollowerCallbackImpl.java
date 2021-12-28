package Winsome.WinsomeClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementazione dell'interfaccia RMI callback per l'aggiornamento dei 
 * follower di un utente Winsome
 */
public class FollowerCallbackImpl extends UnicastRemoteObject implements FollowerCallback {
	private transient Set<String> followers;
	private transient long lastUpdate;

	public FollowerCallbackImpl() throws RemoteException {
		super();
		this.followers = new HashSet<>();
	}

	public void updateFollowers(Set<String> newList) throws RemoteException {
		System.out.println("Aggiornata lista follower: \n"
				+ this.followers + " => " + newList);
		this.followers = newList;
	}

	public void clearFollowers() throws RemoteException {
		this.followers.clear();
	}

	public void setUpdateTimestamp(long timestamp) throws RemoteException {
		this.lastUpdate = timestamp;
	}

	/**
	 * Metodo che ritorna i follower dell'utente connesso, aggiornati all'ultimo update
	 * <p>
	 * Il metodo non provoca alcuna richiesta verso il server, ma restituisce
	 * la lista aggiornata al timestamp {@link #lastUpdate}
	 * @return l'insieme di utenti che seguono l'utente corrente
	 */
	public Set<String> getCurrentFollowers() {
		return this.followers;
	}

	/**
	 * Metodo che ritorna il timestamp dell'ultimo update della lista di followers
	 * 
	 * @return il timestamp dell'ultimo aggiornamento (timestamp UNIX, secondi dal 01/01/1970)
	 */
	public long getLastUpdate() {
		return this.lastUpdate;
	}
}
