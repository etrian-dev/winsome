package WinsomeClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

public class FollowerCallbackImpl extends UnicastRemoteObject implements FollowerCallback {
	private transient Set<String> followers;

	public FollowerCallbackImpl() throws RemoteException {
		super();
		this.followers = new HashSet<>();
	}

	public Set<String> getFollowers() throws RemoteException {
		return Set.copyOf(this.followers);
	}

	public void clearFollowers() throws RemoteException {
		this.followers.clear();
	}

	public void newFollower(String user) throws RemoteException {
		this.followers.add(user);
	}

	public void lostFollower(String user) throws RemoteException {
		this.followers.remove(user);
	}
}
