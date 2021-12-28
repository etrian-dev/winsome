package Winsome.WinsomeClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

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
import Winsome.WinsomeServer.FollowerUpdaterService;
import Winsome.WinsomeServer.ServerMain;
import Winsome.WinsomeServer.Signup;

/**
 * Classe main del client Winsome
 */
public class ClientMain {
	/** Path di default per il file di configurazione */
	public static final String[] CONF_DFLT_PATHS = { "config.json", "data/WinsomeClient/config.json" };
	/** prompt interattivo del client */
	public static final String USER_PROMPT = "> ";
	/** comando utilizzato per uscire dal client */
	public static final String QUIT_COMMAND = "quit";
	/** Messaggio ok */
	public static final String OK_MSG = "ok";
	/** Stringa di formato errore */
	public static final String FMT_ERR = "errore, %s\n";
	/** Dimensione di default di un buffer (ad esempio ByteBuffer di lettura) */
	public static final int BUFSZ = 8192;

	// Costanti utili per la formattazione degli esiti delle operazioni
	private static final String UNAUTHORIZED_FMT = "Operazione non autorizzata: controlla di aver effettuato il login\n";
	private static final String LOGIN_OK_FMT = "Utente \"%s\" autenticato\n";
	private static final String LOGOUT_OK_FMT = "Utente \"%s\" scollegato\n";
	private static final String NOT_LOGGED_FMT = "Utente \"%s\" non autenticato: controlla di aver effettuato il login\n";
	private static final String FOLLOW_OK_FMT = "L\'utente \"%s\" ha iniziato a seguire l\'utente \"%s\"\n";
	private static final String UNFOLLOW_OK_FMT = "L\'utente \"%s\" ha smesso di seguire l\'utente \"%s\"\n";
	private static final String USER_NEXISTS_FMT = "L\'utente \"%s\" (richiedente dell'operazione) non esiste\n";
	private static final String TARGET_NEXISTS_FMT = "L\'utente \"%s\" (oggetto dell'operazione) non esiste\n";
	private static final String ALREADY_FOLLOW_FMT = "L\'utente \"%s\" era già tra i tuoi following\n";
	private static final String NOT_FOLLOWING_FMT = "L\'utente \"%s\" non era tra i tuoi following\n";
	private static final String POST_CREATED_FMT = "Nuovo post creato: Id = %d\n";
	private static final String POST_DELETED_FMT = "Post con Id = %d eliminato\n";
	private static final String POST_NEXISTS_FMT = "Il post con Id = %d non esiste\n";
	private static final String TITLE_OVERFLOW_FMT = "Titolo del post non valido (nullo o di lunghezza > 20 caratteri)\n";
	private static final String CONTENT_OVERFLOW_FMT = "Contenuto del post non valido (nullo o di lunghezza > 500 caratteri)\n";
	private static final String COMMENT_OK_FMT = "Commento al post con Id = %s registrato\n";
	private static final String SELF_COMMMENT_FMT = "Non è possibile commentare un proprio post\n";
	private static final String VOTE_OK_FMT = "Voto %+d al post con Id = %d registrato\n";
	private static final String ALREADY_VOTED_FMT = "Hai già votato il post con Id = %d\n";
	private static final String SELF_VOTE_FMT = "Non è possibile votare un proprio post\n";
	private static final String REWINNED_POST_FMT = "Effettuato il rewin del post con Id = %d\nRewin: Id = %d\n";
	private static final String ALREADY_SUBSCRIBED = "Già iscritto al servizio di callback per followers\n";
	private static final String NOT_SUBSCRIBED = "Non eri iscritto al servizio di callback per followers\n";

	public static void main(String[] args) {
		// Effettua il parsing degli argomenti CLI
		ClientConfig in_config = parseArgs(args);
		if (in_config == null) {
			in_config = new ClientConfig();
		}
		System.out.println("Caricamento file di configurazione...");
		// Carica il file di configurazione
		ClientConfig config = getClientConfig(in_config);
		if (config == null) {
			return;
		}
		System.out.println(config);

		// Loop principale
		mainLoop(config);
	}

	/**
	 * Loop di lettura, parsing ed esecuzione dei comandi: termina al comando "quit"
	 * 
	 * @param config configurazione del client
	 */
	private static void mainLoop(ClientConfig config) {
		// Inizializzo lo stato del programma
		WinsomeClientState state = new WinsomeClientState();

		try (BufferedReader read_stdin = new BufferedReader(
				new InputStreamReader(System.in));) {
			// Setup per lo streamTokenizer
			StreamTokenizer strmtok = new StreamTokenizer(read_stdin);
			strmtok.resetSyntax();
			strmtok.eolIsSignificant(true); // ritorna \n come token
			strmtok.lowerCaseMode(false);
			strmtok.quoteChar('"'); // setta il carattere da riconoscere come delimitatore di stringa
			strmtok.wordChars('#', '~'); // codici ascii caratteri considerati parte di una stringa

			while (!state.isTerminating()) {
				System.out.print(state.getCurrentUser() + ClientMain.USER_PROMPT);

				ArrayList<String> tokens = new ArrayList<>();
				// TODO: improve numbers parsing
				while (strmtok.nextToken() != StreamTokenizer.TT_EOL) {
					if (strmtok.ttype == StreamTokenizer.TT_EOF) {
						state.setTermination();
					}
					if (strmtok.ttype == StreamTokenizer.TT_WORD) {
						tokens.add(strmtok.sval);
					}
				}

				String[] dummyArr = new String[tokens.size()];
				ClientCommand cmd = ClientCommand.parseCommand(tokens.toArray(dummyArr));
				if (cmd == null) {
					System.err.println(state.getCurrentUser() + ClientMain.USER_PROMPT + "comando non riconosciuto");
					continue;
				}
				// Esegue il comando ottenuto (eventualmente ottiene prompt dinamico mutato)
				execCommand(state, cmd, config);
			}
		} catch (NoSuchElementException end) {
			System.out.println("Errore lettura: Terminazione");
		} catch (IOException e) {
			System.err.println("Errore I/O: Terminazione");
		}

		System.out.println("Main loop exited");
	}

	private static void execCommand(WinsomeClientState state, ClientCommand cmd, ClientConfig config) {
		Request req = null;
		ObjectMapper mapper = new ObjectMapper();
		ByteBuffer request_bbuf = null;
		ByteBuffer reply_bbuf = ByteBuffer.allocate(ClientMain.BUFSZ);
		try {
			switch (cmd.getCommand()) {
				case REGISTER:
					register_command(cmd, state, config);
					break;
				case LOGIN:
					login_command(cmd, state, req, request_bbuf, mapper, reply_bbuf, config);
					break;
				case LOGOUT:
					logout_command(cmd, state, req, request_bbuf, mapper, reply_bbuf, config);
					break;
				case LIST:
					if (cmd.getArg(0).equals("followers")) {
						System.out.println("Followers: " + state.getFollowers()
								+ "(last updated: " + (new Date(state.getLastFollowerUpdate())) + ")");
					} else {
						list_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					}
					break;
				case FOLLOW:
					follow_unfollow_command(true, cmd, state, req, request_bbuf, mapper, reply_bbuf);
					break;
				case UNFOLLOW:
					follow_unfollow_command(false, cmd, state, req, request_bbuf, mapper, reply_bbuf);
					break;
				case POST:
					create_post_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					break;
				case DELETE:
					delete_post_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					break;
				case SHOW:
					if (cmd.getArg(0).equals("feed")) {
						show_feed_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					} else {
						show_post_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					}
					break;
				case COMMENT:
					comment_post_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					break;
				case RATE:
					rate_post_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					break;
				case REWIN:
					rewin_post_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					break;
				case BLOG:
					show_blog_command(cmd, state, req, request_bbuf, mapper, reply_bbuf);
					break;
				case QUIT:
					// Prima effettuo logout del client corrente, se necessario
					if (!(state.getCurrentUser() == null || state.getCurrentUser().equals(""))) {
						logout_command(cmd, state, req, request_bbuf, mapper, reply_bbuf, config);
					}
					// Poi mando richiesta di disconnessione
					SocketChannel sc = state.getSocket();
					if (sc != null) {
						req = new QuitRequest(state.getCurrentUser());
						request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
						state.getSocket().write(request_bbuf);
						state.getSocket().close();
						System.out.println("Richiesta di disconnessione inviata");
					}
					state.setUser("");
					state.setTermination();
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

	private static ByteBuffer send_and_receive(Request req, WinsomeClientState state, ByteBuffer request_bbuf,
			ByteBuffer reply_bbuf,
			ObjectMapper mapper) throws IOException {
		request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
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

	private static boolean unset_followers_callback(String user, ClientConfig config) {
		// Inizializzo l'oggetto per la callback
		System.out.println("Unsubscribe from RMI callback...");
		boolean initialized = false;
		try {
			// Ottengo dal registry lo stub per la registrazione al servizio
			Registry reg = LocateRegistry.getRegistry(config.getRegistryPort());
			FollowerUpdaterService fwup = (FollowerUpdaterService) reg.lookup(ServerMain.FOLLOWER_SERVICE_STUB);
			// Registro il client al servizio per l'utente (loggato) corrente
			int res = fwup.unsubscribe(user);
			if (res == 0) {
				System.out.println("Deregistrazione dal servizio callback RMI effettuata");
				initialized = true;
			} else if (res == 1) {
				System.err.printf(USER_NEXISTS_FMT, user);
			} else if (res == 2) {
				System.err.printf(NOT_LOGGED_FMT, user);
			} else if (res == 3) {
				System.err.printf(NOT_SUBSCRIBED);
			} else {
				// non dovrebbe mai essere eseguito, ma permette futura 
				System.err.printf(UNAUTHORIZED_FMT, user);
			}
		} catch (IOException | NotBoundException | IllegalArgumentException e) {
			e.printStackTrace();
			System.err.println("Fallita cancellazione iscrizione a RMI callback");
			System.out.printf(ClientMain.FMT_ERR, e.getMessage());
			return false;
		}
		return initialized;
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
					System.out.println(ClientMain.OK_MSG);
					break;
				case 1:
					System.err.printf(ClientMain.FMT_ERR,
							"utente " + cmd.getArg(0) + " già esistente");
					break;
				case 2:
					System.err.printf(ClientMain.FMT_ERR,
							"password vuota o non specificata");
					break;
				case 3:
					System.err.printf(ClientMain.FMT_ERR,
							"troppi tag specificati (massimo cinque)");
					break;
				default:
					System.err.printf(ClientMain.FMT_ERR,
							"impossibile completare la registrazione, ci scusiamo per il disagio");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void login_command(ClientCommand cmd, WinsomeClientState state, Request req, ByteBuffer request_bbuf,
			ObjectMapper mapper, ByteBuffer reply_bbuf, ClientConfig config) throws IOException {

		// Prima controllo se un utente è già loggato
		if (!(state.getCurrentUser().equals("") || state.getCurrentUser().equals(cmd.getArg(0)))) {
			System.err.printf(ClientMain.FMT_ERR, "altro utente loggato: effettuare il logout");
			return;
		}
		// Creo e setto il SocketChannel per la comunicazione con il server
		try {
			SocketChannel sc = SocketChannel.open();
			sc.connect(new InetSocketAddress(config.getServerHostname(), config.getServerPort()));
			state.setSocket(sc);
		} catch (IOException e) {
			System.err.printf(ClientMain.FMT_ERR, "impossibile connettersi al server all'indrizzo "
					+ config.getServerHostname() + ":" + config.getServerPort());
		}
		// Crea una nuova richiesta di login e la scrive sul channel TCP
		req = new LoginRequest(cmd.getArg(0), cmd.getArg(1));
		request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
		state.getSocket().write(request_bbuf);
		// Legge la risposta (un intero)
		// FIXME: blocking channel might not be always fit, in this case it's probably fine
		int nread = state.getSocket().read(reply_bbuf);
		if (nread == -1) {
			state.setTermination();
		}
		reply_bbuf.flip();
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
				System.err.printf(ClientMain.FMT_ERR, "login già effettuato");
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
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf, ClientConfig config)
			throws IOException {
		// Deregistrazione dal callback (prima del logout per permettere controlli)
		unset_followers_callback(state.getCurrentUser(), config);
		// Invio della richiesta di logout al server sul socket
		int res = -1;
		req = new LogoutRequest(state.getCurrentUser());
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
		res = reply_bbuf.getInt();
		if (res == 0) {
			System.out.printf(LOGOUT_OK_FMT, state.getCurrentUser());
			state.setUser("");
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
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		req = new ListRequest(state.getCurrentUser(), cmd.getArg(0));
		// TODO: incorporare in send & receive?
		request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
		state.getSocket().write(request_bbuf);
		int bytes_read = 0;
		ArrayList<ByteBuffer> buffers = new ArrayList<>();
		do {
			ByteBuffer newBB = ByteBuffer.allocate(ClientMain.BUFSZ);
			buffers.add(newBB);
			bytes_read = state.getSocket().read(newBB);
			newBB.flip();
		} while (bytes_read == ClientMain.BUFSZ);
		StringBuffer sbuf = new StringBuffer();
		for (ByteBuffer bb : buffers) {
			sbuf.append(new String(bb.array()));
		}
		// Controllo se messaggio di errore
		if (sbuf.toString().startsWith("Errore")) {
			// TODO: error display
		}
		// tokenize the output and format in a table
		// TODO: handle errors formatting
		System.out.printf("|%20s|%20s\n|%20s|%20s\n",
				"       Utente       ", "        Tags        ",
				"====================", "====================");
		StringTokenizer tokenizer = new StringTokenizer(sbuf.toString(), "\n");
		while (tokenizer.hasMoreTokens()) {
			String userTok = tokenizer.nextToken();
			String[] username_tags = userTok.split(":");
			if (username_tags.length != 2) {
				continue;
			}
			System.out.printf("|%-20s|", username_tags[0]);
			username_tags[1] = username_tags[1].substring(1, username_tags[1].length() - 1);
			System.out.println(username_tags[1]);
		}
	}

	private static void follow_unfollow_command(boolean follow_unfollow, ClientCommand cmd, WinsomeClientState state,
			Request req,
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		int res = -1;
		req = new FollowRequest(state.getCurrentUser(), cmd.getArg(0), follow_unfollow);
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
		res = reply_bbuf.getInt();
		if (res == 0) {
			if (follow_unfollow) {
				System.out.printf(FOLLOW_OK_FMT, state.getCurrentUser(), cmd.getArg(0));
			} else {
				System.out.printf(UNFOLLOW_OK_FMT, state.getCurrentUser(), cmd.getArg(0));
			}
		} else if (res == 1) {
			// Teoricamente questo ramo è eseguito solo se il comando è lanciato 
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
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		// comando per la creazione di un nuovo post con argomenti titolo e contenuto
		req = new CreatePostRequest(state.getCurrentUser(), cmd.getArg(0), cmd.getArg(1));
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
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
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		int res = -1;
		long postID = Long.valueOf(cmd.getArg(0));
		req = new DeletePostRequest(postID);
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
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
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		req = new ShowFeedRequest();
		// Sta sicuramente in un ByteBuffer singolo perché ClientMain.BUFSZ >> 500 + 20 + costante
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
		// Estraggo token messaggio dal reply_bbuf
		byte[] replyBytes = new byte[reply_bbuf.remaining()];
		reply_bbuf.get(replyBytes, 0, reply_bbuf.remaining());
		String reply = new String(replyBytes);
		if (reply.startsWith("NessunPost")) {
			System.out.println("Nessun post nel feed");
		} else {
			String[] lines = reply.split("\n");
			System.out.printf("%20s|%20s|%20s\n", "Id", "Autore", "Titolo");
			for (int i = 0; i < lines.length; i += 3) {
				String[] idLine = lines[i].split(":");
				String[] authorLine = lines[i + 1].split(":");
				String[] titleLine = lines[i + 2].split(":");
				System.out.printf("%20s|%20s|%20s\n",
						idLine[1].trim(), authorLine[1].trim(), titleLine[1].trim());
			}
		}
	}

	private static void show_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		long postID = Long.valueOf(cmd.getArg(1));
		req = new ShowPostRequest(postID);
		// Sta sicuramente in un ByteBuffer singolo perché ClientMain.BUFSZ >> 500 + 20 + costante
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
		// Estraggo token messaggio dal reply_bbuf
		byte[] replyBytes = new byte[reply_bbuf.remaining()];
		reply_bbuf.get(replyBytes, 0, reply_bbuf.remaining());
		String reply = new String(replyBytes);
		if (reply.startsWith("Errore")) {
			System.err.printf(POST_NEXISTS_FMT, postID);
		} else if (reply.startsWith("NonAutorizzato")) {
			System.err.printf(UNAUTHORIZED_FMT);
		} else {
			// La risposta ha la forma
			// header:val\n
			// header:val\n
			// ...
			String[] all_rows = reply.split("\n");
			for (int r = 0; r < all_rows.length; r++) {
				// Spezzo al più in due segmenti: header + valore
				String[] header_val = all_rows[r].split(":", 2);
				// Se un campo non ha valore associato lo scarto (anche se non dovrebbe accadere)
				if (header_val.length == 2) {
					// Prima stampo la stringa header
					System.out.print(header_val[0] + ": ");
					// Tratto il campo commenti in modo diverso
					if (header_val[0].equals("Commenti")) {
						// Leggo numero di commenti
						int nComments = Integer.valueOf(header_val[1]);
						if (nComments == 0) {
							// Zero commenti
							System.out.println("0");
						} else {
							// Tokenizzo le nComments righe successive, del formato
							// commentatore|contenuto\n
							System.out.print('\n');
							for (int i = 1; i <= nComments; i++) {
								String[] user_comment = all_rows[r + i].split("[|]");
								System.out.println("\t" + user_comment[0] + ": " + user_comment[1]);
							}
							r += nComments;
						}
					} else {
						// Altrimenti stampo semplicemente il valore come stringa
						System.out.println(header_val[1]);
					}
				}
			}
		}
	}

	private static void comment_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		// Ottengo postID e commento
		long postID = Long.valueOf(cmd.getArg(0));
		String comment = cmd.getArg(1);
		req = new CommentRequest(postID, comment);
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
		int res = reply_bbuf.getInt();
		if (res == 0) {
			System.out.printf(COMMENT_OK_FMT, postID);
		} else if (res == 1) {
			System.err.printf(SELF_COMMMENT_FMT);
		} else if (res == -2) {
			System.err.printf(POST_NEXISTS_FMT, postID);
		} else {
			System.err.printf(UNAUTHORIZED_FMT);
		}
	}

	private static void rate_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		// Ottengo postID e effettuo voto
		long postID = Long.valueOf(cmd.getArg(0));
		int vote = Integer.valueOf(cmd.getArg(1));
		req = new RateRequest(postID, vote);
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
		int res = reply_bbuf.getInt();
		if (res == 0) {
			System.out.printf(VOTE_OK_FMT, vote, postID);
		} else if (res == 1) {
			System.err.printf(SELF_VOTE_FMT);
		} else if (res == 2) {
			System.err.printf(ALREADY_VOTED_FMT, postID);
		} else if (res == -2) {
			System.err.printf(POST_NEXISTS_FMT, postID);
		} else {
			System.err.printf(UNAUTHORIZED_FMT);
		}
	}

	private static void show_blog_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		req = new BlogRequest(state.getCurrentUser());
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
		// Estraggo token messaggio dal reply_bbuf
		byte[] replyBytes = new byte[reply_bbuf.remaining()];
		reply_bbuf.get(replyBytes, 0, reply_bbuf.remaining());
		String reply = new String(replyBytes);
		if (reply.startsWith("NonAutorizzato")) {
			System.err.println(UNAUTHORIZED_FMT);
		} else if (reply.startsWith("NessunPost")) {
			System.err.println("Nessun post");
		} else {
			String[] lines = reply.split("\n");
			System.out.printf("%20s|%20s|%20s\n", "Id", "Autore", "Titolo");
			for (int i = 0; i < lines.length; i += 3) {
				String[] idLine = lines[i].split(":");
				String[] authorLine = lines[i + 1].split(":");
				String[] titleLine = lines[i + 2].split(":");
				System.out.printf("%20s|%20s|%20s\n",
						idLine[1].trim(), authorLine[1].trim(), titleLine[1].trim());
			}
		}
	}

	private static void rewin_post_command(ClientCommand cmd, WinsomeClientState state, Request req,
			ByteBuffer request_bbuf, ObjectMapper mapper, ByteBuffer reply_bbuf) throws IOException {
		long res = -1L;
		long postID = Long.valueOf(cmd.getArg(0));
		req = new RewinRequest(postID);
		reply_bbuf = send_and_receive(req, state, request_bbuf, reply_bbuf, mapper);
		res = reply_bbuf.getLong();
		if (res == -1) {
			System.err.printf(UNAUTHORIZED_FMT, state.getCurrentUser());
		} else if (res == -2) {
			System.err.printf(POST_NEXISTS_FMT, postID);
		} else {
			System.out.printf(REWINNED_POST_FMT, postID, res);
		}
	}

	/**
	 * Il metodo setta i parametri della configurazione del client
	 * 
	 * @param in_config Configurazione (parziale) del server proveniente dal parsing
	 */
	private static ClientConfig getClientConfig(ClientConfig in_config) {
		// Se è stato specificato un file di configurazione da riga di comando
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
			if (!(confFile.exists() && confFile.isFile() && confFile.canRead()
					&& confFile.getName().endsWith("json"))) {
				throw new WinsomeConfigException(
						"Il file di configurazione " + confFile.getName() + " non è valido");
			}
			// Utilizzando l'ObjectMapper di Jackson estraggo la configurazione dal file
			ObjectMapper mapper = new ObjectMapper();
			// TODO: override base config with command line params, as done in WinsomeServer
			return mapper.readValue(confFile, ClientConfig.class);
		} catch (WinsomeConfigException | IOException e) {
			e.printStackTrace();
			System.out.println(e);
			return null;
		}
	}

	public static final String CONFIG_OPT = "c";
	public static final String REGISTRY_OPT = "r";
	public static final String HOST_OPT = "s";
	public static final String SERVPORT_OPT = "p";
	public static final String HELP_OPT = "h";

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
						sconf.setRegistryPort((Integer) optValue);
						break;
					case HOST_OPT:
						sconf.setServerHostname((String) optValue);
						break;
					case SERVPORT_OPT:
						sconf.setPort((Integer) optValue);
						break;
					case HELP_OPT:
					default:
						help.printHelp("WinsomeServer", all_options, true);
						Runtime.getRuntime().exit(0);
				}
			}
		} catch (Exception parseEx) {
			parseEx.printStackTrace();
			help.printHelp("WinsomeClient", all_options);
			return null;
		}
		return sconf;
	}
}
