package gov.noaa.ncdc.wct.io;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;


public class THREDDSScanner {

	public interface URLAction {
		public void processURL(URL url);
	}
	
	private String dirRegex = ".*";
	private String fileRegex = ".*";


    /**
     * By default, not recursive
     * @param catalogURI
     * @throws MalformedURLException
     */
    public void process(String catalogURI, ServiceType serviceType, URLAction[] actionArray) throws MalformedURLException {
        process(catalogURI, false, serviceType, actionArray);
    }
    /**
     * 
     * @param catalogURI
     * @param recursive
     * @param serviceType ( 
     * @throws MalformedURLException
     */
    public void process(String catalogURI, boolean recursive, ServiceType serviceType, URLAction[] actionArray) throws MalformedURLException {

        InvCatalogFactory factory = new InvCatalogFactory("default", false);
        InvCatalogImpl catalog = (InvCatalogImpl) factory.readXML( catalogURI);
        List datasets = catalog.getDatasets();
        
        // TOP LEVEL
        for (int n=0; n<datasets.size(); n++) {
            System.out.println(datasets.get(n)); 
            
            InvDataset topDataset = (InvDataset)datasets.get(n);
            
            processDataset(topDataset, recursive, serviceType, actionArray);
            
        }

    }
    

    private void processDataset(InvDataset dataset, boolean recursive, ServiceType serviceType, URLAction[] actionArray) throws MalformedURLException {

        
        System.out.println("PROCESSING DATASET: "+dataset.getFullName());
//        System.out.println("PROCESSING CATALOG: "+dataset.getCatalogUrl());
        
        
        List<InvDataset> nestedDatasets = dataset.getDatasets();
        
        for (int i=0; i<nestedDatasets.size(); i++) {
            InvDataset nestedDataset = (InvDataset)nestedDatasets.get(i);

            if (nestedDataset.hasNestedDatasets() && recursive) {
                if (! nestedDataset.getFullName().matches(dirRegex)) {
                	System.out.println("NO MATCH TO dirRegex="+dirRegex+"    :::: "+nestedDataset.getFullName());
                	continue;
                }
                
            	System.out.println("MATCH TO dirRegex="+dirRegex+"    :::: "+nestedDataset.getFullName());
                processDataset(nestedDataset, recursive, serviceType, actionArray);
            }
            else {
            
                //System.out.println("nested: "+nestedDataset); 
                //System.out.println("nestedURL: "+nestedDataset.getCatalogUrl()); 

                List accessTypes = nestedDataset.getAccess();
                for (int j=0; j<accessTypes.size(); j++) {
                    //System.out.println("accessType: "+accessTypes.get(j));
                }
                               
                InvAccess access = nestedDataset.getAccess(serviceType);
                
                if (access == null) {
                    System.out.println("NULL ACCESS: Dataset="+nestedDataset);
                    continue;
                }
                
                String urlString = access.getStandardUrlName();
                String filename = urlString.substring(urlString.lastIndexOf('/')+1, urlString.length());
                if (! filename.matches(fileRegex)) {
                	continue;
                }
                
//                System.out.println("access - getUrlPath: "+access.getUrlPath());
                //System.out.println("access - getStandardUrlName: "+access.getStandardUrlName()); // this will work!

                for (URLAction action : actionArray) {
                	action.processURL(new URL(access.getStandardUrlName()));
                }
            }
        }

    }

    
	public void setDirRegex(String dirRegex) {
		this.dirRegex = dirRegex;
	}
	public String getDirRegex() {
		return dirRegex;
	}
	public void setFileRegex(String fileRegex) {
		this.fileRegex = fileRegex;
	}
	public String getFileRegex() {
		return fileRegex;
	}

    

	
	public static void main(String[] args) {
//		example2(args);
		run(args);
	}
	
    private static void example1(String[] args) {
        try {
            THREDDSScanner test = new THREDDSScanner();
            
            URLAction action = new URLAction() {
				@Override
				public void processURL(URL url) {
					// do anything here...
					// process URL with something, add to arraylist, whatever...
					System.out.println("Found URL: "+url);
				}
            };
            
            
            //String catalogURI = "http://undertow-d:8080/thredds/catalog/testAll/NDFD_WaveHeights_netCDF/catalog.xml";
//            String catalogURI = "http://motherlode.ucar.edu:8080/thredds/catalog/nexrad/level3/NCR/VTX/catalog.xml";
//          test.process(catalogURI, true, ServiceType.HTTPServer);
            
            
            String catalogURI = "http://nomads.ncdc.noaa.gov/thredds/catalog/cfsr1hr/catalog.xml";
            test.setDirRegex("^[0-9]{6}$");
            test.setFileRegex("^uswtoa.*");
            test.process(catalogURI, true, ServiceType.OPENDAP, new URLAction[] { action });
            
            
//            
//            
//            String catalogURI2 = "http://motherlode.ucar.edu:8080/thredds/catalog/nexrad/level3/NCR/GSP/catalog.xml";
//            test.process(catalogURI2, true);
//            
            
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    

    // save a bunch of level-3 radar files from unidata to local disk
    private static void example2(String[] args) {
        try {
            THREDDSScanner test = new THREDDSScanner();
            
            final File saveDir = new File("H:\\Radar Backfill Unidata\\KLSX");
            
            URLAction action = new URLAction() {
				@Override
				public void processURL(URL url) {
					try {
						WCTTransfer.getURL(url, saveDir);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
            };
            
            
            String catalogURI = "http://motherlode.ucar.edu:8080/thredds/catalog/nexrad/level3/catalog.xml";
            test.setDirRegex(
            		"^NEXRAD Level III Radar/[A-Z]{3}|" +
            		 "[A-Z]{3}/LSX|" +
            		 "LSX/20110423$");
            test.setFileRegex("^Level3_LSX_.*");
            test.process(catalogURI, true, ServiceType.HTTPServer, new URLAction[] { action });
            
            
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void run(String[] args) {
    	
    	Options options = new Options();
    	options.addOption(new Option("h", "print this message"));
    	options.addOption(new Option("r", "crawl through child catalogs recursively"));
    	options.addOption(new Option("l", "just list the files, no download"));
    	options.addOption(OptionBuilder.withArgName("regex").hasArg().withDescription("dataset full name regex to match").create("dregex"));
    	options.addOption(OptionBuilder.withArgName("regex").hasArg().withDescription("individual file name regex to match").create("fregex"));
    	
    	CommandLineParser parser = new GnuParser();
    	try {
			CommandLine cl = parser.parse(options, args);
			
			if (cl.hasOption("h") || cl.getOptions().length == 0) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("thredds-crawl", options);
			}
			
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    	
    }

}
