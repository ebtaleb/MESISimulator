import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.File;

public class Main {

	static int hexStringToInt(String hs) {
		return (int) Long.parseLong(hs, 16);
	}

	public static void main(String[] args) throws FileNotFoundException {

		String protocol = args[0];
		String trace_dir = args[1];
		int no_processors = Integer.parseInt(args[2]);
		int cache_size = Integer.parseInt(args[3]);
		int associativity = Integer.parseInt(args[4]);
		int block_size = Integer.parseInt(args[5]);

		switch (associativity) {
			case 1: break;
			case 2: break;
			case 4: break;
			default: System.exit(0); break;
		}

		switch (no_processors) {
			case 1: break;
			case 2: break;
			case 4: break;
			case 8: break;
			default: System.exit(0); break;
		}

		switch (cache_size) {
			case 1024: break;
			case 2048: break;
			case 4096: break;
			case 8192: break;
			case 16384: break;
			case 32768: break;
			default: System.exit(0); break;
		}

		switch (block_size) {
			case 1: break;
			case 2: break;
			case 4: break;
			case 8: break;
			case 16: break;
			case 32: break;
			case 64: break;
			case 128: break;
			default: System.exit(0); break;
		}
		
		String output_dir = Mkdir.mkdir(trace_dir, cache_size, associativity, block_size);

		ArrayList<Processor> processors = new ArrayList<>();
        Bus sh_bus = new Bus(protocol);

		File dir = new File(trace_dir);
		File[] directoryListing = dir.listFiles();
		
		boolean uniproc_flag = false;
		if (no_processors == 1) {
			uniproc_flag = true;
		}

        for (int i = 0; i < no_processors; i++) {
            Cache cache_creation_var = new Cache(i, cache_size, associativity, block_size, sh_bus, uniproc_flag);
            
            Processor p = new Processor(i, cache_creation_var, directoryListing[i].getAbsolutePath());
            processors.add(p);
            sh_bus.addCache(cache_creation_var);
        }

		while (true) {
			try {
				for (Processor cp: processors) {
					cp.run();
				}
				sh_bus.processBusRequests();

			} catch (Exception e) {
				System.out.println("Total bus traffic : " + sh_bus.getBusTraffic() + " bytes");
				break;
			}
		}
	}
}
