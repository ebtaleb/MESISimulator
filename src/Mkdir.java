import java.io.File;


public class Mkdir {
	
	public static String mkdir(String bench_name, int cache_size, int associativity, int block_size) {
		
		String name = bench_name+"_CS"+cache_size+"_ASSO"+associativity+"_BS"+block_size;
		File theDir = new File(name);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			boolean result = false;

			try{
				theDir.mkdir();
				result = true;
			} catch(SecurityException se){
				//handle it
			}        
			if(result) {    
				return name;  
			}
		}
		
		return "";
	}

}
