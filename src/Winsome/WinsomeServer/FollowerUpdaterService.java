package Winsome.WinsomeServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Winsome.WinsomeClient.FollowerCallback;

/**
 * Interfaccia per l'iscrizione e disiscrizione dal servizio RMI callback
 * per l'aggiornamento dei follower di un utente di Winsome
 */
public interface FollowerUpdaterService extends Remote {
	/**
	 * Metodo remoto per l'adesione del client al servizio di aggiornamento dei follower di un utente
	 *
	 * @param user L'utente che vuole ricevere aggiornamenti sui propri followers
	 * @param callbackObj L'oggetto remoto utilizzato dal server per notificare un aggiornamento
	 * @return 0 sse l'iscrizione ha avuto successo, -1 altrimenti
	 * @throws RemoteException
	 */
	public int subscribe(String user, FollowerCallback callbackObj) throws RemoteException;

	/**
	 * Metodo remoto per la cancellazione dell'iscrizione al servizio di aggiornamento dei follower
	 *
	 * @param user L'utente che non vuole &ugrave; ricevere aggiornamenti sui propri followers
	 * @return 0 sse la cancellazione ha avuto successo, -1 altrimenti
	 * @throws RemoteException
	 */
	public int unsubscribe(String user) throws RemoteException;
}
