package WinsomeServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import WinsomeClient.FollowerCallback;

public class FollowerUpdaterServiceImpl extends UnicastRemoteObject implements FollowerUpdaterService {
	private transient WinsomeServer servRef;

	public FollowerUpdaterServiceImpl(WinsomeServer serv) throws RemoteException {
		super();
		this.servRef = serv;
	}

	public int subscribe(String user, FollowerCallback callbackObj) throws RemoteException {
		this.servRef.addFollowerCallback(user, callbackObj);
		return 0; // TODO: error codes
	}

	public int unsubscribe(String user) throws RemoteException {
		this.servRef.rmFollowerCallback(user);
		return 0; // TODO: error codes
	}
}
