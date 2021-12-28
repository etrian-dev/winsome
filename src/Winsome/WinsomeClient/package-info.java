/**
 * Package che contiene l'implementazione del client Winsome.
 * <p>
 * <ul>
 * <li>
 * La classe contenente il metodo main è ClientMain
 * </li>
 * <li>
 * La classe ClientConfig si occupa del parsing e caricamento del file di
 * configurazione del client.
 * </li>
 * <li>
 * La classe Command contiene la definizione di una enum di tutti i comandi
 * di cui il client può richiedere l'esecuzione.
 * </li>
 * <li>
 * La classe ClientCommand contiene il comando (di tipo Command) e gli argomenti forniti.
 * Inoltre è presente una funzione per effettuare il parsing e validazione dei comandi.
 * </li>
 * <li>
 * La classe WinsomeClientState semplicemente incapsula l'utente loggato nel client e
 * l'indicazione di terminazione (flag booleana) con i relativi getters e setters.
 * </li>
 * </ul>
 */
package Winsome.WinsomeClient;