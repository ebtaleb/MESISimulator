import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Processor {

	private int proc_id;
	private Bus sharedBus;
	private Cache proc_cache;
	private int cycle_count;
	private BufferedReader ins_trace;

	private boolean is_blocked;
	private int blocked_until;

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
        String line;
        int[] ins;

        if (is_blocked()) {
            cycle_count++;
            return;
        } else {
            ins = new int[2];
            if ((line = ins_trace.readLine()) != null) {
                split_line = line.split(" ");
                ins[0] = Integer.parseInt(split_line[0]);
                ins[1] = hexStringToInt(split_line[1]);
                System.out.println("Cycle " + cycle_count + " : [ " + ins[0] + ", " + Integer.toHexString(ins[1]) + " ]");
            } else {
                throw new Exception();
            }
            
    		switch (ins[0]) {
    		case Constants.INS_FETCH:
    			cycle_count++;
    			return;
    		case Constants.INS_READ:
    			break;
    		case Constants.INS_WRITE:
    			break;
    		default:
    		}
            
            if (!proc_cache.execute(ins)) {
                blockProc();
            } else {
                cycle_count++;
            }

            if (cycle_count > 500) {
                System.out.println(proc_cache);
                System.out.println("Total cycle number : " + cycle_count);
                throw new Exception();
            }
        }
	}

	private void blockProc() {
		is_blocked = true;
		blocked_until = cycle_count + 10;
	}

	private boolean is_blocked() {
		if (cycle_count == blocked_until) {
			blocked_until = cycle_count;
			is_blocked = false;
		}

		if (is_blocked == true) {
	        System.out.println( "Cycle " + cycle_count + " :" + "Proc " + proc_id + " is blocked");
	        System.out.println( (blocked_until - cycle_count) + " cycles left before unblocking");
			return is_blocked;
		}
		return is_blocked;
	}


}
