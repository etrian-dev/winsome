package Winsome.WinsomeServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** 
 * Task Runnable eseguita periodicamente dall'updaterPool per aggiornare il valore
 * dei wallet di tutti gli utenti in Winsome
 */
public class WalletNotifier implements Runnable {
	public static String NOTIFICATION_MSG_FMT = "Wallets aggiornati (%s)";

	private ReentrantReadWriteLock postLock;
	private WinsomeServer servRef;

	public WalletNotifier(ReentrantReadWriteLock pLock, WinsomeServer serv) {
		this.postLock = pLock;
		this.servRef = serv;
	}

	// and the end of the task: it may or may not be counted in this update or the next
	// (a post without rewards)
	public void run() {
		System.out.println("Inizio update wallets");
		// Ottengo il timestamp dell'ultimo aggiornamento dei wallet
		long lastUpdate = servRef.getLastWalletsUpdate();
		Collection<User> all_users = this.servRef.getUsers();

		// Per ogni utente winsome calcolo la ricompensa in wincoin da corrispondere
		Map<String, Double> all_rewards = new HashMap<>(all_users.size());

		this.postLock.writeLock().lock();
		try {
			// Acquisisco lock sulla mappa dei post ed i blog
			// non possono essere aggiunti post durante il calcolo, per non comprometterne la consistenza

			// Valuto ricompense per tutti gli utenti
			for (User u : all_users) {
				// Per ogni post pubblicato da questo utente calcolo ricomensa curatori ed autore
				ConcurrentLinkedDeque<Post> posts = this.servRef.getBlog(u.getUsername());
				for (Post p : posts) {
					// Set degli utenti che sono curatori di questo post (hanno commentato o messo like)
					Set<String> curators = new HashSet<>();
					// Somma dei voti al post: +1 per like e -1 per dislike
					List<Vote> all_votes = p.getVotes();
					double likesSum = 0;
					for (Vote v : all_votes) {
						// Voto non valutato dall'ultimo update
						if (v.getTimestamp() > lastUpdate) {
							likesSum += (v.getIsLike() ? 1.0 : -1.0);
							// Se è un like va aggiunto ai curatori
							if (v.getIsLike()) {
								curators.add(v.getVoter());
							}
						}
					}
					// FIXME: debug print
					System.out.println("Somma like a post " + p.getPostID() + ": "
							+ likesSum + " (dal " + lastUpdate + ")");

					// Somma del reward per i commenti
					List<Comment> all_comments = p.getComments();
					double commentSum = 0;
					// Costruisco una map contenente <utente che ha messo un commento> -> <num commenti>
					Map<String, Integer> user_comments = new HashMap<>();
					for (Comment c : all_comments) {
						// Contato commento solo se aggiunto dopo l'ultimo update
						if (c.getTimestamp() > lastUpdate) {
							// Se non presente autore aggiunge con valore 1,
							// altrimenti incrementa di 1 num commenti
							if (user_comments.get(c.getAuthor()) == null) {
								user_comments.put(c.getAuthor(), 1);
							} else {
								user_comments.replace(c.getAuthor(), user_comments.get(c.getAuthor()) + 1);
							}
							// In ogni caso viene aggiunto come curatore
							curators.add(c.getAuthor());

							// FIXME: debug print
							System.out
									.println(c.getAuthor() + " ha commentato "
											+ user_comments.get(c.getAuthor()) + " volte da " + lastUpdate);
						}
					}
					// Calcolo somma commenti
					for (Integer numComments : user_comments.values()) {
						System.out.printf("2/(1 + e^(-%d + 1))\n", numComments);
						Double val = 2.0 / (1.0 + Math.exp(-(numComments.doubleValue() - 1.0)));
						commentSum += val;
					}
					// FIXME: debug print
					System.out.println("Somma commenti al post" + p.getPostID() + " = " + commentSum);

					// Calcolo reward totale del post
					Double reward = (Math.log1p(Math.max(likesSum, 0))
							+ Math.log1p(commentSum))
							/ (p.getAge() + 1);
					// Se vi è almeno un curatore la ricompensa deve essere suddivisa
					// Se non ve ne sono (curators.size() == 0) allora tutto il reward va all'autore del post
					Double authorReward = reward;
					Double curatorReward = 0.0;
					if (curators.size() > 0) {
						authorReward *= servRef.getConfig().getAuthorPercentage();
						// Il reward per i curatori è diviso in parti uguali tra il rimanente
						curatorReward = (reward * (1 - servRef.getConfig().getAuthorPercentage()))
								/ curators.size();
					}

					// FIXME: debug prints
					System.out.println("Reward totale: " + reward);
					System.out.println("Reward autore: " + authorReward);
					System.out.println("Reward curatori : " + curatorReward + " / " + curators.size());

					// Aggiorno i reward nella map con i valori calcolati (mai negativi)
					all_rewards.merge(p.getAuthor(), authorReward, (oldVal, newReward) -> oldVal + newReward);
					for (String curator : curators) {
						all_rewards.merge(curator, curatorReward, (oldVal, newReward) -> oldVal + newReward);
					}
					// L'età del post viene aumentata
					p.setAge(p.getAge() + 1);
				}
			}

		} finally {

			System.out.println("To be Unlocked");
			this.postLock.writeLock().unlock();
			System.out.println("Unlocked");
		}
		System.out.println(all_rewards);

		// Rimuovo reward nulli, per evitare di inserire transazioni con valore 0 nei wallet degli utenti
		all_rewards.entrySet().removeIf(e -> e.getValue() == 0.0);
		System.out.println(all_rewards);
		System.out.println("Removed empty transactions");

		// Calcolati tutti i reward per gli utenti: effetto la modifica del loro wallet
		for (Map.Entry<String, Double> entry : all_rewards.entrySet()) {
			System.out.println("Reward per " + entry.getKey()
					+ " (" + lastUpdate + " to " + System.currentTimeMillis() + ")");
			this.servRef.getUser(entry.getKey()).addReward(entry.getValue());
			System.out.println("Updated " + entry.getKey() + "'s wallet with " + entry.getValue());
		}

		System.out.println("Fine update wallets");

		// Setto al timestamp corrente l'ultimo update dei wallet
		this.servRef.setLastWalletsUpdate(System.currentTimeMillis());

		// Notifico tutti i client sull'indirizzo multicast dell'update
		Date dt = new Date(this.servRef.getLastWalletsUpdate());
		String msg = String.format(NOTIFICATION_MSG_FMT, dt.toString());
		SocketAddress saddr = new InetSocketAddress(
				this.servRef.getConfig().getMulticastGroupAddress(),
				this.servRef.getConfig().getMulticastGroupPort());
		DatagramPacket notification = new DatagramPacket(msg.getBytes(), msg.length(), saddr);
		try {
			this.servRef.getWalletNotifierSocket().send(notification);
			System.out.println(msg);
		} catch (IOException ioex) {
			System.err.println("Impossibile inviare notifica update wallet: "
					+ ioex.getMessage());
		}
	}
}
