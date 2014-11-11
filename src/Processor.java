import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Processor {

	private int proc_id;
	private Bus sharedBus;
	private Cache proc_cache;
	private int cycle_count;
	private BufferedReader ins_trace;

	static int hexStringToInt(String hs) {
		return (int) Long.parseLong(hs, 16);
	}

	public Processor(int proc_id, Bus b, Cache c, String file) throws FileNotFoundException {
		this.proc_id = proc_id;
		this.sharedBus = b;
		this.proc_cache = c;
		this.cycle_count = 0;
		ins_trace = new BufferedReader(new FileReader(file));
	}

	public void run() throws Exception {
		String[] split_line;

        String line = ins_trace.readLine();
        int[] ins = new int[2];
        if (line != null) {
	        split_line = line.split(" ");
	        ins[0] = Integer.parseInt(split_line[0]);
	        ins[1] = hexStringToInt(split_line[1]);
	        System.out.println("[ " + ins[0] + ", " + Integer.toHexString(ins[1]) + " ]");
        } else {
            throw new Exception();
        }

        proc_cache.execute(ins);
        cycle_count++;

        if (cycle_count > 500) {
            System.out.println(proc_cache);
            throw new Exception();
        }
	}


}
