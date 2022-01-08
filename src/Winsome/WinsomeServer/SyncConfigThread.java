package Winsome.WinsomeServer;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SyncConfigThread extends Thread {
	ServerConfig config;
	ObjectMapper mapper;

	public SyncConfigThread(ServerConfig conf, ObjectMapper objMapper) {
		this.config = conf;
		this.mapper = objMapper;
	}

	public void run() {
		String fname = this.config.getDataDir() + "/config.json";
		File f = new File(fname);
		System.out.println("Sincronizzo il file di configurazione...");
		System.out.println("Written to config file " + f.getAbsolutePath());
		System.out.println(this.config);
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			this.mapper.writeValue(f, this.config);
		} catch (IOException e) {
			System.out.println("Impossibile sincronizzare il file di configurazione: "
					+ e.getMessage());
		}
	}
}
