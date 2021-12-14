package WinsomeServer;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class WinsomeServer extends Thread {


	Registry signupRegistry;

	public WinsomeServer() throws RemoteException {
		;
	}

	public void run() {
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException io) {
			System.out.println(io);
		}
	}
}
