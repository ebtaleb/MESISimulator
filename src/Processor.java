
public class Processor {

	private int proc_id;
	private Bus sharedBus;
	private Cache proc_cache;
	private int cycle_count;

	public Processor(int proc_id, Bus b, int[] cache_settings) {
		this.proc_id = proc_id;
		this.sharedBus = b;
		this.proc_cache = new Cache();
		this.cycle_count = 0;
	}


}
