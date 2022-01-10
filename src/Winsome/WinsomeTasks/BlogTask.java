package Winsome.WinsomeTasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.WinsomeServer;

/**
 * Task che implementa la visione del blog da parte del suo proprietario
 */
public class BlogTask extends Task implements Callable<String> {
	/** L'utete che richiede la visione del blog */
	private String user;
	/** L'utente loggato da questo client (ottenuto dall'attachment ClientData) */
	private String currentUser;
	private ObjectMapper mapper;
	private WinsomeServer servRef;

	public BlogTask(String requester, String loggedUser, ObjectMapper objMapper, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("Blog");
		this.user = requester;
		this.currentUser = loggedUser;
		this.mapper = objMapper;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nRequester: " + this.user
				+ "\nCurrentUser: " + this.currentUser;
	}

	/**
	 * Metodo che ritorna ad un client la lista di post dell'utente loggato (il proprio blog)
	 *
	 * @return La rappresentazione dei post in formato JSON, o un messaggio di errore.
	 * La lista di post viene ordinata per timestamp crescente
	 */
	public String call() {
		// Operazione autorizzata sse l'utente loggato su questo client è lo stesso che richiede 
		// la visualizzazione del proprio blog
		if (!this.user.equals(this.currentUser)) {
			return "Errore:operazione non autorizzata";
		}
		// Ottengo tutti i post del blog in una lista, che viene poi serializzata in una stringa
		List<Post> blog = new ArrayList<>(servRef.getBlog(this.currentUser));
		try {
			String blogStr = this.mapper.writeValueAsString(blog);
			return blogStr;
		} catch (JsonProcessingException e) {
			System.err.println("Impossibile completare " + this.getKind() + ": " + e.getMessage());
			return "Errore:impossibile completare la richiesta";
		}
	}
}
