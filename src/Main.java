import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	static int hexStringToInt(String hs) {
		return (int) Long.parseLong(hs, 16);
	}

	public static void main(String[] args) {

		String[] split_line = null;
		int[] int_split_line = null;
		String line;

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

		ArrayList<int[]> ins_list = new ArrayList<>();

		try(BufferedReader br = new BufferedReader(new FileReader(file))) {

            for (int i = 0;  i < 500; i++) {
                line = br.readLine();
                if (line != null) {
                    int_split_line = new int[2];
                    split_line = line.split(" ");
                    int_split_line[0] = Integer.parseInt(split_line[0]);
                    int_split_line[1] = hexStringToInt(split_line[1]);
                    ins_list.add(int_split_line);
                }
            }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 500; i++) {
			System.out.println("[ " + ins_list.get(i)[0] + ", " + Integer.toHexString(ins_list.get(i)[1]) + " ]");
		}

	}

}
