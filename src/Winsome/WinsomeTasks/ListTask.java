package Winsome.WinsomeTasks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Winsome.WinsomeServer.User;
import Winsome.WinsomeServer.WinsomeServer;

/**
 * Task che implementa le operazioni <code>list users</code> e <code>list following</code>
 */
public class ListTask extends Task implements Callable<String> {
	private String entity;
	private String sender;
	private ObjectMapper mapper;
	private WinsomeServer servRef;

	public ListTask(String who, String what, ObjectMapper objMapper, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("List");
		this.sender = who;
		this.entity = what;
		this.mapper = objMapper;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nSender: " + this.sender
				+ "\nEntity: " + this.entity;
	}

	/**
	 * Metodo che ritorna la stringa degli utenti che hanno almeno un tag 
	 * in comune con il richiedente o la lista di utenti seguiti
	 *
	 * Il metodo controlla l'entit&agrave; di cui &egrave; stata richiesta la
	 * lista: utenti seguiti dal richiedente (following) oppure lista di utenti con i quali
	 * ha dei tag in comune (e quindi pu&ograve; seguire).
	 *
	 * @return Una stringa contenente un messaggio di errore oppure la rappresentazione, in
	 * formato JSON, della lista di utenti richiesta
	 */
	public String call() {
		// Utente non registrato
		User u = servRef.getUser(this.sender);
		if (u == null) {
			return "Errore:utente \"" + this.sender + "\" non registrato";
		}
		// Ottengo la lista e la serializzo in JSON su una stringa
		StringBuffer reply = new StringBuffer();
		Map<String, Set<String>> userTagsMap = new HashMap<>();
		try {
			// Richiesta dell'elenco dei seguiti
			if (entity.equals("following")) {
				// Inserisco nella map le entry <utente> -> <set di tag>
				for (String followedUser : u.getFollowing()) {
					userTagsMap.put(followedUser, this.servRef.getUser(followedUser).getTags());
				}
				reply.append(this.mapper.writeValueAsString(userTagsMap));
			} else if (entity.equals("users")) {
				// Richiesta degli utenti con tag in comune
				Set<String> uTags = u.getTags();
				for (User x : servRef.getUsers()) {
					// Salto l'utente che ha richiesto l'operazione
					if (x.equals(u)) {
						continue;
					}
					// se vi è almeno un tag in comune Collections.disjoint ritorna false
					// => inverto il valore di verità per testare se vi è almeno un tag in comune
					if (!Collections.disjoint(uTags, x.getTags())) {
						userTagsMap.put(x.getUsername(), x.getTags());
					}
				}
				reply.append(this.mapper.writeValueAsString(userTagsMap));
			}
			if (userTagsMap.size() == 0) {
				if (entity.equals("users")) {
					return "Warning:nessun utente ha tag in comune con \"" + this.sender + "\"";
				} else {
					return "Warning:Non segui alcun utente";
				}
			}
			return reply.toString();
		} catch (JsonProcessingException e) {
			System.err.println("Impossibile completare " + this.getKind() + ": " + e.getMessage());
			return "Errore:impossibile completare la richiesta";
		}
	}
}
