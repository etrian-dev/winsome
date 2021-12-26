package WinsomeClient;

import java.rmi.RemoteException;
import java.util.List;

/** 
 * Interfaccia per l'aggiornamento della lista di follower di un client
*/
public interface FollowerUpdater {
	/**
	 * Metodo per ottenere la lista di follower (alla login di un client)
	 * @return
	 * @throws RemoteException
	 */
	public List<String> getFollowers() throws RemoteException;

	/**
	 * Metodo per la notifica dell'aggiunta di un nuovo follower
	 * 
	 * @return Il metodo ritorna l'username del nuovo follower
	 * @throws RemoteException
	 */
	public String newFollower() throws RemoteException;

	/**
	 * Metodo per la notifica della perdita di un follower
	 * 
	 * @return Il metodo ritorna l'username del follower perso
	 * @throws RemoteException
	 */
	public String lostFollower() throws RemoteException;
}
