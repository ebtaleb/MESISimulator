import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Processor {

	private int proc_id;
	private Cache proc_cache;
	private int cycle_count;
	private BufferedReader ins_trace;
	private PrintStream output_file;

	private int[] pending_instruction;

	static int hexStringToInt(String hs) {
		return (int) Long.parseLong(hs, 16);
	}

	public Processor(int proc_id, Cache c, String in_file, String out_file) throws IOException {
		this.proc_id = proc_id;
		this.proc_cache = c;
		this.cycle_count = 0;
		this.pending_instruction = null;
		ins_trace = new BufferedReader(new FileReader(in_file));
		
		File file = new File(out_file);
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		output_file = ps;	
	}
	
	public PrintStream getStream() {
		return output_file;
	}

	public void run() throws Exception {

		String[] split_line;
        String line;
        int[] ins;
        String ins_type = "";

		System.setOut(output_file);
        if(this.pending_instruction == null){
        	ins = new int[2];
        	if ((line = ins_trace.readLine()) != null) {
        		split_line = line.split(" ");
        		
        		switch (ins[0] = Integer.parseInt(split_line[0])) {
        			case Constants.INS_FETCH : ins_type = "fetch from "; break;
        			case Constants.INS_READ: ins_type = "read from "; break;
        			case Constants.INS_WRITE: ins_type = "write to "; break;
        			default: break;
        		}
        		
        		ins[1] = hexStringToInt(split_line[1]);
        		System.out.println("C" + cycle_count + ": proc " + proc_id + " executing "+ ins_type + Integer.toHexString(ins[1]));
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
        	System.out.println("Processor: Proc " + proc_id + " is blocked");
        	this.pending_instruction = ins;
        } else {
        	this.pending_instruction = null;
        }
        
    	cycle_count++;

        if (cycle_count > 50000) {
        	throw new Exception();
        }
	}

	@Override
	public String toString() {
		return "Processor " +proc_id+" total cycle number : " + cycle_count + "\n" + proc_cache.toString();
	}
	
	
}
