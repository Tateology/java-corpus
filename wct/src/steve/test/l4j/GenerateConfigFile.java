package steve.test.l4j;

import java.io.File;

public class GenerateConfigFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File libDir = new File("C:\\devel\\ndit\\lib");
		String[] filenames = libDir.list();
		
		for (String file : filenames) {
			
			System.out.println("    <cp>lib/"+file+"</cp>");
			
		}

	}

}
