package WinsomeClient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import WinsomeServer.Signup;

public class ClientMain {
	public static void main(String[] args) {
		try {
			Registry reg = LocateRegistry.getRegistry(11111);
			System.out.println("Hello, server");
			Signup stub = (Signup) reg.lookup("register");

			// register a user
			String username = "NEWUSER";
			String password = "123456";
			String[] tags = { "Music", "Literature", "Programming", "Art", "Fashion" };
			ArrayList<String> taglist = new ArrayList<>();
			for (String s : tags) {
				taglist.add(s);
			}
			System.out.print("Registration of new user\n"
					+ "username: " + username
					+ "\npassword: " + password
					+ "\ntags: ");
			for (String tag : tags) {
				System.out.print(tag + " ");
			}
			System.out.println("\n");

			int res = -1;
			try {
				res = stub.register(username, password, taglist);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			System.out.println("Result: " + res);
		} catch (RemoteException | NotBoundException e) {
			System.out.println(e);
		}
	}
}
