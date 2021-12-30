package Winsome.WinsomeServer;

/**
 * Classe che incapsula lo stato di una transazione all'interno di Winsome
 */
public class Transaction {
	private long timestamp;
	private double amount;

	public Transaction() {
		this.timestamp = 0;
		this.amount = 0;
	}

	public Transaction(long time, double value) {
		this.timestamp = time;
		this.amount = value;
	}

	// Getters & setters
	public long getTimestamp() {
		return this.timestamp;
	}

	public double getAmount() {
		return this.amount;
	}

	public void setTimestamp(long time) {
		this.timestamp = time;
	}

	public void setAmount(double value) {
		this.amount = value;
	}
}
