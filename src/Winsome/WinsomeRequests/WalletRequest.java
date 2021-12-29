package Winsome.WinsomeRequests;

/** Richiesta del valore del wallet, opzionalmente convertito in bitcoin */
public class WalletRequest extends Request {
	String username;
	boolean convert; // flag per indicare conversione in btc

	public WalletRequest() {
		super.setKind("Wallet");
		this.username = null;
		// di default viene restituito il valore in Wincoin; se true restituito valore in BTC
		this.convert = false;
	}

	public WalletRequest(String user, boolean btcResult) {
		super.setKind("Wallet");
		this.username = user;
		this.convert = btcResult;
	}

	public String getUsername() {
		return this.username;
	}

	public boolean getConvert() {
		return this.convert;
	}

	public void setUsername(String user) {
		this.username = user;
	}

	public void setConvert(boolean isConvert) {
		this.convert = isConvert;
	}
}
