
public class Cache {

	private int cache_id;
	public int cache_size;
	public int block_size;
	public int associativity;

	private int countCacheMiss;
	private int countCacheHit;
	
	private Bus bus; //access to bus

	// each set has n blocks for associativity = n
	private CacheSet[] cache_sets;
	
    // Each processor can only have 1 pending instruction in bus request queue
	// if there is already a pending request in bus. set flag = true when enqueue. 
	// set flag = false when cache checks that bus has completed a transaction already in the bus queue
	private boolean pending_bus_request;
	
	private boolean uniproc_flag;

	public Cache(int cache_id, int cache_size, int associativity, int block_size, Bus bus, boolean f){
		this.cache_id = cache_id;
		this.cache_size = cache_size;
		this.associativity = associativity;
		this.block_size = block_size;
		this.bus = bus;
		this.countCacheMiss = 0;
		this.countCacheHit = 0;
		this.cache_sets = new CacheSet[cache_size/(block_size*associativity)];
		uniproc_flag = f;
		this.pending_bus_request = false;

		for (int i = 0; i < cache_sets.length; i++) {
			cache_sets[i] = new CacheSet(i, this.associativity, -1, State.INVALID, -1);
		}
	}
	
	public int getCacheHits(){
		return countCacheHit;
	}
	
	public int getCacheMisses(){
		return countCacheMiss;
	}
	
	public int getBlockSize(){
		return this.block_size;
	}
	
	public int getCacheID(){
		return this.cache_id;
	}

	public int getNoBlocks() {
		//#Blocks = CacheSize / BlockSize
		return this.cache_size / this.block_size;
	}

	public int getNoSets() {
		//#Sets = #Blocks / #Associativity
		//16 sets for assoc 4
		//System.out.println("number of sets is "+ getNoBlocks() / this.associativity);
		return getNoBlocks() / this.associativity;
	}

	private int getOffsetBits() {
		//eg. 16 byte block size --> log16/log2 = 4 bits
		//for 64 byte block, 6 bits
		int no_bits = (int) (Math.log(this.block_size) / Math.log(2));
		
		//System.out.println("no. of offset bits = "+no_bits);
		return no_bits;
	}

	private int getOffset(int address) {
		int bitmask = 0;
		for (int i = 0; i < getOffsetBits(); i++ ) {
			bitmask |= (1 << i);
		}
		int offset_result = (address & bitmask);
		
		//System.out.println("address is " + address + " offset is "+offset_result);
		return offset_result;
		
	}

	private int getIndexBits(){ //index = set index
		//eg. 8 sets --> log8/log2 = 3 bits
		//should have 4 bits
		int no_index_bits = (int) (Math.log(getNoSets()) / Math.log(2));
		//System.out.println("no. index bits = "+no_index_bits);
		return no_index_bits;
	}

	private int getIndex(int address){
		int no_times_printed = 0;
		int bitmask = 0;
		for (int i = getOffsetBits(); i < getOffsetBits()+getIndexBits(); i++ ) {
			bitmask |= (1 << i);
		}
		int index = (address & bitmask) >>> getOffsetBits();
		if(no_times_printed > 5){
//			System.out.println("address is " + address + " index is "+index);
			no_times_printed++;
			return index;
		}
		else {
			return index;
		}
		
	}

	private int getTagBits(){
		//assume 32 bit address
		//System.out.println("no. tag bits is "+ 32 - getOffsetBits() - getIndexBits());
		return 32 - getOffsetBits() - getIndexBits();
	}

	private int getTag(int address){
		int bitmask = 0;
		//bitmask = Integer.MAX_VALUE;

		for (int i = getOffsetBits() + getIndexBits(); i < 32; i++ ) {
			bitmask |= (1 << i);
		}
		
		int tag = (address & bitmask) >>> (getIndexBits() + getOffsetBits());
//		System.out.println("tag is "+tag);
//		System.out.println("tag binary is "+ Integer.toBinaryString(tag));
		return tag;

	}
	
	public void setPendingBusRequest(boolean flag){
		this.pending_bus_request = flag;
	}
	
	public CacheLine getCacheBlock(int addr) {
		
    	int index = getIndex(addr);
    	
    	for (int i = 0; i < associativity; i++) {
    		if (cache_sets[index].getCacheLine(i).getAddress() == addr) {
    			return cache_sets[index].getCacheLine(i);
    		}
    	}
		
		return null;
		
	}
	
	public int getCacheBlockIndex(int addr) {
		
    	int index = getIndex(addr);
    	
    	for (int i = 0; i < associativity; i++) {
    		if (cache_sets[index].getCacheLine(i).getAddress() == addr) {
    			return i;
    		}
    	}
		
		return -1;
		
	}

	private boolean isCacheHit(int address){
		// get index and tag from address argument
		// use index to access cache set, retrieve tag and validity
		//
		//Cache hit: (Tag[index] = Tag[memory address]) AND (Valid[index] = TRUE)

		int input_tag = getTag(address);
		int input_index = getIndex(address);
		boolean result_flag = false;
		
		for(int i=0;i<associativity;i++){
			CacheLine block = cache_sets[input_index].getCacheLine(i);
//			System.out.println("Cache isCacheHit(): tag of block address "+ block.getAddrString() +" is " + getTag(address));
//			int address_int = (int) Long.parseLong("00007952", 16);
//			System.out.println("Cache isCacheHit(): tag for address 7952 is " + getTag(address_int));
			if (block.getTag() == input_tag && block.getState() != State.INVALID)  {
				if(uniproc_flag && pending_bus_request){
					result_flag = false;
				}
				else {
//					System.out.println("Cache isCacheHit(): Cache hit! because I want tag "+input_tag+" and my block is "+block.toString());
					result_flag = true;
				}
				//return true;
			} else {
//				System.out.println("Cache isCacheHit(): Cache miss because I want tag "+input_tag+" but my block is "+block.toString());
				result_flag = false;
			}
			if(result_flag)
				return result_flag;
		}
		return false;
	}

	public boolean execute(int[] ins) {

		int addr = ins[1];
		BusRequest new_request = null;

		if (isCacheHit(addr)) {
//			System.out.println("Cache " + cache_id + ": Cache hit!");
			
			if ((getCacheBlock(addr).getState() == State.SHARED) && (ins[0] == Constants.INS_WRITE) ){
	    		new_request = new BusRequest(cache_id, Transaction.BusRdX, addr, 10);
				if (!pending_bus_request) {
//	    			System.out.println("request to bus is "+ new_request.toString());
	    			bus.enqueueRequest(new_request);
	    			this.pending_bus_request = true;
				} else {
//					System.out.println("Cache" + cache_id +" execute(): cache hit, has pending bus request");
		    		BusRequest current_request = bus.getCurrRequest();
		    		if (current_request.getCache_id() == cache_id && current_request.getCyclesLeft() == 0) {
		    			pending_bus_request = false;
		    			return true;
		    		} else {
		    			return false;
		    		}
				}
			}
			else if ((getCacheBlock(addr).getState() == State.EXCLUSIVE) && (ins[0] == Constants.INS_WRITE) ){
	    		//just go to modified. no bus transaction cos all others in I
//				System.out.println("Cache" + cache_id + "execute(): I am in Exclusive, I am going to Modified");
				getCacheBlock(addr).setState(State.MODIFIED);
			}
			else {
				//normal hit. no need for bus transaction
				updateLRUage(addr);
			}
			countCacheHit++;
			return true;
		} else {
//			System.out.println("Cache " + cache_id + ": Cache miss...");
//			System.out.println("Cache" + cache_id +" execute(): pending bus request: "+ pending_bus_request + " & instruction: "+ins[0]);
//			boolean check_again = isCacheHit(addr);
//			System.out.println("Cache execute(): checking again and result is "+check_again);
			if (!pending_bus_request) {
	    		switch (ins[0]) {
	    		case Constants.INS_READ:
	    			new_request = new BusRequest(cache_id, Transaction.BusRd, addr, 10);
	    			break;
	    		case Constants.INS_WRITE:
	    			new_request = new BusRequest(cache_id, Transaction.BusRdX, addr, 10);
	    			break;
	    		default:
	    			break;
	    		}
//    			System.out.println("running LRU policy");
    			runLRUpolicy(addr);
//    			System.out.println("request to bus is "+ new_request.toString());
    			bus.enqueueRequest(new_request);
    			this.pending_bus_request = true;
    			countCacheMiss++;
			} else {
//				System.out.println("Cache" + cache_id +" execute(): cache miss, has pending bus request");
	    		BusRequest current_request = bus.getCurrRequest();
//	    		System.out.println("Cache" + cache_id +" execute(): current request is "+current_request );
	    		if (current_request.getCache_id() == cache_id && current_request.getCyclesLeft() == 0) {
	    			pending_bus_request = false;
	    			return true;
	    		} else {
	    			//countCacheMiss++;
	    			return false;
	    		}
			}
			
			return false;
		}
	}

	public void updateCache(int addr, int block_index) {
		int index = getIndex(addr);
		CacheLine block = cache_sets[index].getCacheLine(block_index);
		block.setAddress(addr);
		block.setTag(getTag(addr));
		//System.out.println("Cache" + cache_id +" updateCache(): uniproc_flag is "+uniproc_flag);
		if(uniproc_flag){
			block.setState(State.UNIPROC);
		}
		else{
			block.setState(State.INVALID);
		}
//		System.out.println("Cache" + cache_id +" updating cache, set set index "+index+" and block index "+block_index+" with address "+block.getAddrString());
	}
	
	private void updateLRUage(int addr){
		int block_index = getCacheBlockIndex(addr);
		if(block_index > 0){
			updateLRUage(getIndex(addr), block_index);
		}
	}
	
	private void updateLRUage(int set_index, int block_index){
		//block last touched is block_index
		cache_sets[set_index].getCacheLine(block_index).setLRU_age(0);
		for(int i=0;i<associativity;i++){
			CacheLine block = cache_sets[set_index].getCacheLine(i);
			if(i != block_index && block.getLRU_age() >= 0){
				block.setLRU_age(block.getLRU_age() + 1);
			}
		}
	}
	
	private void runLRUpolicy(int addr){
		int index = getIndex(addr);
		int num_cache_lines_occupied = 0;
		for(int i=0;i<associativity;i++){
			CacheLine block = cache_sets[index].getCacheLine(i);
			//if unoccupied
			if (block.getTag() == -1) {
//				System.out.println("in lru policy, has unoccupied cache");
				updateCache(addr, i);
				updateLRUage(index, i);
				return;
			}
			else {
				num_cache_lines_occupied++;
			}
		}
		
		if(num_cache_lines_occupied == associativity){
//			System.out.println("all blocks occupied");
			//all blocks occupied
			//find max age
			int max_age = -1;
			int max_age_block_index = -1;
			for(int i=0;i<associativity;i++){
				if(cache_sets[index].getCacheLine(i).getLRU_age() > max_age){
					max_age_block_index = i;
				}
			}
			//evict max age
			cache_sets[index].getCacheLine(max_age_block_index).setAddress(-1);
			cache_sets[index].getCacheLine(max_age_block_index).setTag(-1);
			cache_sets[index].getCacheLine(max_age_block_index).setState(State.INVALID);
			//update LRU ages
			updateLRUage(index, max_age_block_index);
			updateCache(addr, max_age_block_index);
		}
	}

	@Override
	public String toString() {
		String s = "Cache number " + cache_id + "\n";
		s += "Index  Set Index\tTag\t\tState\t\tAddress\n";

		for (int i = 0; i < cache_sets.length; i++) {
			s += cache_sets[i].toString();
		}
		
		s += "Number of cache hits : " + countCacheHit + "\n";
		s += "Number of cache misses : " + countCacheMiss + "\n";

		return s;
	}

    public void busSnoop(String protocol) {
//    	System.out.println("Cache" + cache_id + " busSnoop() - in bus snoop function.");
		BusRequest request = bus.getCurrRequest();
		CacheLine block = getCacheBlock(request.getAddress());
		if(this.cache_id == request.getCache_id()){
			//I'm the cache that made request
			if(block != null){
				State state = block.getState();
				if(state == State.MODIFIED){
					//never happens but here for sake of completeness
//					System.out.println("Cache" + cache_id + " busSnoop() - I made request, I'm in M, no change");
				}
				else if(state == State.EXCLUSIVE){
					//BusRd will not be produced by an E state since block is already in cache
					//BusRdX never happens because if I'm E and I do BusRdX, I straight away go to M and don't need a trxn
//					if(request.getTransaction() == Transaction.BusRdX){
//						block.setState(State.MODIFIED);
//					}
//					System.out.println("Cache" + cache_id + " busSnoop() - I made request, I'm in E, no change");
				}
				else if(state == State.SHARED){
					//BusRd has no change in state. doesn't happen.
					if(request.getTransaction() == Transaction.BusRdX){
//						System.out.println("Cache" + cache_id + " busSnoop() - I made request, Going to Modified from Shared for BusRdX");
						block.setState(State.MODIFIED);
					}
				}
				else if(state == State.INVALID){
					if(request.getTransaction() == Transaction.BusRd){
						if(protocol.compareTo("MESI") == 0){
							if (bus.doMultipleCachesHaveBlock(request.getAddress(), this.cache_id)){ //shared signal
//								System.out.println("Cache" + cache_id + " busSnoop(): I made request, Going to Shared from Invalid for BusRd");
								block.setState(State.SHARED);
							}
							else {
//								System.out.println("Cache" + cache_id + " busSnoop(): I made request, Going to Exclusive from Invalid for BusRd");
								block.setState(State.EXCLUSIVE);
							}
						}
						else { //MSI protocol
//							System.out.println("Cache" + cache_id + " busSnoop(): I made request, MSI, Going to Shared from Invalid for BusRd");
							block.setState(State.SHARED);
						}
					}
					else if(request.getTransaction() == Transaction.BusRdX){
//						System.out.println("Cache" + cache_id + " busSnoop() - I made request, Going to Modified from Invalid for BusRdX");
						block.setState(State.MODIFIED);
					}
				}
			}
		} else {
			if(block != null) {
				//i'm another cache and I have the block
				State state = block.getState();
//				System.out.println("Cache busSnoop(): I am another cache, I have block. state = "+state);
				if(state == State.MODIFIED){
					if(request.getTransaction() == Transaction.BusRd){
//						System.out.println("Cache" + cache_id + " busSnoop(): Going to Shared from Modified for BusRd");
						block.setState(State.SHARED);
					}
					else if(request.getTransaction() == Transaction.BusRdX){
//						System.out.println("Cache" + cache_id + " busSnoop(): Going to Invalid from Modified for BusRdX");
						block.setState(State.INVALID);
					}
				}
				else if(state == State.EXCLUSIVE){
					if(request.getTransaction() == Transaction.BusRd){
//						System.out.println("Cache" + cache_id + " busSnoop(): Going to Shared from Exclusive for BusRd");
						block.setState(State.SHARED);
					}
					else if(request.getTransaction() == Transaction.BusRdX){
//						System.out.println("Cache" + cache_id + " busSnoop(): Going to Invalid from Exclusive for BusRdX");
						block.setState(State.INVALID);
					}
				}
				else if(state == State.SHARED){
					if(request.getTransaction() == Transaction.BusRdX){
//						System.out.println("Cache" + cache_id + " busSnoop(): Going to Invalid from Shared for BusRdX");
						block.setState(State.INVALID);
					}
				}
				else if(state == State.INVALID){
					//don't care 
				}
			}
		}
		
    }
}
