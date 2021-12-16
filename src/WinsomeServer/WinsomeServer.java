package WinsomeServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import WinsomeExceptions.WinsomeConfigException;
import WinsomeExceptions.WinsomeServerException;

/**
 * Classe che implementa il server Winsome
 */
public class WinsomeServer extends Thread {
	/** riferimento alla configurazione del server */
	private ServerConfig serverConfiguration;

	/** canale NIO non bloccante usato per accettare connessioni TCP */
	ServerSocketChannel connListener;

	/** riferimento alla lista di utenti di Winsome */
	private Map<String, User> all_users;
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
			throw new WinsomeServerException("Impossibile inizializzare il ServerSocketChannel");
		}

		// Crea la mappa Username -> Utente
		this.all_users = new HashMap<>();
		// Crea l'oggetto file degli utenti
		this.userFile = new File(configuration.getDataDir() + "/users.json");
		if (!this.userFile.exists()) {
			throw new WinsomeServerException("Il file degli utenti "
					+ this.userFile.getAbsolutePath() + " non esiste");
		}
		// Inizializzazione vari oggetti Jackson
		this.mapper = new ObjectMapper();
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
		this.factory = new JsonFactory(mapper);

		try {
			readUsers();
		} catch (com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException parserEx) {
			parserEx.printStackTrace();
			System.out.println("ERR: deserializzazione file, formato file errato");
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
	private void readUsers() throws WinsomeConfigException, IOException {
		BufferedInputStream bufIn = new BufferedInputStream(new FileInputStream(userFile));
		JsonParser parser = this.factory.createParser(bufIn);
		// Leggo tutti i token del parser
		JsonToken tok = parser.nextToken();
		if (tok == JsonToken.NOT_AVAILABLE || tok != JsonToken.START_ARRAY) {
			throw new WinsomeConfigException("Il file degli utenti  "
					+ this.userFile.getAbsolutePath() + "non rispetta la formattazione attesa");
		}
		while ((tok = parser.nextToken()) != JsonToken.END_ARRAY) {
			// tok contiene l'oggetto di tipo User, per cui posso deserializzarlo
			User u = parser.readValueAs(User.class);
			this.all_users.put(u.getUsername(), u);
		}
	}

	/**
	 * Metodo sincronizzato per reperire la lista di utenti della rete sociale
	 * 
	 * @return una Map non modificabile contenente le coppie &lt;username,utente&gt;
	 */
	public synchronized Map<String, User> getUsers() {
		return Map.copyOf(this.all_users);
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

		// Creo un nuovo oggetto vuoto e riempo i campi da serializzare
		ObjectNode newUserObj = mapper.createObjectNode();
		newUserObj.put("username", newUser.getUsername());
		newUserObj.put("password", newUser.getPassword());
		ArrayNode tagsArr = mapper.createArrayNode();
		for (String aTag : newUser.getTags()) {
			tagsArr.add(aTag);
		}
		newUserObj.set("tags", tagsArr);

		// TODO: scrittura nuovo utente allo shutdown del server?
		try {
			ArrayNode tree = (ArrayNode) mapper.readTree(this.userFile);
			if (!tree.isArray()) {
				throw new WinsomeServerException("Il file degli utenti  "
						+ this.userFile.getAbsolutePath() + "non rispetta la formattazione attesa");
			}
			// Aggiungo un nodo all'array, contenente l'istanza di User serializzata
			tree.add(newUserObj);
			// Scrivo sul file l'array modificato
			JsonGenerator gen = this.factory.createGenerator(this.userFile, JsonEncoding.UTF8);
			gen.useDefaultPrettyPrinter();
			mapper.writeTree(gen, tree);
		} catch (IOException | WinsomeServerException e) {
			System.out.println(e);
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

	// Accetta una nuova connessione dal client e registra il SocketChannel creato in lettura
	public void accept_connection(Selector sel, SelectionKey key) throws IOException {
		ServerSocketChannel schan = (ServerSocketChannel) key.channel();
		SocketChannel ss = schan.accept();
		System.out.println("Connessione accettata dal client " + ss.getRemoteAddress().toString());
		// SocketChannel settato non bloccante
		ss.configureBlocking(false);
		// Il client usa sempre questo buffer passato come attachment
		ss.register(sel, SelectionKey.OP_READ);
	}

	public void run() {
		// Crea il Selector per smistare le richieste
		try (Selector servSelector = Selector.open();) {
			// Registro il ServerSocketChannel per l'operazione di accept
			// TODO: add attachment for callbacks maybe?
			this.connListener.register(servSelector, SelectionKey.OP_ACCEPT);

			boolean keepRunning = true;
			while (keepRunning) {
				if (servSelector.select() > 0) {
					Iterator<SelectionKey> iter = servSelector.selectedKeys().iterator();
					while (iter.hasNext()) {
						SelectionKey key = iter.next();
						// Rimuovo la selection key corrente
						// => alla prossima iterazione resettato
						iter.remove();

						if (key.isAcceptable()) {
							accept_connection(servSelector, key);
						} else if (key.isReadable()) {
							// TODO: parse request
							;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
	}
}
