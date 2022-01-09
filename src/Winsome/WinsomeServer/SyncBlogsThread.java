package Winsome.WinsomeServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thread che gestisce la scrittura dei blog degli utenti alla terminazione del server
 */
public class SyncBlogsThread extends Thread {
	public static final String BLOGS_SYNC_MSG = "Sincronizzo i blog degli utenti...";
	public static final String CANNOT_SYNC_FMT = "[BLOG SYNC] Impossibile sincronizzare il blog di \"%s\": %s\n";

	private File blogDir;
	private ObjectMapper mapper;
	private JsonFactory factory;
	private WinsomeServer servRef;

	public SyncBlogsThread(File dir, ObjectMapper objMapper, JsonFactory jsonFact, WinsomeServer serv) {
		this.blogDir = new File(dir.getAbsolutePath() + "/blogs");
		this.mapper = objMapper;
		this.factory = jsonFact;
		this.servRef = serv;
	}

	public void run() {
		System.out.println(BLOGS_SYNC_MSG);

		// Creo, se non esiste, la directory blogs
		if (!(blogDir.exists() && blogDir.isDirectory())) {
			System.out.println("[WARNING] La directory dei blog (" + blogDir.getAbsolutePath()
					+ ") non esiste: Creo una directory vuota");
			// creo la directory dei blog
			try {
				if (!blogDir.mkdirs()) {
					throw new IOException("Fallita creazione della directory " + blogDir.getPath());
				}
			} catch (IOException e) {
				System.err.println(e);
				return;
			}
		}

		List<SyncPostsThread> postSyncThreads = new ArrayList<>();
		// Per ciascun utente registrato creo un nuovo thread che va ad effettuare la sincronizzazione
		// dopo aver effettuato alcuni controlli preventivi
		for (String username : this.servRef.getUsernames()) {
			File blogFile = new File(this.blogDir.getAbsolutePath() + "/" + username + ".json");
			try {
				if (!blogFile.exists()) {
					System.out.println("[WARNING] Creazione del blog di "
							+ username + ": " + blogFile.getPath());
					// creo un nuovo file per il blog di username (operazione atomica)
					if (!blogFile.createNewFile()) {
						throw new IOException("Fallita creazione del file " + blogFile.getPath());
					}
				}
				// Creo il thread di sincronizzazione e lo faccio partire
				SyncPostsThread syncTh = new SyncPostsThread(
						blogFile,
						this.servRef.getBlog(username),
						this.mapper,
						this.factory);
				postSyncThreads.add(syncTh);
				syncTh.start();

			} catch (IOException e) {
				System.err.printf(CANNOT_SYNC_FMT, username, e.getMessage());
			}
		}
		for (SyncPostsThread spTh : postSyncThreads) {
			try {
				spTh.join();
			} catch (InterruptedException e) {
				System.err.println("Thread " + Thread.currentThread().getName() + " interrupted");
			}
		}
	}
}
