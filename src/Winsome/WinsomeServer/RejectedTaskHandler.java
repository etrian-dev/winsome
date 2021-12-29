package Winsome.WinsomeServer;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/** 
 * Handler per la gestione delle task rifiutate dalla threadpool 
 * (implementa l'interfaccia RejectedExecutionHandler) 
 * */
public class RejectedTaskHandler implements RejectedExecutionHandler {
	private long waitTimeout = 100L; // 100ms default

	public RejectedTaskHandler(long timeout) {
		if (timeout > 0)
			this.waitTimeout = timeout;
	}

	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		int active = executor.getActiveCount();
		int maxTh = executor.getMaximumPoolSize();
		// Se ho alcuni thread che non stanno eseguendo task provo ad aumentare
		// temporaneamente la max pool size e poi farla tornare al valore originale
		// (si noti che i thread non subiscono cancellazioni se sono tutti attivi e
		//la diminuisco, ma vengono terminati solo al termine della loro task)
		// source: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html#setMaximumPoolSize(int)
		if (active < maxTh) {
			executor.setMaximumPoolSize(maxTh + 1);
			executor.execute(r);
			executor.setMaximumPoolSize(maxTh);
		} else {
			// Se tutti i thread sono attivi allora attendo un intervallo di tempo
			// e poi ricontrollo: se non è variato il numero di thread attivi lancio l'eccezione
			synchronized (r) {
				try {
					r.wait(this.waitTimeout);
				} catch (InterruptedException e) {
					;
				}
				if (executor.getActiveCount() < active) {
					executor.execute(r);
				} else {
					throw new RejectedExecutionException("Server sovraccarico: riprovare più tardi");
				}
			}
		}
	}
}
