
public class BusRequest {

	private int cache_id;
	private Transaction transaction;
	private int address;
	private int ins_type;

	public BusRequest(int cache_id, Transaction transaction, int address) {
		this.cache_id = cache_id;
		this.transaction = transaction;
		this.address = address;
	}

	public int getCache_id() {
		return cache_id;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public int getAddress() {
		return address;
	}
}
