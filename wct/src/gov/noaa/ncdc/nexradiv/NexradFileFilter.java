/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.ScanResults;
import gov.noaa.ncdc.wct.io.SupportedDataType;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public class NexradFileFilter extends FileFilter {

	public static final int SHOW_LEVEL2 = 0;
	public static final int SHOW_LEVEL3 = 1;
	public static final int SHOW_BOTH = 2;

	private int type;
	private boolean directories;
	private String description;
	private String[] work;

	private FileScanner fileScanner;
	// Constructor
	public NexradFileFilter(int type, boolean directories, String description) throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		this.type = type;
		this.directories = directories;
		this.description = description;
		
		fileScanner = new FileScanner();
	}

	public boolean accept(File f) {
		// Accept or reject directories
		if (f.isDirectory()) {
			return directories;
		}

		try {

			ScanResults scanResults = fileScanner.quickScan(f.getName());

			if (scanResults.getDataType() == SupportedDataType.RADIAL
					&& type == SHOW_LEVEL2) {
				return true;
			} else if (scanResults.getDataType() == SupportedDataType.NEXRAD_LEVEL3
					&& type == SHOW_LEVEL3) {
				return true;
			} else if (scanResults.getDataType() != SupportedDataType.UNKNOWN
					&& type == SHOW_BOTH) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			return false;
		}
	}

	//The description of this filter
	public String getDescription() {
		return description;
	}
}
