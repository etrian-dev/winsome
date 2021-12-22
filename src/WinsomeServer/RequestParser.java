package WinsomeServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import WinsomeRequests.CreatePostRequest;
import WinsomeRequests.FollowRequest;
import WinsomeRequests.ListRequest;
import WinsomeRequests.LoginRequest;
import WinsomeRequests.LogoutRequest;
import WinsomeRequests.Request;
import WinsomeTasks.CreatePostTask;
import WinsomeTasks.FollowTask;
import WinsomeTasks.ListTask;
import WinsomeTasks.LoginTask;
import WinsomeTasks.LogoutTask;
import WinsomeTasks.Task;

/**
 * Classe che realizza il parsing delle richieste dei client
 */
public class RequestParser {
	public static Task parseRequest(
			WinsomeServer serv, SelectionKey selKey, ObjectMapper mapper) {
		// Ottengo il riferimento al channel da cui leggere
		SocketChannel channel = (SocketChannel) selKey.channel();
		// Ottengo l'attachment con i dati di questo client
		ClientData cd = (ClientData) selKey.attachment();
		ByteBuffer bb = cd.getBuffer();
		// Buffer pieno: raddoppio
		if (!bb.hasRemaining()) {
			ByteBuffer newBB = ByteBuffer.allocate(bb.capacity() * 2);
			newBB.put(bb);
			bb = newBB;
		}
		try {
			long nread = channel.read(bb);
			// TODO: quit request to close the SocketChannel
			if (nread == -1) {
				channel.close();
				return null;
			}
			System.out.println("Read " + nread + " bytes, string:\n" + new String(bb.array()));
			Request r = mapper.readValue(bb.array(), Request.class);
			System.out.println(r.getKind());
			switch (r.getKind()) {
				case "Login": {
					// Richiesta di login
					LoginRequest lr = mapper.readValue(bb.array(), LoginRequest.class);
					LoginTask lt = new LoginTask(lr.getUsername(), lr.getPassword(),
							serv);
					lt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return lt;
				}
				case "Logout": {
					// Richiesta di logout
					LogoutRequest lr = mapper.readValue(bb.array(), LogoutRequest.class);
					LogoutTask lt = new LogoutTask(lr.getUsername(), serv);
					lt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return lt;
				}
				case "List": {
					ListRequest lr = mapper.readValue(bb.array(), ListRequest.class);
					ListTask lt = new ListTask(lr.getSender(), lr.getEntity(), serv);
					lt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return lt;
				}
				case "Follow": {
					// Richiesta di follow/unfollow
					FollowRequest fr = mapper.readValue(bb.array(), FollowRequest.class);
					FollowTask ft = new FollowTask(fr.getFollower(), fr.getFollowed(), fr.getType(), serv);
					ft.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return ft;
				}
				case "CreatePost": {
					CreatePostRequest pr = mapper.readValue(bb.array(), CreatePostRequest.class);
					CreatePostTask pt = new CreatePostTask(pr.getAuthor(), pr.getTitle(), pr.getContent(), serv);
					pt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return pt;
				}
				default:
					Task task = new Task();
					task.setMessage("Tipo di richiesta " + r.getKind() + " sconosciuto");
					task.setInvalid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return task;
			}
		} catch (JsonMappingException mapEx) {
			mapEx.printStackTrace();
			// read incompleta: non forma una task
			return null;
		} catch (IOException ioExc) {
			ioExc.printStackTrace();
			System.err.println(ioExc);
			Task task = new Task();
			task.setMessage(ioExc.getMessage());
			task.setInvalid();
			return task;
		}
	}
}
