
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

	public Cache(int cache_id, int cache_size, int associativity, int block_size, Bus bus){
		this.cache_id = cache_id;
		this.cache_size = cache_size;
		this.associativity = associativity;
		this.block_size = block_size;
		this.bus = bus;
		this.countCacheMiss = 0;
		this.countCacheHit = 0;
		this.cache_sets = new CacheSet[cache_size/(block_size*associativity)];

		for (int i = 0; i < cache_sets.length; i++) {
			cache_sets[i] = new CacheSet(i, this.associativity, -1, State.INVALID, -1);
		}
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
			//set LRUage for block(ie.cache line)
			countCacheHit++;
			return true;
		} else {
			System.out.println("meh cache miss...");
			
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
    		
			bus.enqueueRequest(new_request);
			
			updateCache(addr);
			countCacheMiss++;
			return false;
		}
	}

	public void updateCache(int addr) {
		int index = getIndex(addr);
		for(int i=0;i<associativity;i++){
			if (cache_sets[index].getCacheLine(i).getTag() == -1) //if there is an unoccupied
			{
				cache_sets[index].getCacheLine(i).setAddress(addr);
				cache_sets[index].getCacheLine(i).setTag(getTag(addr));
				cache_sets[index].getCacheLine(i).setState(State.EXCLUSIVE);
			}
			else {
				//check if all blocks are occupied. 
				//if no, generate BusRd if read inst and BusRdX is write inst and occupy the memory block
				//if yes, LRU policy to evict oldest block (BusWr if in M and nothing if in E) 
				//Remember to change age other other blocks in cache.
				//gen a BusRd/BusRdX for the new mem add. 
				//set the address, tag, state for new mem address
			}
		}
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

    public BusRequest busSnoop(int addr) {
		return null;
    }
}
