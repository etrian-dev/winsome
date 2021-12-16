package WinsomeServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import WinsomeExceptions.WinsomeConfigException;
import WinsomeExceptions.WinsomeServerException;

/**
 * Classe main del server Winsome
 */
public class ServerMain {
	/** Path di default per il file di configurazione */
	public static final String[] CONF_DFLT_PATHS = { "data/WinsomeServer/config.json", "config.json" };
	public static final String SIGNUP_STUB = "register";

	/**
	 * Winsome server main class: parses the args array, loads the config file
	 * and initializes the RMI registry for signup operations
	 * @param args Argomenti da riga di comando passati al programma
	 */
	public static void main(String[] args) {
		// Effettua il parsing degli argomenti CLI
		ServerConfig in_config = parseArgs(args);
		if (in_config == null) {
			in_config = new ServerConfig();
		}
		// Carica il file di configurazione
		ServerConfig config = getServerConfiguration(in_config);
		if (config == null) {
			return;
		}
		System.out.println(config);

		try {
			// crea ed avvia il server
			WinsomeServer server = new WinsomeServer(config);
			server.start();

			// Crea il registry per la procedura di signup
			Registry signupRegistry = LocateRegistry.createRegistry(config.getRegistryPort());
			// Crea lo stub per la registrazione (già esportato)
			Signup signupObj = new SignupImpl(server);
			// Aggiunge lo stub per la registrazione
			signupRegistry.rebind(ServerMain.SIGNUP_STUB, signupObj);
		} catch (WinsomeServerException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (WinsomeConfigException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (RemoteException rmt) {
			rmt.printStackTrace();
			System.out.println(rmt);
		}
	}

	/**
	 * Effettua il parsing dei parametri passati da riga di comando
	 * @param args I parametri passati al programma
	 * @return La configurazione del server specificata dagli 
	 * argomenti da riga di comando
	 */
	public static ServerConfig parseArgs(String[] args) {
		Option configFile = new Option("c", "config", true, "Path del file di configurazione da usare");
		Option registryPort = new Option("r", "registry", true, "Porta del registry");
		Option socketPort = new Option("p", "socket-port", true, "Porta del ServerSocketChannel");
		configFile.setOptionalArg(true);
		registryPort.setOptionalArg(true);
		socketPort.setOptionalArg(true);
		Option[] opts = { configFile, registryPort, socketPort };
		Options all_options = new Options();
		for (Option op : opts) {
			all_options.addOption(op);
		}

		ServerConfig sconf = new ServerConfig();

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
					} else if (op.getOpt().equals("p")) {
						sconf.setServerSocketPort(Integer.valueOf((String) value));
					} else {
						throw new IllegalArgumentException();
					}
				}
			}
		} catch (Exception parseEx) {
			parseEx.printStackTrace();
			help.printHelp("WinsomeServer", all_options);
			return null;
		}
		return sconf;
	}

	/**
	 * Il metodo setta i parametri della configurazione del server
	 * @param in_config Configurazione (parziale) del server proveniente dal parsing
	 */
	private static ServerConfig getServerConfiguration(ServerConfig in_config) {
		// Se è stato specificato un file di configurazione da riga di comando
		// allora viene caricato, altrimenti vengono esaminati i path di default
		File confFile = null;
		try {
			if (in_config.getConfigFile() == null) {
				for (String path : ServerMain.CONF_DFLT_PATHS) {
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
			return mapper.readValue(confFile, ServerConfig.class);
		} catch (WinsomeConfigException | IOException e) {
			e.printStackTrace();
			System.out.println(e);
			return null;
		}
	}
}
