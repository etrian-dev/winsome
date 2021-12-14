package WinsomeServer;

public class RequestParser {
	public class Request {
		private final long userID;
		private final String request;

		public Request() {
			this.userID = -1;
			this.request = null;
		}

		public Request(Long uID, String req) {
			this.userID = uID;
			this.request = req;
		}
	}
}
