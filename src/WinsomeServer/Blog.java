package WinsomeServer;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Classe che incapsula il blog di un utente Winsome
 * <p>
 * Contiene il riferimento all'utente proprietario del blog e la lista
 * di tutti i post pubblicati dall'utente. Vi &egrave; uno ed un solo blog per utente Winsome.
 */
public class Blog {
	private String owner;
	/** 
	 * Coda dei post creati dall'utente, con i relativi commenti e voti
	 * <p>
	 * La coda &egrave; sempre mantenuta ordinata sul timestamp del post, dal
	 * pi&ugrave; recente al meno recente
	 */
	private ConcurrentLinkedDeque<Post> posts;

	public Blog(String user) {
		this.owner = null;
		this.posts = new ConcurrentLinkedDeque<>();
	}

	public String getOwner() {
		return this.owner;
	}

	public List<Post> getPosts() {
		return List.copyOf(this.posts);
	}

	public void addPost(Post p) {
		this.posts.addLast(p);
	}
}
