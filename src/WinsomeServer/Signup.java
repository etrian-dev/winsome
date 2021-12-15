package WinsomeServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import WinsomeExceptions.WinsomeConfigException;

/**
 * Interfaccia per la registrazione degli utenti
 */
public interface Signup extends Remote {
	/**
	 * Metodo per la registrazione di un nuovo utente su Winsome.
	 * 
	 * @param username L'username dell'utente che vuole registrarsi (univoco, case insensitive)
	 * @param password La password dell'utente (diversa dalla stringa vuota)
	 * @param tagList La lista dei tag dei quali l'utente si interessa (massimo cinque, case insensitive)
	 * @return Il metodo ritorna:
	 * <ul>
	 * 	<li>registrazione effettuata senza errori: 0</li>
	 * 	<li>l'utente username è già presente in Winsome: 1</li>
	 *	<li>password vuota o null: 2</li>
	 *  <li>lista di tag contiene più di cinque stringhe: 3</li>
	 *	<li>altro errore: -1</li>
	 * </ul>
	 * @throws RemoteException
	 */
	public int register(String username, String password, List<String> tagList)
			throws RemoteException, WinsomeConfigException;
}
