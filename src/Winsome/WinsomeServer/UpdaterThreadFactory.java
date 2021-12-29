package Winsome.WinsomeServer;

import java.util.concurrent.ThreadFactory;

public class UpdaterThreadFactory implements ThreadFactory {
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		// Una serie di settaggi del thread di update che ho ritenuto ragionevoli
		t.setDaemon(true);
		t.setName("FollowerUpdaterThread");
		t.setPriority(Thread.MIN_PRIORITY);
		return t;
	}
}
