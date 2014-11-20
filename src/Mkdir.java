import java.io.File;


public class Mkdir {
	
	public static String mkdir(String bench, int num_proc, int cache_size, int associativity, int block_size) {
		
		String name = bench+num_proc+"_CS"+cache_size+"_ASSO"+associativity+"_BS"+block_size;
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
		
		return name;
	}

}
