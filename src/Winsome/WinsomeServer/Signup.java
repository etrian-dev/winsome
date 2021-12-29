package Winsome.WinsomeServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import Winsome.WinsomeExceptions.WinsomeConfigException;

/**
 * Interfaccia per la registrazione degli utenti.
 * 
 * L'interfaccia fornisce l'operazione register() per consentire ad un client
 * di creare un nuovo utente Winsome, attraverso il meccanismo di RMI
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
	 * 	<li>0: registrazione effettuata senza errori</li>
	 * 	<li>1: l'utente username è già presente in Winsome</li>
	 *	<li>2: password vuota o null</li>
	 *  <li>3: lista di tag contiene più di cinque stringhe</li>
	 * </ul>
	 * @throws RemoteException
	 */
	public int register(String username, String password, List<String> tagList)
			throws RemoteException, WinsomeConfigException;
}
