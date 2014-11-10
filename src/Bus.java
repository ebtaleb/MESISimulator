import java.util.ArrayList;


public class Bus {
	private ArrayList<Cache> caches;
	private int countMessagesOnBus;

	public Bus(){
		this.caches = new ArrayList();
		this.countMessagesOnBus = 0;
	}

	public ArrayList<Cache> getCaches() {
		return caches;
	}

	public void setCaches(ArrayList<Cache> caches) {
		this.caches = caches;
	}

	public int getCountMessagesOnBus() {
		return countMessagesOnBus;
	}

	public void setCountMessagesOnBus(int countMessagesOnBus) {
		this.countMessagesOnBus = countMessagesOnBus;
	}

    public void addCache(Cache c) {
        caches.add(c);
    }
}
