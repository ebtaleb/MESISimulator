
public class Cache {
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
	//each block has a valid bit, dirty bit and a tag
	//the array index of each block is its cache index. it should be in running order
	//each block also has a cacheHit tracker which holds when it was hit (for LRU policy)
	//eg. block 1 hit --> countCacheHit++; block.cacheHit = countCacheHit; in the case of cache miss, see which block.cacheHit is lowest and evict that block
	private CacheLine[] cache_contents;

	public Cache(int cache_id, int cache_size, int associativity, int block_size, Bus bus, String protocol){
		this.cache_id = cache_id;
		this.cache_size = cache_size;
		this.associativity = associativity;
		this.block_size = block_size;
		this.bus = bus;
		this.protocol = protocol;
		this.countCacheMiss = 0;
		this.countCacheHit = 0;
		this.cache_contents = new CacheLine[cache_size/(block_size*associativity)];

		for (int i = 0; i < cache_contents.length; i++) {
			cache_contents[i] = new CacheLine(-1, State.INVALID, -1, i);
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

	private int getOffset(int address) { //is address an int?
		int bitmask = 0;
		for (int i = 0; i < getOffsetBits(); i++ ) {
			bitmask |= (1 << i);
		}
		return (address & bitmask);
	}

	private int getIndexBits(){
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
		// use index to access cache, retrieve tag and validity
		//
		//Cache hit: (Tag[index] = Tag[memory address]) AND (Valid[index] = TRUE)

		int input_tag = getTag(address);
		int input_index = getIndex(address);

		if (cache_contents[input_index].getTag() == input_tag && cache_contents[input_index].getState() == State.EXCLUSIVE)  {
			return true;
		} else {
			return false;
		}
	}

	public void execute(int[] ins) {

		switch (ins[0]) {
		case 0:
			break;
		case 2:
			break;
		case 3:
			break;
		default:
			return;
		}

		int addr = ins[1];

		if (isCacheHit(addr)) {
			System.out.println("yeah cache hit!");
		} else {
			System.out.println("meh cache miss...");
			updateCache(addr);
		}
	}

	private void updateCache(int addr) {
		int index = getIndex(addr);
		cache_contents[index].setAddress(addr);
		cache_contents[index].setTag(getTag(addr));
		cache_contents[index].setState(State.EXCLUSIVE);
	}

	@Override
	public String toString() {
		String s = "Cache number " + cache_id + "\n";
		s += "Index\t\tTag\t\tState\t\tAddress\n";

		for (int i = 0; i < cache_contents.length; i++) {
			s += cache_contents[i].toString();
		}

		return s;
	}

}
