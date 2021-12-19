package WinsomeClient;

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

import WinsomeExceptions.WinsomeConfigException;
import WinsomeRequests.ListRequest;
import WinsomeRequests.LoginRequest;
import WinsomeRequests.LogoutRequest;
import WinsomeRequests.Request;
import WinsomeServer.Signup;

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

	private static SocketChannel tcpConnection;

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

		try {
			System.out.println("Caricamento stub registrazione...");
			Registry reg = LocateRegistry.getRegistry(config.getRegistryPort());
			Signup stub = (Signup) reg.lookup("register");

			tcpConnection = SocketChannel.open();
			tcpConnection.connect(new InetSocketAddress(config.getServerHostname(), config.getServerPort()));

			// Loop di lettura ed esecuzione comandi
			mainLoop(stub);

		} catch (RemoteException | NotBoundException e) {
			System.out.println(e);
		} catch (IOException io) {
			System.err.printf(ClientMain.FMT_ERR, "impossibile connettersi al server all'indrizzo "
					+ config.getServerHostname() + ":" + config.getServerPort());
			return;
		}
	}

	/**
	 * Loop di lettura, parsing ed esecuzione dei comandi: termina al comando "quit"
	 * 
	 * @param stub Stub per la registrazione con RMI
	 */
	private static void mainLoop(Signup stub) {
		String currentUser = "";

		try (BufferedReader read_stdin = new BufferedReader(
				new InputStreamReader(System.in));) {
			// Setup per lo streamTokenizer
			StreamTokenizer strmtok = new StreamTokenizer(read_stdin);
			strmtok.eolIsSignificant(true); // ritorna \n come token
			strmtok.lowerCaseMode(false);
			strmtok.quoteChar('"'); // setta il carattere da riconoscere come delimitatore di stringa
			strmtok.ordinaryChar(95); // tratta '_' come carattere
			strmtok.wordChars(35, 126); // codici ascii caratteri considerati parte di una stringa

			while (true) {
				System.out.print(currentUser + ClientMain.USER_PROMPT);

				ArrayList<String> tokens = new ArrayList<>();
				if (strmtok.nextToken() == StreamTokenizer.TT_EOF) {
					break;
				} else {
					tokens.add(strmtok.sval);
				}
				while (strmtok.nextToken() != StreamTokenizer.TT_EOL) {
					tokens.add(strmtok.sval);
				}

				if (tokens.get(0).equals(ClientMain.QUIT_COMMAND)) {
					break;
				}

				String[] dummy = new String[1];
				ClientCommand cmd = CommandParser.parseCommand(tokens.toArray(dummy));
				if (cmd == null) {
					System.err.println(currentUser + ClientMain.USER_PROMPT + "comando non riconosciuto");
					continue;
				}
				// Esegue il comando ottenuto (eventualmente ottiene prompt dinamico mutato)
				currentUser = execCommand(currentUser, cmd, stub);

			}
		} catch (NoSuchElementException end) {
			System.out.println("Errore lettura: Terminazione");
		} catch (IOException e) {
			System.err.println("Errore I/O: Terminazione");
		}
	}

	/**
	 * Esegue il comando cmd, scegliendo a seconda del tipo l'operazione da chiamare
	 * 
	 * @param currentUser prompt dinamico della sesssione
	 * @param cmd il comando da eseguire
	 * @param stub lo stub per la registrazione (usato se cmd.getCommmand() == REGISTER)
	 */
	private static String execCommand(String currentUser, ClientCommand cmd, Signup stub) {
		Request req = null;
		ObjectMapper mapper = new ObjectMapper();
		ByteBuffer request_bbuf = null;
		ByteBuffer reply_bbuf = ByteBuffer.allocateDirect(ClientMain.BUFSZ);
		int res;

		try {
			switch (cmd.getCommand()) {
				case REGISTER:
					List<String> tags = new ArrayList<>();
					for (int i = 2; i < cmd.getArgs().length; i++) {
						tags.add(cmd.getArg(i));
					}
					res = -1;
					try {
						res = stub.register(cmd.getArg(0), cmd.getArg(1), tags);
						System.out.print(currentUser + ClientMain.USER_PROMPT);
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
						return currentUser;
					}
					break;
				case LOGIN:
					// Prima controllo se un utente è già loggato
					if (!(currentUser.equals("") || currentUser.equals(cmd.getArg(0)))) {
						System.err.printf(ClientMain.FMT_ERR, "altro utente loggato: effettuare il logout");
						break;
					}
					// Crea una nuova richiesta di login e la scrive sul channel TCP
					req = new LoginRequest(cmd.getArg(0), cmd.getArg(1));
					request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
					ClientMain.tcpConnection.write(request_bbuf);
					// Legge la risposta (un intero)
					// FIXME: blocking channel might not be always fit, in this case it's probably fine
					int nread = ClientMain.tcpConnection.read(reply_bbuf);
					if (nread == -1) {
						return currentUser;
					}
					reply_bbuf.flip();
					int result = reply_bbuf.getInt();
					// Se il login ha avuto successo cambia il prompt
					// altrimenti messaggio di errore
					switch (result) {
						// Login autorizzato
						case 0:
							System.out.println(cmd.getArg(0) + " autenticato");
							// Setto proompt dinamico con username utente
							currentUser = cmd.getArg(0);
							break;
						case 1:
							System.err.printf(ClientMain.FMT_ERR, "utente inesistente");
							break;
						case 2:
							System.err.printf(ClientMain.FMT_ERR, "password errata");
							break;
						case 3:
							System.err.printf(ClientMain.FMT_ERR, "login già effettuato");
							break;
						default:
							System.err.printf(ClientMain.FMT_ERR, "impossibile effettuare il login");
					}

					// clear per riutilizzo del buffer
					reply_bbuf.clear();
					break;
				case LOGOUT:
					req = new LogoutRequest(currentUser);
					request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
					ClientMain.tcpConnection.write(request_bbuf);
					ClientMain.tcpConnection.read(reply_bbuf);
					reply_bbuf.flip();
					res = reply_bbuf.getInt();
					if (res == 0) {
						System.out.println("utente " + currentUser + " scollegato");
						currentUser = "";
					} else {
						System.err.printf(ClientMain.FMT_ERR, "utente \"" + currentUser + "\" non collegato");
						// currentUser inalterato
					}
					break;
				case LIST:
					req = new ListRequest(currentUser, cmd.getArg(0));
					request_bbuf = ByteBuffer.wrap(mapper.writeValueAsBytes(req));
					ClientMain.tcpConnection.write(request_bbuf);
					int bytes_read = 0;
					ArrayList<ByteBuffer> buffers = new ArrayList<>();
					do {
						ByteBuffer newBB = ByteBuffer.allocate(ClientMain.BUFSZ);
						buffers.add(newBB);
						bytes_read = ClientMain.tcpConnection.read(newBB);
					} while (bytes_read == ClientMain.BUFSZ);
					StringBuffer sbuf = new StringBuffer();
					for (ByteBuffer bb : buffers) {
						sbuf.append(bb.asCharBuffer().array());
					}
					// tokenize the output and format in a table
					System.out.printf("|%20s|%20s\n%|20s|%20s\n",
							"=======Utente=======", "========Tags========",
							"====================", "====================");
					StringTokenizer tokenizer = new StringTokenizer(sbuf.toString(), ",");
					while (tokenizer.hasMoreTokens()) {
						String userTok = tokenizer.nextToken();
						if (userTok.equals("")) {
							continue;
						}
						String[] username_tags = userTok.split(":");
						System.out.printf("|%20s|", username_tags[0]);
						System.out.println("|" + username_tags[1]);
					}
					break;
				default:
					System.err.println("TODO");
			}
		} catch (IOException jpe) {
			System.err.printf(ClientMain.FMT_ERR, "errore esecuzione richiesta");
			return currentUser;
		}
		return currentUser;
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
			return mapper.readValue(confFile, ClientConfig.class);
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
		Option configFile = new Option("c", "config", true, "Path del file di configurazione da usare");
		Option registryPort = new Option("r", "registry", true, "Porta del registry");
		Option host = new Option("h", "host", true, "Indirizzo del server (nome o IP)");
		Option socketPort = new Option("p", "port", true, "Porta sulla quale connettersi al server");
		configFile.setOptionalArg(true);
		registryPort.setOptionalArg(true);
		host.setOptionalArg(true);
		socketPort.setOptionalArg(true);
		Option[] opts = { configFile, registryPort, host, socketPort };
		Options all_options = new Options();
		for (Option op : opts) {
			all_options.addOption(op);
		}

		ClientConfig sconf = new ClientConfig();

		HelpFormatter help = new HelpFormatter();
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine parsed_args = parser.parse(all_options, args);
			for (Option op : opts) {
				Object value = parsed_args.getParsedOptionValue(op);
				if (value != null) {
					if (op.getOpt().equals("c")) {
						sconf.setConfigFile((String) value);
					} else if (op.getOpt().equals("r")) {
						sconf.setRegistryPort(Integer.valueOf((String) value));
					} else if (op.getOpt().equals("h")) {
						sconf.setServerHostname((String) value);
					} else if (op.getOpt().equals("p")) {
						sconf.setPort(Integer.valueOf((String) value));
					} else {
						throw new IllegalArgumentException();
					}
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
