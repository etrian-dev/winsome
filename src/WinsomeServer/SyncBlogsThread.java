package WinsomeServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SyncBlogsThread extends Thread {
	public static final String BLOGS_SYNC_MSG = "Sincronizzo i blog degli utenti...";
	public static final String CANNOT_SYNC_FMT = "[BLOG SYNC] Impossibile sincronizzare il blog di \"%s\": %s\n";

	private String dataDir;
	private ObjectMapper mapper;
	private JsonFactory factory;
	private WinsomeServer servRef;

	public SyncBlogsThread(String dir, ObjectMapper objMapper, JsonFactory jsonFact, WinsomeServer serv) {
		this.dataDir = dir + "/blogs";
		this.mapper = objMapper;
		this.factory = jsonFact;
		this.servRef = serv;
	}

	public void run() {
		System.out.println(BLOGS_SYNC_MSG);
		List<Thread> postSyncThreads = new ArrayList<>();
		// Per ciascun utente registrato creo un nuovo thread che va ad effettuare la sincronizzazione
		// dopo aver effettuato alcuni controlli preventivi
		for (String username : this.servRef.getUsernames()) {
			File blogFile = new File(this.dataDir + "/" + username + ".json");
			try {
				if (!blogFile.exists()) {
					// creo un nuovo file per il blog di username (operazione atomica)
					if (!blogFile.createNewFile()) {
						throw new IOException("Fallita creazione del file" + blogFile.getPath());
					}
				}
				// Creo il thread di sincronizzazione e lo faccio partire
				SyncPostsThread syncTh = new SyncPostsThread(
						blogFile,
						this.servRef.getBlog(username),
						this.mapper,
						this.factory);
				syncTh.start();
				postSyncThreads.add(syncTh);

			} catch (IOException e) {
				System.err.printf(CANNOT_SYNC_FMT, username, e.getMessage());
			}
		}
	}
}
