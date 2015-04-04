package gov.noaa.ncdc.wct.io;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public class FilenamePatternParser {


	/**
	 * Extracts the timestamp from the filename using
	 * the pattern set with 'setFileTimestamp' using the 
	 * format like 'yyyy:8-12; MM:12-14; dd:14-16; HH:17-19; mm:19-21; ss:21-23'.
	 * @param filename
	 * @return
	 * @throws ParseException 
	 */
	public static String getTimestamp(FilenamePattern fp, String filename, String dateFormatString) throws ParseException {
		if (fp.getFileTimestampLocation() == null) {
			return null;
		}
		
		StringBuilder dateFormat = new StringBuilder();
		StringBuilder dateString = new StringBuilder();
		
		String[] fields = fp.getFileTimestampLocation().split(";");
		for (String field : fields) {
			String[] typeAndRange = field.split(":");
			String dateFormatType = typeAndRange[0].trim();
			String[] strRange = typeAndRange[1].split("-");
			int startSubstring = Integer.parseInt(strRange[0].trim());
			int endSubstring = Integer.parseInt(strRange[1].trim());
			
			dateFormat.append(dateFormatType);
			dateString.append(filename.substring(startSubstring, endSubstring));
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.toString());
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//		System.out.println("PARSING: "+dateString+" USING: "+dateFormat);
		Date date = sdf.parse(dateString.toString());
		
//		System.out.println(date);
		
		SimpleDateFormat outSdf = new SimpleDateFormat(dateFormatString);
		outSdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return outSdf.format(date);
	}
	
	
	/**
	 * Gets the source id element value from the filename using the 
	 * supplied pattern for parsing.
	 * @param fp
	 * @param filename
	 * @return
	 */
	public static String getSourceID(FilenamePattern fp, String filename) {
		
		if (fp.getSourceID() == null) {
			return null;
		}
		
		StringBuilder idString = new StringBuilder();
		
		String[] rangeFields = fp.getSourceID().split(";");
		for (String field : rangeFields) {
			String[] strRange = field.split("-");
			int startSubstring = Integer.parseInt(strRange[0].trim());
			int endSubstring = Integer.parseInt(strRange[1].trim());
			
			idString.append(filename.substring(startSubstring, endSubstring));
		}
		
		try {
		    if (fp.getIdAliasLookupTable() != null) {
		        IdAliasLookupTableManager.getInstance().addLookupTable(fp.getIdAliasLookupTable());
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		
        String idAlias = IdAliasLookupTableManager.getInstance().getIdAlias(idString.toString());

        if (idAlias != null) {
            return idAlias;
        }
        else {
            return idString.toString();    
        }
				
	}
	
	
	/**
	 * Gets the productCode element value from the filename using the 
	 * supplied pattern for parsing.
	 * @param fp
	 * @param filename
	 * @return
	 */
	public static String getProductCode(FilenamePattern fp, String filename) {
		StringBuilder productString = new StringBuilder();
		
		// this is optional
		if (fp.getProductCode() == null) {
			return null;
		}
		
		String[] rangeFields = fp.getProductCode().split(";");
		for (String field : rangeFields) {
			String[] strRange = field.split("-");
			int startSubstring = Integer.parseInt(strRange[0].trim());
			int endSubstring = Integer.parseInt(strRange[1].trim());
			
			productString.append(filename.substring(startSubstring, endSubstring));
		}
		return productString.toString();
	}

    /**
     * Gets the product description based on the productCode element value from the 
     * filename using the supplied pattern for parsing.
     * @param fp
     * @param filename
     * @return
     * @throws IOException 
     */
    public static String getProductDesc(FilenamePattern fp, String filename) throws IOException {
        StringBuilder productString = new StringBuilder();
        
        // this is optional
        if (fp.getProductCode() == null) {
            return null;
        }
        
        String[] rangeFields = fp.getProductCode().split(";");
        for (String field : rangeFields) {
            String[] strRange = field.split("-");
            int startSubstring = Integer.parseInt(strRange[0].trim());
            int endSubstring = Integer.parseInt(strRange[1].trim());
            
            productString.append(filename.substring(startSubstring, endSubstring));
        }
        if (fp.getProductLookupTable() != null) {
            ProductLookupTableManager.getInstance().addLookupTable(fp.getProductLookupTable());
        }
        String desc = ProductLookupTableManager.getInstance().getProductDesc(productString.toString());
        
        return desc;
    }

	
	/** 
	 * Matches the supplied DataType in the FilenamePattern configuration
	 * @param fp
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static SupportedDataType getDataType(FilenamePattern fp) throws ClassNotFoundException {
		
		SupportedDataType[] dataTypes = (SupportedDataType[]) Class.forName("gov.noaa.ncdc.wct.io.SupportedDataType").getEnumConstants();
		
		for (SupportedDataType type : dataTypes) {
//			System.out.println(type.name());
			if (type.name().equalsIgnoreCase(fp.getDataType().trim())) {
//				System.out.println("FOUND DATA TYPE MATCH!!!");
				return type;
			}
		}
		throw new ClassNotFoundException("Data type '"+fp.getDataType()+"' was not found.\n" +
				"Supported data types are: "+Arrays.deepToString(dataTypes));
	}
	
	
}
