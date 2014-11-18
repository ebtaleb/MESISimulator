import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Bus {

	private ArrayList<Cache> caches;
	private int countMessagesOnBus;
	private Queue message_queue;
	private String protocol;

	public Bus(String p) {
		this.caches = new ArrayList<>();
		this.countMessagesOnBus = 0;
		message_queue = new ArrayDeque<BusRequest>();
		protocol = p;
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
