package Winsome.WinsomeServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import Winsome.WinsomeExceptions.BlogException;

public class BlogLoaderThread extends Thread {
	public static final String LOADER_ERROR_FMT = "[BLOG LOADER ERROR]: %s\n";
	public static final String BLOGS_DIR = "blogs";
	public static final String BLOG_FILE_FORMAT = ".json";
	public static final String END_FMT = "Blog di %s caricato con successo\n";

	private String owner;
	private String dataDir;
	private ConcurrentLinkedDeque<Post> blogDeque;

	/**
	 * Costruttore del thread per il caricamento del blog di user
	 * @param user l'utente di cui si vuole caricare il blog
	 * @param dataDirectory la directory contenente la subdirectory blogs/
	 * @param bucket la coda in cui inserire i post letti dal file del blog
	 * @throws BlogException il blog dell'utente specificato non esiste
	 */
	public BlogLoaderThread(String user,
			String dataDirectory,
			ConcurrentLinkedDeque<Post> bucket) {
		this.owner = user;
		this.dataDir = dataDirectory;
		this.blogDeque = bucket;
	}

	public void run() {
		// path costruito di default: datadir/blogs/owner.json
		String path = this.dataDir + File.separator
				+ BLOGS_DIR + File.separator
				+ this.owner + BLOG_FILE_FORMAT;
		try {
			// Apre il file contenente i post del blog
			File blogFile = new File(path);
			if (!blogFile.exists()) {
				throw new BlogException(this.owner, path, "Il file non esiste");
			}
			if (!blogFile.canRead()) {
				throw new BlogException(this.owner, path, "Imopossibile leggere il file");
			}
			try (BufferedInputStream bufIn = new BufferedInputStream(new FileInputStream(blogFile));) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(bufIn);
				// Se la radice è null il blog è vuoto => nessun post da aggiungere
				if (root != null) {
					// Aggiungo tutti i post, controllando prima che il nodo radice sia un array
					if (!root.isArray()) {
						throw new BlogException(this.owner, path, "Formato del file non corretto");
					}
					ObjectReader postReader = mapper.readerFor(Post.class);
					Iterator<JsonNode> all_elems = root.elements();
					while (all_elems.hasNext()) {
						this.blogDeque.add(postReader.readValue(all_elems.next()));
					}
				}
			}
			// Messaggio di fine sincronizzazione
			System.out.printf(END_FMT, this.owner);
		} catch (BlogException bg) {
			System.err.printf(LOADER_ERROR_FMT, bg.toString());
		} catch (IOException e) {
			System.err.printf(LOADER_ERROR_FMT,
					"Fallito il caricamento del blog dell'utente "
							+ this.owner + " (" + path + ") : " + e.getMessage());
		}
	}
}
