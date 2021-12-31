package Winsome.WinsomeClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;

import Winsome.WinsomeExceptions.WinsomeServerException;

public class McastListener implements Runnable {
	public static final int RECV_BUFSZ = 512;

	private InetAddress mcastAddr;
	private int mcastPort;
	private NetworkInterface netif;
	private WinsomeClientState state;
	private MulticastSocket group;

	public McastListener(InetAddress addr, int port, String iface, WinsomeClientState client)
			throws SocketException {
		this.mcastAddr = addr;
		this.mcastPort = port;
		this.netif = NetworkInterface.getByName(iface);
		this.state = client;
		this.group = null;
	}

	public MulticastSocket getSocket() {
		return this.group;
	}

	public void run() {
		// Creo multicast socket
		try (MulticastSocket groupSock = new MulticastSocket(this.mcastPort);) {
			this.group = groupSock;
			groupSock.setReuseAddress(true);
			SocketAddress groupAddress = new InetSocketAddress(this.mcastAddr, this.mcastPort);
			// Tento di eseguire la join sul gruppo multicast, sull'interfaccia di rete specificata
			groupSock.joinGroup(groupAddress, this.netif);
			System.out.println("Join gruppo " + groupAddress + " on " + this.netif);
			// Nessuna interfaccia disponibile: eccezione
			if (this.netif == null) {
				throw new WinsomeServerException("Impossibile effettuare join sul gruppo multicast " + groupAddress);
			}
			// Join effettuata: il thread di mette in attesa di datagrammi
			// fino alla chiusura del client
			byte[] packArr = new byte[RECV_BUFSZ]; // buffer di ricezione
			DatagramPacket pack = new DatagramPacket(packArr, packArr.length);
			while (!this.state.isTerminating()) {
				try {
					groupSock.receive(pack);
					String msg = new String(pack.getData(), pack.getOffset(), pack.getLength());
					System.out.println("Messaggio dal gruppo multicast: " + msg);
				} catch (IOException e) {
					;
				}
			}
		} catch (IOException e) {
			System.err.println("Impossibile inizializzare socket multicast: " + e.getMessage());
		} catch (WinsomeServerException we) {
			System.err.println(we.getMessage());
		}
	}
}
