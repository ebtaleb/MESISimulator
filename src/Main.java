import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Main {

	public static void main(String[] args) {

		String file = args[1];
		String line;
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {

            for (int i = 0;  i < 10; i++) {
                line = br.readLine();
                if (line != null)
                    System.out.println(line);

		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
