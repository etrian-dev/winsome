package Winsome.WinsomeClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import Winsome.WinsomeExceptions.WinsomeServerException;

public class McastListener implements Runnable {
	public static final int RECV_BUFSZ = 512;

	private NetworkInterface netif;
	private WinsomeClientState state;
	private AtomicBoolean runFlag;

	public McastListener(String iface, AtomicBoolean run, WinsomeClientState client)
			throws SocketException {
		this.netif = NetworkInterface.getByName(iface);
		this.runFlag = run;
		this.state = client;
	}

	public void run() {
		// Creo multicast socket
		try (MulticastSocket groupSock = new MulticastSocket(state.getMcastPort());) {
			groupSock.setReuseAddress(true);
			groupSock.setSoTimeout(100);
			SocketAddress groupAddress = new InetSocketAddress(state.getMcastAddr(), state.getMcastPort());
			// Tento di eseguire la join sul gruppo multicast, sull'interfaccia di rete specificata
			groupSock.joinGroup(groupAddress, this.netif);

			System.out.print("Join gruppo " + groupAddress + " sull'interfaccia " + this.netif
					+ "\n" + this.state.getCurrentUser() + ClientMain.USER_PROMPT);

			// Nessuna interfaccia disponibile: eccezione
			if (this.netif == null) {
				throw new WinsomeServerException("Impossibile effettuare join sul gruppo multicast " + groupAddress);
			}
			// Join effettuata: il thread di mette in attesa di datagrammi
			// fino alla chiusura del client o al logout
			byte[] packArr = new byte[RECV_BUFSZ]; // buffer di ricezione
			DatagramPacket pack = new DatagramPacket(packArr, packArr.length);
			while (this.runFlag.get()) {
				try {
					groupSock.receive(pack);
					String msg = new String(pack.getData(), pack.getOffset(), pack.getLength());

					System.out.print(msg + "\n" + this.state.getCurrentUser() + ClientMain.USER_PROMPT);
				} catch (IOException e) {
					;
				}
			}
			System.out.println("Lascio il gruppo multicast " + this.state.getMcastSocket());
			// Lascio il gruppo multicast
			groupSock.leaveGroup(groupAddress, this.netif);
			// Resetto indirizzo multicast e porta
			this.state.setMcastAddress(null);
			this.state.setMcastPort(0);
		} catch (IOException e) {
			System.err.println("Impossibile inizializzare socket multicast: " + e.getMessage());
		} catch (WinsomeServerException we) {
			System.err.println(we.getMessage());
		}
	}
}
