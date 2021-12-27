package WinsomeServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import WinsomeRequests.BlogRequest;
import WinsomeRequests.CommentRequest;
import WinsomeRequests.CreatePostRequest;
import WinsomeRequests.DeletePostRequest;
import WinsomeRequests.FollowRequest;
import WinsomeRequests.ListRequest;
import WinsomeRequests.LoginRequest;
import WinsomeRequests.LogoutRequest;
import WinsomeRequests.QuitRequest;
import WinsomeRequests.RateRequest;
import WinsomeRequests.Request;
import WinsomeRequests.RewinRequest;
import WinsomeRequests.ShowPostRequest;
import WinsomeTasks.BlogTask;
import WinsomeTasks.CommentTask;
import WinsomeTasks.CreatePostTask;
import WinsomeTasks.DeletePostTask;
import WinsomeTasks.FollowTask;
import WinsomeTasks.ListTask;
import WinsomeTasks.LoginTask;
import WinsomeTasks.LogoutTask;
import WinsomeTasks.QuitTask;
import WinsomeTasks.RateTask;
import WinsomeTasks.RewinTask;
import WinsomeTasks.ShowFeedTask;
import WinsomeTasks.ShowPostTask;
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
		// Buffer pieno: raddoppio capacità
		if (!bb.hasRemaining()) {
			ByteBuffer newBB = ByteBuffer.allocate(bb.capacity() * 2);
			newBB.put(bb);
			bb = newBB;
		}
		try {
			long nread = channel.read(bb);
			// Se la read fallisce chiudo il socket
			if (nread == -1) {
				channel.close();
				return null;
			}
			// FIXME: print of raw socket read just for testing
			System.out.println("Read " + nread + " bytes, string:\n" + new String(bb.array()));
			// La richiesta è deserializzata nel supertipo request, poi in base al tipo
			// eseguo il casting al sottotipo appropriato
			Request r = mapper.readValue(bb.array(), Request.class);
			switch (r.getKind()) {
				case "Login": {
					// Richiesta di login
					LoginRequest lr = mapper.readValue(bb.array(), LoginRequest.class);
					LoginTask lt = new LoginTask(lr.getUsername(), lr.getPassword(), cd, serv);
					lt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return lt;
				}
				case "Logout": {
					// Richiesta di logout
					LogoutRequest lr = mapper.readValue(bb.array(), LogoutRequest.class);
					LogoutTask lt = new LogoutTask(lr.getUsername(), cd, serv);
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
				case "DeletePost": {
					DeletePostRequest dp = mapper.readValue(bb.array(), DeletePostRequest.class);
					DeletePostTask pt = new DeletePostTask(dp.getPostID(), cd.getCurrentUser(), serv);
					pt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return pt;
				}
				case "ShowPost": {
					ShowPostRequest sp = mapper.readValue(bb.array(), ShowPostRequest.class);
					ShowPostTask pt = new ShowPostTask(sp.getPostID(), cd.getCurrentUser(), serv);
					pt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return pt;
				}
				case "CommentPost": {
					CommentRequest sp = mapper.readValue(bb.array(), CommentRequest.class);
					CommentTask ct = new CommentTask(sp.getPostID(), sp.getComment(), cd.getCurrentUser(), serv);
					ct.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return ct;
				}
				case "RatePost": {
					RateRequest rr = mapper.readValue(bb.array(), RateRequest.class);
					RateTask rt = new RateTask(rr.getPostID(), rr.getVote(), cd.getCurrentUser(), serv);
					rt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return rt;
				}
				case "RewinPost": {
					RewinRequest rr = mapper.readValue(bb.array(), RewinRequest.class);
					RewinTask rt = new RewinTask(rr.getPostID(), cd.getCurrentUser(), serv);
					rt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return rt;
				}
				case "ShowFeed": {
					ShowFeedTask ft = new ShowFeedTask(cd.getCurrentUser(), serv);
					ft.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return ft;
				}
				case "Blog": {
					BlogRequest br = mapper.readValue(bb.array(), BlogRequest.class);
					BlogTask bt = new BlogTask(br.getUsername(), cd.getCurrentUser(), serv);
					bt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return bt;
				}
				case "Quit": {
					QuitRequest qr = mapper.readValue(bb.array(), QuitRequest.class);
					QuitTask qt = new QuitTask(qr.getUsername());
					qt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetBuffer();
					return qt;
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
