package WinsomeServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class SignupImpl extends UnicastRemoteObject implements Signup {
	public Long register(String username, String password, List<String> tagList)
			throws RemoteException {

	}
}
