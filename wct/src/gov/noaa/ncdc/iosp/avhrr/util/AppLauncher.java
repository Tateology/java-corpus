package gov.noaa.ncdc.iosp.avhrr.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class AppLauncher implements ConverterConstants {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	/**	
		InputStream stream = null;
		BufferedReader br = null;
		try{
			stream = AppLauncher.class.getResourceAsStream("/help.html");
			InputStreamReader rd = new InputStreamReader(stream);
			br = new BufferedReader(rd);
			String line;
			while ((line = br.readLine()) != null) {
				 System.out.println(line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally{
		      try {
		          if (br != null) br.close();
		          if (stream != null) stream.close();
		        }
		        catch (IOException e) {
		          e.printStackTrace();
		        }
		}
	   */
		Options options = new Options();
		options.addOption("memory", false, "java heap space");

		// now lets parse the input
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		String memory = "1024M";
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("memory")) {
				memory = cmd.getOptionValue("memory");
			}
		} catch (ParseException pe) {
			// usage(options);
			// return;
		}
		String optionString = "";
		for (String s : args) {
			optionString += " " + s;
		}

		Runtime rt = Runtime.getRuntime();

		try {
			String process = "java -Xmx" + memory + " -cp sat2netcdf-0.5.jar gov.noaa.ncdc.iosp.avhrr.util.AvhrrLevel1B2Netcdf  "
					+ optionString;
//			System.out.println("Executing process: " + process);
			Process proc = rt.exec(process);
			InputStream is = proc.getInputStream();
			InputStream es = proc.getErrorStream();

			// handle error and output streams form process
			OutputHandler errorHandler = new OutputHandler(es, "Error");
			OutputHandler outputHandler = new OutputHandler(is, "Output");
			errorHandler.start();
			outputHandler.start();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * prints program options to screen
	 * 
	 * @param options
	 */
	private static void usage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("AvhrrLevel1B2Netcdf", options);
	}
}
