package WinsomeServer;

import java.rmi.RemoteException;

/**
 * Classe main del server: fa crea l'istanza di WinsomeServer ed inizializza
 * il registry per la procedura di registrazione
 */
public class ServerMain {
	public static void main(String[] args) {
		WinsomeServer server = null;
		try {
			server = new WinsomeServer(WinsomeServer.REGPORT);
		} catch (RemoteException rmt) {
			System.out.println(rmt);
		}
		server.start();
	}
}
