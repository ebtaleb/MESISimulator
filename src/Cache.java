
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
		return getNoBlocks() / this.associativity;
	}

	private int getOffsetBits() {
		//eg. 16 byte block size --> log16/log2 = 4 bits
		return (int) (Math.log(this.block_size) / Math.log(2));
	}

	private int getOffset(int address) {
		int bitmask = 0;
		for (int i = 0; i < getOffsetBits(); i++ ) {
			bitmask |= (1 << i);
		}
		return (address & bitmask);
	}

	private int getIndexBits(){ //index = set index
		//eg. 8 sets --> log8/log2 = 3 bits
		return (int) (Math.log(getNoSets()) / Math.log(2));
	}

	private int getIndex(int address){
		int bitmask = 0;
		for (int i = getOffsetBits(); i < getOffsetBits()+getIndexBits(); i++ ) {
			bitmask |= (1 << i);
		}
		return (address & bitmask) >>> getOffsetBits();
	}

	private int getTagBits(){
		//assume 32 bit address
		return 32 - getOffsetBits() - getIndexBits();
	}

	private int getTag(int address){
		int bitmask = 0;
		//bitmask = Integer.MAX_VALUE;

		for (int i = getOffsetBits() + getIndexBits(); i < 32; i++ ) {
			bitmask |= (1 << i);
		}

		return (address & bitmask) >>> (getIndexBits() + getOffsetBits());
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
		
		for(int i=0;i<associativity;i++){
			if (cache_sets[input_index].getCacheLine(i).getTag() == input_tag && cache_sets[input_index].getCacheLine(i).getState() != State.INVALID)  {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean execute(int[] ins) {

		int addr = ins[1];
		BusRequest new_request = null;

		if (isCacheHit(addr)) {
			System.out.println("Cache " + cache_id + ": Cache hit!");
			
			if ((getCacheBlock(addr).getState() == State.SHARED) && (ins[0] == Constants.INS_WRITE) ){
	    		new_request = new BusRequest(cache_id, Transaction.BusRdX, addr, 10);
				if (!pending_bus_request) {
	    			System.out.println("request to bus is "+ new_request.toString());
	    			bus.enqueueRequest(new_request);
	    			this.pending_bus_request = true;
				} else {
					System.out.println("Cache" + cache_id +" execute(): cache hit, has pending bus request");
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
				getCacheBlock(addr).setState(State.MODIFIED);
			}
			else {
				//normal hit. no need for bus transaction
				updateLRUage(addr);
			}
			countCacheHit++;
			return true;
		} else {
			System.out.println("Cache " + cache_id + ": Cache miss...");
			countCacheMiss++;
			System.out.println("Cache" + cache_id +" execute(): pending bus request: "+ pending_bus_request + " & instruction: "+ins[0]);
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
    			System.out.println("running LRU policy");
    			runLRUpolicy(addr);
    			System.out.println("request to bus is "+ new_request.toString());
    			bus.enqueueRequest(new_request);
    			this.pending_bus_request = true;
			} else {
				System.out.println("Cache" + cache_id +" execute(): cache miss, has pending bus request");
	    		BusRequest current_request = bus.getCurrRequest();
	    		System.out.println("Cache" + cache_id +" execute(): current request is "+current_request );
	    		if (current_request.getCache_id() == cache_id && current_request.getCyclesLeft() == 0) {
	    			pending_bus_request = false;
	    			return true;
	    		} else {
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
		System.out.println("Cache" + cache_id +" updateCache(): uniproc_flag is "+uniproc_flag);
		if(uniproc_flag){
			block.setState(State.UNIPROC);
		}
		else{
			block.setState(State.INVALID);
		}
		System.out.println("Cache" + cache_id +" updating cache, set set index "+index+" and block index "+block_index+" with address "+block.getAddrString());
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
				System.out.println("in lru policy, has unoccupied cache");
				updateCache(addr, i);
				updateLRUage(index, i);
			}
			else {
				num_cache_lines_occupied++;
			}
		}
		
		if(num_cache_lines_occupied == associativity){
			System.out.println("all blocks occupied");
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
		s += "Index\t\tSet Index\t\tTag\t\tState\t\tAddress\n";

		for (int i = 0; i < cache_sets.length; i++) {
			s += cache_sets[i].toString();
		}
		
		s += "Number of cache hits : " + countCacheHit + "\n";
		s += "Number of cache misses : " + countCacheMiss + "\n";

		return s;
	}

    public void busSnoop(String protocol) {
		BusRequest request = bus.getCurrRequest();
		CacheLine block = getCacheBlock(request.getAddress());
		if(this.cache_id == request.getCache_id()){
			//I'm the cache that made request
			if(block != null){
				State state = block.getState();
				if(state == State.MODIFIED){
					//never happens but here for sake of completeness
				}
				else if(state == State.EXCLUSIVE){
					//BusRd will not be produced by an E state since block is already in cache
					//BusRdX never happens because if I'm E and I do BusRdX, I straight away go to M and don't need a trxn
//					if(request.getTransaction() == Transaction.BusRdX){
//						block.setState(State.MODIFIED);
//					}
				}
				else if(state == State.SHARED){
					//BusRd has no change in state. doesn't happen.
					if(request.getTransaction() == Transaction.BusRdX){
						block.setState(State.MODIFIED);
					}
				}
				else if(state == State.INVALID){
					if(request.getTransaction() == Transaction.BusRd){
						if(protocol == "MESI"){
							if (bus.doMultipleCachesHaveBlock(request.getAddress(), this.cache_id)){ //shared signal
								block.setState(State.SHARED);
							}
							else {
								block.setState(State.EXCLUSIVE);
							}
						}
						else { //MSI protocol
							block.setState(State.SHARED);
						}
					}
					else if(request.getTransaction() == Transaction.BusRdX){
						block.setState(State.MODIFIED);
					}
				}
			}
		} else {
			if(block != null) {
				//i'm another cache and I have the block
				State state = block.getState();
				if(state == State.MODIFIED){
					if(request.getTransaction() == Transaction.BusRd){
						block.setState(State.SHARED);
					}
					else if(request.getTransaction() == Transaction.BusRdX){
						block.setState(State.INVALID);
					}
				}
				else if(state == State.EXCLUSIVE){
					if(request.getTransaction() == Transaction.BusRd){
						block.setState(State.SHARED);
					}
					else if(request.getTransaction() == Transaction.BusRdX){
						block.setState(State.INVALID);
					}
				}
				else if(state == State.SHARED){
					if(request.getTransaction() == Transaction.BusRdX){
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
