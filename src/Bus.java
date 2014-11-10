
public class Bus {
	private Cache[] caches;
	private int countMessagesOnBus;
	
	public Bus(Cache[] caches){
		this.caches = caches;
		this.countMessagesOnBus = 0;
	}

	public Cache[] getCaches() {
		return caches;
	}

	public void setCaches(Cache[] caches) {
		this.caches = caches;
	}

	public int getCountMessagesOnBus() {
		return countMessagesOnBus;
	}

	public void setCountMessagesOnBus(int countMessagesOnBus) {
		this.countMessagesOnBus = countMessagesOnBus;
	}
	
}
