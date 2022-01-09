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
		File confDir = new File(this.config.getOutputDir());
		File confFile = new File(confDir.getPath() + "/config.json");
		System.out.println("Sincronizzo il file di configurazione...");
		try {
			if (!(confDir.exists())) {
				confDir.mkdirs();
			}
			if (!confFile.exists()) {
				confFile.createNewFile();
			}
			this.mapper.writeValue(confFile, this.config);
		} catch (IOException e) {
			System.out.println("Impossibile sincronizzare il file di configurazione: "
					+ e.getMessage());
		}
	}
}
