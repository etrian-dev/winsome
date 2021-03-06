package Winsome.WinsomeClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import Winsome.WinsomeExceptions.WinsomeConfigException;
import Winsome.WinsomeRequests.BlogRequest;
import Winsome.WinsomeRequests.CommentRequest;
import Winsome.WinsomeRequests.CreatePostRequest;
import Winsome.WinsomeRequests.DeletePostRequest;
import Winsome.WinsomeRequests.FollowRequest;
import Winsome.WinsomeRequests.ListRequest;
import Winsome.WinsomeRequests.LoginRequest;
import Winsome.WinsomeRequests.LogoutRequest;
import Winsome.WinsomeRequests.QuitRequest;
import Winsome.WinsomeRequests.RateRequest;
import Winsome.WinsomeRequests.Request;
import Winsome.WinsomeRequests.RewinRequest;
import Winsome.WinsomeRequests.ShowFeedRequest;
import Winsome.WinsomeRequests.ShowPostRequest;
import Winsome.WinsomeRequests.WalletRequest;
import Winsome.WinsomeServer.Comment;
import Winsome.WinsomeServer.FollowerUpdaterService;
import Winsome.WinsomeServer.Post;
import Winsome.WinsomeServer.ServerMain;
import Winsome.WinsomeServer.Signup;
import Winsome.WinsomeServer.Transaction;
import Winsome.WinsomeServer.Vote;

/**
 * Classe main del client Winsome
 */
public class ClientMain {
	// opzioni da riga di comando
	public static final String CONFIG_OPT = "c";
	public static final String REGISTRY_OPT = "r";
	public static final String HOST_OPT = "s";
	public static final String SERVPORT_OPT = "p";
	public static final String HELP_OPT = "h";
	/** Path di default per il file di configurazione */
	public static final String[] CONF_DFLT_PATHS = { "config.json", "data/WinsomeClient/config.json" };
	/** prompt interattivo del client */
	public static final String USER_PROMPT = "> ";
	/** comando utilizzato per terminare il client */
	public static final String QUIT_COMMAND = "quit";
	/** Stringa di formato errore generico */
	public static final String FMT_ERR = "errore, %s\n";
	/** Dimensione di default di un buffer (ad esempio un ByteBuffer) */
	public static final int BUFSZ = 2048;

	// Header tabelle con una, due o tre colonne
	public static final String TABLE_HEADER_SINGLE_FMT = "|%20s|\n|%20s|\n";
	public static final String TABLE_HEADER_DOUBLE_FMT = "|%20s|%20s\n|%20s|%20s\n";
	public static final String TABLE_HEADER_TRIPLE_FMT = "|%20s|%20s|%20s\n|%20s|%20s|%20s\n";
	public static final String TABLE_HEADER_QUADRUPLE_FMT = "|%20s|%20s|%20s|%20s\n|%20s|%20s|%20s|%20s\n";
	public static final String[] TABLE_HEADERS = {
			"       Utente       ",
			"        Tags        ",
			"         Id         ",
			"       Autore       ",
			"       Titolo       ",
			"     Transazione    ",
			"        Data        ",
			"        Rewin       ",
			"====================" };

	// Costanti utili per la formattazione degli esiti delle operazioni
	private static final String REGISTER_OK_FMT = "Utente \"%s\" registrato\n";
	private static final String ALREADY_REGISTERED_FMT = "Registrazione fallita: Utente \"%s\" gi?? esistente\n";
	private static final String EMPTY_PWD_FMT = "Registrazione fallita: password vuota o non specificata\n";
	private static final String TOOMANY_TAGS = "Registrazione fallita: troppi tag specificati (massimo 5)";
	private static final String UNAUTHORIZED_FMT = "Operazione non autorizzata: controlla di aver effettuato il login\n";
	private static final String LOGIN_OK_FMT = "Utente \"%s\" autenticato\n";
	private static final String LOGOUT_OK_FMT = "Utente \"%s\" scollegato\n";
	private static final String NOT_LOGGED_FMT = "Utente \"%s\" non autenticato: controlla di aver effettuato il login\n";
	private static final String FOLLOW_OK_FMT = "L\'utente \"%s\" ha iniziato a seguire l\'utente \"%s\"\n";
	private static final String UNFOLLOW_OK_FMT = "L\'utente \"%s\" ha smesso di seguire l\'utente \"%s\"\n";
	private static final String USER_NEXISTS_FMT = "L\'utente \"%s\" (richiedente dell'operazione) non esiste\n";
	private static final String TARGET_NEXISTS_FMT = "L\'utente \"%s\" (oggetto dell'operazione) non esiste\n";
	private static final String ALREADY_FOLLOW_FMT = "L\'utente \"%s\" era gi?? tra i tuoi following\n";
	private static final String NOT_FOLLOWING_FMT = "L\'utente \"%s\" non era tra i tuoi following\n";
	private static final String POST_CREATED_FMT = "Nuovo post creato: Id = %d\n";
	private static final String POST_DELETED_FMT = "Post con Id = %d eliminato\n";
	private static final String POST_NEXISTS_FMT = "Il post con Id = %d non esiste\n";
	private static final String ORIGINAL_POST_NEXISTS_FMT = "Il rewin con Id = %d si riferisce ad un post cancellato\n";
	private static final String TITLE_OVERFLOW_FMT = "Titolo del post non valido (nullo o di lunghezza > 20 caratteri)\n";
	private static final String CONTENT_OVERFLOW_FMT = "Contenuto del post non valido (nullo o di lunghezza > 500 caratteri)\n";
	private static final String COMMENT_OK_FMT = "Commento al post con Id = %s registrato\n";
	private static final String SELF_COMMMENT_FMT = "Non ?? possibile commentare un proprio post\n";
	private static final String VOTE_OK_FMT = "Voto %+d al post con Id = %d registrato\n";
	private static final String ALREADY_VOTED_FMT = "Hai gi?? votato il post con Id = %d\n";
	private static final String SELF_VOTE_FMT = "Non ?? possibile votare un proprio post\n";
	private static final String NOT_IN_FEED_FMT = "Il post %d non ?? nel proprio feed (non segui il blog che lo ha condiviso)";
	private static final String REWINNED_POST_FMT = "Effettuato il rewin del post con Id = %d\nRewin: Id = %d\n";
	private static final String ALREADY_SUBSCRIBED = "Gi?? iscritto al servizio di callback per followers\n";
	private static final String NOT_SUBSCRIBED = "Non eri iscritto al servizio di callback per followers\n";
	private static final String WALLET_FMT = "Saldo corrente del wallet di %s: %f %s\n";

	/**
	 * Metodo main del client: carica il file di configurazione 
	 * e fa partire il loop principale
	 * 
	 * @param args gli argomenti da riga di comando di cui effettuare il parsing
	 */
	public static void main(String[] args) {
		// Effettua il parsing degli argomenti CLI
		ClientConfig in_config = parseArgs(args);
		if (in_config == null) {
			return;
		}
		// Carica il file di configurazione
		ClientConfig config = getClientConfig(in_config);
		if (config == null) {
			return;
		}
		// Stampo file di configurazione
		System.out.println(config + "\n");

		// Loop principale
		mainLoop(config);
	}

	/**
	 * Loop di lettura, parsing ed esecuzione dei comandi: termina al comando "quit"
	 * 
	 * @param config configurazione del client
	 */
	private static void mainLoop(ClientConfig config) {
		// Inizializzo la classe che incapsula lo stato del client
		WinsomeClientState state = new WinsomeClientState();
		// ObjectMapper per serializzazione e deserializzazione
		ObjectMapper mapper = new ObjectMapper();

		// Legge comandi da standard input fino a che non riceve il comando "quit"
		try (BufferedReader read_stdin = new BufferedReader(
				new InputStreamReader(System.in));) {
			// Setup dello streamtokenizer per la lettura dei comandi
			StreamTokenizer strmtok = new StreamTokenizer(read_stdin);
			strmtok.resetSyntax();
			strmtok.eolIsSignificant(true); // ritorna \n come token
			strmtok.lowerCaseMode(false);
			strmtok.quoteChar('"'); // setta il carattere da riconoscere come delimitatore di stringa
			strmtok.wordChars('#', '~'); // codici ascii caratteri considerati parte di una parola

			// Loop principale del client: legge il comando, ne fa il parsing e lo esegue
			while (!state.isTerminating()) {
				// Prompt utente (currentUser all'avvio ?? "")
				System.out.print(state.getCurrentUser() + ClientMain.USER_PROMPT);
				// Ogni token letto ?? inserito in una lista di stringhe
				ArrayList<String> tokens = new ArrayList<>();
				int token = strmtok.nextToken();
				while (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF) {
					if (strmtok.ttype == StreamTokenizer.TT_WORD) {
						tokens.add(strmtok.sval);
					} else if (strmtok.ttype == '"') {
						// Se vi sono token racchiusi tra '"' ottengo la stringa al loro interno
						tokens.add(strmtok.sval);
					}
					token = strmtok.nextToken();
				}
				// lettura di EOF equivale a eseguire quit
				if (token == StreamTokenizer.TT_EOF) {
					quit_command(null, state, null, mapper, config);
					continue;
				}

				// Dall'array di stringhe estraggo un ClientCommand
				String[] dummyArr = new String[tokens.size()];
				ClientCommand cmd = ClientCommand.parseCommand(tokens.toArray(dummyArr));
				if (cmd == null) {
					System.err.println(state.getCurrentUser() + ClientMain.USER_PROMPT
							+ "comando non riconosciuto");
					continue;
				}
				// Esegue il comando
				if (state.getSocket() == null
						&& cmd.getCommand() != Command.LOGIN
						&& cmd.getCommand() != Command.REGISTER
						&& cmd.getCommand() != Command.QUIT
						&& cmd.getCommand() != Command.HELP
						&& cmd.getCommand() != Command.UNKNOWN) {
					System.out.printf(FMT_ERR, "client non connesso");
				} else {
					execCommand(state, cmd, mapper, config);
				}
			}
		} catch (NoSuchElementException end) {
			System.out.println("Errore lettura: Terminazione");
		} catch (IOException e) {
			System.err.println("Errore I/O: Terminazione");
		}
	}

	private static void execCommand(
			WinsomeClientState state,
			ClientCommand cmd,
			ObjectMapper mapper,
			ClientConfig config) {
		// Variabili comuni tra le funzioni che implementano i comandi
		Request req = null;
		try {
			// In base al tipo di comando richiamo uno dei metodi definiti in seguito
			// Ciascuno di essi invia, se necessario, una richiesta al server e riceve la risposta
			switch (cmd.getCommand()) {
				case REGISTER:
					register_command(cmd, state, config);
					break;
				case LOGIN:
					login_command(cmd, state, req, mapper, config);
					break;
				case LOGOUT:
					logout_command(cmd, state, req, mapper, config);
					break;
				case LIST:
					// list users|following|followers
					list_command(cmd, state, req, mapper);
					break;
				case FOLLOW:
					follow_unfollow_command(true, cmd, state, req, mapper);
					break;
				case UNFOLLOW:
					follow_unfollow_command(false, cmd, state, req, mapper);
					break;
				case POST:
					create_post_command(cmd, state, req, mapper);
					break;
				case DELETE:
					delete_post_command(cmd, state, req, mapper);
					break;
				case SHOW:
					if (cmd.getArg(0).equals("feed")) {
						show_feed_command(cmd, state, req, mapper);
					} else {
						show_post_command(cmd, state, req, mapper);
					}
					break;
				case COMMENT:
					comment_post_command(cmd, state, req, mapper);
					break;
				case RATE:
					rate_post_command(cmd, state, req, mapper);
					break;
				case REWIN:
					rewin_post_command(cmd, state, req, mapper);
					break;
				case BLOG:
					show_blog_command(cmd, state, req, mapper);
					break;
				case WALLET:
					// Se ho un argmento esso ?? "btc" per la conversione
					if (cmd.getArgs() != null) {
						wallet_command(true, cmd, state, req, mapper);
					} else {
						// Valore corrente del wallet
						wallet_command(false, cmd, state, req, mapper);
					}
					break;
				case QUIT:
					quit_command(cmd, state, req, mapper, config);
					break;
				case HELP:
					ClientCommand.getHelp();
					break;
				default:
					System.err.printf(ClientMain.FMT_ERR, "comando non riconosciuto");
					// Stampo messaggio di aiuto di default
					ClientCommand.getHelp();

			}
		} catch (IOException jpe) {
			System.err.printf(ClientMain.FMT_ERR, "errore esecuzione richiesta");
		}
	}

	/**
	 * Funzione di utilit?? per serializzare una richiesta e riceverne la risposta
	 * in un ByteBuffer
	 * @param req la richiesta da serializzare
	 * @param state stato del client
	 * @param reply_bbuf buffer per la lettura dal socket
	 * @param mapper mapper per la serializzazione della richiesta
	 * @return
	 * @throws IOException
	 */
	private static ByteBuffer send_and_receive(Request req, WinsomeClientState state,
			ByteBuffer reply_bbuf, ObjectMapper mapper) throws IOException {
		// Richiesta serializzata e scritta sul socket
		ByteBuffer request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
		state.getSocket().write(request_bbuf);
		state.getSocket().read(reply_bbuf);
		reply_bbuf.flip();
		return reply_bbuf;
	}

	private static boolean set_followers_callback(WinsomeClientState state, ClientConfig config) {
		// Inizializzo l'oggetto per la callback
		System.out.println("Creazione RMI callback per followers...");
		boolean initialized = false;
		try {
			// Creo oggetto per callback
			FollowerCallbackImpl fcall = new FollowerCallbackImpl();
			// Ottengo dal registry lo stub per la registrazione al servizio
			Registry reg = LocateRegistry.getRegistry(config.getRegistryPort());
			FollowerUpdaterService fwup = (FollowerUpdaterService) reg.lookup(ServerMain.FOLLOWER_SERVICE_STUB);
			// Registro il client al servizio per l'utente (loggato) corrente
			int res = fwup.subscribe(state.getCurrentUser(), fcall);
			if (res == 0) {
				System.out.println("RMI callback creata");
				state.setCallback(fcall);
				initialized = true;
			} else if (res == 1) {
				System.err.printf(USER_NEXISTS_FMT, state.getCurrentUser());
			} else if (res == 2) {
				System.err.printf(NOT_LOGGED_FMT, state.getCurrentUser());
			} else if (res == 3) {
				System.err.printf(ALREADY_SUBSCRIBED);
			} else {
				// non dovrebbe mai essere eseguito, ma permette futura 
				System.err.printf(UNAUTHORIZED_FMT, state.getCurrentUser());
			}
		} catch (IOException | NotBoundException | IllegalArgumentException e) {
			e.printStackTrace();
			System.err.println("Fallita inizializzazione RMI callback");
			System.out.printf(ClientMain.FMT_ERR, e.getMessage());
			return false;
		}
		return initialized;
	}

	private static boolean unset_followers_callback(WinsomeClientState state, ClientConfig config) {
		// Inizializzo l'oggetto per la callback
		System.out.println("Unsubscribe from RMI callback...");
		boolean initialized = false;
		try {
			// Ottengo dal registry lo stub per la registrazione al servizio
			Registry reg = LocateRegistry.getRegistry(config.getRegistryPort());
			FollowerUpdaterService fwup = (FollowerUpdaterService) reg.lookup(ServerMain.FOLLOWER_SERVICE_STUB);
			// Registro il client al servizio per l'utente (loggato) corrente
			int res = fwup.unsubscribe(state.getCurrentUser());
			if (res == 0) {
				FollowerCallbackImpl callbackObj = state.getCallback();
				// Devo togliere l'oggetto remoto usato per il callback da RMI, altrimenti
				// il runtime che gestisce RMI mantiene la reference ed il client non termina
				UnicastRemoteObject.unexportObject(callbackObj, true);
				System.out.println("Deregistrazione dal servizio callback RMI effettuata");
				initialized = true;
			} else if (res == 1) {
				System.err.printf(USER_NEXISTS_FMT, state.getCurrentUser());
			} else if (res == 2) {
				System.err.printf(NOT_LOGGED_FMT, state.getCurrentUser());
			} else if (res == 3) {
				System.err.printf(NOT_SUBSCRIBED);
			} else {
				// non dovrebbe mai essere eseguito, ma permette futura 
				System.err.printf(UNAUTHORIZED_FMT, state.getCurrentUser());
			}
		} catch (IOException | NotBoundException | IllegalArgumentException e) {
			e.printStackTrace();
			System.err.println("Fallita cancellazione iscrizione a RMI callback");
			System.out.printf(ClientMain.FMT_ERR, e.getMessage());
			return false;
		}
		return initialized;
	}

	private static void set_multicast_addr(WinsomeClientState state, ObjectMapper mapper) throws IOException {
		// Crea una Request generica, con kind="Multicast"
		Request mcastReq = new Request();
		mcastReq.setKind("Multicast");
		ByteBuffer req_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(mcastReq));
		state.getSocket().write(req_bbuf);
		ByteBuffer rep_bbuf = ByteBuffer.allocate(100);
		int bytes_read = state.getSocket().read(rep_bbuf);
		rep_bbuf.flip();
		// La risposta ricevuta ?? un'InetSocketAddress sotto forma di stringa <ip>:<porta>
		String str = new String(rep_bbuf.array(), 0, bytes_read);
		InetSocketAddress addr = mapper.readValue(str, InetSocketAddress.class);
		state.setMcastAddress(addr.getAddress());
		state.setMcastPort(addr.getPort());
	}

	private static void register_command(ClientCommand cmd, WinsomeClientState state, ClientConfig config) {
		Signup stub = state.getStub();
		// Stub non inizializzato: lo inizializzo e lo setto in state
		if (stub == null) {
			try {
				System.out.println("Caricamento stub registrazione...");
				Registry reg = LocateRegistry.getRegistry(config.getRegistryPort());
				stub = (Signup) reg.lookup("register");
				state.setStub(stub);
			} catch (RemoteException | NotBoundException e) {
				System.out.println(e);
				return;
			}
		}

		int res = -1;
		List<String> tags = new ArrayList<>();
		for (int i = 2; i < cmd.getArgs().length; i++) {
			tags.add(cmd.getArg(i));
		}
		try {
			res = stub.register(cmd.getArg(0), cmd.getArg(1), tags);
			System.out.print(state.getCurrentUser() + ClientMain.USER_PROMPT);
			switch (res) {
				case 0:
					System.out.printf(REGISTER_OK_FMT, cmd.getArg(0));
					break;
				case 1:
					System.err.printf(ALREADY_REGISTERED_FMT, cmd.getArg(0));
					break;
				case 2:
					System.err.printf(EMPTY_PWD_FMT);
					break;
				case 3:
					System.err.printf(TOOMANY_TAGS);
					break;
				default:
					System.err.printf(ClientMain.FMT_ERR,
							"al momento non ?? possibile completare la registrazione, ci scusiamo per il disagio");
			}
		} catch (Exception e) {
			System.out.println("Eccezione: " + e.getMessage());
		}
	}

	private static void login_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper, ClientConfig config) throws IOException {
		// Prima controllo se un utente ?? gi?? loggato
		if (!(state.getCurrentUser().equals("") || state.getCurrentUser().equals(cmd.getArg(0)))) {
			System.err.printf(ClientMain.FMT_ERR, "altro utente loggato: effettuare il logout");
			return;
		}
		// Creo e setto il SocketChannel per la comunicazione con il server
		try {
			SocketChannel sc = SocketChannel.open();
			sc.connect(new InetSocketAddress(config.getServerHostname(), config.getServerPort()));
			state.setSocket(sc);
			// Ottengo indirizzo del gruppo multicast a cui partecipare per gli update del wallet
			if (state.getMcastAddr() == null) {
				set_multicast_addr(state, mapper);
				// Creo e faccio partire il thread che sta in ascolto su tale indirizzo
				state.startMcastThread(config.getNetIf());
			}
		} catch (IOException e) {
			System.err.printf(ClientMain.FMT_ERR, "impossibile connettersi al server all'indrizzo "
					+ config.getServerHostname() + ":" + config.getServerPort());
			return;
		}
		// Crea una nuova richiesta di login e la scrive sul channel TCP
		req = new LoginRequest(cmd.getArg(0), cmd.getArg(1));
		ByteBuffer reply_bbuf = ByteBuffer.allocate(Integer.BYTES);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);
		int result = reply_bbuf.getInt();
		// Se il login ha avuto successo cambia il prompt
		// altrimenti messaggio di errore
		switch (result) {
			// Login autorizzato
			case 0:
				System.out.printf(LOGIN_OK_FMT, cmd.getArg(0));
				// Setto prompt dinamico con username utente
				state.setUser(cmd.getArg(0));
				break;
			case 1:
				System.err.printf(USER_NEXISTS_FMT, cmd.getArg(0));
				break;
			case 2:
				System.err.printf(ClientMain.FMT_ERR, "password errata");
				break;
			case 3:
				System.err.printf(ClientMain.FMT_ERR, "login gi?? effettuato");
				break;
			default:
				System.err.printf(UNAUTHORIZED_FMT);
		}
		// clear per riutilizzo del buffer
		reply_bbuf.clear();

		if (result == 0) {
			// Creo RMI callback per la notifica dei follower
			set_followers_callback(state, config);
		}
	}

	private static void logout_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper, ClientConfig config)
			throws IOException {
		// Deregistrazione dal callback (prima del logout per permettere controlli)
		unset_followers_callback(state, config);
		// Join del thread per gli update
		state.stopMcastThread();
		// Invio della richiesta di logout al server sul socket
		int res = -1;
		req = new LogoutRequest(state.getCurrentUser());
		ByteBuffer reply_bbuf = ByteBuffer.allocate(Integer.BYTES);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);

		res = reply_bbuf.getInt();
		if (res == 0) {
			System.out.printf(LOGOUT_OK_FMT, state.getCurrentUser());
			// Chiudo il socket
			state.getSocket().close();
			// Resetto i parametri dello stato
			state.setUser("");
			state.setSocket(null);
			state.setCallback(null);
			state.setMcastSocket(null);
		} else if (res == 1) {
			System.err.printf(USER_NEXISTS_FMT, state.getCurrentUser());
		} else if (res == 2) {
			System.err.printf(NOT_LOGGED_FMT, state.getCurrentUser());
			// state.getCurrentUser() inalterato
		} else {
			System.err.printf(UNAUTHORIZED_FMT, state.getCurrentUser());
		}
	}

	private static void list_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		if (cmd.getArg(0).equals("followers")) {
			if (state.getFollowers() == null || state.getFollowers().size() == 0) {
				System.out.println("Nessun follower");
			} else {
				System.out.println("Ultimo update: " + (new Date(state.getLastFollowerUpdate())));
				System.out.printf(TABLE_HEADER_SINGLE_FMT, TABLE_HEADERS[0], TABLE_HEADERS[8]);
				for (String follower : state.getFollowers()) {
					System.out.printf("|%-20s|\n", follower);
				}
			}
			return;
		}

		req = new ListRequest(state.getCurrentUser(), cmd.getArg(0));
		ByteBuffer request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
		state.getSocket().write(request_bbuf);
		int bytes_read = 0;
		StringBuilder sbuf = new StringBuilder();
		do {
			ByteBuffer newBB = ByteBuffer.allocate(ClientMain.BUFSZ);
			bytes_read = state.getSocket().read(newBB);
			newBB.flip();
			sbuf.append(new String(newBB.array(), 0, bytes_read));
		} while (bytes_read == ClientMain.BUFSZ);

		String replyStr = sbuf.toString();
		// Controllo se messaggio di errore
		if (replyStr.startsWith("Errore") || replyStr.startsWith("Warning")) {
			String errorMsg = replyStr.substring(replyStr.indexOf(':', 0) + 1, replyStr.length());
			System.out.printf(errorMsg + "\n");
		} else {
			// deserializzo la mappa <utente> -> <tags> 
			//e formatto in una tabella <nomeutente>|<lista tag>
			Map<String, Set<String>> resultingMap = new HashMap<>();
			TypeReference<Map<String, Set<String>>> typeRef = new TypeReference<Map<String, Set<String>>>() {
			};
			try {
				resultingMap = mapper.readValue(replyStr, typeRef);
			} catch (JsonProcessingException ex) {
				System.err.println("Impossibile deserializzare la risposta: " + ex.getMessage());
				return;
			}
			System.out.printf(TABLE_HEADER_DOUBLE_FMT,
					TABLE_HEADERS[0], TABLE_HEADERS[1],
					TABLE_HEADERS[8], TABLE_HEADERS[8]);
			for (Map.Entry<String, Set<String>> entry : resultingMap.entrySet()) {
				System.out.printf("|%-20s|", entry.getKey());
				for (String tag : entry.getValue()) {
					System.out.print(tag + " ");
				}
				System.out.print('\n');
			}
		}
	}

	private static void follow_unfollow_command(boolean follow_unfollow, ClientCommand cmd, WinsomeClientState state,
			Request req, ObjectMapper mapper) throws IOException {
		int res = -1;
		req = new FollowRequest(state.getCurrentUser(), cmd.getArg(0), follow_unfollow);
		ByteBuffer reply_bbuf = ByteBuffer.allocate(Integer.BYTES);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);
		res = reply_bbuf.getInt();
		if (res == 0) {
			if (follow_unfollow) {
				System.out.printf(FOLLOW_OK_FMT, state.getCurrentUser(), cmd.getArg(0));
			} else {
				System.out.printf(UNFOLLOW_OK_FMT, state.getCurrentUser(), cmd.getArg(0));
			}
		} else if (res == 1) {
			// Teoricamente questo ramo ?? eseguito solo se il comando ?? lanciato 
			// prima che l'utente abbia effettuato il login (e quindi currentUser="")
			System.err.printf(USER_NEXISTS_FMT, state.getCurrentUser());
		} else if (res == 2) {
			System.err.printf(TARGET_NEXISTS_FMT, cmd.getArg(0));
		} else {
			if (follow_unfollow) {
				System.err.printf(ALREADY_FOLLOW_FMT, cmd.getArg(0));
			} else {
				System.err.printf(NOT_FOLLOWING_FMT, cmd.getArg(0));
			}
		}
	}

	private static void create_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		// comando per la creazione di un nuovo post con argomenti titolo e contenuto
		req = new CreatePostRequest(state.getCurrentUser(), cmd.getArg(0), cmd.getArg(1));
		ByteBuffer reply_bbuf = ByteBuffer.allocate(Long.BYTES);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);
		long newPostID = reply_bbuf.getLong();
		if (newPostID == -1) {
			System.err.printf(UNAUTHORIZED_FMT);
		} else if (newPostID == -2) {
			System.err.printf(TITLE_OVERFLOW_FMT);
		} else if (newPostID == -3) {
			System.err.printf(CONTENT_OVERFLOW_FMT);
		} else {
			System.out.printf(POST_CREATED_FMT, newPostID);
		}
	}

	private static void delete_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		int res = -1;
		long postID = Long.valueOf(cmd.getArg(0));
		req = new DeletePostRequest(postID);
		ByteBuffer reply_bbuf = ByteBuffer.allocate(Integer.BYTES);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);
		res = reply_bbuf.getInt();
		if (res == -1) {
			System.err.printf(UNAUTHORIZED_FMT, state.getCurrentUser());
		} else if (res == -2) {
			System.err.printf(POST_NEXISTS_FMT, postID);
		} else {
			System.out.printf(POST_DELETED_FMT, postID);
		}
	}

	private static void show_feed_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		req = new ShowFeedRequest();
		ByteBuffer request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
		ByteBuffer reply_bbuf = ByteBuffer.allocate(BUFSZ);
		state.getSocket().write(request_bbuf);

		boolean finish_read = false;
		int bytes_read = 0;
		while (!finish_read) {
			bytes_read = state.getSocket().read(reply_bbuf);
			reply_bbuf.flip();

			// Controllo se la risposta ?? un messaggio di errore
			String reply = new String(reply_bbuf.array());
			if (reply.startsWith("Errore")) {
				System.err.printf(FMT_ERR, reply.substring(reply.indexOf(':') + 1));
				finish_read = true;
			} else {
				// Deserializzo la lista di post
				List<Post> feed = new ArrayList<>();
				TypeReference<List<Post>> typeRef = new TypeReference<List<Post>>() {
				};
				try {
					feed = mapper.readValue(reply, typeRef);
					// Ciascun post nel feed viene visualizzato come segue:
					// Il campo Id contiene l'id del post, indipendentemente dal fatto che esso sia un rewin
					// o sia un post originale.
					// Il campo autore mostra il nome dell'utente che ha pubblicato il post se esso ?? un
					// post originale, altrimenti mostra l'autore del post originale (potrebbe anche non essere
					// tra gli autori seguiti da questo utente)
					// Il campo Rewin ha valore "Y" se il post ?? un rewin, "N" altrimenti
					System.out.printf(TABLE_HEADER_QUADRUPLE_FMT,
							TABLE_HEADERS[2], TABLE_HEADERS[3], TABLE_HEADERS[7], TABLE_HEADERS[4],
							TABLE_HEADERS[8], TABLE_HEADERS[8], TABLE_HEADERS[8], TABLE_HEADERS[8]);
					for (Post p : feed) {
						System.out.printf("|%-20d|%-20s|%-20s|%s\n",
								p.getPostID(), (p.getIsRewin() ? p.getOriginalAuthor() : p.getAuthor()),
								(p.getIsRewin() ? "Y" : "N"), p.getTitle());
					}
					finish_read = true;
				} catch (JsonProcessingException ex) {
					// Deserializzazione fallita per buffer troppo corto
					// Lo raddoppio
					if (bytes_read > 0) {
						ByteBuffer newbb = ByteBuffer.allocate(reply_bbuf.capacity() * 2);
						newbb.put(reply_bbuf);
						reply_bbuf = newbb;
					} else {
						System.err.println("Impossibile deserializzare la risposta: " + ex.getMessage());
						finish_read = true;
					}
				}
			}
		}
	}

	private static void show_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		long postID = Long.valueOf(cmd.getArg(1));
		req = new ShowPostRequest(postID);
		// Sta sicuramente in un ByteBuffer singolo perch?? ClientMain.BUFSZ >> 500 + 20 + costante
		ByteBuffer reply_bbuf = ByteBuffer.allocate(BUFSZ);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);

		String reply = new String(reply_bbuf.array());
		// Controllo se la risposta ?? un messaggio di errore
		if (reply.startsWith("Errore")) {
			System.err.printf(FMT_ERR, reply.substring(reply.indexOf(':') + 1));
		} else {
			// Deserializzo il post
			try {
				Post p = mapper.readValue(reply, Post.class);

				// Stampo info sul post
				if (p.getIsRewin()) {
					System.out.println("Rewin del post "
							+ p.getOriginalID() + " di " + p.getOriginalAuthor());
				}
				System.out.println("Titolo: " + p.getTitle());
				System.out.println("Contenuto: " + p.getContent());
				int positiveVotes = 0;
				int negativeVotes = 0;
				for (Vote v : p.getVotes()) {
					if (v.getIsLike()) {
						positiveVotes++;
					} else {
						negativeVotes++;
					}
				}
				if (!p.getIsRewin()) {
					System.out.println("Voti: "
							+ positiveVotes + " positivi, " + negativeVotes + " negativi");
					System.out.println("Commenti:"
							+ (p.getComments() == null || p.getComments().size() == 0 ? "0" : ""));
					// I commenti sono nell'ordine in cui sono stati inseriti nella lista, 
					// quindi in ordine di timestamp crescente
					for (Comment c : p.getComments()) {
						System.out.println("\t"
								+ c.getAuthor() + ": \"" + c.getContent()
								+ "\" (" + (new Date(c.getTimestamp())) + ")");
					}
				}
			} catch (JsonProcessingException ex) {
				System.err.println("Impossibile deserializzare la risposta: " + ex.getMessage());
			}
		}
	}

	private static void comment_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		// Ottengo postID e commento
		long postID = Long.valueOf(cmd.getArg(0));
		String comment = cmd.getArg(1);
		req = new CommentRequest(postID, comment);
		ByteBuffer reply_bbuf = ByteBuffer.allocate(Integer.BYTES);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);
		int res = reply_bbuf.getInt();
		if (res == 0) {
			System.out.printf(COMMENT_OK_FMT, postID);
		} else if (res == 1) {
			System.err.printf(SELF_COMMMENT_FMT);
		} else if (res == -2) {
			System.err.printf(POST_NEXISTS_FMT, postID);
		} else if (res == -3) {
			System.err.printf(ORIGINAL_POST_NEXISTS_FMT, postID);
		} else {
			System.err.printf(UNAUTHORIZED_FMT);
			System.out.println("Controlla se stai seguendo l'autore del post");
		}
	}

	private static void rate_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		// Ottengo postID e effettuo voto
		long postID = Long.valueOf(cmd.getArg(0));
		int vote = Integer.valueOf(cmd.getArg(1));
		req = new RateRequest(postID, vote);
		ByteBuffer reply_bbuf = ByteBuffer.allocate(Integer.BYTES);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);
		int res = reply_bbuf.getInt();
		if (res == 0) {
			System.out.printf(VOTE_OK_FMT, vote, postID);
		} else if (res == 1) {
			System.err.printf(SELF_VOTE_FMT);
		} else if (res == 2) {
			System.err.printf(ALREADY_VOTED_FMT, postID);
		} else if (res == -2) {
			System.err.printf(POST_NEXISTS_FMT, postID);
		} else if (res == -3) {
			System.err.printf(ORIGINAL_POST_NEXISTS_FMT, postID);
		} else {
			System.err.printf(UNAUTHORIZED_FMT);
			System.out.println("Controlla se stai seguendo l'autore del post");
		}
	}

	private static void show_blog_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {

		req = new BlogRequest(state.getCurrentUser());
		ByteBuffer request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
		ByteBuffer reply_bbuf = ByteBuffer.allocate(BUFSZ);
		state.getSocket().write(request_bbuf);

		boolean finish_read = false;
		int bytes_read = 0;
		while (!finish_read) {
			bytes_read = state.getSocket().read(reply_bbuf);
			reply_bbuf.flip();

			// Controllo se la risposta ?? un messaggio di errore
			String reply = new String(reply_bbuf.array());
			if (reply.startsWith("Errore")) {
				System.err.printf(FMT_ERR, reply.substring(reply.indexOf(':') + 1));
				finish_read = true;
			} else {
				// Deserializzo la lista di post
				List<Post> blog = new ArrayList<>();
				TypeReference<List<Post>> typeRef = new TypeReference<List<Post>>() {
				};
				try {
					blog = mapper.readValue(reply, typeRef);
					System.out.printf(TABLE_HEADER_QUADRUPLE_FMT,
							TABLE_HEADERS[2], TABLE_HEADERS[3], TABLE_HEADERS[7], TABLE_HEADERS[4],
							TABLE_HEADERS[8], TABLE_HEADERS[8], TABLE_HEADERS[8], TABLE_HEADERS[8]);
					for (Post p : blog) {
						System.out.printf("|%-20d|%-20s|%-20s|%s\n",
								p.getPostID(), p.getAuthor(),
								(p.getIsRewin() ? p.getOriginalID() : ""), p.getTitle());
					}
					finish_read = true;
				} catch (JsonProcessingException ex) {
					// Deserializzazione fallita per buffer troppo corto
					// Lo raddoppio
					if (bytes_read > 0) {
						ByteBuffer newbb = ByteBuffer.allocate(reply_bbuf.capacity() * 2);
						newbb.put(reply_bbuf);
						reply_bbuf = newbb;
					} else {
						System.err.println("Impossibile deserializzare la risposta: " + ex.getMessage());
						finish_read = true;
					}
				}
			}
		}
	}

	private static void rewin_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		long res = -1L;
		long postID = Long.valueOf(cmd.getArg(0));
		req = new RewinRequest(postID);
		ByteBuffer reply_bbuf = ByteBuffer.allocate(Long.BYTES);
		reply_bbuf = send_and_receive(req, state, reply_bbuf, mapper);
		res = reply_bbuf.getLong();
		if (res == -1) {
			System.err.printf(UNAUTHORIZED_FMT, state.getCurrentUser());
		} else if (res == -2) {
			System.err.printf(POST_NEXISTS_FMT, postID);
		} else if (res == -3) {
			System.out.printf(NOT_IN_FEED_FMT, postID);
		} else {
			System.out.printf(REWINNED_POST_FMT, postID, res);
		}
	}

	private static void wallet_command(boolean inBTC, ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper) throws IOException {
		req = new WalletRequest(state.getCurrentUser(), inBTC);
		ByteBuffer request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
		ByteBuffer reply_bbuf = ByteBuffer.allocate(BUFSZ);
		state.getSocket().write(request_bbuf);

		boolean finish_read = false;
		int bytes_read = 0;
		while (!finish_read) {
			bytes_read = state.getSocket().read(reply_bbuf);
			reply_bbuf.flip();

			// Controllo se la risposta ?? un messaggio di errore
			String reply = new String(reply_bbuf.array());
			if (reply.startsWith("Errore")) {
				System.err.printf(FMT_ERR, reply.substring(reply.indexOf(':') + 1));
				finish_read = true;
			} else {
				if (inBTC) {
					if (reply.equals("-1.0")) {
						System.err.println("Errore durante la coversione. Riprova");
					} else {
						System.out.println(reply + " BTC");
					}
					finish_read = true;
				} else {
					// Deserializzo la lista di transazioni
					// Calcolo localmente bilancio corrente del wallet sommando le transazioni
					double currentBalance = 0.0;
					List<Transaction> all_transactions = new ArrayList<>();
					TypeReference<List<Transaction>> typeRef = new TypeReference<List<Transaction>>() {
					};
					try {
						all_transactions = mapper.readValue(reply, typeRef);
						System.out.printf(TABLE_HEADER_DOUBLE_FMT,
								TABLE_HEADERS[5], TABLE_HEADERS[6],
								TABLE_HEADERS[8], TABLE_HEADERS[8]);
						for (Transaction t : all_transactions) {
							System.out.printf("|%+20f|%-20s\n",
									t.getAmount(), (new Date(t.getTimestamp())).toString());
							currentBalance += t.getAmount();
						}
						// Stampo bilancio corrente
						System.out.printf(WALLET_FMT, state.getCurrentUser(), currentBalance, "WINCOIN");
						finish_read = true;
					} catch (JsonProcessingException ex) {
						// Deserializzazione fallita per buffer troppo corto
						// Lo raddoppio
						if (bytes_read > 0) {
							ByteBuffer newbb = ByteBuffer.allocate(reply_bbuf.capacity() * 2);
							newbb.put(reply_bbuf);
							reply_bbuf = newbb;
						} else {
							System.err.println("Impossibile deserializzare la risposta: " + ex.getMessage());
							finish_read = true;
						}
					}
				}
			}
		}
	}

	private static void quit_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ObjectMapper mapper, ClientConfig config) throws IOException {
		// Prima effettuo logout dell'utente corrente, se necessario
		if (!(state.getCurrentUser() == null || state.getCurrentUser().equals(""))) {
			logout_command(cmd, state, req, mapper, config);
		}
		// Poi mando richiesta di disconnessione (serve ad eliminare il SocketChannel lato server)
		SocketChannel sc = state.getSocket();
		if (sc != null) {
			req = new QuitRequest(state.getCurrentUser());
			ByteBuffer request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
			state.getSocket().write(request_bbuf);
			System.out.println("Richiesta di disconnessione inviata");
		}
		state.setUser("");
		state.setTermination(); // Alla prossima iterazione del loop principale il client termina
	}

	/**
	 * Il metodo setta i parametri della configurazione del client
	 * 
	 * @param in_config Configurazione (parziale) del server proveniente dal parsing
	 */
	private static ClientConfig getClientConfig(ClientConfig in_config) {
		// Se ?? stato specificato un file di configurazione da riga di comando
		// allora viene caricato, altrimenti vengono esaminati i path di default
		File confFile = null;
		try {
			if (in_config.getConfigFile() == null) {
				for (String path : ClientMain.CONF_DFLT_PATHS) {
					if (Files.exists(Paths.get(path), LinkOption.NOFOLLOW_LINKS)) {
						in_config.setConfigFile(path);
						confFile = new File(path);
						break;
					}
				}
			} else {
				confFile = new File(in_config.getConfigFile());
			}
			// Vari controlli prima di leggere il file
			if (confFile == null) {
				throw new WinsomeConfigException("Nessun file di configurazione trovato");
			}
			if (!(confFile.exists() && confFile.isFile() && confFile.canRead()
					&& confFile.getName().endsWith("json"))) {
				throw new WinsomeConfigException(
						"Il file di configurazione " + confFile.getName() + " non ?? valido");
			}
			// Utilizzando l'ObjectMapper di Jackson estraggo la configurazione dal file
			ObjectMapper mapper = new ObjectMapper();
			ClientConfig baseConf = mapper.readValue(confFile, ClientConfig.class);
			// Sovrascrivo i valori letti dal file di configurazione con i valori 
			// specificati da opzioni da riga di comando
			int regPort_cmd = in_config.getRegistryPort();
			baseConf.setRegistryPort(
					regPort_cmd == ClientConfig.DFL_REGPORT ? baseConf.getRegistryPort() : regPort_cmd);
			InetAddress host = in_config.getServerHostname();
			baseConf.setServerHostname(
					host == ClientConfig.DFL_SERVADDRESS ? baseConf.getServerHostname().toString()
							: host.toString());
			int port = in_config.getServerPort();
			baseConf.setPort(
					port == ClientConfig.DFL_SERVPORT ? baseConf.getServerPort() : port);
			return baseConf;
		} catch (WinsomeConfigException | IOException e) {
			e.printStackTrace();
			System.out.println(e);
			return null;
		}
	}

	/**
	 * Effettua il parsing dei parametri passati da riga di comando
	 * 
	 * @param args I parametri passati al programma
	 * @return La configurazione del client specificata dagli 
	 * argomenti da riga di comando
	 */
	public static ClientConfig parseArgs(String[] args) {
		Option configFile = Option.builder(CONFIG_OPT)
				.longOpt("config").hasArg().numberOfArgs(1).argName("FILE").required(false)
				.desc("Path del file di configurazione da usare").build();
		Option registryPort = Option.builder(REGISTRY_OPT).longOpt("registry").required(false)
				.hasArg().numberOfArgs(1).argName("PORT")
				.desc("Porta sulla quale cercare il registry per il signup").build();
		Option host = Option.builder(HOST_OPT)
				.longOpt("host").hasArg().numberOfArgs(1).argName("HOSTNAME").required(false)
				.desc("hostname del server o indirizzo IP").build();
		Option socketPort = Option.builder(SERVPORT_OPT)
				.longOpt("socket").hasArg().numberOfArgs(1).argName("PORT").required(false)
				.desc("Porta sulla quale connettersi al server").build();
		Option helpMsg = Option.builder(HELP_OPT).longOpt("help").required(false)
				.hasArg(false).desc("Messaggio di help").build();
		Option[] opts = { configFile, registryPort, host, socketPort, helpMsg };
		Options all_options = new Options();
		for (Option op : opts) {
			all_options.addOption(op);
		}

		ClientConfig sconf = new ClientConfig();

		HelpFormatter help = new HelpFormatter();
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine parsed_args = parser.parse(all_options, args);
			for (Option op : parsed_args.getOptions()) {
				Object optValue = parsed_args.getParsedOptionValue(op);
				switch (op.getOpt()) {
					case CONFIG_OPT:
						sconf.setConfigFile((String) optValue);
						break;
					case REGISTRY_OPT:
						sconf.setRegistryPort(Integer.valueOf((String) optValue));
						break;
					case HOST_OPT:
						sconf.setServerHostname((String) optValue);
						break;
					case SERVPORT_OPT:
						sconf.setPort(Integer.valueOf((String) optValue));
						break;
					case HELP_OPT:
					default:
						help.printHelp("WinsomeClient", all_options, true);
						Runtime.getRuntime().exit(0);
				}
			}
		} catch (Exception parseEx) {
			help.printHelp("WinsomeClient", all_options);
			return null;
		}
		return sconf;
	}
}
