package gov.noaa.ncdc.iosp.avhrr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Handles output and error streams form a process.
 * Prevents process from hanging on windows by reading both error and 
 * output concurrently
 * 
 * @author afotos
 * March 06 2008
 */
public class OutputHandler extends Thread {

	InputStream is;
	String type;
	
	OutputHandler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(line);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}