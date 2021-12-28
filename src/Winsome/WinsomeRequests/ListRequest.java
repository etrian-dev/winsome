package Winsome.WinsomeRequests;

public class ListRequest extends Request {
	String sender;
	String entity;

	public ListRequest() {
		super.setKind("List");
		this.sender = null;
		this.entity = null;
	}

	public ListRequest(String user, String subcommand) {
		super.setKind("List");
		this.sender = user;
		this.entity = subcommand;
	}

	public String getSender() {
		return this.sender;
	}

	public String getEntity() {
		return this.entity;
	}

	public boolean setSender(String sender) {
		if (sender == null) {
			return false;
		}
		this.sender = sender;
		return true;
	}

	public boolean setEntity(String cmd) {
		if (cmd == null) {
			return false;
		}
		this.entity = cmd;
		return true;
	}
}
