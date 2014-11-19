import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Bus {

	private ArrayList<Cache> caches;
	private int countMessagesOnBus;
	private Queue message_queue;
	private String protocol;
	private BusRequest curr_request;
	private int bus_traffic;

	public Bus(String p) {
		this.caches = new ArrayList<>();
		this.countMessagesOnBus = 0;
		message_queue = new ArrayDeque<BusRequest>();
		protocol = p;
		this.bus_traffic = 0;
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
    
    public int getBusTraffic(){
    	return this.bus_traffic;
    }
    
    public BusRequest getCurrRequest(){
    	return this.curr_request;
    }
    
    public void runCacheProtocol(BusRequest br) {
    	
    	int cache_id = br.getCache_id();
    	Cache target_cache = caches.get(cache_id);
    	CacheLine block = target_cache.getCacheBlock(br.getAddress());
    	
        switch(br.getTransaction()){
	        case BusRd:
	        	
	        	if (protocol == "MSI") {

	                switch (block.getState()) {
	                    case MODIFIED:
	                    	//will never get this case but here for sake of completeness
	                        break;
	                    case SHARED:
	                    	//will never get this case cos block in cache that's why in S. so cache hit. so will not generate BusRd
	                    	//I remain in S, others in I remain in I, other in M flushes
	                        break;
	                    case INVALID:
	                    	//I go to S, others in S remain in S, other in M flushes
	                    	for (Cache cache : caches) {
								cache.busSnoop(this.protocol);
							}
	                        break;
	                    default:
	                        break;
	                }
	                return;
	            }

	            if (protocol == "MESI") {

	                switch (block.getState()) {
	                    case MODIFIED:
	                    	//will never get this case but here for sake of completeness
	                        break;
	                    case EXCLUSIVE:
	                    	//will never get this case but here for sake of completeness (cos block alr in cache that's why in E state)
	                        break;
	                    case SHARED:
	                    	//will never get this case but here for sake of completeness
	                        break;
	                    case INVALID:
	                    	for (Cache cache : caches) {
								cache.busSnoop(this.protocol);
							}
	                        break;
	                    default:
	                        break;
	                }
	                return;
	            }
	            this.bus_traffic += target_cache.getBlockSize();
	        	break;
	        case BusRdX:
	        	if (protocol == "MSI") {

	                switch (block.getState()) {
	                    case MODIFIED:
	                    	//will never get this case but here for sake of completeness
	                        break;
	                    case SHARED:
	                    	//generates BusRdX if sees a PrWr. other in M will flush I go to M, others go to I
	                    	target_cache.busSnoop(this.protocol);
	                    	invalidateOthers(br);
	                        break;
	                    case INVALID:
	                    	//generates BusRdX if sees a PrWr. other M will flush. I go to M, others go to I
	                    	target_cache.busSnoop(this.protocol);
	                    	invalidateOthers(br);
	                        break;
	                    default:
	                        break;
	                }
	                return;
	            }

	            if (protocol == "MESI") {

	                switch (block.getState()) {
	                    case MODIFIED:
	                    	//will never get this case but here for sake of completeness
	                        break;
	                    case EXCLUSIVE:
	                    	//only change this cache's state because the rest are all invalid anyway
	                    	target_cache.busSnoop(this.protocol);
	                        break;
	                    case SHARED:
	                    	target_cache.busSnoop(this.protocol);
	                    	invalidateOthers(br);
	                        break;
	                    case INVALID:
	                    	for (Cache cache : caches) {
								cache.busSnoop(this.protocol);
							}
	                        break;
	                    default:
	                        break;
	                }
	                return;
	            }
	        	this.bus_traffic += target_cache.getBlockSize();
	        	break;
	    	default:
	    		break;
        }
    }
    
    public void invalidateOthers(BusRequest br){
    	int cache_id = br.getCache_id();
    	for (Cache cache : caches) {
    		CacheLine block = cache.getCacheBlock(br.getAddress());
			if(cache.getCacheID() != cache_id && block != null){ //if this is another cache and has the block, invalidate
				block.setState(State.INVALID);
			}
			else {
				//don't bother because 
				//1) it is the cache that gen bus trxn OR
				//2) it is another cache which doesn't have the block
			}
		}
    }
    
	public boolean doMultipleCachesHaveBlock(int address, int cache_id) {
		int count = 0;
		for (Cache cache : caches) {
			CacheLine block = cache.getCacheBlock(address);
			if(cache.getCacheID() != cache_id && block != null){
				count ++;
			}
		}
		if(count > 0)
			return true;
		else 
			return false;
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
