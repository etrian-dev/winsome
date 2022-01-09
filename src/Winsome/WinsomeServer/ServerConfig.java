package Winsome.WinsomeServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Classe che contiene la configurazione del server
 */
public class ServerConfig {
	public static final String DFL_DATADIR = null;
	public static final int DFL_REGPORT = 12345;
	public static final InetAddress DFL_MCASTADDR = null;
	public static final int DFL_MCASTPORT = 10101;
	public static final InetAddress DFL_SERVSOCK = null;
	public static final int DFL_SERVPORT = 9999;
	public static final int DFL_MINPOOL = Integer.MIN_VALUE;
	public static final int DFL_MAXPOOL = Integer.MAX_VALUE;
	public static final int DFL_QUEUE = 50;
	public static final long DFL_RETRY_TIMEOUT = 100L;
	public static final int DFL_UPDATER_POOLSZ = 2;
	public static final long DFL_CALLBACK_INTERVAL = 1;
	public static final TimeUnit DFL_CALLBACK_INTERVAL_UNIT = TimeUnit.MINUTES;
	public static final long DFL_REWARD_INTERVAL = 1;
	public static final TimeUnit DFL_REWARD_INTERVAL_UNIT = TimeUnit.MINUTES;
	public static final double DFL_AUTHOR_PERC = 0.7;
	private static final long DFL_LASTUPDATE = 0L;

	private transient String configFile; // non serializzato

	/** Path della directory dalla quale caricare i dati */
	private String dataDir;
	/** Path della directory nella quale salvare lo stato del server */
	private String outputDir;

	private int registryPort;
	private InetAddress serverSocketAddress;
	private int serverSocketPort;
	private int multicastGroupPort;
	private InetAddress multicastGroupAddress;

	private int minPoolSize;
	private int maxPoolSize;
	private int workQueueSize;
	private long retryTimeout;

	/** 
	 * Dimensione minima del threadpool che si occupa degli aggiornamenti
	 * dei wallet e della lista di followers
	 */
	private int coreUpdaterPoolSize;
	private long callbackInterval;
	private TimeUnit callbackIntervalUnit;

	/** Intervallo tra due calcoli delle ricompense (&gt; 0) */
	private long rewardInterval;
	private TimeUnit rewardIntervalUnit;
	/** Timestamp dell'ultimo aggiornamento dei wallet */
	private long lastUpdate;
	/** 
	 * Percentuale della ricompensa per un post che viene accreditata
	 * all'autore di tale post.
	 * 
	 * Il valore deve essere un reale compreso strettamente
	 * tra 0.0 e 1.0. La percentuale di ricompensa che va ai curatori
	 * del post è ottenuta come 1.0 - authorPercentage
	 */
	private double authorPercentage;

	public ServerConfig() {
		this.dataDir = DFL_DATADIR;
		this.outputDir = DFL_DATADIR;

		this.registryPort = DFL_REGPORT;
		this.serverSocketAddress = DFL_SERVSOCK;
		this.serverSocketPort = DFL_SERVPORT;
		this.multicastGroupAddress = DFL_MCASTADDR;
		this.multicastGroupPort = DFL_MCASTPORT;

		this.minPoolSize = DFL_MINPOOL;
		this.maxPoolSize = DFL_MAXPOOL;
		this.workQueueSize = DFL_QUEUE;
		this.retryTimeout = DFL_RETRY_TIMEOUT;

		this.coreUpdaterPoolSize = DFL_UPDATER_POOLSZ;
		this.callbackInterval = DFL_CALLBACK_INTERVAL;
		this.callbackIntervalUnit = DFL_CALLBACK_INTERVAL_UNIT;

		this.rewardInterval = DFL_REWARD_INTERVAL;
		this.rewardIntervalUnit = DFL_REWARD_INTERVAL_UNIT;
		this.lastUpdate = DFL_LASTUPDATE;
		this.authorPercentage = DFL_AUTHOR_PERC;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("=== Server Configuration ===");
		s.append("\nData dir: " + this.dataDir);
		s.append("\nOutput dir: " + this.outputDir);
		s.append("\n----------");
		s.append("\nRegistry port: " + this.registryPort);
		s.append("\nServer socket address: " + this.serverSocketAddress + ":" + this.serverSocketPort);
		s.append("\nMulticast group: " + this.multicastGroupAddress + ":" + this.multicastGroupPort);
		s.append("\n----------");
		s.append("\nMin threadpool size: " + this.minPoolSize);
		s.append("\nMax threadpool size: " + this.maxPoolSize);
		s.append("\nThreadpool queue size: " + this.workQueueSize);
		s.append("\nThreadpool task retry timeout : " + this.retryTimeout + "ms");
		s.append("\n----------");
		s.append("\nMin updater pool size: " + this.coreUpdaterPoolSize);
		s.append("\nFollower update interval: "
				+ this.callbackInterval + " " + this.callbackIntervalUnit);
		s.append("\n----------");
		s.append("\nReward update interval: "
				+ this.rewardInterval + " " + this.rewardIntervalUnit);
		s.append("\nLast wallet update: " + this.lastUpdate);
		s.append("\nAuthor reward percentage: " + (this.authorPercentage * 100) + "%");
		return s.toString();
	}

	// All the getters
	public String getConfigFile() {
		return (this.configFile == null ? null : new String(this.configFile));
	}

	public String getDataDir() {
		return (this.dataDir == null ? null : new String(this.dataDir));
	}

	public String getOutputDir() {
		return (this.outputDir == null ? null : new String(this.outputDir));
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

	public InetAddress getMulticastGroupAddress() {
		return this.multicastGroupAddress;
	}

	public int getMulticastGroupPort() {
		return this.multicastGroupPort;
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

	public int getCoreUpdaterPoolSize() {
		return this.coreUpdaterPoolSize;
	}

	public long getCallbackInterval() {
		return this.callbackInterval;
	}

	public TimeUnit getCallbackIntervalUnit() {
		return this.callbackIntervalUnit;
	}

	public long getRewardInterval() {
		return this.rewardInterval;
	}

	public TimeUnit getRewardIntervalUnit() {
		return this.rewardIntervalUnit;
	}

	public long getLastUpdate() {
		return this.lastUpdate;
	}

	public double getAuthorPercentage() {
		return this.authorPercentage;
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

	public boolean setOutputDir(String outputDir) {
		if (outputDir == null) {
			return false;
		}
		this.outputDir = outputDir;
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

	public boolean setMulticastGroupAddress(String host) {
		try {
			this.multicastGroupAddress = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			return false;
		}
		return true;
	}

	public boolean setMulticastGroupPort(int port) {
		if (port < 1024 || port > 65535) {
			return false;
		}
		this.multicastGroupPort = port;
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

	public boolean setCoreUpdaterPoolSize(int poolsz) {
		if (poolsz <= 0) {
			return false;
		}
		this.coreUpdaterPoolSize = poolsz;
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

	public boolean setRewardInterval(long interval) {
		if (interval < 0) {
			return false;
		}
		this.rewardInterval = interval;
		return true;
	}

	public void setRewardIntervalUnit(TimeUnit unit) {
		this.rewardIntervalUnit = unit;
	}

	public boolean setLastUpdate(long timestamp) {
		if (timestamp < 0 || timestamp > System.currentTimeMillis()) {
			return false;
		}
		this.lastUpdate = timestamp;
		return true;
	}

	public boolean setAuthorPercentage(double perc) {
		if (perc <= 0.0 || perc >= 1.0) {
			return false;
		}
		this.authorPercentage = perc;
		return true;
	}

}
