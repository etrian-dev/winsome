package Winsome.WinsomeTasks;

import java.rmi.RemoteException;

import Winsome.WinsomeClient.FollowerCallback;
import Winsome.WinsomeServer.User;

/**
 * Task che implementa l'aggiornamento dei follower di un utente Winsome
 * <p>
 * NOTA: l'aggiornamento non avviene in risposta ad una Request, 
 * ma &egrave; eseguito periodicamente come parte del servizio di update
 */
public class UpdateFollowersTask extends Task implements Runnable {
	private User user;
	private FollowerCallback callback;

	public UpdateFollowersTask(User userRef, FollowerCallback callbackRef) {
		this.user = userRef;
		this.callback = callbackRef;
	}

	public void run() {
		System.out.println("Aggiorno followers di " + user.getUsername() + "...");
		try {
			this.callback.updateFollowers(this.user.getFollowers());
			this.callback.setUpdateTimestamp(System.currentTimeMillis());
		} catch (RemoteException e) {
			System.err.println("Fallito update followers del client"
					+ user.getUsername() + ": " + e.getMessage());
		}
	}
}
