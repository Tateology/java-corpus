/**
*      Copyright (c) 2007-2008 Work of U.S. Government.
*      No rights may be assigned.
*
* LIST OF CONDITIONS
* Redistribution and use of this program in source and binary forms, with or
* without modification, are permitted for any purpose (including commercial purposes) 
* provided that the following conditions are met:
*
* 1.  Redistributions of source code must retain the above copyright notice,
*     this list of conditions, and the following disclaimer.
*
* 2.  Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions, and the following disclaimer in the documentation
*    and/or materials provided with the distribution.
*
* 3.  In addition, redistributions of modified forms of the source or binary
*     code must carry prominent notices stating that the original code was
*     changed, the author of the revisions, and the date of the change.
*
* 4.  All publications or advertising materials mentioning features or use of
*     this software are asked, but not required, to acknowledge that it was
*     developed at the NOAA's National Climatic Data Center in Asheville, NC and to
*     credit the contributors.
*
* 5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS  "AS IS"
*     WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event
*     shall the Government or the Contributors be liable for any damages
*     suffered by the users arising out of the use of this software, even if
*     advised of the possibility of such damage.
*/

package gov.noaa.ncdc.iosp.avhrr;


/**
* This program checks for and reads any NOAA L1B ARS Header given the file path and prints results 
* to the screen. 
*
* @author Philip Jones
* @since 2006-12-13
*/

public class ArsHeader extends AvhrrFile implements AvhrrConstants{
    
    // VARIABLES        
    private String costNumber;
    private String classNumber;
    private String orderCreationYear;
    private String orderCreationDay;
    private String processingSiteCode;
    private String processingSoftwareID;
    private String dataSetName;  
    private String selectFlag;
    private String beginnigLat;
    private String endingLat;
    private String beginnigLon;
    private String endingLon;
    private String startHour;
    private String startMinutes;
    private String numberOfMinutes;
    private String appendDataFlag;
    private String channelSelectFlags;
    private String dataWordSize;
    private String ascDescFlag;
    private String firstLat;
    private String lastLat;
    private String fistLon;
    private String lastLon;
    private String dataFormat;
    private String sizeOfRecords;
    private String numberOfRecords;     
    	
	public void readArsHeader(ucar.unidata.io.RandomAccessFile raf) {
   
        try {         
            // Check if ARS Header exist and read strings                       
            if (hasARSHeader(raf)){            
                // Seek to byte number 0 (ARS Header at beginning of NOAA file)
                raf.seek(0);                                                               
                
                // Read Strings passing raf and string length                
                costNumber = readString(raf, 6);
                classNumber = readString(raf, 8);
                orderCreationYear = readString(raf, 4);
                orderCreationDay = readString(raf, 3);
                processingSiteCode = readString(raf, 1);
                processingSoftwareID = readString(raf, 8);
                dataSetName = readString(raf, 42);        
                
                raf.skipBytes(2);            
                
                selectFlag = readString(raf, 1);
                beginnigLat = readString(raf, 3);
                endingLat = readString(raf, 3);
                beginnigLon = readString(raf, 4);                          
                endingLon = readString(raf, 4);
                startHour = readString(raf, 2);
                startMinutes = readString(raf, 2);
                numberOfMinutes = readString(raf, 3);
                appendDataFlag = readString(raf, 1);
                channelSelectFlags = readString(raf, 20);
                dataWordSize = readString(raf, 2);
                
                raf.skipBytes(27);
                
                ascDescFlag = readString(raf, 1);
                firstLat = readString(raf, 3);
                lastLat = readString(raf, 3);
                fistLon = readString(raf, 4);
                lastLon = readString(raf, 4);
                dataFormat = readString(raf, 20); 
                sizeOfRecords = readString(raf, 6);
                numberOfRecords = readString(raf, 6);                         

            }else{
//                System.out.println("No ARS Header detected\n");
            }
           
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
    
    // Read Methods
    
    // Seeks to position of the Processing Site Code in ARS Header to check for valid field --returns
    // a boolean value
	public static boolean hasARSHeader(ucar.unidata.io.RandomAccessFile raf ) throws java.io.IOException
	{
		String field = "";
        char oneByteChar;
        
        raf.seek(22-1);
        oneByteChar = (char)raf.readByte();
		field = String.valueOf(oneByteChar);
//        System.out.println("hasARSHeader char: " + field);
        if ((field.equals("A"))||(field.equals("S"))||(field.equals("N"))) 
		{
			return true;
		}else 
		{ 
//			System.out.println("No ARS Header present");
			return false;
		}	
	}    


	public String getAppendDataFlag() {
		return appendDataFlag;
	}

	public String getAscDescFlag() {
		return ascDescFlag;
	}

	public String getBeginnigLat() {
		return beginnigLat;
	}

	public String getBeginnigLon() {
		return beginnigLon;
	}

	public String getChannelSelectFlags() {
		return channelSelectFlags;
	}

	public String getClassNumber() {
		return classNumber;
	}

	public String getCostNumber() {
		return costNumber;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public String getDataSetName() {
		return dataSetName;
	}

	public String getDataWordSize() {
		return dataWordSize;
	}

	public String getEndingLat() {
		return endingLat;
	}

	public String getEndingLon() {
		return endingLon;
	}

	public String getFirstLat() {
		return firstLat;
	}

	public String getFistLon() {
		return fistLon;
	}

	public String getLastLat() {
		return lastLat;
	}

	public String getLastLon() {
		return lastLon;
	}

	public String getNumberOfMinutes() {
		return numberOfMinutes;
	}

	public String getNumberOfRecords() {
		return numberOfRecords;
	}

	public String getOrderCreationDay() {
		return orderCreationDay;
	}

	public String getOrderCreationYear() {
		return orderCreationYear;
	}

	public String getProcessingSiteCode() {
		return processingSiteCode;
	}

	public String getProcessingSoftwareID() {
		return processingSoftwareID;
	}

	public String getSelectFlag() {
		return selectFlag;
	}

	public String getSizeOfRecords() {
		return sizeOfRecords;
	}

	public String getStartHour() {
		return startHour;
	}

	public String getStartMinutes() {
		return startMinutes;
	} 
    
}         
