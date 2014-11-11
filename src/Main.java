import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Main {

	static int hexStringToInt(String hs) {
		return (int) Long.parseLong(hs, 16);
	}

	public static void main(String[] args) throws FileNotFoundException {

		String protocol = args[0];
		String file = args[1];
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

		ArrayList<Processor> processors = new ArrayList<>();
        Bus sh_bus = new Bus();

        for (int i = 0; i < no_processors; i++) {
            Cache cache_creation_var = new Cache(i, cache_size, associativity, block_size, sh_bus, protocol);
            Processor p = new Processor(i, sh_bus, cache_creation_var, file);
            processors.add(p);
        }

		while (true) {
			try {
				for (Processor cp: processors) {
					cp.run();
				}

			} catch (Exception e) {
				break;
			}
		}

	}

}
