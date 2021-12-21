package WinsomeTasks;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import WinsomeServer.Post;
import WinsomeServer.WinsomeServer;

/**
 * Task che implementa la creazione di un nuovo post
 */
public class CreatePostTask extends Task implements Callable<Long> {
	private String author;
	private String title;
	private String content;
	private WinsomeServer servRef;

	public CreatePostTask(String postAuthor, String postTitle, String postContent, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("CreatePost");
		this.author = postAuthor;
		this.title = postTitle;
		this.content = postContent;
		this.servRef = serv;
	}

	/**
	 * Metodo che esegue l'operazione di creazione del post, ritornando l'id del post creato
	 * 
	 * @return Il risultato dell'operazione richiesta è un intero:
	 * <ul>
	 * <li>&ge; 0 sse il post è stato creato con successo (id del post creato)</li>
	 * <li>-1: l'utente richiedente non esiste in Winsome</li>
	 * <li>-2: Titolo nullo o lunghezza del titolo del post &egrave; maggiore di 20 caratteri</li>
	 * <li>-3: Contenuto nullo o lunghezza del contenuto del post &egrave; maggiore di 500 caratteri</li>
	 * </ul>
	 */
	public Long call() {
		// Validazione campi del post
		if (this.servRef.getUser(this.author) == null) {
			return Long.valueOf(-1);
		}
		if (this.title == null || this.title.length() > 20) {
			return Long.valueOf(-2);
		}
		if (this.content == null || this.content.length() > 500) {
			return Long.valueOf(-3);
		}
		// Generazione ID univoco
		long postID = 0;
		do {
			postID = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
		} while (servRef.getPost(postID) != null);
		// creazione post + inserimento nel blog e nella mappa post
		Post p = new Post(postID, false, this.author, this.title, this.content);
		servRef.addPost(p);
		servRef.addPostToBlog(this.author, p);
		return postID;
	}
}
