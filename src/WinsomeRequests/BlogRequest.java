package WinsomeRequests;

/**
 * Classe che implementa la richiesta dei post del proprio blog
 */
public class BlogRequest extends Request {
	private String username;

	public BlogRequest() {
		super.setKind("Blog");
	}

	public BlogRequest(String user) {
		super.setKind("Blog");
		this.username = user;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String user) {
		this.username = user;
	}

}
