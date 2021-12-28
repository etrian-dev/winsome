package WinsomeServer;

import java.util.concurrent.ScheduledFuture;

import WinsomeClient.FollowerCallback;

/**
 * Classe che incapsula lo stato del callback di un utente
 */
public class FollowerCallbackState {
	private FollowerCallback callback;
	private ScheduledFuture<?> future;

	public FollowerCallbackState(FollowerCallback fcall, ScheduledFuture<?> fut) {
		this.callback = fcall;
		this.future = fut;
	}

	/**
	 * Metodo che tenta di cancellare la task, anche interrompendola
	 * @return true sse la task è stata cancellata, false altrimenti 
	 * (magari era stata gi&agrave; completata)
	 */
	public boolean cancelFuture() {
		return this.future.cancel(true);
	}

	public FollowerCallback getCallback() {
		return this.callback;
	}

	public ScheduledFuture<?> getFuture() {
		return this.future;
	}

}
