import java.util.ArrayList;


public class Cache {
	//FORMULAS
	//#Blocks = CacheSize / BlockSize
	//#Sets = #Blocks / #Associativity
	//Block number = mem address / blocksize
	//Block offset (bits) = mem address mod blocksize
	//Set index (bits) = log2(#Sets) --> set index and cache index are the same thing
	//Tag bits in mem = length of address - block offset bits - set index bits  --> to differentiate memory blocks that map to the same cache block
	//Mapping Function: Cache Index = (Block Number) modulo (Number of Cache Blocks)
	//Cache hit: (Tag[index] = Tag[memory address]) AND (Valid[index] = TRUE)
	
	//do we calculate the no. bits for offset within block and the no. sets? then determine which bits represent the set index?
	
	private int cache_id;
	public int cache_size;
	public int block_size;
	public int associativity;
	private int countCacheMiss;
	private int countCacheHit;
	private String state; //MODIFIED, EXCLUSIVE, SHARED, INVALID
	private String protocol; //MSI or MESI
	private Bus bus; //access to bus
	
	//each set has n blocks for associativity = n
	//each block has a valid bit, dirty bit and a tag
	//each array index is the set index
	private ArrayList<int[]> sets; 
	
	public Cache(int cache_id, int cache_size, int associativity, int block_size, Bus bus, String protocol){
		this.cache_id = cache_id;
		this.cache_size = cache_size;
		this.associativity = associativity;
		this.block_size = block_size;
		this.bus = bus;
		this.protocol = protocol;
		this.state = "INVALID";
		this.countCacheMiss = 0;
		this.countCacheHit = 0;
		this.sets = new ArrayList<>();
	}
	
	public int getNoBlocks(){
		//#Blocks = CacheSize / BlockSize
		return this.cache_size/this.block_size;
	}
	
	public int getNoSets(){
		//#Sets = #Blocks / #Associativity
		return getNoBlocks()/this.associativity;
	}
	
	private int getOffsetBits(){
		//eg. 16 byte block size --> log16/log2 = 4 bits
		int bits = (int) (Math.log(this.block_size)/Math.log(2));
		return bits;
	}
	
	private int getOffset(int address){ //is address an int?
		int bits = getOffsetBits();
		return address % 10^bits;
	}
	
	private int getSetIndexBits(){
		//eg. 8 sets --> log8/log2 = 3 bits
		int bits = (int) (Math.log(getNoSets())/Math.log(2));
		return bits;
	}
	
	private int getSetIndex(int address){
		int bits = getSetIndexBits();
		String addressString = Integer.toString(address);
		String result = addressString.substring(31-getOffsetBits()-bits+1,31-getOffsetBits());
		return Integer.parseInt(result);
	}
	
	private int getTagBits(){
		//assume 32 bit address
		int bits = 32 - getOffsetBits() - getSetIndexBits();
		return bits;
	}	
	
	private int getTag(int address){
		//int bits = getTagBits();
		String addressString = Integer.toString(address);
		String result = addressString.substring(0,31-getOffsetBits()-getSetIndexBits());
		return Integer.parseInt(result);
	}
	
	private int getMapping(int address){
		//Mapping Function: Cache Index = (Block Number) modulo (Number of Cache Blocks)
		return 0;
	}
	
	private boolean isCacheHit(){
		//Cache hit: (Tag[index] = Tag[memory address]) AND (Valid[index] = TRUE)
		return false;
	}
	
}
