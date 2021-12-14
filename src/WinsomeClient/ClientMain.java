package WinsomeClient;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import WinsomeServer.WinsomeServer;

public class ClientMain {
	public static void main(String[] args) {
		try {
			Registry reg = LocateRegistry.getRegistry(WinsomeServer.REGPORT);
			System.out.println("Hello, server");
		} catch (RemoteException rmt) {
			System.out.println(rmt);
		}
	}
}
