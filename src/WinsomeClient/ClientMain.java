package WinsomeClient;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
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
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import WinsomeExceptions.WinsomeConfigException;
import WinsomeServer.Signup;

/**
 * Classe main del client Winsome
 */
public class ClientMain {
	/** Path di default per il file di configurazione */
	public static final String[] CONF_DFLT_PATHS = { "data/WinsomeClient/config.json", "config.json" };

	/** comando utilizzato per uscire dal client */
	public static final String QUIT_COMMAND = "quit";
	/** prompt interattivo del client */
	public static final String USER_PROMPT = "> ";
	/** Messaggio ok */
	public static final String OK_MSG = "ok";
	/** Stringa di formato errore */
	public static final String FMT_ERR = "errore, %s\n";

	private static SocketChannel tcpConnection;

	public static void main(String[] args) {
		// Effettua il parsing degli argomenti CLI
		ClientConfig in_config = parseArgs(args);
		if (in_config == null) {
			in_config = new ClientConfig();
		}
		// Carica il file di configurazione
		ClientConfig config = getClientConfig(in_config);
		if (config == null) {
			return;
		}
		System.out.println(config);

		try {
			Registry reg = LocateRegistry.getRegistry(config.getRegistryPort());
			System.out.println("Hello, server");
			Signup stub = (Signup) reg.lookup("register");

			//TODO: connect();
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

	private static void mainLoop(Signup stub) {
		try (Scanner scan = new Scanner(System.in);) {
			while (true) {
				System.out.print(ClientMain.USER_PROMPT);
				if (!scan.hasNextLine()) {
					break;
				}

				String command = scan.nextLine();
				// TODO: support quoting to distinguish args
				String[] tokens = command.split(" ");

				if (tokens[0].equals(ClientMain.QUIT_COMMAND)) {
					break;
				}

				ClientCommand cmd = CommandParser.parseCommand(tokens);
				if (cmd == null) {
					System.err.println(ClientMain.USER_PROMPT + "comando non riconosciuto");
					continue;
				}
				// Esegue il comando ottenuto
				execCommand(cmd, stub);

			}
		} catch (NoSuchElementException end) {
			System.out.println("Quitting client...");
		}
	}

	private static void execCommand(ClientCommand cmd, Signup stub) {
		switch (cmd.getCommand()) {
			case REGISTER:
				List<String> tags = new ArrayList<>();
				for (int i = 2; i < cmd.getArgs().length; i++) {
					tags.add(cmd.getArg(i));
				}
				int res = -1;
				try {
					res = stub.register(cmd.getArg(0), cmd.getArg(1), tags);
					System.out.print(ClientMain.USER_PROMPT);
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
					return;
				}
				break;
			case LOGIN:
				break;
			default:
				System.err.println("TODO");
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
