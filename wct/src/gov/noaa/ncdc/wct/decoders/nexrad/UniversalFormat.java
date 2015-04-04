//***********************************************************************
//									*
//  File: UniversalFormat.java						*
//									*
//  Description: This class provides an interface to Universal Format	*
//               radar data as defined in the report by Barnes, et al 	*
//               in the June 1980 issue of the Bulletin of the American	*
//		 Meteorological Society.				*
//									*
//  Developer: David L. Priegnitz					*
//             CIMMS/NSSL						*
//             1313 Halley Circle					*
//             Norman, OK  73069					*
//             (405)-366-0577						*
//             David.Priegnitz@noaa.gov					*
//                                                                      *
//  History:   09/13/04 - created					*
//									*
//***********************************************************************

package gov.noaa.ncdc.wct.decoders.nexrad;
  
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;

//----------------------------------------------------------------------
//  Universal Format class definition
//----------------------------------------------------------------------

public class UniversalFormat {

// Constants

  static public final int SIZE_OF_RADAR_NAME                =  8;
  static public final int SIZE_OF_SITE_NAME                 =  8;
  static public final int SIZE_OF_TIME_ZONE                 =  2;
  static public final int SIZE_OF_GENERATION_FACILITY_NAME  =  8;
  static public final int MAX_NUMBER_OF_FIELDS              = 16;
  static public final int SIZE_OF_PROJECT_NAME              =  8;
  static public final int SIZE_OF_FIELD_TAPE_NAME           =  8;
  static public final int SIZE_OF_FIELD_NAME                =  2;
  static public final int SIZE_OF_EDIT_CODE                 =  2;

  static public final int UF_RECORD_TYPE_1                  =  1;
  static public final int UF_RECORD_TYPE_2                  =  2;

  static public final int MAX_FIELDS                        =    8;
  static public final int MAX_CUTS                          =   50;
  static public final int MAX_BINS                          = 2048;
  static public final int MAX_RADIALS_IN_CUT                =  512;

  static public final int EOF_FLAG     = -1;
  static public final int IOERROR_FLAG = -2;

  static public final int MOMENT_TYPE_UNKNOWN        = 0;
  static public final int MOMENT_TYPE_REFLECTIVITY   = 1;
  static public final int MOMENT_TYPE_VELOCITY       = 2;
  static public final int MOMENT_TYPE_SPECTRUM_WIDTH = 3;

  static public final String reflectivityFields[]  = {"CZ", "DZ", "Z1", "S1"};
  static public final String velocityFields    []  = {"VF", "VE", "VR", "VT",
  				"VP", "V1"};
  static public final String spectrumWidthFields[] = {"SW", "W1"};				
// Mandatory Header Block Fields

  static int   recordSize                 = 0;
  static byte  U;
  static byte  F;
  static short recordLength               = 0;
  static short optionalHeaderBlock        = 0;
  static short localUseHeaderBlock        = 0;
  static short dataHeaderBlock            = 0;
  static short physicalRecordNumberFile   = 0;
  static short volumeScanNumber           = 0;
  static short rayNumber                  = 0;
  static short physicalRecordNumberRay    = 0;
  static short sweepNumber                = 0;
  static byte  radarName[] = new byte [SIZE_OF_RADAR_NAME];
  static byte  siteName[]  = new byte [SIZE_OF_SITE_NAME];
  static short latitudeDegrees            = 0;
  static short latitudeMinutes            = 0;
  static short latitudeSeconds64          = 0;
  static short longitudeDegrees           = 0;
  static short longitudeMinutes           = 0;
  static short longitudeSeconds64         = 0;
  static short antennaHeight              = 0; // meters above sea level
  static short year                       = 0;
  static short month                      = 0;
  static short day                        = 0;
  static short hour                       = 0;
  static short minute                     = 0;
  static short second                     = 0;
  static byte  timeZone[] = new byte [SIZE_OF_TIME_ZONE];
  static short azimuth64                  = 0; // degrees x 64
  static short elevation64                = 0; // degrees x 64
  static short sweepMode                  = 0;
  static short fixedAngle64               = 0; // degrees x 64
  static short sweepRate64                = 0; // degrees/sec x 64
  static short generationYear             = 0;
  static short generationMonth            = 0;
  static short generationDay              = 0;
  static byte  generationFacility[] = new byte [SIZE_OF_GENERATION_FACILITY_NAME];
  static short missingDataFlag            = 0;

// Optional Header Block Fields

  static byte  projectName[] = new byte [SIZE_OF_PROJECT_NAME];
  static short baselineAzimuth64          = 0; // degrees x 64
  static short baselineElevation64        = 0; // degrees x 64
  static short volumeHour                 = 0;
  static short volumeMinute               = 0;
  static short volumeSecond               = 0;
  static byte  fieldTapeName[] = new byte [SIZE_OF_FIELD_TAPE_NAME];
  static short optionFlag                 = 0;

// Data Header Fields

  static short rayFields                  = 0;
  static short rayRecords                 = 0;
  static short recordFields               = 0;
  static String  fieldName[]     = new String [MAX_FIELDS*SIZE_OF_FIELD_NAME];
  static short fieldPosition[]            = new short [MAX_FIELDS];

// Field Header Fields

  static short dataPosition[]          = new short [MAX_FIELDS];
  static short scaleFactor[]           = new short [MAX_FIELDS];
  static short rangeFirstGate[]        = new short [MAX_FIELDS]; // meters
  static short gateAdjustment[]        = new short [MAX_FIELDS]; // meters
  static short sampleSpacing[]         = new short [MAX_FIELDS]; // meters
  static short numberSamples[]         = new short [MAX_FIELDS]; 
  static short sampleDepth[]           = new short [MAX_FIELDS]; // meters
  static short horizontalBeamWidth64[] = new short [MAX_FIELDS]; // degrees x 64
  static short verticalBeamWidth64[]   = new short [MAX_FIELDS]; // degrees x 64
  static short bandwidth[]             = new short [MAX_FIELDS]; // MHz
  static short polarization[]          = new short [MAX_FIELDS]; // 0 - horizontal; 1 - vertical;
                                        // 2 - circular; > 2 - elliptical
  static short wavelength64[]          = new short [MAX_FIELDS]; // cm x 64
  static short sampleAverage[]         = new short [MAX_FIELDS]; // number of samples used in estimate
  static byte  thresholdField[][] = new byte [MAX_FIELDS][SIZE_OF_FIELD_NAME]; // 2 ASCII
  static short thresholdValue[]        = new short [MAX_FIELDS];
  static short scale[]                 = new short [MAX_FIELDS];
  static byte  editCode[][] = new byte [MAX_FIELDS][SIZE_OF_EDIT_CODE]; // 2 ASCII
  static short prt[]                   = new short [MAX_FIELDS]; // pulse repetition time (microsec)
  static short bitsPerSample[]         = new short [MAX_FIELDS]; // bits per sample volume
  static int   binData[][] = new int [MAX_FIELDS][MAX_BINS];

// Cut data

  static int   cutNumberRadials;
  static float cutAzimuth[]            = new float[MAX_RADIALS_IN_CUT];
  static float cutElevation[]          = new float[MAX_RADIALS_IN_CUT];
  static int   cutMomentNumberSamples[][] = new int  [MAX_RADIALS_IN_CUT][MAX_FIELDS];
  static int   cutMomentScale        [][] = new int  [MAX_RADIALS_IN_CUT][MAX_FIELDS];
  //static int   cutMomentBinData[][][]  = new int  [MAX_RADIALS_IN_CUT][MAX_FIELDS][MAX_BINS];
  static int   cutMomentBinData[][][]  = new int  [MAX_RADIALS_IN_CUT][MAX_FIELDS][MAX_BINS];
  static int cutBinData[][] = new int [MAX_RADIALS_IN_CUT][MAX_BINS];
 

  static int   cutHour   = 0;
  static int   cutMinute = 0;
  static int   cutSecond = 0;
  static int   cutMonth  = 0;
  static int   cutDay    = 0;
  static int   cutYear   = 0;

  static int   cutRecord = 0;
  
// Field Header Fields that are depedent on the field

// For VF, VE, VR, VT, VP (velocity fields)

  static short nyquistVelocity[]       = new short [MAX_FIELDS];  // scaled m/s
  static byte  FL[][] = new byte [MAX_FIELDS][SIZE_OF_FIELD_NAME]; // lsb of data; 1 = good, 0 = bad

// static For DM

  static short radarConstant[]         = new short [MAX_FIELDS];  // dB(Z) = RC+data/scale+20log(range)
                                         // in km
  static short noisePower[]            = new short [MAX_FIELDS];  // dB (mW) x scale
  static short receiverGain[]          = new short [MAX_FIELDS];  // dB x scale
  static short peakPower[]             = new short [MAX_FIELDS];  // dB (mW) x scale
  static short antennaGain[]           = new short [MAX_FIELDS];  // dB x scale
  static short pulseDuration64[]       = new short [MAX_FIELDS];  // microseconds x 64

//
  static File UFfile = null;
  static int  numberOfRecords = 0;
  static RandomAccessFile din;
  static byte data[] = new byte[32768];

  static int UFRecordType = 1;
  static int offset       = 0; // Byte pointer for random access
  
  static int   recordLUT   [] = new int  [32768]; // Byte offset to each
  						  // record in the file
  static int   azimuth64LUT[] = new int  [32768]; // Scaled Azimuth angle;Scaled Azimuth angle;
  static float azimuthLUT  [] = new float[32768]; // Azimuth angle for each
  						  // record in the file
  static float elevationLUT[] = new float[32768]; // Elevation angle for each
  						  // record in the file
  static int   fixedLUT    [] = new int  [32768]; // Fixed angle for each
                                                  // record in the file

  static int   cut64LUT    [] = new int  [32768]; // Scaled set of unique
                                                  // fixed angles in the file
  static float cutLUT      [] = new float[32768]; // Unscaled set of unique
  						  // fixed angles in the file
  static String fieldNameLUT[] = new String [MAX_FIELDS*SIZE_OF_FIELD_NAME];
  static int   fieldTypeLUT [] = new int [MAX_FIELDS];
  static int   numberOfFields = 0;                // Number of unique fields
                                                  // in volume
  static int recordFieldsLUT[][] = new int [32768][MAX_FIELDS];
  static int cutFieldsLUT[]  = new int [MAX_FIELDS];
  static int fieldInCut [][] = new int [MAX_CUTS][MAX_FIELDS];
  static int fieldsInCut []  = new int [MAX_CUTS];
  static int startOfCut []   = new int [MAX_CUTS];

//  NOTE:  For regular volume scans the fixed angles are elevavtion angles.
//         For RHI type volume scans the fixed angles are azimuth angles.

  static int   numberOfCuts   = 0;

  static String file = null;
  static String dir  = null;

//---------------------------------------------------------------------
//  Constructor
//---------------------------------------------------------------------

  void UniversalFormat() {
  }
//__________________________-


  
  
  

//--------------------------------------------------------------------
//  Method to open a Universal Format File and build radial table
//--------------------------------------------------------------------

  public static int open (File file) {

    int ret;
    boolean found;

//  Read the entire file, building a radial table in the process

    try {

      if (file != UFfile) {

        UFfile          = file;
	numberOfCuts    = 0;
	numberOfRecords = 0;
	numberOfFields  = 0;
	offset          = 0;

	for (int field=0;field<MAX_FIELDS;field++)
	  for (int cut=0;cut<MAX_CUTS;cut++)
	    fieldInCut [0][field] = 0;

        din = new RandomAccessFile (UFfile,"r");

	System.out.println ("Reading data and building internal tables....This may take a while.....");

//      The first two bytes in the file must contain the characters
//      "UF" or "uf".  Some of the older UF files were written in a
//      slightly different format that probably goes back to the old
//      FORTRAN days when the record length was written at the beginning
//      and end of each record.

        U = din.readByte();
	F = din.readByte();
	  
	UFRecordType = UF_RECORD_TYPE_2;

	if (U == 85) {
	  if (F == 70) {
	  
	    UFRecordType = UF_RECORD_TYPE_1;
	    System.out.println ("UF_RECORD_TYPE_1");

          }
        }

//      Restart reading at the beginning of the file.

        din.seek(0);

//      Loop until the end of file.  The assumption here is that the
//      file contains at most 32768 records.

        for (int i=0;i<32768;i++)  {

          ret = readMandatoryHeader();

	  if (ret == IOERROR_FLAG) {

            System.out.println ("ERROR reading mandatory header");
	    return (0);

	  } else if (ret == EOF_FLAG) {

//          Display the mnemonics that were found in the file.  This
//          will possibly help to debug problems in the file if they exist.

            for (int j=0;j<numberOfFields;j++)
              System.out.println ("field ["+j+"] = "+fieldNameLUT[j]+
	      	" --- Type: "+fieldTypeLUT[j]);
	    return (0);

	  }

//        At this time we don't care about anything from the optional
//        use and local header blocks.  What we want to do is skip over
//        them to the start of the data header block.

          din.readFully (data,0,(dataHeaderBlock-46)*2);

//        The next thing we want to do is read the data header
//        so we can determine which fields are available.

          readDataHeader();

//        The last thing is to read the data.

          readData();

//        Build radial lookup table.

          recordLUT   [numberOfRecords] = offset;
	  azimuth64LUT[numberOfRecords] = azimuth64;
	  azimuthLUT  [numberOfRecords] = ((float) azimuth64/((float) 64.0));
	  elevationLUT[numberOfRecords] = ((float) elevation64/((float) 64.0));
	  fixedLUT    [numberOfRecords] = fixedAngle64;

//          System.out.println ("Record: "+numberOfRecords+" -- ["+
//	      azimuthLUT[numberOfRecords]+","+
//	      elevationLUT[numberOfRecords]+"]");

//        Next we want to check the field names so we can build a field
//        name lookup table.  We need to do this because there is no
//        guarantee that the all field names exist in all records.
//        We also want to create a lookup table associating fields
//        with each record since all records may not contain the same
//        fields.  As an example, the initial phased array moment data
//        contain a calibration set at the beginning of the file
//        followed by the regular set of data.

          for (int field=0;field<recordFields;field++) {

            found = false;

//          Loop through the field in the current record and check if
//          is already defined.

            for (int fields=0;fields<numberOfFields;fields++) {
	      if (fieldName[field].equals(fieldNameLUT[fields])) {
                recordFieldsLUT [numberOfRecords][field] = fields;
                found = true;
	      }
	    }

//          If the mnemonic was not yet defined in the lookup table
//          add it to the end of the table.

	    if (!found) {
	      fieldNameLUT[numberOfFields] = fieldName[field];
	      fieldTypeLUT[numberOfFields] = getFieldType(fieldName[field]);
              recordFieldsLUT [numberOfRecords][field] = numberOfFields;
	      numberOfFields++;
	    }
	  }

//        We want to build a cut table so after we read the file we
//        know the unique elevation cuts in the file.

          if (numberOfCuts == 0) {

            cut64LUT   [0] = fixedAngle64;
            cutLUT     [0] = ((float) fixedAngle64/((float)64.0));
	    startOfCut [0] = numberOfRecords;
	    fieldsInCut[0] = recordFields;
	    
	    for (int field=0;field<recordFields;field++) {
	      fieldInCut [0][field] = recordFieldsLUT[numberOfRecords][field];
//	      System.out.println ("fieldInCut["+numberOfCuts+"]["+field+"] = "+fieldInCut[numberOfCuts][field]);
	    }

	    numberOfCuts++;
//	    System.out.println ("Cut ["+numberOfCuts+"] = "+fixedAngle64);
	    
	  } else {

            found = false;

            for (int cut=0;cut<numberOfCuts;cut++) {

              if (fixedAngle64 == cut64LUT[cut]) {

                found = true;
		break;

              }
            }

	    if (!found) {

              cut64LUT   [numberOfCuts] = fixedAngle64;
              cutLUT     [numberOfCuts] = ((float) fixedAngle64/((float)64.0));
	      startOfCut [numberOfCuts] = numberOfRecords;
	      fieldsInCut[numberOfCuts] = recordFields;
//	      System.out.println ("Cut ["+numberOfCuts+"] = "+fixedAngle64);
	    
	      for (int field=0;field<recordFields;field++) {
	        fieldInCut [numberOfCuts][field] = recordFieldsLUT[numberOfRecords][field];
//		System.out.println ("fieldInCut["+numberOfCuts+"]["+field+"] = "+fieldInCut[numberOfCuts][field]);
	      }
	      numberOfCuts++;
	    }
	  }

//        Increment the record pointer and update the file offset
//        register to point to the begnning of the next record.

          numberOfRecords++;
	  offset = offset + recordLength*2;

//        For universal format type 2 we need to account for the
//        32 bit integer fields at the beginnig and end of the
//        record.

          if (UFRecordType == UF_RECORD_TYPE_2)
            offset = offset+8;

        }
      }
    }
    catch (IOException e) {
        System.out.println (e);
	System.out.println (numberOfRecords+" records processed");

    for (int i=0;i<numberOfFields;i++)
      System.out.println ("field ["+i+"] = "+fieldNameLUT[i]);
    }	 

//  Debug code to dump out table data after file is read.

//    for (int i = 0;i<numberOfRecords;i++) {
//      System.out.println ("Record "+i+"  offset "+recordLUT[i]+
//           "  azimuth "+azimuthLUT[i]+"  elevation "+elevationLUT[i]+
//	   "  fixed "+fixedLUT[i]);
//    }

    return (0);

  }

//--------------------------------------------------------------------
//  Method to read all radials in selected cut  
//--------------------------------------------------------------------

  public static int readCut (int cutNumber) {

      System.out.println("Reading cut: " + cutNumber);

    int ret  = 0;
    int indx = 0;
    int firstRecord = 0;
    int lastRecord  = 0;

    if (cutNumber >= numberOfCuts) {
      return (0);
    }
   
    firstRecord = startOfCut[cutNumber];

    if (cutNumber >= (cutNumber-1)) {
      lastRecord = numberOfRecords-1;
    } else {
      lastRecord = startOfCut[cutNumber+1];
    }

    indx = 0;

    System.out.println("lastRecord" + lastRecord);

    for (int i=firstRecord;i<lastRecord;i++) {

      if (fixedLUT[i] == cut64LUT[cutNumber]) {

        cutRecord = i;

        ret = read (i);
	
	if (ret != 0) {
          return (ret);
	}

//      Store the azimuth and elevation angles in the cut table.

	cutAzimuth[indx]   = azimuthLUT[i];
	cutElevation[indx] = elevationLUT[i];

    //    System.out.println("cutElevation" + cutElevation[indx]);
     //   System.out.println("cutAzimuth" + cutAzimuth[indx]);

        
//      Separate and store the data for each moment in the cut into
//      lookup tables for easy access.

	for (int j=0;j<recordFields;j++) {

          cutMomentScale        [indx][j] = scale[j];
          cutMomentNumberSamples[indx][j] = numberSamples[j];


          

	  for (int k=0;k<numberSamples[j];k++) {
	    cutMomentBinData[indx][j][k] = binData[j][k];
           // System.out.println("cutMomentBinData["+indx+"]["+j+"]["+k+"]" + cutMomentBinData[indx][j][k]);
          }

          }


        

        if (indx == 0) {

	  cutHour   = hour;
	  cutMinute = minute;
	  cutSecond = second;
	  cutMonth  = month;
	  cutDay    = day;
	  cutYear   = year;

	  for (int field=0;field<numberOfFields;field++) {
            cutFieldsLUT[field] = recordFieldsLUT[i][field];
//	    System.out.println ("cutFieldsLUT["+field+"] = "+
//	        cutFieldsLUT[field]+" ----> Type: "+
//		fieldTypeLUT[cutFieldsLUT[field]]);
          }
        }

	indx++;
      }
    }

    cutNumberRadials = indx;
    return (0);

  }

//--------------------------------------------------------------------
//  Method to read selected radial  
//--------------------------------------------------------------------

  public static int read (int radialNumber) {

    int ret = 0;

//  Make sure the specified radial number is valid.

    if ((radialNumber <  0              ) ||
        (radialNumber >= numberOfRecords))
      return (-1);

    try {

//    Position the file pointer to the beginning of the specified
//    record.

      din.seek (recordLUT[radialNumber]);

//    First read the mandatory header portion of the record.

      ret = readMandatoryHeader();

      if (ret != 0) {

        System.out.println ("ERROR reading mandatory header");
        System.exit(0);

      }

//    Next, read the optional header block if it exists.

      if (dataHeaderBlock > optionalHeaderBlock) {

//    If the start of the optional header block is not 46, then we need
//    to skip the bytes in between,

      if ((optionalHeaderBlock-46) != 0)
          din.readFully (data,0,(optionalHeaderBlock-46)*2);
	  
        readOptionalHeader();

      }

//    Right now skip over the local use header block since we don't
//    know what it contains (if it exists).

      if (optionalHeaderBlock < localUseHeaderBlock) {

        din.readFully (data,0,dataHeaderBlock-localUseHeaderBlock);

      } 

//    The next thing we want to do is read the data header
//    so we can determine which fields are available.

      readDataHeader();

//    The last thing is to read the data.

      readData();

    }

    catch (IOException e) {
        System.out.println (e);
	return (-1);
    }
    return (0);

  }

//  The following method reads the mandatory header data.  On success
//  0 is returned.  On failure a non-zero value is returned.

  public static int readMandatoryHeader() {

    try {

      if (UFRecordType == UF_RECORD_TYPE_2) {

        recordSize = din.readInt();

      }

      U = din.readByte();
      F = din.readByte();

      if (U == 85) {
        if (F == 70) {

//      We successfully matched UF (or uf) at the beginning of the
//      file so we will continue.

          recordLength               = din.readShort();
          optionalHeaderBlock        = din.readShort();
          localUseHeaderBlock        = din.readShort();
          dataHeaderBlock            = din.readShort();

//          System.out.println ("recordLength "+recordLength+
//	   "  optionalHeaderBlock "+optionalHeaderBlock+
//	   "  localUseHeaderBlock "+localUseHeaderBlock+
//	   "  dataHeaderBlock "+dataHeaderBlock);
          physicalRecordNumberFile   = din.readShort();
          volumeScanNumber           = din.readShort();
          rayNumber                  = din.readShort();
          physicalRecordNumberRay    = din.readShort();
          sweepNumber                = din.readShort();
          din.readFully (radarName,0,SIZE_OF_RADAR_NAME);
          din.readFully (siteName ,0,SIZE_OF_SITE_NAME);
          latitudeDegrees            = din.readShort();
          latitudeMinutes            = din.readShort();
          latitudeSeconds64          = din.readShort();
          longitudeDegrees           = din.readShort();
          longitudeMinutes           = din.readShort();
          longitudeSeconds64         = din.readShort();
          antennaHeight              = din.readShort();
          year                       = din.readShort();
          month                      = din.readShort();
          day                        = din.readShort();
          hour                       = din.readShort();
          minute                     = din.readShort();
          second                     = din.readShort();
          din.readFully (timeZone,0,SIZE_OF_TIME_ZONE);
          azimuth64                  = din.readShort();
          elevation64                = din.readShort();
          sweepMode                  = din.readShort();
          fixedAngle64               = din.readShort();
          sweepRate64                = din.readShort();
          generationYear             = din.readShort();
          generationMonth            = din.readShort();
          generationDay              = din.readShort();
          din.readFully (generationFacility,0,SIZE_OF_GENERATION_FACILITY_NAME);
          missingDataFlag            = din.readShort();

	  return (0);

        }
      }
    }  
    catch (EOFException e) {
	return (EOF_FLAG);
    }	
    catch (IOException e) {
        System.out.println (e);
	return (IOERROR_FLAG);
    }	
    return (0);
  }

//  The following method reads the optional header block.  It is assumed
//  that the file pointer is at the start of the optional header block.

  public static void readOptionalHeader() {

    try {
      din.readFully (projectName,0,SIZE_OF_PROJECT_NAME);
      baselineAzimuth64          = din.readShort();
      baselineElevation64        = din.readShort();
      volumeHour                 = din.readShort();
      volumeMinute               = din.readShort();
      volumeSecond               = din.readShort();
      din.readFully (fieldTapeName,0,SIZE_OF_FIELD_TAPE_NAME);
      optionFlag                 = din.readShort();
    }
    catch (IOException e) {
        System.out.println (e);
    }	
  }

//   This method reads the data header block.  On success 0 is returned.
//   On error a non-zero value is returned.  It is assumed that the file
//   pointer is at the start of the data header block.

  public static void readDataHeader() {
  
    char field[] = new char[2];
    String oldFieldName = null;
    int    oldRecordFields = 0;

    try {

      rayFields    = din.readShort();
      rayRecords   = din.readShort();
      recordFields = din.readShort();

      for (int j=0;j<recordFields;j++) {

        field[0] = (char) din.readByte();
        field[1] = (char) din.readByte();
        fieldPosition[j] = din.readShort();

        StringWriter sw = new StringWriter (3);
        sw.write (field,0,2);
        String string = sw.toString();
        fieldName[j] = string;

//	if ((numberOfRecords == 0) ||
//	    (recordFields != oldRecordFields) ||
//	    (!fieldName[0].equals(oldFieldName)))  {

//	  oldRecordFields = recordFields;
//	  oldFieldName = string;
//          System.out.print ("   "+fieldName[j]+"  pos: "+fieldPosition[j]);
//        }
      }
      if (numberOfRecords == 0) {
        System.out.println (" ");
      }
    }
    catch (IOException e) {
        System.out.println (e);
    }	
  }

//  This method reads the data portion of the record (both field header
//  and bin data.  It is assumed that the file pointer is at the start
//  of a data block.

  public static void readData () {

    byte[] localData = new byte [16384];
    int	   dataLength = 0;

    dataLength = (recordLength-dataHeaderBlock-recordFields*2-3+1)*2;

    try {

      din.readFully (data,0,dataLength);

      if (UFRecordType == UF_RECORD_TYPE_2) {

        recordSize = din.readInt();

      }
    }
    catch (IOException e) {
        System.out.println (e);
    }	

    for (int i=0;i<recordFields;i++) {

      int dataPos = 0;
      int pos = (fieldPosition[i]-fieldPosition[0])*2;

      for (int j=0;j<dataLength-pos;j++) {

        localData[j] = data[j+pos];

      }

      ByteArrayInputStream bin = new ByteArrayInputStream (localData);
      DataInputStream ddn = new DataInputStream (bin);
  
      try {

        dataPosition[i]          = ddn.readShort();
        scaleFactor[i]           = ddn.readShort();
 //       System.out.println ("scaleFactor ["+i+"] = "+scaleFactor[i]);
        rangeFirstGate[i]        = ddn.readShort();
  //      System.out.println ("rangeFirstGate ["+i+"] = "+rangeFirstGate[i]);
        gateAdjustment[i]        = ddn.readShort();
      //  System.out.println ("gateAdjustment ["+i+"] = "+gateAdjustment[i]);
        sampleSpacing[i]         = ddn.readShort();
        numberSamples[i]         = ddn.readShort();
        sampleDepth[i]           = ddn.readShort();
        horizontalBeamWidth64[i] = ddn.readShort();
        verticalBeamWidth64[i]   = ddn.readShort();
        bandwidth[i]             = ddn.readShort();
        polarization[i]          = ddn.readShort();
        wavelength64[i]          = ddn.readShort();
        sampleAverage[i]         = ddn.readShort();
        ddn.readFully (thresholdField[i],0,SIZE_OF_FIELD_NAME);
        thresholdValue[i]        = ddn.readShort();
        scale[i]                 = ddn.readShort();
        ddn.readFully (editCode[i],0,SIZE_OF_EDIT_CODE);
        prt[i]                   = ddn.readShort();
        bitsPerSample[i]         = ddn.readShort();

//      We want the file pointer to point to the start of bin data
//      for this moment.  There are 19 shorts which are mandatory in
//      each field header.  Some moments require a few more items so
//      we need to skip them.

        int offset = dataPosition[i] - fieldPosition[i] - 19;

	for (int j=0;j<offset;j++) {
	  short dummy = ddn.readShort();
	}  

//      Now we want to move the bin data for each moment into local
//      arrays so we can access them more easily.  Since the bin data
//      element size may differ, we need to read the data accordingly
//      and cast them to 4 byte integers (assumimng that this will be
//      largest element size we will deal with.

        if (bitsPerSample[i] == (short)8) {
          for (int j=0;j<numberSamples[i];j++)
	    binData[i][j] = (int) ddn.readByte();
	} else if (bitsPerSample[i] == (short)16) {
          for (int j=0;j<numberSamples[i];j++)
	    binData[i][j] = (int) ddn.readShort();
	} else if (bitsPerSample[i] == (short)32) {
          for (int j=0;j<numberSamples[i];j++)
	    binData[i][j] = (int) ddn.readInt();
          
	} else {
          return;
	}
      }
      catch (IOException e) {
        System.out.println (e);
      }	
    }	

//    System.exit(0);
// Field Header Fields that are depedent on the field

// For VF, VE, VR, VT, VP (velocity fields)
//
//  static short nyquistVelocity            = 0;  // scaled m/s
//  static byte  FL[] = new byte [SIZE_OF_FIELD_NAME]; // lsb of data; 1 = good, 0 = bad

// static For DM

//  static short radarConstant              = 0;  // dB(Z) = RC+data/scale+20log(range)
                                         // in km
//  static short noisePower                 = 0;  // dB (mW) x scale
//  static short receiverGain               = 0;  // dB x scale
//  static short peakPower                  = 0;  // dB (mW) x scale
//  static short antennaGain                = 0;  // dB x scale
//  static short pulseDuration64            = 0;  // microseconds x 64

    
  }

//  The following method returns the value of the specified moment
//  and gate.  The data are scaled to match scaled NEXRAD level 2
//  moment data so the same color lookup methods can be used.

  public static int getBinData (int radial, int moment, int bin) {

    float value = 100;

    if ((fieldName[moment].equals("DZ")) ||
        (fieldName[moment].equals("Z1")) ||
        (fieldName[moment].equals("DM")) ||
	(fieldName[moment].equals("CZ"))) {

      value = (float) (cutMomentBinData[radial][moment][bin]/scaleFactor[moment]);
      value = (float) 2.0*(value+(float)33.0);
      if (value < 0)
        value = 0;
      else if (value > 255)
        value = 255;     
    } else if ((fieldName[moment].equals("VE")) ||
               (fieldName[moment].equals("VF"))||
               (fieldName[moment].equals("VP"))||
               (fieldName[moment].equals("VR"))||
               (fieldName[moment].equals("V1"))||
               (fieldName[moment].equals("VT"))) {

      value = (float)(cutMomentBinData[radial][moment][bin]/scaleFactor[moment]);
      value = (float) 2.0*(value+(float)63.5)+(float)2;

     if (value < 0)
        value = 0;
      else if (value > 255)
        value = 255;   

    } else if ((fieldName[moment].equals("SW")) ||
               (fieldName[moment].equals("V1"))) {

      value = (float) (cutMomentBinData[radial][moment][bin]/scaleFactor[moment]);
      value = (float) 2.0*(value+(float)64.5);

      if (value < 0)
        value = 0;
      else if (value > 255)
        value = 255;     
    } else {

      value = (float) (cutMomentBinData[radial][moment][bin]/scaleFactor[moment]);
      value = (float) 2.0*(value+(float)64.5);

      if (value < 0)
        value = 0;
      else if (value > 255)
        value = 255;    

    }

    return (int) value;
        
  }

//  The following method returns the value of the specified moment
//  and gate.  The data are scaled to match scaled NEXRAD level 2
//  moment data so the same color lookup methods can be used.

  public static int getBinData (int moment, int bin) {

    float value = 100;

    if ((fieldName[moment].equals("DZ")) ||
        (fieldName[moment].equals("Z1")) ||
        (fieldName[moment].equals("DM")) ||
	(fieldName[moment].equals("CZ"))) {

      value = (float) (binData[moment][bin]/scaleFactor[moment]);

      value = (float) 2.0*(value+(float)33.0);
      if (value < 0)
        value = 0;
      else if (value > 255)
        value = 255;

    } else if ((fieldName[moment].equals("VE")) ||
               (fieldName[moment].equals("VF"))||
               (fieldName[moment].equals("VP"))||
               (fieldName[moment].equals("VR"))||
               (fieldName[moment].equals("V1"))||
               (fieldName[moment].equals("VT"))) {

      value = (float)(binData[moment][bin]/scaleFactor[moment]);
      value = (float) 2.0*(value+(float)64.5);

      if (value < 0)
        value = 0;
      if (value > 255)
        value = 255;

    } else if ((fieldName[moment].equals("SW")) ||
               (fieldName[moment].equals("V1"))) {

      value = (float) (binData[moment][bin]/scaleFactor[moment]);
      value = (float) 2.0*(value+(float)64.5);

      if (value < 0)
        value = 0;
      if (value > 255)
        value = 255;

    } else {

      value = (float) (binData[moment][bin]/scaleFactor[moment]);
      value = (float) 2.0*(value+(float)64.5);

      if (value < 0)
        value = 0;
      if (value > 255)
        value = 255;

    }

    return (int) value;
    
  }

//-----------------------------------------------------------------------
//  Description: This method returns the value of the specified moment
//               at the bin closest to the specified azimuth and range.
//  Input: moment - moment identifier
//         azi    - azimuth angle (degrees)
//         ran    - radial range (kilometers)
//-----------------------------------------------------------------------

  public static float getBinMoment (int moment, float azi, float ran) {

    float diff    = 0;
    float oldDiff = 9999;
    float value;
    int   radial;
    int   scale;
    int   bin;
    int   momentIndex = 0;

//  Since the moment fields may change within a given file we need to
//  determine the moment index for the specified moment within the current
//  cut.

    momentIndex = -1;

    for (int i=0;i<numberOfFields;i++) {
      if (moment == cutFieldsLUT[i]) {
        momentIndex = i;
	break;
      }
    }

    if (momentIndex < 0)
      return ((float) -99.9);

    value   = (float) -99.9;
    oldDiff = (float) 999.9;
    radial  = -1;
    
//  We need to find the radial that is closest to the specified azimuth.
//  If the closest radial is more than one beamwidth away then we want
//  to return -99.9.

    for (int i=0;i<cutNumberRadials;i++) {

      diff = Math.abs (cutAzimuth[i] - azi);

      if (diff < oldDiff) {
	oldDiff = diff;
	radial  = i;
      }
    }

    System.out.println ("azimuth: "+azi+" -- cut radial index: "+radial+
                        " -- delta azi: "+oldDiff);
    if (radial < 0)
      return ((float) -99.9);

//  Now that we found the closest radial we need to convert the
//  input range to a bin index.

    System.out.println ("Input range: "+ran+
                        " -- rangeFirstGate[moment]: "+rangeFirstGate[momentIndex]+
			" -- gateAdjustment[moment]: "+gateAdjustment[momentIndex]+
			" -- sampleSpacing[moment]: "+sampleSpacing[momentIndex]);
    bin = (int) ((ran - rangeFirstGate[momentIndex]/1000 - gateAdjustment[momentIndex]/1000)/
                 ((float)sampleSpacing[momentIndex]/(float)1000.0));

    System.out.println ("bin index: "+bin);
    if ((bin < 0) || (bin > numberSamples[momentIndex])) {
      return ((float) -99.9);
    } else {

      value = (float) (cutMomentBinData[radial][momentIndex][bin]/scaleFactor[momentIndex]);

    }

    return (float) value;
    
  }
  
//  This method determines the moment type for the specified field
//  mnemonic.

  public static int getFieldType (String mnemonic) {

    int i;

    for (i=0;i<reflectivityFields.length;i++) {
      if (reflectivityFields[i].equalsIgnoreCase(mnemonic)) {
        return MOMENT_TYPE_REFLECTIVITY;
      }
    }  

    for (i=0;i<velocityFields.length;i++)
      if (velocityFields[i].equalsIgnoreCase(mnemonic))
        return MOMENT_TYPE_VELOCITY;

    for (i=0;i<spectrumWidthFields.length;i++)
      if (spectrumWidthFields[i].equalsIgnoreCase(mnemonic))
        return MOMENT_TYPE_SPECTRUM_WIDTH;

    System.out.println ("mnemonic not found in list");
    return MOMENT_TYPE_UNKNOWN;
  }

//  Main method for stand-alone testing

  public static void main (String[] args) {

   /* UniversalFormat uf = new UniversalFormat();
    System.out.println ("args[0] = "+args[0]);
    System.out.println ("args[1] = "+args[1]);
    File file = new File (args[0], args[1]);    */
   

   // File file = new File("C:\\npoltogis\\Output04np1040929232100.RAWTNLD");
    try{  
         UniversalFormat uf = new UniversalFormat();

       File file = new File("C:\\npoltogis\\Output04np1040929232100.RAWTNLD");
       uf.open(file);


  
    System.out.println("Reading cut 0");
    uf.readCut(0);

    
        for(int i=215;i<225;i++)
        
    System.out.println("cutMomentBinData[0][2]["+i+"]=" + cutMomentBinData[0][2][i] );
            }catch(Exception e)
    {e.printStackTrace();
    }

  }
}

