package Winsome.WinsomeServer;

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

import Winsome.WinsomeExceptions.WinsomeConfigException;
import Winsome.WinsomeExceptions.WinsomeServerException;

/**
 * Classe main del server Winsome
 */
public class ServerMain {
	// Opzioni da riga di comando
	public static final String CONFIG_OPT = "c";
	public static final String REGISTRY_OPT = "r";
	public static final String SERVSOCKET_OPT = "p";
	public static final String HELP_OPT = "h";
	/** Path di default per il file di configurazione */
	public static final String[] CONF_DFLT_PATHS = { "config.json", "data/WinsomeServer/config.json" };
	public static final String SIGNUP_STUB = "register";
	public static final String FOLLOWER_SERVICE_STUB = "followerUpdater";
	/** Dimensione di default di un buffer (ad esempio ByteBuffer di lettura) */
	public static final int BUFSZ = 8192;

	/**
	 * Winsome server main class: parses the args array, loads the config file
	 * and initializes the RMI registry for signup operations
	 * @param args Argomenti da riga di comando passati al programma
	 */
	public static void main(String[] args) {
		// Effettua il parsing degli argomenti CLI
		ServerConfig in_config = parseArgs(args);
		if (in_config == null) {
			return;
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

			// Crea il registry del server
			Registry reg = LocateRegistry.createRegistry(config.getRegistryPort());
			// Crea lo stub per la registrazione (già esportato)
			Signup signupObj = new SignupImpl(server);
			// Aggiunge lo stub per la registrazione al registry
			reg.rebind(ServerMain.SIGNUP_STUB, signupObj);
			// Creo lo stub per il servizio di update dei followers
			FollowerUpdaterService fwupObj = new FollowerUpdaterServiceImpl(server);
			// Aggiunge lo stub al registry
			reg.rebind(ServerMain.FOLLOWER_SERVICE_STUB, fwupObj);
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
		Option configFile = Option.builder(CONFIG_OPT)
				.longOpt("config").hasArg().numberOfArgs(1).argName("FILE").required(false)
				.desc("Path del file di configurazione da usare").build();
		Option registryPort = Option.builder(REGISTRY_OPT).longOpt("registry").required(false)
				.hasArg().numberOfArgs(1).argName("PORT")
				.desc("Porta sulla quale viene creato il registry per il signup").build();
		Option socketPort = Option.builder(SERVSOCKET_OPT)
				.longOpt("socket-port").hasArg().numberOfArgs(1).argName("PORT").required(false)
				.desc("Porta sulla quale il server accetta connessioni dai client").build();
		Option helpMsg = Option.builder(HELP_OPT).longOpt("help").required(false)
				.hasArg(false).desc("Messaggio di help").build();
		Option[] opts = { configFile, registryPort, socketPort, helpMsg };
		Options all_options = new Options();
		for (Option op : opts) {
			all_options.addOption(op);
		}

		ServerConfig sconf = new ServerConfig();

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
					case SERVSOCKET_OPT:
						sconf.setServerSocketPort(Integer.valueOf((String) optValue));
						break;
					case HELP_OPT:
					default:
						help.printHelp("WinsomeServer", all_options, true);
						Runtime.getRuntime().exit(0);
				}
			}
		} catch (Exception parseEx) {
			parseEx.printStackTrace();
			help.printHelp("WinsomeServer", all_options, true);
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
			ServerConfig baseConf = mapper.readValue(confFile, ServerConfig.class);
			// Al file di configurazione letto sovrascrivo i parametri passati da riga di comando
			int regPort_cmd = in_config.getRegistryPort();
			baseConf.setRegistryPort(
					regPort_cmd == ServerConfig.DFL_REGPORT ? baseConf.getRegistryPort() : regPort_cmd);
			int ssocketPort_cmd = in_config.getServerSocketPort();
			baseConf.setServerSocketPort(
					ssocketPort_cmd == ServerConfig.DFL_SERVPORT ? baseConf.getServerSocketPort() : ssocketPort_cmd);
			// Ritorno il file di configurazione letto, con eventuali modifiche
			return baseConf;

		} catch (WinsomeConfigException | IOException e) {
			//e.printStackTrace();
			System.out.println(e);
			return null;
		}
	}
}
