import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Processor {

	private int proc_id;
	private Cache proc_cache;
	private int cycle_count;
	private BufferedReader ins_trace;

	private int[] pending_instruction;

	static int hexStringToInt(String hs) {
		return (int) Long.parseLong(hs, 16);
	}

	public Processor(int proc_id, Cache c, String file) throws FileNotFoundException {
		this.proc_id = proc_id;
		this.proc_cache = c;
		this.cycle_count = 0;
		this.pending_instruction = null;
		ins_trace = new BufferedReader(new FileReader(file));
	}

	public void run() throws Exception {

		String[] split_line;
        String line;
        int[] ins;

        if(this.pending_instruction == null){
        	ins = new int[2];
        	if ((line = ins_trace.readLine()) != null) {
        		split_line = line.split(" ");
        		ins[0] = Integer.parseInt(split_line[0]);
        		ins[1] = hexStringToInt(split_line[1]);
        		System.out.println("[ " + ins[0] + ", " + Integer.toHexString(ins[1]) + " ]");
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
        }
        else {
        	ins = this.pending_instruction;
        }

        if (!proc_cache.execute(ins)) {
        	System.out.println("Proc " + proc_id + " is blocked");
        	this.pending_instruction = ins;
        } else {
        	this.pending_instruction = null;
        }
        
    	cycle_count++;

        if (cycle_count > 5000) {
        	System.out.println(proc_cache);
        	System.out.println("Total cycle number : " + cycle_count);
        	throw new Exception();
        }
	}
}
