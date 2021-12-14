package WinsomeServer;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import WinsomeExceptions.WinsomeConfigException;

/**
 * Implementazione dell'interfaccia di Signup
 * 
 * @see Signup
 */
public class SignupImpl extends UnicastRemoteObject implements Signup {
	List<User> all_users;
	File userFile;

	protected SignupImpl() throws RemoteException {
		super();
	}

	public SignupImpl(String dataDir) throws RemoteException, WinsomeConfigException {
		this();
		// Crea l'oggetto file degli utenti
		this.userFile = new File(dataDir + "/users.json");
		if (!this.userFile.exists()) {
			throw new WinsomeConfigException("Il file degli utenti " + this.userFile.getAbsolutePath() + " non esiste");
		}
		// TODO: streaming or tree JSON reading to read users from file
	}

	public Long register(String username, String password, List<String> tagList)
			throws RemoteException {
		long newUserID = ThreadLocalRandom.current().nextLong(1);
		// TODO: search userID in a while on all_users, then update and write object to file 

		StringBuffer s = new StringBuffer();
		s.append("User: " + username);
		s.append("\nPassword: " + password);
		s.append("\nTags: ");
		for (String t : tagList) {
			s.append(t + ", ");
		}
		s.append('\n');
		System.out.println(s);

		return newUserID;
	}
}
