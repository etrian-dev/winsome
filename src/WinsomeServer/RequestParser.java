package WinsomeServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.fasterxml.jackson.databind.ObjectMapper;

import WinsomeRequests.LoginRequest;
import WinsomeRequests.Request;
import WinsomeTasks.LoginTask;
import WinsomeTasks.Task;

/**
 * Classe che realizza il parsing delle richieste dei client
 */
public class RequestParser {
	public static Task parseRequest(
			WinsomeServer serv, SelectionKey selKey, ObjectMapper mapper) {
		SocketChannel channel = (SocketChannel) selKey.channel();
		ByteBuffer bb = ByteBuffer.allocate(ServerMain.BUFSZ);
		try {
			long nread = channel.read(bb);
			System.out.println("Read " + nread + " bytes, string:\n" + new String(bb.array()));
			// TODO: handle incomplete reads
			Request r = mapper.readValue(bb.array(), Request.class);
			System.out.println("Req = " + r.getKind());
			switch (r.getKind()) {
				case "Login":
					LoginRequest lr = mapper.readValue(bb.array(), LoginRequest.class);
					LoginTask loginTask = new LoginTask(lr.getUsername(), lr.getPassword(),
							serv.getUsers());
					loginTask.setValid();
					System.out.println("Task parsed:\n" + loginTask);
					return loginTask;
				default:
					Task task = new Task();
					task.setMessage("Tipo di richiesta " + r.getKind() + " sconosciuto");
					task.setInvalid();
					System.out.println("Task parsed:\n" + task);
					return task;
			}
		} catch (IOException ioExc) {
			ioExc.printStackTrace();
			System.out.println(ioExc);
			Task task = new Task();
			task.setMessage(ioExc.getMessage());
			task.setInvalid();
			System.out.println("Task parsed:\n" + task);
			return task;
		}
	}
}
