package WinsomeServer;

import java.io.Serializable;

/**
 * Class that implements the server configuration
 */
public class ServerConfig implements Serializable {
	public static final long SerialVersionUID = 1L;

	private String configFile;

	private String dataDir;

	private int registryPort;
	private int serverSocketPort;

	private int minPoolSize;

	public ServerConfig() {
		this.dataDir = null;

		this.registryPort = 0;
		this.serverSocketPort = 0;

		this.minPoolSize = 0;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Config file:");
		s.append("\n\tData dir: " + this.dataDir);
		s.append("\n\tRegistry port: " + this.registryPort);
		s.append("\n\tServer socket port: " + this.serverSocketPort);
		s.append("\n\tMin threadpool size: " + this.minPoolSize);
		return s.toString();
	}

	// All the getters
	public String getConfigFile() {
		return (this.configFile == null ? null : new String(this.configFile));
	}

	public String getDataDir() {
		return (this.dataDir == null ? null : new String(this.dataDir));
	}

	public int getRegistryPort() {
		return this.registryPort;
	}

	public int getServerSocketPort() {
		return this.serverSocketPort;
	}

	public int getMinPoolSize() {
		return this.minPoolSize;
	}

	// All the setters
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

	public boolean setServerSocketPort(int port) {
		if (port < 1024 || port > 65535) {
			return false;
		}
		this.serverSocketPort = port;
		return true;
	}

	public boolean setMinPoolSize(int poolsz) {
		if (poolsz < 0) {
			return false;
		}
		this.minPoolSize = poolsz;
		return true;
	}
}
