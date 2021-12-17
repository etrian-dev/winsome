package WinsomeRequests;

import java.io.Serializable;

/**
 * Classe astratta che definisce una generica richiesta del client al server.
 * 
 * Le sottoclassi di Requests specificheranno il tipo di richiesta ed il contenuto
 */
public abstract class Request implements Serializable {
	public static final long SerialVersionUID = 1L;

	private String kind;

	public String getKind() {
		return this.kind;
	}

	public void setKind(String rkind) {
		if (rkind != null)
			this.kind = rkind;
	}

	@Override
	public String toString() {
		return "=== Request ===\nKind: " + this.getKind();
	}
}
