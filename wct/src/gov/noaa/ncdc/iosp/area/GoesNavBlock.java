package gov.noaa.ncdc.iosp.area;

import java.io.IOException;

/**
 * Reads navigation information for a GOES area file
 * http://www.ssec.wisc.edu/mcidas/doc/misc_doc/area.txt
 * @author arthur.fotos@noaa.gov
 * March 26, 2008
 *
 */
public class GoesNavBlock {
	
	private String type;
	private int idDate;
	private int time;
	private int orbit;                                                
	private int epochDate;
	private int epochTime;
	private int semiMajorAxis;
	private int eccentricity;
	private int inclination;
	private int meanAnomaly; 
	private int perigee;
    private int ascendingNode;
    private int declination;
    private int rightAscension;
    private int picCL;
    private int spinPeriod;
    private int sweepAngle;
    private int lineTotal;
    private int elementDir;
    private int elementTotal;
    private int pitch;
    private int yaw;
    private int roll;
    private int iajust;
    private int iajtim;
    private int iseang;
    private int skew; 
 	private int beta1Scan;
 	private int beta1Time;
 	private int beta1Time2;
 	private int beta1Count;
 	private int beta2Scan;
 	private int beta2Time;
 	private int beta2Time2;
 	private int beta2Count;                          
 	private int gamma;
 	private int gammaDot;
 	private String memo;
	
	public void readNav(ucar.unidata.io.RandomAccessFile raf) {
		try {
			raf.seek(256);
//			System.out.println("file pos at start of read: " + raf.getFilePointer());
			type = raf.readString(4);
			idDate = raf.readInt();
			time = raf.readInt();
			orbit = raf.readInt();                                             
			epochDate = raf.readInt();
			epochTime = raf.readInt();
			semiMajorAxis = raf.readInt();
			eccentricity = raf.readInt();
			inclination = raf.readInt();
			meanAnomaly = raf.readInt(); 
			perigee = raf.readInt();
		    ascendingNode = raf.readInt();
		    declination = raf.readInt();
		    rightAscension = raf.readInt();
		    picCL = raf.readInt();
		    spinPeriod = raf.readInt();
		    sweepAngle = raf.readInt();
		    lineTotal = raf.readInt();
		    elementDir = raf.readInt();
		    elementTotal = raf.readInt();
		    pitch = raf.readInt();
		    yaw = raf.readInt();
		    roll = raf.readInt();
		    raf.skipBytes(4);
		    iajust = raf.readInt();
		    iajtim = raf.readInt();
		    raf.skipBytes(4);
		    iseang = raf.readInt();
		    skew = raf.readInt();
		    raf.skipBytes(4);
		 	beta1Scan = raf.readInt();
		 	beta1Time = raf.readInt();
		 	beta1Time2 = raf.readInt();
		 	beta1Count = raf.readInt();
		 	beta2Scan = raf.readInt();
		 	beta2Time = raf.readInt();
		 	beta2Time2 = raf.readInt();
		 	beta2Count = raf.readInt();                        
		 	gamma = raf.readInt();
		 	gammaDot = raf.readInt();
		 	raf.skipBytes(4);
		 	memo = raf.readString(32);
/**		 	
		 	System.out.println("file pos at end of read: " + raf.getFilePointer());
		 	System.out.println("type : "   +  type );
		 	System.out.println(" idDate: "   + idDate  );
		 	System.out.println("time : "   +  time );
		 	System.out.println(" orbit: "   +  orbit );
		 	System.out.println("epochDate : "   +  epochDate );
		 	System.out.println(" epochTime: "   +  epochTime );
		 	System.out.println(" semiMajorAxis: "   +  semiMajorAxis );
		 	System.out.println(" eccentricity: "   +   eccentricity);
		 	System.out.println("inclination : "   +   inclination);
		 	System.out.println("meanAnomaly : "   + meanAnomaly  );
		 	System.out.println("perigee : "   +  perigee );
		 	System.out.println("ascendingNode : "   +   ascendingNode);
		 	System.out.println("declination : "   +  declination );
		 	System.out.println(" rightAscension: "   +  rightAscension );
		 	System.out.println(" picCL: "   +   picCL);
		 	System.out.println(" spinPeriod : "   +    spinPeriod);
		 	System.out.println("  sweepAngle: "   +   sweepAngle );
		 	System.out.println(" lineTotal : "   +   lineTotal );
		 	System.out.println("   elementDir: "   +     elementDir);
		 	System.out.println("elementTotal  : "   +    elementTotal);
		 	System.out.println("  pitch: "   +    pitch);
		 	System.out.println("  yaw: "   +    yaw);
		 	System.out.println("roll   : "   +  roll   );
		 	System.out.println(" iajust : "   +    iajust);
		 	System.out.println("  iajtim: "   +    iajtim);
		 	System.out.println("  iseang: "   +   iseang );
		 	System.out.println("  skew: "   +  skew  );
		 	System.out.println(" beta1Scan : "   +   beta1Scan );
		 	System.out.println(" beta1Time : "   +   beta1Time );
		 	System.out.println("  beta1Time2: "   +    beta1Time2);
		 	System.out.println(" beta1Count : "   +    beta1Count);
		 	System.out.println("  beta2Scan: "   +    beta2Scan);
		 	System.out.println(" beta2Time : "   +    beta2Time);
		 	System.out.println(" beta2Time2 : "   +   beta2Time2 );
		 	System.out.println(" beta2Count : "   +   beta2Count );
		 	System.out.println(" gamma : "   +   gamma );
		 	System.out.println(" gammaDot : "   +    gammaDot);
		 	System.out.println("  memo: "   +   memo );
*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	public static void main(String[] arg){
		
		String inFile="c:/goes-area/goes11.2007.200.000014.BAND_05";
		//open the file using same object as netcdf
		try {
			ucar.unidata.io.RandomAccessFile raf = new ucar.unidata.io.RandomAccessFile(inFile,"r");

			GoesNavBlock gnv = new GoesNavBlock();
			gnv.readNav(raf);
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}


	public String getType() {
		return type;
	}



	public int getIdDate() {
		return idDate;
	}



	public int getTime() {
		return time;
	}



	public int getOrbit() {
		return orbit;
	}



	public int getEpochDate() {
		return epochDate;
	}



	public int getEpochTime() {
		return epochTime;
	}



	public int getSemiMajorAxis() {
		return semiMajorAxis;
	}



	public int getEccentricity() {
		return eccentricity;
	}



	public int getInclination() {
		return inclination;
	}



	public int getMeanAnomaly() {
		return meanAnomaly;
	}



	public int getPerigee() {
		return perigee;
	}



	public int getAscendingNode() {
		return ascendingNode;
	}



	public int getDeclination() {
		return declination;
	}



	public int getRightAscension() {
		return rightAscension;
	}



	public int getPicCL() {
		return picCL;
	}



	public int getSpinPeriod() {
		return spinPeriod;
	}



	public int getSweepAngle() {
		return sweepAngle;
	}



	public int getLineTotal() {
		return lineTotal;
	}



	public int getElementDir() {
		return elementDir;
	}



	public int getElementTotal() {
		return elementTotal;
	}



	public int getPitch() {
		return pitch;
	}



	public int getYaw() {
		return yaw;
	}



	public int getRoll() {
		return roll;
	}



	public int getIajust() {
		return iajust;
	}



	public int getIajtim() {
		return iajtim;
	}



	public int getIseang() {
		return iseang;
	}



	public int getSkew() {
		return skew;
	}



	public int getBeta1Scan() {
		return beta1Scan;
	}



	public int getBeta1Time() {
		return beta1Time;
	}



	public int getBeta1Time2() {
		return beta1Time2;
	}



	public int getBeta1Count() {
		return beta1Count;
	}



	public int getBeta2Scan() {
		return beta2Scan;
	}



	public int getBeta2Time() {
		return beta2Time;
	}



	public int getBeta2Time2() {
		return beta2Time2;
	}



	public int getBeta2Count() {
		return beta2Count;
	}



	public int getGamma() {
		return gamma;
	}



	public int getGammaDot() {
		return gammaDot;
	}



	public String getMemo() {
		return memo;
	}



}
