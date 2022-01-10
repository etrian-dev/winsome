package Winsome.WinsomeServer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;

/**
 * Classe di utilità per raggruppare le informazioni relative ad un client connesso
 */
public class ClientData {
	/** Dimensione del buffer di lettura dal socket di default */
	public static final int READ_BUFSZ = 768;
	/** Dimensione del buffer di scrittura sul socket di default */
	public static final int WRITE_BUFSZ = 768;

	/** Username del client, settato al login e resettato al logout
	 * <p>
	 * Serve ad evitare che un utente si spacci per un altro utente
	 */
	private String currentUser;
	/** Buffer di dati relativi a letture in sospeso */
	private ByteBuffer readBuffer;
	/** Buffer di dati relativi a scritture in sospeso */
	private ByteBuffer writeBuffer;
	/** Lista delle task in attesa di completamento */
	private LinkedList<Future<?>> tasksInProgress;

	public ClientData() {
		// Inizialmente nessun utente associato
		this.currentUser = null;
		// Inizializzo senza alcun bytebuffer
		// NOTA: importante NON avere allocateDirect(), per avere garanzia che il ByteBuffer
		// creato abbia un array come struttura dati
		this.readBuffer = ByteBuffer.allocate(READ_BUFSZ);
		this.writeBuffer = null;
		// Inizializzazione lista task in esecuzione
		this.tasksInProgress = new LinkedList<>();
	}

	public String getCurrentUser() {
		return this.currentUser;
	}

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

	public ByteBuffer getReadBuffer() {
		return this.readBuffer;
	}

	public void resetReadBuffer() {
		this.readBuffer.clear();
		Arrays.fill(this.readBuffer.array(), (byte) 0);
	}

	public ByteBuffer getWriteBuffer() {
		return this.writeBuffer;
	}

	public void setWriteBuffer(ByteBuffer bb) {
		this.writeBuffer = bb;
	}

	public void resetWriteBuffer() {
		this.writeBuffer = null;
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

	public synchronized void addTask(Future<?> pendingTask) {
		this.tasksInProgress.add(pendingTask);
	}

	public synchronized Future<?> removeTask() {
		return this.tasksInProgress.remove();
	}
}
