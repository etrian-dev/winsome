package Winsome.WinsomeServer;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Thread per la scrittura del file degli utenti alla terminazione del server
 */
public class SyncUsersThread extends Thread {
	public static final String USERS_SYNC_MSG = "Sincronizzo il file degli utenti...";

	private File usersFile;
	private ObjectMapper mapper;
	private JsonFactory factory;
	private WinsomeServer servRef;

	public SyncUsersThread(
			File userFileObj,
			ObjectMapper ObjMapper,
			JsonFactory fact,
			WinsomeServer serv) {
		this.usersFile = userFileObj;
		this.mapper = ObjMapper;
		this.factory = fact;
		this.servRef = serv;
	}

	public void run() {
		System.out.println(USERS_SYNC_MSG);
		// Creo un ArrayNode
		ArrayNode all_users = mapper.createArrayNode();
		// Per ogni utente Winsome genero un ObjectNode
		for (User u : servRef.getUsers()) {
			// Riempo i campi da serializzare per ogni utente
			ObjectNode newUserObj = mapper.createObjectNode();
			newUserObj.put("username", u.getUsername());
			newUserObj.put("password", u.getPassword());
			ArrayNode tagsArr = mapper.createArrayNode();
			for (String aTag : u.getTags()) {
				tagsArr.add(aTag);
			}
			newUserObj.set("tags", tagsArr);
			ArrayNode followingArr = mapper.createArrayNode();
			for (String username : u.getFollowing()) {
				followingArr.add(username);
			}
			newUserObj.set("following", followingArr);
			ArrayNode followersArr = mapper.createArrayNode();
			for (String username : u.getFollowers()) {
				followersArr.add(username);
			}
			newUserObj.set("followers", followersArr);
			newUserObj.put("wallet", u.getWallet());
			ArrayNode transactions = mapper.createArrayNode();
			for (Transaction t : u.getTransactions()) {
				ObjectNode transactionObj = mapper.createObjectNode();
				transactionObj.put("timestamp", t.getTimestamp());
				transactionObj.put("amount", t.getAmount());
				transactions.add(transactionObj);
			}
			newUserObj.set("transactions", transactions);
			// Aggiungo l'oggetto dell'utente all'array
			all_users.add(newUserObj);
		}
		try {
			// Aggiungo un nodo all'array, contenente l'istanza di User serializzata
			// Scrivo sul file l'array modificato
			if (!usersFile.exists()) {
				System.out.println("[WARNING] Creazione nuovo file utenti: " + usersFile.getPath());
				usersFile.createNewFile();
			}
			JsonGenerator gen = this.factory.createGenerator(usersFile, JsonEncoding.UTF8);
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			gen.useDefaultPrettyPrinter();
			mapper.writeTree(gen, all_users);
		} catch (IOException e) {
			System.out.println("[ERROR] " + e.getMessage());
		}
	}
}
