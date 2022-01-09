/**
 * Package che contiene l'implementazione del client Winsome.
 * <p>
 * <ul>
 * <li>
 * La classe contenente il metodo main è nella classe {@link Winsome.WinsomeClient.ClientMain}
 * </li>
 * <li>
 * La classe {@link Winsome.WinsomeClient.ClientConfig} si occupa del parsing e caricamento del file di
 * configurazione del client.
 * </li>
 * <li>
 * La classe {@link Winsome.WinsomeClient.Command} contiene la definizione di una enum di tutti i comandi
 * di cui il client può richiedere l'esecuzione.
 * </li>
 * <li>
 * La classe {@link Winsome.WinsomeClient.ClientCommand} incapsula il comando (di tipo Command) 
 * e gli argomenti forniti dall'utente via standard input.
 * Inoltre è presente una funzione per effettuare il parsing e la validazione dei comandi.
 * </li>
 * <li>
 * La classe {@link Winsome.WinsomeClient.WinsomeClientState} &egrave; la classe che contiene lo stato corrente
 * del client (l'utente loggato, il riferimento al socket TCP, etc...)
 * </li>
 * </ul>
 */
package Winsome.WinsomeClient;