package Winsome.WinsomeServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

/** 
 * Task Runnable eseguita periodicamente dall'updaterPool per aggiornare il valore
 * dei wallet di tutti gli utenti in Winsome
 */
public class WalletUpdater implements Runnable {
	public static String NOTIFICATION_MSG_FMT = "Wallets aggiornati al %d";

	private WinsomeServer servRef;

	public WalletUpdater(WinsomeServer serv) {
		this.servRef = serv;
	}

	// TODO: handle concurrency issues if a post is created between the start of this task
	// and the end of the task: it may or may not be counted in this update or the next
	// (a post without rewards)
	public void run() {
		long lastUpdate = servRef.getLastWalletsUpdate();
		Collection<User> all_users = this.servRef.getUsers();

		// Per ogni utente winsome si calcola la ricompensa in wincoin da corrispondere
		Map<String, Double> all_rewards = new HashMap<>(all_users.size());
		for (User u : all_users) {
			// Ottengo riferimento alla lista di post di questo utente
			ConcurrentLinkedDeque<Post> posts = this.servRef.getBlog(u.getUsername());
			for (Post p : posts) {
				// Set degli utenti che sono curatori di questo post (hanno commentato o messo like)
				Set<String> curators = new HashSet<>();
				// Implementazione della formula per il calcolo della ricompensa
				List<Vote> all_votes = p.getVotes();
				int likesSum = 0;
				for (Vote v : all_votes) {
					if (v.getTimestamp() > lastUpdate) {
						likesSum += (v.getIsLike() ? 1 : -1);
						// Se è un like va aggiunto ai curatori
						if (v.getIsLike()) {
							curators.add(v.getVoter());
						}
					}
				}
				List<Comment> all_comments = p.getComments();
				double commentSum = 0;
				for (Comment c : all_comments) {
					int totComments = this.servRef.getUser(c.getAuthor()).getTotalComments();
					if (c.getTimestamp() > lastUpdate) {
						commentSum += 2 / (1 + Math.pow(Math.E, -(totComments - 1)));
						// Il commentatore va aggiunto ai curatori
						curators.add(c.getAuthor());
					}
				}
				Double reward = (Math.log(Math.max(likesSum, 0) + 1)
						+ Math.log(commentSum + 1))
						/ p.getAge() + 1;
				Double authorReward = reward;
				Double curatorReward = 0.0;
				// Se vi è almeno un curatore la ricompensa deve essere suddivisa
				// Se non ve ne sono (curators.size() == 0) allora tutto il reward va all'autore del post
				if (curators.size() > 0) {
					authorReward *= servRef.getConfig().getAuthorPercentage();
					// Il reward per i curatori è diviso in parti uguali tra il rimanente
					curatorReward = (reward * (1 - servRef.getConfig().getAuthorPercentage()))
							/ curators.size();
				}
				// Aggiorno i reward nella map con i valori calcolati (mai negativi)
				all_rewards.merge(p.getAuthor(), authorReward, (oldVal, newReward) -> oldVal + newReward);
				for (String curator : curators) {
					all_rewards.merge(curator, curatorReward, (oldVal, newReward) -> oldVal + newReward);
				}
				// L'età del post viene aumentata
				p.setAge(p.getAge() + 1);
			}
		}
		// Setto al timestamp corrente l'ultimo update dei wallet
		this.servRef.setLastWalletsUpdate(System.currentTimeMillis());
		// Calcolati tutti i reward per gli utenti: effetto la modifica del loro wallet
		for (User u : all_users) {
			Double myReward = all_rewards.get(u.getUsername());
			if (myReward != null) {
				u.addReward(myReward);
			}
		}
		// Notifico tutti i client sull'indirizzo multicast dell'update
		String msg = String.format(NOTIFICATION_MSG_FMT, this.servRef.getLastWalletsUpdate());
		SocketAddress saddr = new InetSocketAddress(
				this.servRef.getConfig().getMulticastGroupAddress(),
				this.servRef.getConfig().getMulticastGroupPort());
		DatagramPacket notification = new DatagramPacket(
				msg.getBytes(),
				msg.length(), saddr);
		try {
			this.servRef.getWalletNotifierSocket().send(notification);
		} catch (IOException ioex) {
			System.err.println("Impossibile inviare notifica update wallet: "
					+ ioex.getMessage());
		}
	}
}
