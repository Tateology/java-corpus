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

import java.io.IOException;

public abstract class AvhrrFile {
	
	
    // Reads x-number of bytes (size) and returns as a concatenated string
    public static String readString(ucar.unidata.io.RandomAccessFile raf, int size) throws IOException
	{
		String field = "";
        char oneByteChar;
        
		for (int j = 0; j < size; j++){
            oneByteChar = (char)raf.readByte();
			field += String.valueOf(oneByteChar);
        }
		return field;
	} 
    
    // Reads x-number of short integers (size) and returns values in a short array
    public static short[] readShortArray(ucar.unidata.io.RandomAccessFile raf, int size) throws IOException
	{
		short[] array = new short[size];        
        
		for (int j = 0; j < size; j++){
            array[j] = raf.readShort();
        }
		return array;
	} 

    // Reads x-number of integers (size) and returns values in a array
    public static int[] readIntArray(ucar.unidata.io.RandomAccessFile raf, int size) throws IOException
	{
		int[] array = new int[size];        
        
		for (int j = 0; j < size; j++){
            array[j] = raf.readShort();
        }
		return array;
	}    
    
    
    // Reads an integer (4 bytes) and returns a positive, unsigned integer as a long
    public static long readUnsignedInt(ucar.unidata.io.RandomAccessFile raf) throws IOException
	{
		int test;
        long value;
        
        test = raf.readInt();
        if (test < 0){
            value = 4294967296L + test  ; // = (2^32 - abs(test))
        }else{
            value = test;
        }
        return value;        
	}     
    
    // Reads 1-bit mask (0x01) shifted to a position (pos) in the integer (bitField) --returns a 0 or 1
    public static int readOneBitFlag(int bitField, int pos) {     
          int flag = (bitField & (0x01 << pos)) >> pos;   
          return flag;      
    }   

    // Reads 2-bit mask (0x03) shifted to the lower of the two bits position (pos) in the integer 
    // (bitField), so if bit flag to read is bits 5-4, then pos = 4 --returns a 0, 1, 2, or 3     
    public static int readTwoBitFlag(int bitField, int pos) {  
          int flag = (bitField & (0x03 << pos)) >> pos;   
          return flag;      
    }             
    
    // Reads 3-bit mask (0x07) shifted to the lower of the three bits position (pos) in the integer 
    // (bitField), so if bit flag to read is bits 6-5-4, then pos = 4 --returns a 0, 1, 2, or 3     
    public static int readThreeBitFlag(int bitField, int pos) {  
          int flag = (bitField & (0x07 << pos)) >> pos;   
          return flag;      
    }
    
    
    // Reads 4-bit mask (0x0f) shifted to the lower of the four bits position (pos) in the integer 
    // (bitField), so if bit flag to read is bits 7-4, then pos = 4 --returns a 0 to 15      
    public static int readFourBitFlag(int bitField, int pos) {  
          int flag = (bitField & (0x0f << pos)) >> pos;   
          return flag;       
    }
    
    
    public static int readSevenBitFlag(int bitField, int pos) {
    	int flag = (bitField >>> pos) & 0x7f;
    	return flag;
    }
    
    public static int readNineBitFlag(int bitField, int pos){
    	int flag = (bitField >>> pos) & (0x1ff);
    	return flag;
    }
    
    // Reads 1-bit mask (0x01) shifted to a position (pos) in the integer (bitField) --returns a 0 or 1
    public static boolean readOneBitFlagAsBoolean(int bitField, int pos) {     
    	int flag = (bitField & (0x01 << pos)) >> pos;
        if(flag == 0){
        	return false;
        }else{
        	return true;
        }
    } 
    
    
}
