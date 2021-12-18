package WinsomeServer;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.Future;

/**
 * Classe di utilità per raggruppare le informazioni relative ad un client connesso
 */
public class ClientData {
	/** Buffer di dati relativi a letture in sospeso */
	// TODO: maybe arraylist of buffers, but complicated handling in RequestParser
	private ByteBuffer readBuffer;
	/** Lista delle task in attesa di completamento */
	private LinkedList<Future<?>> tasksInProgress;

	public ClientData() {
		// Inizializzo senza alcun bytebuffer
		// NOTA: importante NON avere allocateDirect(), per avere garanzia che il ByteBuffer 
		// creato abbia un array come struttura dati
		this.readBuffer = ByteBuffer.allocate(ServerMain.BUFSZ);
		// Inizializzazione lista task in esecuzione
		this.tasksInProgress = new LinkedList<>();
	}

	public ByteBuffer getBuffer() {
		return this.readBuffer;
	}

	public int getTasklistSize() {
		return this.tasksInProgress.size();
	}

	public boolean hasTasksDone() {
		if (this.tasksInProgress.size() > 0 && this.tasksInProgress.peek().isDone()) {
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
