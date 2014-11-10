
public class CacheLine {

	private int memory_address;
	private String state;
	private int tag;
	private int index;

	public CacheLine(int addr, String s, int tag, int index) {
		memory_address = addr;
		state = s;
		this.tag = tag;
		this.index = index;
	}

	public int getMemory_address() {
		return memory_address;
	}

	public void setMemory_address(int addr) {
		this.memory_address = addr;
	}

	public String getState() {
		return state;
	}

	public void setState(String s) {
		state = s;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
