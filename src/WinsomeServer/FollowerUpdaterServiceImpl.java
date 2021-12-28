package WinsomeServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import WinsomeClient.FollowerCallback;
import WinsomeTasks.UpdateFollowersTask;

public class FollowerUpdaterServiceImpl extends UnicastRemoteObject implements FollowerUpdaterService {
	private transient WinsomeServer servRef;

	public FollowerUpdaterServiceImpl(WinsomeServer serv) throws RemoteException {
		super();
		this.servRef = serv;
	}

	public int subscribe(String user, FollowerCallback callbackObj) throws RemoteException {
		// Utente non esistente
		User u = this.servRef.getUser(user);
		if (u == null) {
			return 1;
		}
		// Utente non loggato
		if (!u.isLogged()) {
			return 2;
		}
		// Utente già iscritto al servizio
		if (this.servRef.getFollowerCallback(user) != null) {
			return 3;
		}
		// task di update schedulata 
		// (delay iniziale 0 perché il client al login riceve un update 
		// per inizializzare la propria struttura dati)
		// TODO: rate update configurabile dal server
		ScheduledFuture<?> fut = this.servRef.getFollowerUpdaterPool().scheduleAtFixedRate(
				new UpdateFollowersTask(u, callbackObj), 0L, 30L, TimeUnit.SECONDS);
		this.servRef.addFollowerCallback(user, new FollowerCallbackState(callbackObj, fut));
		return 0;
	}

	public int unsubscribe(String user) throws RemoteException {
		// Utente non esistente
		User u = this.servRef.getUser(user);
		if (u == null) {
			return 1;
		}
		// Utente non loggato
		if (!u.isLogged()) {
			return 2;
		}
		// Utente non iscritto al servizio
		FollowerCallbackState callbackState = this.servRef.getFollowerCallback(user);
		if (callbackState == null) {
			return 3;
		}
		// Rimuovo dalla mappa di utenti che ricevono callback
		this.servRef.rmFollowerCallback(user);
		// Devo cancellare anche la task associata
		if (callbackState.cancelFuture()) {
			System.err.println("Task di callback dell'utente "
					+ u.getUsername() + " cancellata con successo");
		} else {
			System.err.println("Impossibile cancellare la task di callback dell'utente "
					+ u.getUsername());
		}
		return 0;
	}
}
