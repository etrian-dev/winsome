package WinsomeClient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import WinsomeServer.ServerMain;
import WinsomeServer.Signup;

public class ClientMain {
	public static void main(String[] args) {
		try {
			Registry reg = LocateRegistry.getRegistry(ServerMain.REGPORT);
			System.out.println("Hello, server");
			Signup stub = (Signup) reg.lookup("register");

			// register a user
			String[] tags = {"Music", "Literature", "Programming"};
			ArrayList<String> taglist = new ArrayList<>();
			for (String s : tags) {
				taglist.add(s);
			}
			stub.register("nicola", "password1", taglist);
		} catch (RemoteException | NotBoundException e) {
			System.out.println(e);
		}
	}
}
