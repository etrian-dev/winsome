package Winsome.WinsomeTasks;

import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.WinsomeServer;

public class ShowPostTask extends Task implements Callable<String> {
	private long postID;
	private String currentUser;
	private ObjectMapper mapper;
	private WinsomeServer servRef;

	public ShowPostTask(Long id, String loggedUser, ObjectMapper objMapper, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("ShowPost");
		this.postID = id;
		this.currentUser = loggedUser;
		this.mapper = objMapper;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString() + "\nId: " + this.postID;
	}

	/**
	 * Metodo che ritorna ad un client una rappresentazione del post richiesto,
	 * oppure un errore se il post non esiste.
	 *
	 * @return La rappresentazione su stringa del post serializzato in formato JSON
	 */
	public String call() {
		// Recupero il post dalla mappa globale
		Post p = this.servRef.getPost(this.postID);
		if (p == null) {
			return "Errore:post inesistente";
		}
		// Posso vederlo sse sono l'autore o seguo l'autore del post
		if (!(p.getAuthor().equals(this.currentUser)
				|| servRef.getUser(this.currentUser).getFollowing().contains(p.getAuthor()))) {
			return "Errore:operazione non autorizzata";
		}
		// Serializzo il post
		try {
			String s = mapper.writeValueAsString(p);
			return s;
		} catch (JsonProcessingException e) {
			System.err.println("Impossibile completare " + this.getKind() + ": " + e.getMessage());
			return "Errore:impossibile completare la richiesta";
		}
	}
}
