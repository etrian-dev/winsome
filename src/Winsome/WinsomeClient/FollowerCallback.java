package Winsome.WinsomeClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/** 
 * Interfaccia per l'aggiornamento della lista di follower di un client
*/
public interface FollowerCallback extends Remote {

	/**
	 * Metodo per aggiornare i followers
	 * @throws RemoteException
	 */
	public void updateFollowers(Set<String> newList) throws RemoteException;

	/**
	 * Metodo che resetta il set di followers (utile al logout di un utente)
	 * @throws RemoteException
	 */
	public void clearFollowers() throws RemoteException;

	/**
	 * Metodo per settare il timestamp dell'ultimo aggiornamento della lista dei followers
	 * 
	 * @param timestamp timestamp dell'ultimo aggiornamento
	 * @throws RemoteException
	 */
	public void setUpdateTimestamp(long timestamp) throws RemoteException;
}
