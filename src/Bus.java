import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Bus {

	private ArrayList<Cache> caches;
	private int countMessagesOnBus;
	private Queue message_queue;
	private String protocol;
	private BusRequest curr_request;

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
    
    public void runCacheProtocol(BusRequest br) {
    	
    	int cache_id = br.getCache_id();
    	Cache target_cache = caches.get(cache_id);
    	CacheLine block = target_cache.getCacheBlock(br.getAddress());
    	
        switch(br.getTransaction()){
	        case BusRd:
	        	//get state of block that made this trxn
	        	//if state was M, go to 
	        	//if state was E, remain in E. 
	        	//if state was S, remain in S. other S dont do anything. other in M flush and goes to S
	        	//if state was I, go to S, others in S don't do anything. other in M flush and goes to S
	        	
	        	if (protocol == "MSI") {

	                switch (block.getState()) {
	                    case MODIFIED:
	                        break;
	                    case SHARED:
	                        break;
	                    case INVALID:
	                        break;
	                    default:
	                        break;
	                }
	                return;
	            }

	            if (protocol == "MESI") {

	                switch (block.getState()) {
	                    case MODIFIED:
	                        break;
	                    case EXCLUSIVE:
	                        break;
	                    case SHARED:
	                        break;
	                    case INVALID:
	                        break;
	                    default:
	                        break;
	                }
	                return;
	            }
	        	break;
	        case BusRdX:
	        	//get state of block that made this trxn
	        	//if state was M, no need to to anything cos the others are already I and you remain in M
	        	//if state was E, go to M, rest go to I
	        	//if state was S, go to M, rest go to I
	        	//if state was I, go to M, rest go to I
	        	break;
	    	default:
	    		break;
        }
    }
    
    public void enqueueRequest(BusRequest br){
    	this.message_queue.add(br);
    	return;
    }
    
    public void processBusRequests(){
    	if (curr_request == null) {
    		return;
    	}
    	if(this.curr_request.getCyclesLeft() == 0) {
    		runCacheProtocol(this.curr_request); 
    		this.curr_request = (BusRequest) this.message_queue.remove();
        	this.curr_request.decrementCyclesLeft();
    	}
    	else {
    		this.curr_request.decrementCyclesLeft();
    	}
    	return;
    }
}
