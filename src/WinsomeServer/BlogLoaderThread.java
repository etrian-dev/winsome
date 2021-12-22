package WinsomeServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import WinsomeExceptions.BlogException;

public class BlogLoaderThread extends Thread {
	public static final String BLOGS_DIR = "blogs";
	public static final String BLOG_FILE_FORMAT = ".json";

	private String owner;
	private String dataDir;
	private ConcurrentLinkedDeque<Post> bucket;

	/**
	 * Costruttore del thread per il caricamento del blog di user
	 * @param user l'utente di cui si vuole caricare il blog
	 * @param dataDirectory la directory contenente la subdirectory blogs/
	 * @param blogDeque la coda in cui inserire i post letti dal file del blog
	 * @throws BlogException il blog dell'utente specificato non esiste
	 */
	public BlogLoaderThread(String user,
			String dataDirectory,
			ConcurrentLinkedDeque<Post> blogDeque) {
		this.owner = user;
		this.dataDir = dataDirectory;
		this.bucket = blogDeque;
	}

	public void run() {
		// path costruito di default: datadir/blogs/owner.json
		String path = this.dataDir + File.separator
				+ BLOGS_DIR + File.separator
				+ this.owner + BLOG_FILE_FORMAT;
		try {
			// Apre il fileChannel contenente i post del blog
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
				if (root == null) {
					return;
				}
				// Aggiungo tutti i post, controllando prima che sia un array
				if (!root.isArray()) {
					throw new BlogException(this.owner, path, "Formato del file non corretto");
				}
				JsonParser postlistParser = root.traverse(mapper);
				Iterator<Post> all_posts = postlistParser.readValuesAs(Post.class);
				while (all_posts.hasNext()) {
					this.bucket.add(all_posts.next());
				}
			}
		} catch (BlogException bg) {
			System.err.println(bg);
		} catch (IOException e) {
			System.err.println("[BLOG ERROR]: Fallito il caricamento del blog dell'utente \""
					+ this.owner + "\" (" + path + ") : "
					+ e.getMessage());
		}
	}
}