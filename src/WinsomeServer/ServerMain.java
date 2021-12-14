package WinsomeServer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import WinsomeExceptions.WinsomeConfigException;
import org.apache.commons.cli.*;

/**
 * Classe main del server: fa crea l'istanza di WinsomeServer ed inizializza
 * il registry per la procedura di registrazione
 */
public class ServerMain {
	public static final String[] CONF_DFLT_PATHS = { "data/WinsomeServer/config.json", "config.json" };
	public static final int REGPORT = 22222;

	/**
	 * Winsome server main class: parses the args array, loads the config file
	 * and initializes the RMI registry for signup operations
	 * @param args Command line parameters passed to the program
	 */
	public static void main(String[] args) {
		// TODO: set options and parse args[] with Apache Commons CLI
		ServerConfig config = parseArgs(args);
		if (config == null) {
			config = new ServerConfig();
		}

		// Loads the config file passed as a parameter (or the default)
		try {
			File confFile = null;
			if (config.getConfigFile() == null) {
				for (String path : ServerMain.CONF_DFLT_PATHS) {
					if (Files.exists(Paths.get(path), LinkOption.NOFOLLOW_LINKS)) {
						config.setConfigFile(path);
						confFile = new File(path);
						break;
					}
				}
			}

			// various sanity checks
			if (!(confFile.exists() && confFile.isFile() && confFile.canRead())) {
				throw new WinsomeConfigException("Config file cannot be accessed");
			}
			System.out.println(config);
		} catch (WinsomeConfigException e) {
			e.printStackTrace();
			System.out.println(e);
		}

		int registryPort = 22222;
		WinsomeServer server = null;
		try {
			// Create the registry
			int regPort = ServerMain.REGPORT;
			if (registryPort > 1024 && registryPort < 65536) {
				regPort = registryPort;
			}
			Registry signupRegistry = LocateRegistry.createRegistry(regPort);
			Signup signupObj = new SignupImpl();
			signupRegistry.rebind("register", signupObj);
			// Create the server instance
			server = new WinsomeServer();
		} catch (RemoteException rmt) {
			System.out.println(rmt);
		}
		server.start();
	}

	/**
	 * Effettua il parsing dei parametri passati da riga di comando
	 * @param args I parametri passati al programma
	 * @return L'array di opzioni ottenuto dal parsing degli argomenti
	 */
	public static ServerConfig parseArgs(String[] args) {
		Option configFile = new Option("c", "config", true, "Path del file di configurazione da usare");
		Option registryPort = new Option("r", "registry", true, "Porta del registry");
		Option socketPort = new Option("s", "socket", true, "Porta del ServerSocketChannel");
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
					} else if (op.getOpt().equals("s")) {
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
}
