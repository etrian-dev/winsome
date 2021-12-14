package WinsomeServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WinsomeServer extends Thread {
	public static final int REGPORT = 22222;

	Registry signupRegistry;

	public WinsomeServer(int registryPort) throws RemoteException {
		int regPort = WinsomeServer.REGPORT;
		if (registryPort > 1024 && registryPort < 65536) {
			regPort = registryPort;
		}
		this.signupRegistry = LocateRegistry.createRegistry(regPort);
	}

	public void run() {
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException io) {
			System.out.println(io);
		}
	}
}
