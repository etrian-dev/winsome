package Winsome.WinsomeServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledFuture;

import Winsome.WinsomeClient.FollowerCallback;
import Winsome.WinsomeTasks.UpdateFollowersTask;

/** Implementazione dell'interfaccia di aggiornamento dei follower */
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
		// Sottomissione della task di update dei follower alla threadpool
		// La task di update viene eseguita ad intervalli regolari (letti dalla configurazione del server)
		// Il delay iniziale è 0 per far sì che la task venga eseguita immediatamente dopo
		// che il client fa la richiesta di subscribe alla callback, in modo da inizializzare
		// la lista di followers lato client
		ScheduledFuture<?> fut = this.servRef.getFollowerUpdaterPool().scheduleAtFixedRate(
				new UpdateFollowersTask(u, callbackObj),
				0L,
				this.servRef.getConfig().getCallbackInterval(),
				this.servRef.getConfig().getCallbackIntervalUnit());
		// Viene aggiunto alla mappa delle callback attive quella del client (con il relativo future)
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
		// Cancello la task che esegue la callback: il client non riceve più l'update dei follower
		if (callbackState.cancelFuture()) {
			System.err.println("Task di callback dell'utente "
					+ u.getUsername() + " cancellata con successo");
		} else {
			System.err.println("Impossibile cancellare la task di callback dell'utente "
					+ u.getUsername());
		}
		// Rimuovo dalla map delle callback attive questo utente
		this.servRef.rmFollowerCallback(user);
		return 0;
	}
}
