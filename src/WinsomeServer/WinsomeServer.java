package WinsomeServer;

/**
 * Classe che implementa il server Winsome
 */
public class WinsomeServer extends Thread {

	public WinsomeServer() {
		System.out.println("Server created");
	}

	public void run() {
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException io) {
			System.out.println(io);
		}
	}
}
