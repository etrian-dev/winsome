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
	public static <T> Task<T> parseRequest(
			WinsomeServer serv, SelectionKey selKey, ObjectMapper mapper) {
		SocketChannel channel = (SocketChannel) selKey.channel();
		ByteBuffer bb = ByteBuffer.allocate(ServerMain.BUFSZ);
		try {
			long nread = channel.read(bb);
			// TODO: handle incomplete reads
			Request r = mapper.readValue(bb.array(), Request.class);
			switch (r.getKind()) {
				case "Login":
					LoginRequest lr = (LoginRequest) r;
					LoginTask<T> loginTask = new LoginTask<>(lr.getUsername(), lr.getPassword(),
							serv.getUsers());
					loginTask.setValid();
					return loginTask;
				default:
					Task<T> task = new Task<>();
					task.setMessage("Tipo di richiesta " + r.getKind() + " sconosciuto");
					task.setInvalid();
					return task;
			}
		} catch (IOException ioExc) {
			Task<T> task = new Task<>();
			task.setMessage(ioExc.getMessage());
			task.setInvalid();
			return task;
		}
	}
}
