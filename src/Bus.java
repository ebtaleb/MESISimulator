import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

public class Bus {

	private ArrayList<Cache> caches;
	private Queue<BusRequest> message_queue;
	private String protocol;
	private boolean uniproc_flag;
	private BusRequest curr_request;
	private int bus_traffic;
	private boolean occupied_bus_flag;
	private PrintStream output_file;

	public Bus(String p, boolean flag, String out_file) throws FileNotFoundException {
		
		this.caches = new ArrayList<>();
		message_queue = new ArrayDeque<BusRequest>();
		protocol = p;
		bus_traffic = 0;
		occupied_bus_flag = false;
		uniproc_flag = flag;
		
		File file = new File(out_file);
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		output_file = ps;
	}
	
	public PrintStream getStream() {
		return output_file;
	}

	public ArrayList<Cache> getCaches() {
		return caches;
	}

	public void setCaches(ArrayList<Cache> caches) {
		this.caches = caches;
	}

	public int getCountMessagesOnBus() {
		return message_queue.size() + 1;
	}

    public void addCache(Cache c) {
        caches.add(c);
    }
    
    public boolean isOccupied() {
    	return occupied_bus_flag;
    }
    
    public int getBusTraffic(){
    	return this.bus_traffic;
    }
    
    public BusRequest getCurrRequest(){
    	return this.curr_request;
    }

    public void runCacheProtocol() {
    	BusRequest br = this.curr_request;  
    	int cache_id = br.getCache_id();
    	Cache target_cache = caches.get(cache_id);
    	CacheLine block = target_cache.getCacheBlock(br.getAddress());

//    	System.out.println("Bus runCacheProtocol: block is "+block.toString());
//    	System.out.println("Bus runCacheProtocol: transaction is "+br.getTransaction().toString());
    	//System.out.println("Bus runCacheProtocol: protocol is "+protocol);
    	//System.out.println("Bus runCacheProtocol: uniproc_flag is "+uniproc_flag);

    	if(!uniproc_flag) {
	        switch(br.getTransaction()){
		        case BusRd:
		        	
		        	if (protocol.compareTo("MSI") == 0) {
	
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
		                //return;
		            }
	
		            if (protocol.compareTo("MESI") == 0) {
	
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
		                //return;
		            }
		            this.bus_traffic += target_cache.getBlockSize();
		        	break;
		        case BusRdX:
		        	//System.out.println("Bus runCacheProtocol: in switch case BusRdX, protocol is "+protocol);

		        	if (protocol.compareTo("MSI") == 0) {
	
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
		                //return;
		            }
	
		            if (protocol.compareTo("MESI") == 0) {
		            	//System.out.println("Bus runCacheProtocol: in switch case BusRdX with MESI. block state is "+block.getState());
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
//		                    	System.out.println("Bus runCacheProtocol: MESI, BusRdX, Invalid. About to run snooping");
		                    	for (Cache cache : caches) {
									cache.busSnoop(this.protocol);
								}
		                        break;
		                    default:
//		                    	System.out.println("Bus runCacheProtocol: in default case for BusRdX");
		                        break;
		                }
		                //return;
		            }
		        	this.bus_traffic += target_cache.getBlockSize();
		        	break;
		        case BusUpd:
		        	
		        	break;
		    	default:
//		    		System.out.println("Bus runCacheProtocol: in default case for BusRdX 1");
		    		break;
	        }
    	}//end if not uniproc 
    	else {
    		this.bus_traffic += target_cache.getBlockSize();
    		//System.out.println("Bus runCacheProtocol: uniproc_flag is true");
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
    }
    
    public void processBusRequests(){
    	
    	System.setOut(output_file);
//    	System.out.println("Bus processBusRequests():" +toString());

    	if (curr_request == null && message_queue.isEmpty() == true) {
    		return;
    	}
    	
    	if (curr_request == null && message_queue.isEmpty() == false) {
//    		System.out.println("Bus processBusRequests(): size of message queue is "+ message_queue.size());
    		curr_request = (BusRequest) this.message_queue.remove();
    		occupied_bus_flag = true;
    	}
    	
    	if (this.curr_request.getCyclesLeft() == 0) {
//    		System.out.println("cycles left is now 0");
    		runCacheProtocol(); 
//    		System.out.println("ran cache protocol");
    		caches.get(this.curr_request.getCache_id()).setPendingBusRequest(false);
//    		curr_request = null;
//        	occupied_bus_flag = false;
    		if(message_queue.isEmpty() == false){
        		curr_request = (BusRequest) this.message_queue.remove();
        		occupied_bus_flag = true;
    		}
    		else {
        		curr_request = null;
            	occupied_bus_flag = false;
    		}
    	}
    	else {
    		this.curr_request.decrementCyclesLeft();
    	}
    	return;
    }
    
    public String toString() {
    	String s = "";
    	if (curr_request != null)
    		s += "Current request: " + curr_request.toString() + "\n";
    	
    	if (!message_queue.isEmpty()) {
	    	Iterator<BusRequest> it = message_queue.iterator();
	    	for (BusRequest i = it.next(); it.hasNext() ; i = it.next()) {
	    		s += i.toString() + "\n";
	    	}
    	}
    	
    	return s;
    }
    
    public boolean emptyBus() {
    	return message_queue.isEmpty() && curr_request == null;
    }
}
