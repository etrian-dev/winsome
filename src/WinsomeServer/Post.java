package WinsomeServer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe che implementa un Post nel social network
 * <p>
 * Il post è l'unit&agrave; di contenuto del blog di un utente di Winsome.
 * Ogni post ha un autore, un titolo ed un contenuto.
 * Altri utenti possono votare il post, effettuarne il rewin o commentarlo
 */
public class Post {
	/** id univoco del post, generato automaticamente */
	private long postID;
	/** Id del post originale: significativo sse la flag isRewin == true */
	private long originalID;
	/** flag true sse il post &egrave; un rewin
	 * <p>
	 * Vale che: isRewin == true &rArr; originalID == id del post originale
	 * */
	private boolean isRewin;
	/** timestamp del post */
	private long timestamp;
	/** L'et&agrave; del post, ovvero quante volte &egrave; stato assegnato il reward dal server */
	private int age;
	/** Autore del post (nel caso di rewin &egrave; l'autore del rewin) */
	private String author;
	/** Titolo del post (nel caso di rewin &egrave; null) */
	private String title;
	/** Contenuto del post (nel caso di rewin &egrave; null) */
	private String content;
	/** Lista di voti del post. Ogni voto pu&ograve; essere sia positivo che negativo */
	private List<Vote> votes;
	/** Lista dei commenti al post fatti da altri utenti */
	private List<Comment> comments;

	public Post() {
		this.postID = 0;
		this.isRewin = false; // default post originale
		this.originalID = 0;
		this.timestamp = 0;
		this.age = 0;
		this.author = null;
		this.title = null;
		this.content = null;
		this.votes = new ArrayList<>();
		this.comments = new ArrayList<>();
	}

	/**
	 * Costruttore con parametri del post
	 * <p>
	 * Il nuovo post creato, oltre ai dati specificati dai parametri, avr&agrave;:
	 * <ul>
	 * <li>timestamp corrente</li>
	 * <li>age = 0</li>
	 * <li>lista dei voti e dei commenti inizializzate a liste vuote</li>
	 * </ul>
	 * Sta al chiamante assicurarsi che i valori passati rispettino i requisiti di Winsome:
	 * <ul>
	 * <li>Assicurare l'univocit&agrave; di id tra tutti i post della rete e che id &gt; 0</li>
	 * <li>Verificare che l'autore del post sia un utente Winsome 
	 * e che sia loggato dal client che ha effettuato la richesta</li>
	 * <li>Verificare che il titolo del post sia non nullo e &lt; 20 caratteri</li>
	 * <li>Verificare che il contenuto del post sia non nullo e &lt; 500 caratteri</li>
	 * </ul>
	 * 
	 * @param id
	 * @param rewin
	 * @param author
	 * @param postTitle
	 * @param postContent
	 */
	public Post(long id, boolean rewin, String author, String postTitle, String postContent) {
		this.postID = id;
		this.originalID = 0;
		this.isRewin = rewin;
		this.timestamp = System.currentTimeMillis();
		this.age = 0;
		this.author = author;
		this.title = postTitle;
		this.content = postContent;
		this.votes = new ArrayList<>();
		this.comments = new ArrayList<>();
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer("=== Post ===");
		sbuf.append("\nId: " + this.postID);
		sbuf.append("\nrewin: " + this.isRewin);
		sbuf.append("\ndate: " + new Date(this.timestamp));
		sbuf.append("\nage: " + this.age);
		sbuf.append("\nauthor: " + this.author);
		sbuf.append("\ntitle: " + this.title);
		sbuf.append("\ncontent: " + this.content);
		sbuf.append("\nvotes: " + this.votes);
		sbuf.append("\ncomments: " + this.comments);
		return sbuf.toString();
	}

	/**
	 * Rimuove, se presente, il voto v dal post
	 * 
	 * @param v il voto da rimovere
	 * @return true sse il voto v era presente, false altrimenti
	 */
	public boolean removeVote(Vote v) {
		if (this.votes.contains(v)) {
			return this.votes.remove(v);
		}
		return false;
	}

	/**
	 * Rimuove, se presente, il commento c dal post
	 * 
	 * @param c il commento da rimovere
	 * @return true sse il commento c era presente, false altrimenti
	 */
	public boolean removeComment(Comment c) {
		if (this.comments.contains(c)) {
			return this.comments.remove(c);
		}
		return false;
	}

	// Getters
	public long getPostID() {
		return this.postID;
	}

	public long getOriginalID() {
		return this.originalID;
	}

	public boolean getIsRewin() {
		return this.isRewin;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public int getAge() {
		return this.age;
	}

	public String getAuthor() {
		return this.author;
	}

	public String getTitle() {
		return this.title;
	}

	public String getContent() {
		return this.content;
	}

	public List<Vote> getVotes() {
		return List.copyOf(this.votes);
	}

	public List<Comment> getComments() {
		return List.copyOf(this.comments);
	}

	// Setters
	public void setPostID(long id) {
		this.postID = id;
	}

	public void setOriginalID(long original) {
		this.originalID = original;
	}

	public void setIsRewin(boolean rewin) {
		this.isRewin = rewin;
	}

	public boolean setTimestamp(long tstamp) {
		if (tstamp < 0 || tstamp > System.currentTimeMillis()) {
			return false;
		}
		this.timestamp = tstamp;
		return true;
	}

	public boolean setAge(int postAge) {
		if (postAge < 0) {
			return false;
		}
		this.age = postAge;
		return true;
	}

	public void setAuthor(String auth) {
		this.author = auth;
	}

	public void setTitle(String postTitle) {
		this.title = postTitle;
	}

	public void setContent(String postContent) {
		this.content = postContent;
	}

	public boolean setVote(Vote vote) {
		if (this.votes.contains(vote)) {
			return false;
		}
		return this.votes.add(vote);
	}

	public boolean setComment(Comment comm) {
		if (this.comments.contains(comm)) {
			return false;
		}
		return this.comments.add(comm);
	}

}