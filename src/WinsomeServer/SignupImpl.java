package WinsomeServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

import WinsomeExceptions.WinsomeConfigException;

/**
 * Implementazione dell'interfaccia di Signup.
 * 
 * @see Signup
 */
public class SignupImpl extends UnicastRemoteObject implements Signup {

	WinsomeServer server;

	/**
	 * Costruttore di default
	 * 
	 * @throws RemoteException
	 */
	protected SignupImpl() throws RemoteException {
		super();
	}

	/**
	 * crea l'istanza dello stub per la registrazione, con riferimento al server Winsome
	 * @param servRef riferimento al server winsome presso cui registrarsi
	 * @throws RemoteException
	 * @throws WinsomeConfigException
	 */
	public SignupImpl(WinsomeServer servRef) throws RemoteException, WinsomeConfigException {
		this();
		this.server = servRef;
	}

	public int register(String username, String password, List<String> tagList)
			throws RemoteException {
		// Controlli sull'utente da registrare: sono replicati
		// in addUser(), ma se fatti qui permettono di disaccoppiare
		// la gestione dei codici di errore tra le due componenti
		if (tagList.size() > 5) {
			return 3; // troppi tag specificati dall'utente
		}
		if (password == null || password.equals("")) {
			return 2;
		}
		Map<String, User> userMap = server.getUsers();
		if (userMap.keySet().contains(username.toLowerCase())) {
			return 1;
		}

		// Parametri utente ok: utente aggiunto a all_users e scritto sul file json
		User newUser = new User();
		newUser.setUsername(username);
		newUser.setPassword(password);
		for (String tag : tagList) {
			newUser.setTag(tag);
		}
		if (!server.addUser(newUser)) {
			return -1;
		}

		// Utente registrato con successo
		return 0;
	}
}
