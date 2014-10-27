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

		String file = args[1];
		String[] split_line = null;
		int[] int_split_line = {0, 0};
		String line;

		ArrayList<int[]> ins_list = new ArrayList<>();

		try(BufferedReader br = new BufferedReader(new FileReader(file))) {

            for (int i = 0;  i < 500; i++) {
                line = br.readLine();
                if (line != null)
                    split_line = line.split(" ");
                    int_split_line[0] = Integer.parseInt(split_line[0]);
                    int_split_line[1] = hexStringToInt(split_line[1]);
                    ins_list.add(int_split_line);
                    System.out.println(split_line[0] + " " + split_line[1]);
                    System.out.println(Integer.toHexString(int_split_line[1]));

		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
