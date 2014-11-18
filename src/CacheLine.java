
public class CacheLine {

	private int address;
	private State state;
//	private boolean dirty;
//	private boolean valid;
	private int tag;
	private int LRU_age;

	public CacheLine(int addr, State s, int tag) {
		address = addr;
		state = s;
		this.tag = tag;
		this.LRU_age = -1;
	}

	public int getAddress() {
		return address;
	}

	public String getAddrString() {
		return Integer.toHexString(address);
	}

	public void setAddress(int addr) {
		this.address = addr;
	}

	public State getState() {
		return state;
	}

	public void setState(State s) {
		state = s;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}
	

	public int getLRU_age() {
		return LRU_age;
	}

	public void setLRU_age(int lRU_age) {
		LRU_age = lRU_age;
	}

	@Override
	public String toString() {
		return tag + "\t\t" + state + "\t\t" + getAddrString() + "\n";
	}
}
