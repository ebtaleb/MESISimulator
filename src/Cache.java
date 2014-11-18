
public class Cache {

	public static final int INS_FETCH = 0;
	public static final int INS_READ = 2;
	public static final int INS_WRITE = 3;
	//FORMULAS
	//#Blocks = CacheSize / BlockSize
	//#Sets = #Blocks / #Associativity
	//Block number = mem address / blocksize
	//Block offset (bits) = mem address mod blocksize
	//Cache index (bits) = log2(#Sets) --> cache index and set index are the same thing
	//Tag bits in mem = length of address - block offset bits - set index bits  --> to differentiate memory blocks that map to the same cache block
	//Mapping Function: Cache Index = (Block Number) modulo (Number of Cache Blocks)
	//Cache hit: (Tag[index] = Tag[memory address]) AND (Valid[index] = TRUE)

	private int cache_id;
	public int cache_size;
	public int block_size;
	public int associativity;

	private int countCacheMiss;
	private int countCacheHit;

	private String protocol; //MSI or MESI
	private Bus bus; //access to bus

	//each set has n blocks for associativity = n
	private CacheSet[] cache_sets;

	public Cache(int cache_id, int cache_size, int associativity, int block_size, Bus bus, String protocol){
		this.cache_id = cache_id;
		this.cache_size = cache_size;
		this.associativity = associativity;
		this.block_size = block_size;
		this.bus = bus;
		this.protocol = protocol;
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

	private boolean isCacheHit(int address){
		// get index and tag from address argument
		// use index to access cache set, retrieve tag and validity
		//
		//Cache hit: (Tag[index] = Tag[memory address]) AND (Valid[index] = TRUE)

		int input_tag = getTag(address);
		int input_index = getIndex(address);
		
		for(int i=0;i<associativity;i++){
			if (cache_sets[input_index].getCacheLine(i).getTag() == input_tag && cache_sets[input_index].getCacheLine(i).getState() != State.INVALID)  {
				//set LRUage for block(ie.cache line)
				return true;
			} else {
				//check if all blocks are occupied. 
				//if no, generate BusRd if read inst and BusRdX is write inst and occupy the memory block
				//if yes, LRU policy to evict oldest block (BusWr if in M and nothing if in E) and then gen a BusRd/BusRdX for the new mem add. Remember to change age other other blocks in cache.
				return false;
			}
		}
		return false;
	}

	public void execute(int[] ins) {

		switch (ins[0]) {
		case INS_FETCH:
			break;
		case INS_READ:
			break;
		case INS_WRITE:
			break;
		default:
			return;
		}

		int addr = ins[1];

		if (isCacheHit(addr)) {
			System.out.println("yeah cache hit!");
			countCacheHit++;
		} else {
			System.out.println("meh cache miss...");
			updateCache(addr);
			countCacheMiss++;
		}
	}

	private void updateCache(int addr) {
		int index = getIndex(addr);
		for(int i=0;i<associativity;i++){
			if (cache_sets[index].getCacheLine(i).getTag() == -1) //if there is an unoccupied
			{
				cache_sets[index].getCacheLine(i).setAddress(addr);
				cache_sets[index].getCacheLine(i).setTag(getTag(addr));
				cache_sets[index].getCacheLine(i).setState(State.EXCLUSIVE);
			}
		}
	}

	@Override
	public String toString() {
		String s = "Cache number " + cache_id + "\n";
		s += "Index\t\tTag\t\tState\t\tAddress\n";

		for (int i = 0; i < cache_sets.length; i++) {
			s += cache_sets[i].toString();
		}

		return s;
	}

    public void runProtocol(CacheLine block) {
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
    }

    public void busSnoop() {

    }
}
