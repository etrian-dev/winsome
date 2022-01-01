package Winsome.WinsomeTasks;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Winsome.WinsomeServer.ServerConfig;

/**
 * Task che restituisce l'indirizzo IP multicast su cui ricevere le notifiche di aggiornamenti
 */
public class MulticastTask extends Task implements Callable<String> {
	private ObjectMapper mapper;
	private ServerConfig conf;

	public MulticastTask(ObjectMapper objMapper, ServerConfig config) {
		super.setKind("Multicast");
		this.mapper = objMapper;
		this.conf = config;
	}

	public String call() {
		try {
			return mapper.writeValueAsString(
					new InetSocketAddress(conf.getMulticastGroupAddress(), conf.getMulticastGroupPort()));
		} catch (JsonProcessingException e) {
			return "Errore:impossibile completare la richiesta";
		}
	}

}
