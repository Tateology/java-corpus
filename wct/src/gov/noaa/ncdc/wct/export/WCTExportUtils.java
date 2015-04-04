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

package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;

public class WCTExportUtils {

    public static String getExportFileExtension(WCTExport exporter) {
        return getExportFileExtension(exporter.getOutputFormat());  
    }

    public static String getExportFileExtension(ExportFormat outputFormat) {

        if (outputFormat == ExportFormat.SHAPEFILE) {
            return ".shp";
        }
        else if (outputFormat == ExportFormat.WKT) {
            return ".wkt";
        }
        else if (outputFormat == ExportFormat.GML) {
            return ".gml";
        }
        else if (outputFormat == ExportFormat.CSV) {
            return ".csv";
        }
        else if (outputFormat == ExportFormat.ARCINFOASCII) {
            return ".asc";
        }
        else if (outputFormat == ExportFormat.ARCINFOBINARY) {
            return ".flt";
        }
        else if (outputFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT) {
            return "-8bit.tif";
        }
        else if (outputFormat == ExportFormat.GEOTIFF_32BIT) {
            return ".tif";
        }
        else if (outputFormat == ExportFormat.GRIDDED_NETCDF) {
            return ".nc";
        }
        else if (outputFormat == ExportFormat.RAW_NETCDF) {
            return ".nc";
        }
        else if (outputFormat == ExportFormat.KMZ) {
        	return ".kmz";
        }


        return "";
    }


    
    public static String getOutputFilename(String inputFilename, ExportFormat outputFormat) {
    	
    	String[] extList = new String[] { ".gz", ".Z", ".bz2", ".zip" };
    	for (String ext : extList) {
    		if (inputFilename.endsWith(ext)) {
    			inputFilename = inputFilename.substring(0, inputFilename.length()-ext.length());
    			break;
    		}
    	}
    	
    	inputFilename = inputFilename + getExportFileExtension(outputFormat);
    	
    	return inputFilename;    	
    }

}
