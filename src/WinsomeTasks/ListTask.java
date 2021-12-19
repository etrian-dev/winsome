package WinsomeTasks;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;

import WinsomeServer.User;
import WinsomeServer.WinsomeServer;

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

	public String call() {
		User u = servRef.getUser(this.sender);
		if (u == null) {
			return "Errore: utente " + this.sender + " non presente";
		}
		StringBuilder reply = new StringBuilder();
		if (entity.equals("followers")) {
			for (String username : u.getFollowers()) {
				User x = servRef.getUser(username);
				reply.append(username + ":" + x.getTags().toString() + ",");
			}
		} else if (entity.equals("following")) {
			for (String username : u.getFollowing()) {
				User x = servRef.getUser(username);
				reply.append(username + ":" + x.getTags().toString() + ",");
			}
		} else if (entity.equals("users")) {
			ArrayList<User> matchingUsers = new ArrayList<>();
			for (User x : servRef.getUsers()) {
				// Salto l'utente che ha richiesto l'operazione
				if (x.equals(u)) {
					continue;
				}
				Set<String> xTags = x.getTags();
				Set<String> uTags = u.getTags();
				for (String userTag : uTags) {
					if (xTags.contains(userTag))
						matchingUsers.add(x);
				}
			}
			for (User x : matchingUsers) {
				reply.append(x.getUsername() + ":" + x.getTags().toString() + ",");
			}
		}

		return reply.toString();
	}
}
