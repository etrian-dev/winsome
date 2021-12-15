package WinsomeServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import WinsomeExceptions.WinsomeConfigException;

/**
 * Implementazione dell'interfaccia di Signup.
 * 
 * @see Signup
 */
public class SignupImpl extends UnicastRemoteObject implements Signup {

	Map<String, User> all_users;
	File userFile;
	ObjectMapper mapper;
	JsonFactory factory;

	/**
	 * Costruttore di default
	 * 
	 * @throws RemoteException
	 */
	protected SignupImpl() throws RemoteException {
		super();
	}

	/**
	 * Costruttore con path della directory dati del server
	 * 
	 * @param dataDir path della directory contenente i dati del server
	 * @throws RemoteException
	 * @throws WinsomeConfigException
	 */
	public SignupImpl(String dataDir) throws RemoteException, WinsomeConfigException {
		this();
		// Crea la mappa Username -> Utente
		this.all_users = new HashMap<>();
		// Crea l'oggetto file degli utenti
		this.userFile = new File(dataDir + "/users.json");
		if (!this.userFile.exists()) {
			throw new WinsomeConfigException("Il file degli utenti "
					+ this.userFile.getAbsolutePath() + " non esiste");
		}
		// Inizializzazione vari oggetti Jackson
		this.mapper = new ObjectMapper();
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
		this.factory = new JsonFactory(mapper);

		try {
			readUsers();
		} catch (com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException parserEx) {
			parserEx.printStackTrace();
			System.out.println("ERR: deserializzazione file, formato file errato");
		} catch (IOException e) {
			e.printStackTrace();
			throw new WinsomeConfigException(
					"Impossibile leggere il file degli utenti " + this.userFile.getAbsolutePath());
		}
		/* FIXME : testing print
		System.out.println("Users read from " + this.userFile.getAbsolutePath());
		for (User u : this.all_users.values()) {
			System.out.println(u.toString());
		}
		*/
	}

	/**
	 * Metodo per la lettura degli utenti dal file nella directory dati
	 */
	private void readUsers() throws WinsomeConfigException, IOException {
		BufferedInputStream bufIn = new BufferedInputStream(new FileInputStream(userFile));
		JsonParser parser = this.factory.createParser(bufIn);
		// Leggo tutti i token del parser
		JsonToken tok = parser.nextToken();
		if (tok == JsonToken.NOT_AVAILABLE || tok != JsonToken.START_ARRAY) {
			throw new WinsomeConfigException("Il file degli utenti  "
					+ this.userFile.getAbsolutePath() + "non rispetta la formattazione attesa");
		}
		while ((tok = parser.nextToken()) != JsonToken.END_ARRAY) {
			// tok contiene l'oggetto di tipo User, per cui posso deserializzarlo
			User u = parser.readValueAs(User.class);
			this.all_users.put(u.getUsername(), u);
		}
	}

	public int register(String username, String password, List<String> tagList)
			throws RemoteException, WinsomeConfigException {
		if (tagList.size() > 5) {
			return 3; // troppi tag specificati dall'utente
		}
		if (password == null || password.equals("")) {
			return 2;
		}
		Set<String> all_usernames = this.all_users.keySet();
		if (all_usernames.contains(username.toLowerCase())) {
			return 1;
		}

		// Parametri utente ok: utente aggiunto a all_users e scritto sul file json
		User newUser = new User();
		newUser.setUsername(username);
		newUser.setPassword(password);
		for (String tag : tagList) {
			newUser.setTag(tag);
		}
		this.all_users.put(new String(username), newUser);

		// Creo un nuovo oggetto vuoto e riempo i campi da serializzare
		ObjectNode newUserObj = mapper.createObjectNode();
		newUserObj.put("username", newUser.getUsername());
		newUserObj.put("password", newUser.getPassword());
		ArrayNode tagsArr = mapper.createArrayNode();
		for (String aTag : newUser.getTags()) {
			tagsArr.add(aTag);
		}
		newUserObj.set("tags", tagsArr);

		try {
			ArrayNode tree = (ArrayNode) mapper.readTree(this.userFile);
			if (!tree.isArray()) {
				throw new WinsomeConfigException("Il file degli utenti  "
						+ this.userFile.getAbsolutePath() + "non rispetta la formattazione attesa");
			}
			// Aggiungo un nodo all'array, contenente l'istanza di User serializzata
			tree.add(newUserObj);
			// Scrivo sul file l'array modificato
			JsonGenerator gen = this.factory.createGenerator(this.userFile, JsonEncoding.UTF8);
			gen.useDefaultPrettyPrinter();
			mapper.writeTree(gen, tree);
		} catch (IOException e) {
			throw new WinsomeConfigException("Impossibile aggiungere utente in " + this.userFile.getAbsolutePath());
		}

		// Log dell'operazione
		StringBuffer s = new StringBuffer();
		s.append("=== New user created ===\nUser: " + username);
		s.append("\nPassword: " + password);
		s.append("\nTags: ");
		for (String t : tagList) {
			s.append(t + ", ");
		}
		s.append('\n');
		System.out.println(s);

		// Utente registrato con successo
		return 0;
	}
}
