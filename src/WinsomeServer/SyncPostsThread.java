package WinsomeServer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SyncPostsThread extends Thread {
	private File file;
	private ConcurrentLinkedDeque<Post> posts;
	private ObjectMapper mapper;
	private JsonFactory factory;

	public SyncPostsThread(File outFile, ConcurrentLinkedDeque<Post> postQueue, ObjectMapper objMapper,
			JsonFactory fact) {
		this.file = outFile;
		this.posts = postQueue;
		this.mapper = objMapper;
		this.factory = fact;
	}

	public void run() {
		// Creo nodo array al cui interno inserire i post
		ArrayNode postArr = this.mapper.createArrayNode();
		// Iteratore dall'ultimo al primo elemento per scrivere su file dal post più recente
		// al meno recente, per poi essere caricati all'avvio semplicemente leggendo
		// sequenzialmente il file ed aggiungendo in fondo alla coda i nuovi post letti
		Iterator<Post> iter = this.posts.descendingIterator();
		while (iter.hasNext()) {
			Post p = iter.next();
			ObjectNode postObject = this.mapper.createObjectNode();
			postObject.put("postID", p.getPostID());
			postObject.put("isRewin", p.getIsRewin());
			postObject.put("timestamp", p.getTimestamp());
			postObject.put("age", p.getAge());
			postObject.put("author", p.getAuthor());
			postObject.put("title", p.getTitle());
			postObject.put("content", p.getContent());
			ArrayNode postVotes = postObject.putArray("votes");
			for (Vote v : p.getVotes()) {
				ObjectNode voteObj = this.mapper.createObjectNode();
				voteObj.put("timestamp", v.getTimestamp());
				voteObj.put("voter", v.getVoter());
				voteObj.put("isLike", v.getIsLike());
				postVotes.add(voteObj);
			}
			postObject.set("votes", postVotes);
			ArrayNode postComments = postObject.putArray("comments");
			for (Comment c : p.getComments()) {
				ObjectNode commentObj = this.mapper.createObjectNode();
				commentObj.put("timestamp", c.getTimestamp());
				commentObj.put("author", c.getAuthor());
				commentObj.put("content", c.getContent());
				postComments.add(commentObj);
			}
			postObject.set("comments", postComments);

			postArr.add(postObject);
		}
		try {
			// Scrivo sul file l'array di Post serializzati
			JsonGenerator gen = this.factory.createGenerator(this.file, JsonEncoding.UTF8);
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			gen.useDefaultPrettyPrinter();
			mapper.writeTree(gen, postArr);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
