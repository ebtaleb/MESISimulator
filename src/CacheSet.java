import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class CacheSet {

	private ArrayList<CacheLine> cache_lines;
	private int set_index;
	private int asso;

	public CacheSet(int set_index, int associativity) {
		this.cache_lines = new ArrayList<>();
		this.set_index = set_index;
		asso = associativity;
	}
	
	public CacheSet(int set_index, int associativity, int addr, State s, int tag){
		this.cache_lines = new ArrayList<>();
		this.set_index = set_index;
		asso = associativity;
		for(int i=0;i<associativity;i++){
			CacheLine line = new CacheLine(addr, s, tag);
			addCacheLine(line);
		}
	}

	public ArrayList<CacheLine> getCacheLines() {
		return cache_lines;
	}
	
	public CacheLine getCacheLine(int index){
		return cache_lines.get(index);
	}

	public void setCacheLines(ArrayList<CacheLine> cache_lines) {
		this.cache_lines = cache_lines;
	}

	public int getSetIndex() {
		return this.set_index;
	}

    private void addCacheLine(CacheLine c) {
    	cache_lines.add(c);
    }
    
	@Override
	public String toString() {
		String s = "";
		int ind = asso * set_index;
		for (CacheLine cl : cache_lines) {
			s += ind + "\t\t" + set_index + "\t\t" + cl.toString();
			ind++;
		}
		return s;
	}
}
