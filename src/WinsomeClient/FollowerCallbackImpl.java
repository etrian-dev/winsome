package WinsomeClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

public class FollowerCallbackImpl extends UnicastRemoteObject implements FollowerCallback {
	private transient Set<String> followers;
	private transient long lastUpdate;

	public FollowerCallbackImpl() throws RemoteException {
		super();
		this.followers = new HashSet<>();
	}

	public void updateFollowers(Set<String> newList) throws RemoteException {
		System.out.println("Aggiornata lista follower: \n"
				+ this.followers + " => " + newList);
		this.followers = newList;
	}

	public void clearFollowers() throws RemoteException {
		this.followers.clear();
	}

	public void setUpdateTimestamp(long timestamp) throws RemoteException {
		this.lastUpdate = timestamp;
	}

	public Set<String> getCurrentFollowers() {
		return this.followers;
	}

	public long getLastUpdate() {
		return this.lastUpdate;
	}
}
