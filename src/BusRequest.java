
public class BusRequest {

	private int cache_id;
	private Transaction transaction;
	private int address;
	private int cycles_left;

	public BusRequest(int cache_id, Transaction transaction, int address, int cycles) {
		this.cache_id = cache_id;
		this.transaction = transaction;
		this.address = address;
		this.cycles_left = cycles;
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
	
	public void decrementCyclesLeft(){
		this.cycles_left--;
		return;
	}
	
	public int getCyclesLeft(){
		return this.cycles_left;
	}
	
	public String toString() {
		String s = "";
		s += "Request is, proc :" + cache_id + ", transaction: " + transaction.toString() + ", address: " + Integer.toHexString(address) + ", " +  cycles_left + " cycles left";
		return s;
	}
}
