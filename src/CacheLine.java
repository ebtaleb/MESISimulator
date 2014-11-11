
public class CacheLine {

	private int address;
	private State state;
	private boolean dirty;
	private boolean valid;
	private int tag;
	private int index;

	public CacheLine(int addr, State s, int tag, int index) {
		address = addr;
		state = s;
		dirty = false;
		valid = false;
		this.tag = tag;
		this.index = index;
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

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return index + "\t\t" + (valid ? 1 : 0) + "\t\t" + (dirty ? 1 : 0) + "\t\t" + tag + "\t\t" + state + "\t\t" + getAddrString() + "\n";
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}



}
