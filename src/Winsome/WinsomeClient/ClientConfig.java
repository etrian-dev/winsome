package Winsome.WinsomeClient;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Classe che racchiude la configurazione del client Winsome
 */
public class ClientConfig implements Serializable {
	public static final long SerialVersionUID = 1L;

	private String configFile;

	private String dataDir;

	private int registryPort;
	private InetAddress serverHostname;
	private int serverPort;

	public ClientConfig() {
		this.serverHostname = null;
		this.serverPort = 0;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("=== Client Configuration ===");
		s.append("\n\tData dir: " + this.dataDir);
		s.append("\n\tRegistry port: " + this.registryPort);
		s.append("\n\tServer address: " + this.serverHostname);
		s.append("\n\tServer port: " + this.serverPort);
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
}
