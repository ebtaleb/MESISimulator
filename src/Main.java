
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

public class Main {

	static int hexStringToInt(String hs) {
		return (int) Long.parseLong(hs, 16);
	}

	public static void main(String[] args) throws IOException {

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

		String output_dir = Mkdir.mkdir(protocol, trace_dir.substring(0, 3), no_processors, cache_size, associativity, block_size);

		File dir = new File(trace_dir);
		File[] directoryListing = dir.listFiles();
		
		boolean uniproc_flag = false;
		if (no_processors == 1) {
			uniproc_flag = true;
		}
		
		ArrayList<Processor> processors = new ArrayList<>();
        Bus sh_bus = new Bus(protocol, uniproc_flag, output_dir+"/bus_traffic");

        for (int i = 0; i < no_processors; i++) {
            Cache cache_creation_var = new Cache(i, cache_size, associativity, block_size, sh_bus, uniproc_flag);
            
            Processor p = new Processor(i, cache_creation_var, directoryListing[i].getAbsolutePath(), output_dir+"/out"+i);
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
				System.out.println(e.toString());
				for (Processor cp: processors) {
					System.setOut(cp.getStream());
					System.out.println(cp.toString());
				}
				System.setOut(sh_bus.getStream());
				System.out.println("Total bus traffic : " + sh_bus.getBusTraffic() + " bytes");
				int total_hits = 0;
				int total_misses = 0;
				for (Processor cp: processors) {
					total_hits += cp.getCacheHits();
					total_misses += cp.getCacheMisses();
				}
				System.out.println("Total hits: "+total_hits);
				System.out.println("Total misses: "+total_misses);
				float miss_rate = (total_misses/(total_hits + total_misses));
				System.out.println("Miss rate: "+ String.format( "%.4f", miss_rate ));
				break;
			}
		}
	}
}
