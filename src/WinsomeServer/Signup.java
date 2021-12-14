package WinsomeServer;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Signup interface
 */
public interface Signup {
	// TODO: refine return type
	/**
	 * 
	 * @param username L'username dell'utente che vuole registrarsi
	 * @param password La password dell'utente
	 * @param tagList La lista dei tag dei quali l'utente si interessa
	 * @return lo userID sse la registrazione è andata a buon fine, null altrimenti
	 * @throws RemoteException
	 */
	public Long register(String username, String password, List<String> tagList)
			throws RemoteException;
}
