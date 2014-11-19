
public class Cache {

	private int cache_id;
	public int cache_size;
	public int block_size;
	public int associativity;

	private int countCacheMiss;
	private int countCacheHit;
	
     //Each processor can only have 1 pending instruction in bus request queue
	
	private Bus bus; //access to bus

	//each set has n blocks for associativity = n
	private CacheSet[] cache_sets;
	private boolean uniproc_flag;
	private boolean pending_bus_request; //if there is already a pending request in bus. set flag = true when enqueue. set flag = false when cache checks that bus has completed a transaction already in the bus queue

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
	
	public CacheLine getCacheBlock(int addr) {
		
    	int index = getIndex(addr);
    	
    	for (int i = 0; i < associativity; i++) {
    		if (cache_sets[index].getCacheLine(i).getAddress() == addr) {
    			return cache_sets[index].getCacheLine(i);
    		}
    	}
		
		return null;
		
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
			System.out.println("yeah cache hit!");
			
			if ((getCacheBlock(addr).getState() == State.SHARED) && (ins[0] == Constants.INS_WRITE) ){
	    		new_request = new BusRequest(cache_id, Transaction.BusRdX, addr, 10);
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
			System.out.println("meh cache miss...");
			countCacheMiss++; 
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
    		
    		if(this.pending_bus_request){ 
    			return false;
    		}
    		else {
    			bus.enqueueRequest(new_request);
    			this.pending_bus_request = true;
    			return true;
    		}
    		
			//updateCache(addr); //should not update here. updateCache when cache checks that bus has finished processing its transaction
		}
	}

	public void updateCache(int addr) {
		int index = getIndex(addr);
		int num_cache_lines_occupied = 0;
		for(int i=0;i<associativity;i++){
			CacheLine block = cache_sets[index].getCacheLine(i);
			if (block.getTag() == -1) //if there is an unoccupied
			{
				block.setAddress(addr);
				block.setTag(getTag(addr));
				block.setState(State.EXCLUSIVE);
				updateLRUage(addr);
			}
			else {
				
				if (uniproc_flag == true) {
					block.setAddress(addr);
					block.setTag(getTag(addr));
					block.setState(State.EXCLUSIVE); 
					//there is actually no need for a concept of state in uniprocessor. but if you're using it for the purposes of testing then its fine.
				} else {
					num_cache_lines_occupied++;
				}

			}
		}
		
		if(num_cache_lines_occupied == associativity){
			//all blocks are occupied so do LRU policy
			runLRUpolicy(addr);
		}
	}
	
	private void updateLRUage(int addr){
		
	}
	
	private void runLRUpolicy(int addr){
		//inside this updateLRUage(addr);
	}

	@Override
	public String toString() {
		String s = "Cache number " + cache_id + "\n";
		s += "Index\t\tSet Index\t\tTag\t\tState\t\tAddress\n";

		for (int i = 0; i < cache_sets.length; i++) {
			s += cache_sets[i].toString();
		}

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
					if(request.getTransaction() == Transaction.BusRdX){
						block.setState(State.MODIFIED);
					}
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
