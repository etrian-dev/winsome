package Winsome.WinsomeServer;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Classe che contiene la configurazione del server
 */
public class ServerConfig implements Serializable {
	public static final long SerialVersionUID = 1L;
	public static final String DFL_DATADIR = null;
	public static final int DFL_REGPORT = 12345;
	public static final InetAddress DFL_SERVSOCK = null;
	public static final int DFL_SERVPORT = 9999;
	public static final int DFL_MINPOOL = Integer.MIN_VALUE;
	public static final int DFL_MAXPOOL = Integer.MAX_VALUE;
	public static final int DFL_QUEUE = 50;
	public static final long DFL_RETRY_TIMEOUT = 100L;
	public static final long DFL_CALLBACK_INTERVAL = 1;
	public static final TimeUnit DFL_CALLBACK_INTERVAL_UNIT = TimeUnit.MINUTES;

	private transient String configFile; // non serializzato

	private String dataDir;

	private int registryPort;
	private InetAddress serverSocketAddress;
	private int serverSocketPort;

	private int minPoolSize;
	private int maxPoolSize;
	private int workQueueSize;
	private long retryTimeout;

	private long callbackInterval;
	private TimeUnit callbackIntervalUnit;

	public ServerConfig() {
		this.dataDir = DFL_DATADIR;

		this.registryPort = DFL_REGPORT;
		this.serverSocketAddress = DFL_SERVSOCK;
		this.serverSocketPort = DFL_SERVPORT;

		this.minPoolSize = DFL_MINPOOL;
		this.maxPoolSize = DFL_MAXPOOL;
		this.workQueueSize = DFL_QUEUE;
		this.retryTimeout = DFL_RETRY_TIMEOUT;

		this.callbackInterval = DFL_CALLBACK_INTERVAL;
		this.callbackIntervalUnit = DFL_CALLBACK_INTERVAL_UNIT;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("=== Server Configuration ===");
		s.append("\nData dir: " + this.dataDir);
		s.append("\n----------");
		s.append("\nRegistry port: " + this.registryPort);
		s.append("\nServer socket address: " + this.serverSocketAddress + ":" + this.serverSocketPort);
		s.append("\n----------");
		s.append("\nMin threadpool size: " + this.minPoolSize);
		s.append("\nMax threadpool size: " + this.maxPoolSize);
		s.append("\nThreadpool queue size: " + this.workQueueSize);
		s.append("\nThreadpool task retry timeout : " + this.retryTimeout + "ms");
		s.append("\n----------");
		s.append("\nFollower update interval: "
				+ this.callbackInterval + " " + this.callbackIntervalUnit);
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

	public InetAddress getServerSocketAddress() {
		return this.serverSocketAddress;
	}

	public int getServerSocketPort() {
		return this.serverSocketPort;
	}

	public int getMinPoolSize() {
		return this.minPoolSize;
	}

	public int getMaxPoolSize() {
		return this.maxPoolSize;
	}

	public int getWorkQueueSize() {
		return this.workQueueSize;
	}

	public long getRetryTimeout() {
		return this.retryTimeout;
	}

	public long getCallbackInterval() {
		return this.callbackInterval;
	}

	public TimeUnit getCallbackIntervalUnit() {
		return this.callbackIntervalUnit;
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

	public boolean setServerSocketAddress(String host) {
		try {
			this.serverSocketAddress = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			return false;
		}
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
		if (poolsz <= 0 || poolsz > this.maxPoolSize) {
			return false;
		}
		this.minPoolSize = poolsz;
		return true;
	}

	public boolean setMaxPoolSize(int poolsz) {
		if (poolsz < this.minPoolSize) {
			return false;
		}
		this.maxPoolSize = poolsz;
		return true;
	}

	public boolean setWorkQueueSize(int qsize) {
		if (qsize < 0) {
			return false;
		}
		this.workQueueSize = qsize;
		return true;
	}

	public boolean setRetryTimeout(long timeout) {
		if (timeout < 0) {
			return false;
		}
		this.retryTimeout = timeout;
		return true;
	}

	public boolean setCallbackInterval(long interval) {
		if (interval < 0) {
			return false;
		}
		this.callbackInterval = interval;
		return true;
	}

	public void setCallbackIntervalUnit(TimeUnit unit) {
		this.callbackIntervalUnit = unit;
	}
}