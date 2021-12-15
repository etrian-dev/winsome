package WinsomeClient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import WinsomeServer.Signup;

public class ClientMain {
	public static final String QUIT_COMMAND = "quit";
	public static final String USER_PROMPT = "$ ";

	public static void main(String[] args) {
		try {
			Registry reg = LocateRegistry.getRegistry(11111);
			System.out.println("Hello, server");
			Signup stub = (Signup) reg.lookup("register");
			try (Scanner scan = new Scanner(System.in);) {
				while (true) {
					System.out.print(ClientMain.USER_PROMPT);
					if (!scan.hasNextLine()) {
						break;
					}

					String command = scan.nextLine();
					String[] tokens = command.split(" ");

					if (tokens[0].equals(ClientMain.QUIT_COMMAND)) {
						break;
					}

					// FIXME: check lenght of tokens (lenght 1 crashes at line 46)

					List<String> all_tags = new ArrayList<>();
					for (int i = 3; i < tokens.length; i++) {
						all_tags.add(tokens[i]);
					}

					int res = -1;
					try {
						res = stub.register(tokens[1], tokens[2], all_tags);
						System.out.println("Result: " + res);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			} catch (NoSuchElementException end) {
				System.out.println("Quitting client...");
			}

		} catch (RemoteException | NotBoundException e) {
			System.out.println(e);
		}
	}
}
