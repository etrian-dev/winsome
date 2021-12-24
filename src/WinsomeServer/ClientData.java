package WinsomeServer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;

/**
 * Classe di utilità per raggruppare le informazioni relative ad un client connesso
 */
public class ClientData {
	public static final long TIMER_DURATION = 1800000L;

	/** Username del client, settato al login e resettato al logout
	 * <p>
	 * Serve ad evitare che un utente si spacci per un altro utente
	 */
	private String currentUser;
	/** Buffer di dati relativi a letture in sospeso */
	// TODO: maybe arraylist of buffers, but complicated handling in RequestParser
	private ByteBuffer readBuffer;
	/** Lista delle task in attesa di completamento */
	private LinkedList<Future<?>> tasksInProgress;

	public ClientData() {
		// Inizialmente nessun utente associato
		this.currentUser = null;
		// Inizializzo senza alcun bytebuffer
		// NOTA: importante NON avere allocateDirect(), per avere garanzia che il ByteBuffer 
		// creato abbia un array come struttura dati
		this.readBuffer = ByteBuffer.allocate(ServerMain.BUFSZ);
		// Inizializzazione lista task in esecuzione
		this.tasksInProgress = new LinkedList<>();
	}

	public String getCurrentUser() {
		return this.currentUser;
	}

	// FIXME: fix this, maybe move elsewhere
	// TODO: set this at login and unset at logout
	public boolean setCurrentUser(Collection<String> all_users, String user) {
		if (all_users == null || !all_users.contains(user)) {
			return false;
		}
		this.currentUser = user;
		return true;
	}

	public boolean unsetCurrentUser(String user) {
		if (this.currentUser != null && this.currentUser.equals(user)) {
			this.currentUser = null;
			return true;
		}
		return false;
	}

	public ByteBuffer getBuffer() {
		return this.readBuffer;
	}

	public void resetBuffer() {
		this.readBuffer = ByteBuffer.allocate(ServerMain.BUFSZ);
	}

	public int getTasklistSize() {
		return this.tasksInProgress.size();
	}

	public boolean hasTasksDone() {
		if (this.tasksInProgress.size() > 0
				&& this.tasksInProgress.peek() != null
				&& this.tasksInProgress.peek().isDone()) {
			return true;
		}
		return false;
	}

	public void addTask(Future<?> pendingTask) {
		this.tasksInProgress.add(pendingTask);
	}

	public Future<?> removeTask() {
		return this.tasksInProgress.remove();
	}
}
