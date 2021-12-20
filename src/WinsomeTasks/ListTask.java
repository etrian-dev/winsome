package WinsomeTasks;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;

import WinsomeServer.User;
import WinsomeServer.WinsomeServer;

/**
 * Task che implementa le operazioni <code>list users</code> e <code>list following</code>
 */
public class ListTask extends Task implements Callable<String> {
	private String entity;
	private String sender;
	private WinsomeServer servRef;

	public ListTask(String who, String what, WinsomeServer serv) {
		super.setInvalid();
		super.setKind("List");
		this.sender = who;
		this.entity = what;
		this.servRef = serv;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "\nSender: " + this.sender
				+ "\nEntity: " + this.entity;
	}

	/**
	 * Metodo che esegue l'operazione di elencazione degli utenti seguiti/seguibili
	 * 
	 * Il metodo controlla l'entit&agrave; di cui &egrave; stata richiesta la
	 * lista: utenti seguiti dal richiedente oppure lista di utenti con i quali 
	 * ha dei tag in comune (e quindi pu&ograve; seguire).
	 * 
	 * @return La stringa contenente la lista di utenti richiesta ed i loro tag,
	 *  oppure un messaggio di errore
	 */
	public String call() {
		User u = servRef.getUser(this.sender);
		if (u == null) {
			return "Errore: utente \"" + this.sender + "\" non presente";
		}
		StringBuilder reply = new StringBuilder();
		if (entity.equals("following")) {
			for (String username : u.getFollowing()) {
				User x = servRef.getUser(username);
				reply.append(username + ":" + x.getTags().toString() + "\n");
			}
		} else if (entity.equals("users")) {
			ArrayList<User> matchingUsers = new ArrayList<>();
			Set<String> uTags = u.getTags();
			for (User x : servRef.getUsers()) {
				// Salto l'utente che ha richiesto l'operazione
				if (x.equals(u)) {
					continue;
				}
				Set<String> xTags = x.getTags();
				for (String userTag : uTags) {
					if (xTags.contains(userTag)) {
						matchingUsers.add(x);
						break;
					}
				}
			}
			for (User x : matchingUsers) {
				reply.append(x.getUsername() + ":" + x.getTags().toString() + "\n");
			}
		}
		if (reply.length() == 0) {
			reply.append("Info: nessun utente ha tag in comune con \"" + this.sender + "\"");
		}
		return reply.toString();
	}
}
