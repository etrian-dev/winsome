package Winsome.WinsomeClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Classe che racchiude la configurazione del client Winsome
 */
public class ClientConfig {
	public static final int DFL_REGPORT = 12345;
	public static final InetAddress DFL_SERVADDRESS = null;
	public static final int DFL_SERVPORT = 9999;
	public static final String DFL_NETIF = "wifi0";

	private String configFile;

	private String dataDir;

	private int registryPort;
	private InetAddress serverHostname;
	private int serverPort;

	private String netIf;

	public ClientConfig() {
		this.registryPort = DFL_REGPORT;
		this.serverHostname = DFL_SERVADDRESS;
		this.serverPort = DFL_SERVPORT;
		this.netIf = DFL_NETIF;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("=== Client Configuration ===");
		s.append("\nData dir: " + this.dataDir);
		s.append("\n----------");
		s.append("\nRegistry port: " + this.registryPort);
		s.append("\n----------");
		s.append("\nServer address: " + this.serverHostname);
		s.append("\nServer port: " + this.serverPort);
		s.append("\n----------");
		s.append("\nMulticast netif: " + this.netIf);
		return s.toString();
	}

	// Getters
	public String getConfigFile() {
		return (this.configFile == null ? null : new String(this.configFile));
	}

	public String getDataDir() {
		return (this.dataDir == null ? null : new String(this.dataDir));
	}

	public int getRegistryPort() {
		return this.registryPort;
	}

	public InetAddress getServerHostname() {
		return this.serverHostname;
	}

	public int getServerPort() {
		return this.serverPort;
	}

	public String getNetIf() {
		return this.netIf;
	}

	//Setters
	public boolean setConfigFile(String path) {
		if (path == null) {
			return false;
		}
		this.configFile = path;
		return true;
	}

	public boolean setDataDir(String dataDir) {
		if (dataDir == null) {
			return false;
		}
		this.dataDir = dataDir;
		return true;
	}

	public boolean setRegistryPort(int port) {
		if (port < 1024 || port > 65535) {
			return false;
		}
		this.registryPort = port;
		return true;
	}

	public boolean setServerHostname(String addr) {
		try {
			this.serverHostname = InetAddress.getByName(addr);
		} catch (UnknownHostException uk) {
			return false;
		}
		return true;
	}

	public boolean setPort(int port) {
		if (port < 1024 || port > 65535) {
			return false;
		}
		this.serverPort = port;
		return true;
	}

	public void setNetIf(String netif) {
		this.netIf = netif;
	}
}
