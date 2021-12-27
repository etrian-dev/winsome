package WinsomeClient;

import java.rmi.RemoteException;
import java.util.Set;

/** 
 * Interfaccia per l'aggiornamento della lista di follower di un client
*/
public interface FollowerCallback {
	/**
	 * Metodo che ritorna i follower correnti
	 * @return il set di username di utenti che seguono l'utente
	 * @throws RemoteException
	 */
	public Set<String> getFollowers() throws RemoteException;

	/**
	 * Metodo che resetta il set di followers (utile al logout di un utente)
	 * @throws RemoteException
	 */
	public void clearFollowers() throws RemoteException;

	/**
	 * Metodo che aggiunge un nuovo follower al set di followers
	 * @param user
	 * @throws RemoteException
	 */
	public void newFollower(String user) throws RemoteException;

	/**
	 * Metodo che rimuove un follower dal set di followers
	 * @param user
	 * @throws RemoteException
	 */
	public void lostFollower(String user) throws RemoteException;
}
