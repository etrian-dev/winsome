package WinsomeServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SignupImpl extends UnicastRemoteObject implements Signup {
	protected SignupImpl() throws RemoteException {
		super();
	}

	public Long register(String username, String password, List<String> tagList)
			throws RemoteException {
		StringBuffer s = new StringBuffer();
		s.append("User: " + username);
		s.append("\nPassword: " + password);
		s.append("\nTags: ");
		for (String t : tagList) {
			s.append(t);
		}
		s.append('\n');
		System.out.println(s);
		return ThreadLocalRandom.current().nextLong(1);
	}
}
