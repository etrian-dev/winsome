package Winsome.WinsomeServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import Winsome.WinsomeExceptions.WinsomeConfigException;
import Winsome.WinsomeExceptions.WinsomeServerException;
import Winsome.WinsomeTasks.BlogTask;
import Winsome.WinsomeTasks.CommentTask;
import Winsome.WinsomeTasks.CreatePostTask;
import Winsome.WinsomeTasks.DeletePostTask;
import Winsome.WinsomeTasks.FollowTask;
import Winsome.WinsomeTasks.ListTask;
import Winsome.WinsomeTasks.LoginTask;
import Winsome.WinsomeTasks.LogoutTask;
import Winsome.WinsomeTasks.MulticastTask;
import Winsome.WinsomeTasks.QuitTask;
import Winsome.WinsomeTasks.RateTask;
import Winsome.WinsomeTasks.RewinTask;
import Winsome.WinsomeTasks.ShowFeedTask;
import Winsome.WinsomeTasks.ShowPostTask;
import Winsome.WinsomeTasks.Task;
import Winsome.WinsomeTasks.WalletTask;

/**
 * Classe che implementa il server Winsome
 */
public class WinsomeServer extends Thread {
	/** riferimento alla configurazione del server */
	private ServerConfig serverConfiguration;

	/** canale NIO non bloccante usato per accettare connessioni TCP */
	ServerSocketChannel connListener;
	/** Datagram socket per l'invio della notifica di aggiornamento del wallet */
	DatagramSocket walletNotificationGroup;

	/** riferimento alla lista di utenti di Winsome */
	private ConcurrentHashMap<String, User> all_users;
	/** Albero dei post globale */
	private HashMap<Long, Post> postMap;
	private ReentrantReadWriteLock postMapLock;
	/** mappa dei blog */
	private HashMap<String, ConcurrentLinkedDeque<Post>> all_blogs;
	private ReentrantReadWriteLock blogMapLock;
	/** mappa degli oggetti che remoti per l'aggiornamento dei follower di un utente */
	private Map<String, FollowerCallbackState> callbacks;

	/** threadpool per il processing e l'esecuzione delle richieste */
	private ThreadPoolExecutor tpool;
	/** coda di richieste per la threadpool */
	private ArrayBlockingQueue<Runnable> tpoolQueue;
	/** Handler custom per richieste rifiutate dal threadpool */
	private RejectedTaskHandler tpoolHandler;

	/** Threadpool per l'update dei follower e dei wallet ad intervalli fissi */
	private ScheduledThreadPoolExecutor updaterPool;

	/** Timestamp dell'ultimo aggiornamento dei wallet */
	private long lastWalletsUpdate;

	/** file contenente la lista di utenti (persistente, letta all'avvio del server) */
	private File userFile;
	private ObjectMapper mapper;
	private JsonFactory factory;

	/**
	 * Crea l'istanza del WinsomeServer con la configurazione specificata
	 *
	 * @param configuration la configurazione del server da lanciare
	 */
	public WinsomeServer(ServerConfig configuration) throws WinsomeServerException, WinsomeConfigException {
		// Riferimento alla configurazione del server letta dal file
		this.serverConfiguration = configuration;

		// Creazione del ServerSocket (non bloccante)
		try {
			this.connListener = ServerSocketChannel.open();
			this.connListener.configureBlocking(false);
			this.connListener.bind(new InetSocketAddress(
					this.serverConfiguration.getServerSocketAddress(),
					this.serverConfiguration.getServerSocketPort()));
		} catch (IOException e) {
			throw new WinsomeServerException("Impossibile inizializzare il ServerSocketChannel: "
					+ e.getMessage());
		}

		// Creazione del DatagramSocket per la notifica di update del wallet
		// Non è legato ad alcuna porta in quanto non deve ricevere alcun datagramma
		try {
			this.walletNotificationGroup = new DatagramSocket();
		} catch (SocketException se) {
			throw new WinsomeServerException("Impossibile inizializzare il DatagramSocket: "
					+ se.getMessage());
		}

		// Crea la mappa Username -> Utente
		this.all_users = new ConcurrentHashMap<>();
		// Crea l'oggetto file degli utenti
		this.userFile = new File(configuration.getDataDir() + "/users.json");
		if (!this.userFile.exists()) {
			throw new WinsomeServerException("Il file degli utenti "
					+ this.userFile.getAbsolutePath() + " non esiste");
		}

		this.postMap = new HashMap<>();
		this.postMapLock = new ReentrantReadWriteLock(true);

		this.all_blogs = new HashMap<>();
		this.blogMapLock = new ReentrantReadWriteLock();

		// coda fair (accesso thread bloccati FIFO)
		this.tpoolQueue = new ArrayBlockingQueue<>(configuration.getWorkQueueSize(), true);
		this.tpoolHandler = new RejectedTaskHandler(configuration.getRetryTimeout());

		// Inizializzo la mappa degli oggetti remoti per le callback RMI
		this.callbacks = new HashMap<>();

		// Inizializzazione threadpool
		this.tpool = new ThreadPoolExecutor(
				Math.max(configuration.getMinPoolSize(), 1),
				configuration.getMaxPoolSize(),
				60L, TimeUnit.SECONDS, this.tpoolQueue, this.tpoolHandler);
		this.tpool.prestartCoreThread(); // fa partire un thread in attesa di richieste

		// TODO: config file updaterPool core size
		this.updaterPool = new ScheduledThreadPoolExecutor(1);
		this.updaterPool.setRemoveOnCancelPolicy(true);
		this.updaterPool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

		// TODO: Maybe one walletUpdater per user and configurable rate
		this.updaterPool.scheduleAtFixedRate(new WalletNotifier(this), 30L, 30L, TimeUnit.SECONDS);

		// TODO: fix this, should probably persist somewhere
		// Settato inizialmente a 0 (01/01/1970) per provocare il ricalcolo di tutti i post
		this.lastWalletsUpdate = 0L;

		// Inizializzazione vari oggetti Jackson
		this.mapper = new ObjectMapper();
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
		this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		this.mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
		this.factory = new JsonFactory(mapper);

		try {
			// Leggo file users.json
			loadUsers();
			// Dato che ho caricato tutti gli utenti posso iniziare a caricare i loro blog
			// in modo concorrente, dedicando un nuovo thread ad ogni utente
			// Non vi è la necessità di meccanismi di sincronizzazione,
			// dato che ciascun thread legge un file diverso

			// Carica i blog di tutti gli utenti di Winsome
			loadBlogs();
			// Dopo aver caricato tutti i blog viene costruita la mappa dei post globale
			for (ConcurrentLinkedDeque<Post> dq : this.all_blogs.values()) {
				for (Post p : dq) {
					this.postMap.put(p.getPostID(), p);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new WinsomeServerException(
					"Impossibile leggere il file degli utenti " + this.userFile.getAbsolutePath());
		}

		System.out.println("Server created");
	}

	/**
	 * Metodo per la lettura degli utenti dal file nella directory dati
	 *
	 * @throws WinsomeConfigException
	 * @throws IOException
	 */
	private void loadUsers() throws WinsomeConfigException, IOException {
		BufferedInputStream bufIn = new BufferedInputStream(new FileInputStream(userFile));
		JsonParser parser = this.factory.createParser(bufIn);
		// Il file degli utenti è un'array di oggetti, ciascuno dei quali rappresenta un utente Winsome
		// Ogni oggetto contiene i campi username, password, etc...
		JsonToken tok = parser.nextToken(); // avanzo al primo token (deve essere START_ARRAY)
		if (tok == JsonToken.NOT_AVAILABLE || tok != JsonToken.START_ARRAY) {
			throw new WinsomeConfigException("Il file degli utenti \""
					+ this.userFile.getAbsolutePath() + "\" non rispetta la formattazione attesa "
					+ "\n(non è un array di oggetti)");
		}
		// parsing di ciascun oggetto nell'array
		while ((tok = parser.nextToken()) != JsonToken.END_ARRAY) {
			if (tok != JsonToken.START_OBJECT) {
				throw new WinsomeConfigException("Il file degli utenti \""
						+ this.userFile.getAbsolutePath() + "\" non rispetta la formattazione attesa " +
						"\n(elemento dell'array non è un oggetto)");
			}
			// Preparo un oggetto utente in cui scrivere i valori deserializzati
			User u = new User();
			while ((tok = parser.nextToken()) != JsonToken.END_OBJECT) {
				// ottengo il prossimo campo all'interno dell'oggetto User (se null esco)
				String field = null;
				if (tok == JsonToken.FIELD_NAME) {
					field = parser.getCurrentName();
				}
				if (field == null) {
					break;
				}

				switch (field) {
					case "username":
						u.setUsername(parser.nextTextValue());
						break;
					case "password":
						u.setPassword(parser.nextTextValue());
						break;
					case "wallet":
						// Valore di default del wallet a 0 Wincoin, se vi sono errori nella deserializzazione
						parser.nextValue();
						u.setWallet(parser.getValueAsDouble(0.0));
						break;
					case "tags":
					case "followers":
					case "following":
					case "transactions":
						// Le propritetà tags, followers e following devono avere 
						// come valore un array di stringhe
						if (parser.nextToken() != JsonToken.START_ARRAY) {
							throw new WinsomeConfigException("Il file degli utenti \""
									+ this.userFile.getAbsolutePath() + "\" non rispetta la formattazione attesa "
									+ "\n(campo dell'oggetto User non riconosciuto)");
						}
						while ((tok = parser.nextToken()) != JsonToken.END_ARRAY) {
							String val = parser.getValueAsString();
							if (field.equals("tags")) {
								u.setTag(val);
							} else if (field.equals("followers")) {
								u.setFollower(val);
							} else if (field.equals("following")) {
								u.setFollowing(val);
							} else if (field.equals("transactions")) {
								Transaction t = parser.readValueAs(Transaction.class);
								u.setTransactions(t);
							} else {
								throw new WinsomeConfigException("Proprietà "
										+ field + " non riconosciuta");
							}
						}
						break;
					default:
						throw new WinsomeConfigException("Il file degli utenti \""
								+ this.userFile.getAbsolutePath() + "\" non rispetta la formattazione attesa "
								+ "\n(campo dell'oggetto User non riconosciuto)");
				}
			}
			// Aggiungo l'utente deserializzato alla map
			this.all_users.put(u.getUsername(), u);
		}
	}

	/**
	 * Metodo che carica i blog degli utenti di Winsome all'avvio del server
	 * dalla directory specificata nel file di configurazione.
	 *
	 * Crea tanti thread quanti sono gli utenti, che lavorano in modo concorrente
	 */
	private void loadBlogs() {
		System.out.println("\n\nInizio caricamento dei blog dalla directory "
				+ this.serverConfiguration.getDataDir());
		List<BlogLoaderThread> loaders = new LinkedList<>();
		for (String username : getUsernames()) {
			// Il blog viene creato con la lista di post vuota, ed il thread BlogLoader la riempe
			this.all_blogs.put(username, new ConcurrentLinkedDeque<Post>());
			BlogLoaderThread loaderTh = new BlogLoaderThread(
					username,
					this.serverConfiguration.getDataDir(),
					this.all_blogs.get(username));
			loaders.add(loaderTh);
			loaderTh.start();
		}
		// Blocca esecuzione del thread corrente fino a che
		// tutti thread loader non hanno caricato tutti i blog e sono terminati
		for (BlogLoaderThread bt : loaders) {
			try {
				bt.join();
			} catch (InterruptedException e) {
				System.err.println("Thread " + Thread.currentThread().getName() + " interrupted");
			}
		}
	}

	public DatagramSocket getWalletNotifierSocket() {
		return this.walletNotificationGroup;
	}

	/**
	 * Metodo per reperire la lista di utenti della rete sociale
	 *
	 * @return la collezione di utenti di Winsome
	 */
	public Collection<User> getUsers() {
		return this.all_users.values();
	}

	/**
	 * Metodo per ritornare la lista di nomi utente nella rete sociale
	 *
	 * @return il set contenente tutti gli username di utenti di Winsome
	 */
	public Set<String> getUsernames() {
		return this.all_users.keySet();
	}

	/**
	 * Ritorna l'utente individuato da username
	 *
	 * @param username
	 * @return
	 */
	public User getUser(String username) {
		return this.all_users.get(username);
	}

	public ServerConfig getConfig() {
		return this.serverConfiguration;
	}

	/**
	 * Metodo sincronizzato per aggiungere un utente a Winsome.
	 *
	 * Dopo aver effettuato dei controlli sui parametri ricevuti inserisce
	 * l'utente tra quelli di Winsome e modifica il file degli utenti
	 * @param newUser il nuovo utente da aggiungere
	 * @return true sse il nuovo utente è stato aggiunto, false altrimenti
	 */
	public synchronized boolean addUser(User newUser) {
		// Una serie di controlli prima di aggiungere l'utente
		if (newUser.getTags().size() > 5
				|| newUser.getPassword() == null
				|| newUser.getPassword().equals("")
				|| this.all_users.keySet()
						.contains(newUser.getUsername().toLowerCase())) {
			return false;
		}
		// Aggiungo l'utente alla map, se non presente
		if (this.all_users.putIfAbsent(newUser.getUsername(), newUser) != null) {
			return false;
		}
		// Log dell'operazione
		StringBuffer s = new StringBuffer();
		s.append("=== New user created ===\nUser: " + newUser.getUsername());
		s.append("\nPassword: " + newUser.getPassword());
		s.append("\nTags: ");
		for (String t : newUser.getTags()) {
			s.append(t + ", ");
		}
		s.append('\n');
		System.out.println(s);

		return true;
	}

	public Post getPost(long id) {
		boolean locked = false;
		if (!this.postMapLock.isWriteLocked()) {
			locked = this.postMapLock.readLock().tryLock();
		}
		if (!locked) {
			this.postMapLock.readLock().lock();
		}
		Post p = this.postMap.get(id);
		this.postMapLock.readLock().unlock();
		return p;
	}

	/**
	 * Aggiunge un post p alla mappa globale dei post
	 * <p>
	 * Il post viene aggiunto, in mutua esclusione, alla mappa globale dei post: {@link #postMap}
	 * @param p il post da aggiungere
	 * @return true sse il post è stato inserito, false altrimenti
	 */
	public boolean addPost(Post p) {
		boolean locked = false;
		if (!this.postMapLock.isWriteLocked()) {
			locked = this.postMapLock.writeLock().tryLock();
		}
		if (!locked) {
			this.postMapLock.writeLock().lock();
		}
		Post oldP = this.postMap.putIfAbsent(p.getPostID(), p);
		this.postMapLock.writeLock().unlock();
		return (oldP == null ? true : false);
	}

	/**
	 * Rimuove un post p alla mappa globale dei post
	 * <p>
	 * Il post viene rimosso, in mutua esclusione, dalla mappa globale dei post: {@link #postMap}
	 * @param p il post da rimuovere
	 * @return true sse il post è stato rimosso, false altrimenti
	 */
	public boolean rmPost(Post p) {
		boolean locked = false;
		if (!this.postMapLock.isWriteLocked()) {
			locked = this.postMapLock.writeLock().tryLock();
		}
		if (!locked) {
			this.postMapLock.writeLock().lock();
		}
		boolean res = (this.postMap.remove(p.getPostID()) == null ? false : true);
		this.postMapLock.writeLock().unlock();
		return res;
	}

	/**
	 * Metodo per ottenere la coda di post del blog di un utente (inizializzata dal costruttore)
	 * <p>
	 * Il metodo è synchronized poich&eacute; il numero di thread che effettuano accessi concorrenti
	 * potrebbe essere elevato (al pi&ugrave; tanti quanti i thread attivi nella threadpool {@link tpool}),
	 * ma tali chiamate non aggiungono nuovi blog a {@link blogMapLock}, perci&ograve;
	 * la loro durata sar&agrave; molto breve
	 *
	 * @param user l'utente di cui si vuole ottenere il blog
	 * @return la coda concorrente di post del blog dell'utente user
	 */
	public synchronized ConcurrentLinkedDeque<Post> getBlog(String user) {
		boolean locked = false;
		if (!this.blogMapLock.isWriteLocked()) {
			locked = this.blogMapLock.readLock().tryLock();
		}
		if (!locked) {
			this.blogMapLock.readLock().lock();
		}
		ConcurrentLinkedDeque<Post> bucket = this.all_blogs.get(user);
		this.blogMapLock.readLock().unlock();
		return bucket;
	}

	/**
	 * Aggiunge un post al blog del suo autore (da usare anche nel caso di post di tipo rewin)
	 * @param blog nome utente del blog a cui il post va aggiunto
	 * @param p il post da aggiungere
	 */
	public void addPostToBlog(String blog, Post p) {
		getBlog(blog).addLast(p);
	}

	/**
	 * Rimuove un post dal blog del suo autore
	 * @param p il post da rimuovere
	 */
	public void rmPostFromBlog(Post p) {
		getBlog(p.getAuthor()).remove(p);
	}

	public synchronized void addFollowerCallback(String user, FollowerCallbackState state) {
		this.callbacks.put(user, state);
	}

	public synchronized void rmFollowerCallback(String user) {
		this.callbacks.remove(user);
	}

	public synchronized FollowerCallbackState getFollowerCallback(String user) {
		return this.callbacks.get(user);
	}

	public ScheduledThreadPoolExecutor getUpdaterTpool() {
		return this.updaterPool;
	}

	public long getLastWalletsUpdate() {
		return this.lastWalletsUpdate;
	}

	// Non necessario controllo di concorrenza perché viene chiamato soltanto da un thread (WalletUpdater)
	public void setLastWalletsUpdate(long timestamp) {
		this.lastWalletsUpdate = timestamp;
	}

	// Accetta una nuova connessione dal client e registra il SocketChannel creato in lettura
	public void accept_connection(Selector sel, SelectionKey key) throws IOException {
		ServerSocketChannel schan = (ServerSocketChannel) key.channel();
		SocketChannel ss = schan.accept();
		System.out.println("Connessione accettata dal client " + ss.getRemoteAddress().toString());
		// SocketChannel settato non bloccante
		ss.configureBlocking(false);
		// Creo una istanza di clientData e la allego alla SelectionKey
		ClientData data = new ClientData();
		// registro questo socket solo per lettura
		ss.register(sel, SelectionKey.OP_READ, data);
	}

	public void run() {
		// Creo shutdown hook per persistenza dello stato del server alla terminazione
		// Thread per la persistenza dell'elenco di utenti
		SyncUsersThread userSync = new SyncUsersThread(this.userFile, this.mapper, this.factory, this);
		SyncBlogsThread blogSync = new SyncBlogsThread(this.serverConfiguration.getDataDir(), this.mapper,
				this.factory, this);
		Runtime runtimeInst = Runtime.getRuntime();
		runtimeInst.addShutdownHook(userSync);
		runtimeInst.addShutdownHook(blogSync);
		/*runtimeInst.addShutdownHook(null);
		runtimeInst.addShutdownHook(null);*/

		// Crea il Selector per smistare le richieste
		try (Selector servSelector = Selector.open();) {
			// Registro il ServerSocketChannel per l'operazione di accept
			this.connListener.register(servSelector, SelectionKey.OP_ACCEPT);

			boolean keepRunning = true;
			while (keepRunning) {
				// Select bloccante: attende che almeno uno dei channel registrati abbia del lavoro da fare
				servSelector.select();
				// Itero su tutte le selectionKey che sono ready
				Iterator<SelectionKey> iter = servSelector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					// Rimuovo la selection key corrente
					// => alla prossima iterazione resettato
					iter.remove();

					if (key.isAcceptable()) {
						accept_connection(servSelector, key);
					} else if (key.isReadable()) {
						Task t = RequestParser.parseRequest(this, key, mapper);
						// Lettura task fallita
						if (t == null) {
							continue;
						}
						// Task valida: submit al threadpool
						if (t.getState().equals("Valid")) {
							System.out.println(t);
							Future<?> res = null;
							switch (t.getKind()) {
								case "Multicast":
									res = this.tpool.submit((MulticastTask) t);
									break;
								case "Login":
									res = this.tpool.submit((LoginTask) t);
									break;
								case "Logout":
									res = this.tpool.submit((LogoutTask) t);
									break;
								case "List":
									res = this.tpool.submit((ListTask) t);
									break;
								case "Follow":
									res = this.tpool.submit((FollowTask) t);
									break;
								case "CreatePost":
									res = this.tpool.submit((CreatePostTask) t);
									break;
								case "DeletePost":
									res = this.tpool.submit((DeletePostTask) t);
									break;
								case "ShowPost":
									res = this.tpool.submit((ShowPostTask) t);
									break;
								case "CommentPost":
									res = this.tpool.submit((CommentTask) t);
									break;
								case "RatePost":
									res = this.tpool.submit((RateTask) t);
									break;
								case "RewinPost":
									res = this.tpool.submit((RewinTask) t);
									break;
								case "ShowFeed":
									res = this.tpool.submit((ShowFeedTask) t);
									break;
								case "Blog":
									res = this.tpool.submit((BlogTask) t);
									break;
								case "Wallet":
									res = this.tpool.submit((WalletTask) t);
									break;
								case "Quit":
									QuitTask qt = (QuitTask) t;
									// Chiudo il socketChannel del client
									closeClientConnection(qt.getUsername(), key);
									// Deregistro, se necessario, dal servizio di callback
									if (this.callbacks.remove(qt.getUsername()) != null) {
										System.out.println("Utente " + qt.getUsername()
												+ " deregistrato dal servizio di callback");
									}
									break;
							}
							// Metto la task in esecuzione sulla lista della struttura dati associata al socket
							if (!t.getKind().equals("Quit")) {
								ClientData cd = (ClientData) key.attachment();
								cd.addTask(res);
								// Dopo aver letto la task registro il SocketChannel per la scrittura del risultato
								setWritable(servSelector, key);
							}

						}
					} else if (key.isWritable()) {
						// Controllo se delle task sono state completate
						ClientData cd = (ClientData) key.attachment();

						if (cd.hasTasksDone()) {
							Object res;
							try {
								res = cd.removeTask().get();
							} catch (InterruptedException | ExecutionException e) {
								res = null;
							}
							SocketChannel client = (SocketChannel) key.channel();
							if (res != null) {
								if (res instanceof Integer) {
									ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
									bb.putInt((Integer) res);
									bb.flip();
									client.write(bb);
								} else if (res instanceof Long) {
									ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
									bb.putLong((Long) res);
									bb.flip();
									client.write(bb);
								} else if (res instanceof Double) {
									ByteBuffer bb = ByteBuffer.allocate(Double.BYTES);
									bb.putDouble((Double) res);
									bb.flip();
									client.write(bb);
								} else if (res instanceof String) {
									String resStr = (String) res;
									ByteBuffer bb = ByteBuffer.wrap(resStr.getBytes());
									// TODO: pending writes with support by ClientData with if on top of writable()
									client.write(bb);
								} else {
									System.err.println("Istanza risultato richiesta non riconosciuto:\n"
											+ res);
								}
							}

							// Adesso che ho completato la richiesta rimetto il SocketChannel in ascolto per lettura
							setReadable(servSelector, key);
						}
					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
	}

	private void closeClientConnection(String user, SelectionKey selKey) {
		SocketChannel clientChannel = (SocketChannel) selKey.channel();
		ClientData cData = (ClientData) selKey.attachment();
		User u = this.all_users.get(user);
		if (u != null && u.isLogged()) {
			cData.unsetCurrentUser(user);
			u.logout();
		}
		try {
			System.out.println("Chiusura della connessione del client " + clientChannel.getLocalAddress());
			clientChannel.close();
		} catch (IOException e) {
			System.err.println("Fallita chiusura connessione");
		}
	}

	private void setWritable(Selector sel, SelectionKey k) throws ClosedChannelException {
		SocketChannel sk = (SocketChannel) k.channel();
		sk.register(sel, SelectionKey.OP_WRITE, k.attachment());
		k.interestOps(SelectionKey.OP_WRITE);
	}

	private void setReadable(Selector sel, SelectionKey k) throws ClosedChannelException {
		SocketChannel sk = (SocketChannel) k.channel();
		sk.register(sel, SelectionKey.OP_READ, k.attachment());
		k.interestOps(SelectionKey.OP_READ);
	}
}
