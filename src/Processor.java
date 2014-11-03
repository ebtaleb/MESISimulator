import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Processor {

	private int proc_id;
	private Bus sharedBus;
	private Cache proc_cache;
	private int cycle_count;
	private BufferedReader ins_trace;

	static int hexStringToInt(String hs) {
		return (int) Long.parseLong(hs, 16);
	}

	public Processor(int proc_id, Bus b, int[] cache_settings, String file) throws FileNotFoundException {
		this.proc_id = proc_id;
		this.sharedBus = b;
		this.proc_cache = new Cache();
		this.cycle_count = 0;
		ins_trace = new BufferedReader(new FileReader(file));
	}

	public void run() throws Exception {
		String line;
		String[] split_line = null;
		int[] int_split_line = null;

        line = ins_trace.readLine();
        if (line != null) {
            int_split_line = new int[2];
	        split_line = line.split(" ");
	        int_split_line[0] = Integer.parseInt(split_line[0]);
	        int_split_line[1] = hexStringToInt(split_line[1]);
	        System.out.println("[ " + int_split_line[0] + ", " + Integer.toHexString(int_split_line[1]) + " ]");
        } else {
            throw new Exception();
        }

        if (cycle_count > 500) {
            throw new Exception();
        }

        cycle_count++;

	}


}
