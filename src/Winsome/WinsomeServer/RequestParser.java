package Winsome.WinsomeServer;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Winsome.WinsomeRequests.BlogRequest;
import Winsome.WinsomeRequests.CommentRequest;
import Winsome.WinsomeRequests.CreatePostRequest;
import Winsome.WinsomeRequests.DeletePostRequest;
import Winsome.WinsomeRequests.FollowRequest;
import Winsome.WinsomeRequests.ListRequest;
import Winsome.WinsomeRequests.LoginRequest;
import Winsome.WinsomeRequests.LogoutRequest;
import Winsome.WinsomeRequests.QuitRequest;
import Winsome.WinsomeRequests.RateRequest;
import Winsome.WinsomeRequests.Request;
import Winsome.WinsomeRequests.RewinRequest;
import Winsome.WinsomeRequests.ShowPostRequest;
import Winsome.WinsomeRequests.WalletRequest;
import Winsome.WinsomeTasks.BlogTask;
import Winsome.WinsomeTasks.CommentTask;
import Winsome.WinsomeTasks.CreatePostTask;
import Winsome.WinsomeTasks.DeletePostTask;
import Winsome.WinsomeTasks.FollowTask;
import Winsome.WinsomeTasks.ListTask;
import Winsome.WinsomeTasks.LoginTask;
import Winsome.WinsomeTasks.LogoutTask;
import Winsome.WinsomeTasks.MulticastTask;
import Winsome.WinsomeTasks.QuitTask;
import Winsome.WinsomeTasks.RateTask;
import Winsome.WinsomeTasks.RewinTask;
import Winsome.WinsomeTasks.ShowFeedTask;
import Winsome.WinsomeTasks.ShowPostTask;
import Winsome.WinsomeTasks.Task;
import Winsome.WinsomeTasks.WalletTask;

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
		ByteBuffer bb = cd.getReadBuffer();

		try {
			long nread = channel.read(bb);
			// Se la read fallisce chiudo il socket
			if (nread == -1) {
				channel.close();
				return null;
			}
			// FIXME: print of raw socket read just for testing
			//System.out.println("Read " + nread + " bytes, string:\n" + new String(bb.array()));
			// La richiesta è deserializzata nel supertipo request, poi in base al tipo
			// eseguo il casting al sottotipo appropriato
			Request r = mapper.readValue(bb.array(), Request.class);
			switch (r.getKind()) {
				case "Multicast": {
					// Richiesta dell'indirizzo IP multicast
					MulticastTask mt = new MulticastTask(mapper, serv.getConfig());
					mt.setValid();
					cd.resetReadBuffer();
					return mt;
				}
				case "Login": {
					// Richiesta di login
					LoginRequest lr = mapper.readValue(bb.array(), LoginRequest.class);
					LoginTask lt = new LoginTask(lr.getUsername(), lr.getPassword(), cd, serv);
					lt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return lt;
				}
				case "Logout": {
					// Richiesta di logout
					LogoutRequest lr = mapper.readValue(bb.array(), LogoutRequest.class);
					LogoutTask lt = new LogoutTask(lr.getUsername(), cd, serv);
					lt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return lt;
				}
				case "List": {
					ListRequest lr = mapper.readValue(bb.array(), ListRequest.class);
					ListTask lt = new ListTask(lr.getSender(), lr.getEntity(), mapper, serv);
					lt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return lt;
				}
				case "Follow": {
					// Richiesta di follow/unfollow
					FollowRequest fr = mapper.readValue(bb.array(), FollowRequest.class);
					FollowTask ft = new FollowTask(fr.getFollower(), fr.getFollowed(), fr.getType(), serv);
					ft.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return ft;
				}
				case "CreatePost": {
					CreatePostRequest pr = mapper.readValue(bb.array(), CreatePostRequest.class);
					CreatePostTask pt = new CreatePostTask(pr.getAuthor(), pr.getTitle(), pr.getContent(), serv);
					pt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return pt;
				}
				case "DeletePost": {
					DeletePostRequest dp = mapper.readValue(bb.array(), DeletePostRequest.class);
					DeletePostTask pt = new DeletePostTask(dp.getPostID(), cd.getCurrentUser(), serv);
					pt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return pt;
				}
				case "ShowPost": {
					ShowPostRequest sp = mapper.readValue(bb.array(), ShowPostRequest.class);
					ShowPostTask pt = new ShowPostTask(sp.getPostID(), cd.getCurrentUser(), mapper, serv);
					pt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return pt;
				}
				case "CommentPost": {
					CommentRequest sp = mapper.readValue(bb.array(), CommentRequest.class);
					CommentTask ct = new CommentTask(sp.getPostID(), sp.getComment(), cd.getCurrentUser(), serv);
					ct.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return ct;
				}
				case "RatePost": {
					RateRequest rr = mapper.readValue(bb.array(), RateRequest.class);
					RateTask rt = new RateTask(rr.getPostID(), rr.getVote(), cd.getCurrentUser(), serv);
					rt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return rt;
				}
				case "RewinPost": {
					RewinRequest rr = mapper.readValue(bb.array(), RewinRequest.class);
					RewinTask rt = new RewinTask(rr.getPostID(), cd.getCurrentUser(), serv);
					rt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return rt;
				}
				case "ShowFeed": {
					ShowFeedTask ft = new ShowFeedTask(cd.getCurrentUser(), mapper, serv);
					ft.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return ft;
				}
				case "Blog": {
					BlogRequest br = mapper.readValue(bb.array(), BlogRequest.class);
					BlogTask bt = new BlogTask(br.getUsername(), cd.getCurrentUser(), mapper, serv);
					bt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return bt;
				}
				case "Quit": {
					QuitRequest qr = mapper.readValue(bb.array(), QuitRequest.class);
					QuitTask qt = new QuitTask(qr.getUsername());
					qt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return qt;
				}
				case "Wallet": {
					WalletRequest wr = mapper.readValue(bb.array(), WalletRequest.class);
					WalletTask wt = new WalletTask(wr.getUsername(), wr.getConvert(), cd, mapper, serv);
					wt.setValid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return wt;
				}
				default:
					Task task = new Task();
					task.setMessage("Tipo di richiesta " + r.getKind() + " sconosciuto");
					task.setInvalid();
					// read completa: resetto ByteBuffer
					cd.resetReadBuffer();
					return task;
			}
		} catch (JsonMappingException mapEx) {
			// read incompleta: non forma una task
			return null;
		} catch (IOException ioExc) {
			System.err.println("Eccezione parsing richiesta: " + ioExc.getMessage());
			Task task = new Task();
			task.setMessage(ioExc.getMessage());
			task.setInvalid();
			return task;
		}
	}
}
