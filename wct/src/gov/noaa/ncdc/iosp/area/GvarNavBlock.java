package gov.noaa.ncdc.iosp.area;

import edu.wisc.ssec.mcidas.AreaFile;
import edu.wisc.ssec.mcidas.GVARnav;
import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;


public class GvarNavBlock {

	private String type;
	private String id;
	private byte[] scanStatus= new byte[32];
	private int refLong;
	private int refNominalDist;
	private int refLat;
	private int refYaw;
	private int refAttitudeRoll;
	private int refAttitudePitch;
	private int refAttitudeYaw;
	private String epochTime = "";
	private int imcRoll;
	private int imcPitch;
	private int imcYaw;
	private int[] longDeltas = new int[13];
	private int[] radialDeltas = new int[11];
	private int[] geocentricLatDeltas = new int[9];
	private int[] orbitYawDeltas = new int[9];
	private int solarRate;
	private int expEpochStartTime;
	private ImgrRep rollAtts;
	private String more1;
	private String gvar1;
	private ImgrRep pitchAtts;
	private ImgrRep yawAtts;
	private String more2;
	private String gvar2;
	private ImgrRep rollMisalign;
	private ImgrRep pitchMisalign;
	private int imgDate;
	private int imgStart;
	private int instrument;
	private int cyclesNS;
	private int cyclesEW;
	private int incrementNS;
	private int incrementEW;
	private String more3;
	private String gvar3;
	private String more4;
	private String gvar4;
	
	public void readNav(ucar.unidata.io.RandomAccessFile raf) {
		try {
			raf.seek(256);
			type = raf.readString(4);
			id = raf.readString(4);
			 
			int count = 0;
			for(int i=0;i<4;i++){
				byte temp = raf.readByte();
				scanStatus[count] = (byte)AvhrrFile.readOneBitFlag(temp, 7-i);
				count++;
			}

			refLong = raf.readInt();
			refNominalDist = raf.readInt();
			refLat = raf.readInt();
			refYaw = raf.readInt();
			refAttitudeRoll = raf.readInt();
			refAttitudePitch = raf.readInt();
			refAttitudeYaw = raf.readInt();
			int pos = 0;
			for(int i=0;i>4;i++){
				byte temp = raf.readByte();
				epochTime +=  Integer.toString(AvhrrFile.readFourBitFlag(temp, 0));
				pos++;
				epochTime = Integer.toString((byte)AvhrrFile.readFourBitFlag(temp, 4));
				pos++;
			}

//	 System.out.println(epochTime);

			
			imcRoll = raf.readInt();
			imcPitch = raf.readInt();
			imcYaw = raf.readInt();
			for(int i=0;i<13;i++){
				longDeltas[i] = raf.readInt();
			}
			for(int i=0;i<11;i++){
				radialDeltas[i]  = raf.readInt();
			}
			for(int i=0;i<9;i++){
				geocentricLatDeltas[i] = raf.readInt();
			}
			for(int i=0;i<9;i++){
				orbitYawDeltas[i] = raf.readInt();
			}
			
			solarRate = raf.readInt();
			expEpochStartTime = raf.readInt();
			
			rollAtts = new ImgrRep();
			rollAtts.readImgrRep(raf);
			more1 = raf.readString(4);
			gvar1 = raf.readString(4);
			pitchAtts = new ImgrRep();
			pitchAtts.readImgrRep(raf);
			yawAtts = new ImgrRep();
			yawAtts.readImgrRep(raf);
			more2 = raf.readString(4);
			gvar2 = raf.readString(4);
			rollMisalign = new ImgrRep();
			rollMisalign.readImgrRep(raf);
			pitchMisalign = new ImgrRep();
			pitchMisalign.readImgrRep(raf);
			imgDate = raf.readInt();
			imgStart = raf.readInt();
			instrument = raf.readInt();
			cyclesNS = raf.readInt();
			cyclesEW = raf.readInt();
			incrementNS = raf.readInt();
			incrementEW = raf.readInt();
			more3 = raf.readString(4);
			gvar3 = raf.readString(4);
			more4 = raf.readString(4);
			gvar4 = raf.readString(4);
			
			
/**			
			System.out.println("  type: " + type);
			System.out.println("  id: " + id);
			for(byte b:scanStatus){
				System.out.print(b);
			}
			System.out.println("refLong -->" + refLong);
			System.out.println("refNominalDist -->" + refNominalDist);
			System.out.println("refLat -->" + refLat);
			System.out.println("refYaw -->" + refYaw);
			System.out.println("refAttitudeRoll -->" + refAttitudeRoll);
			System.out.println("refAttitudePitch -->" + refAttitudePitch);
			System.out.println("refAttitudeYaw -->" + refAttitudeYaw);
			System.out.println("epochTime -->" +epochTime );
			System.out.println("imcRoll -->" + imcRoll);
			System.out.println("imcPitch -->" + imcPitch);
			System.out.println("imcYaw -->" + imcYaw);

			System.out.println("longitude deltas--->>>");
			for(int i:longDeltas){
				System.out.println(i);
			}
			
			System.out.println("radial Deltas--->>>>");
			for(int i:radialDeltas){
				System.out.println(i);
			}
			System.out.println("geocentricLatDeltas--->>>");
			for(int i:geocentricLatDeltas){
				System.out.println(i);
			}
			System.out.println("orbitalYawDeltas----->>>");
			for(int i:orbitYawDeltas){
				System.out.println(i);
			}
	
			
			System.out.println("solarRate -->" + solarRate);
			System.out.println("expEpochStartTime -->" + expEpochStartTime);
			System.out.println("rollAtts -->");
			rollAtts.print();

			System.out.println("more1 -->" + more1);
			System.out.println("gvar1 -->" + gvar1);			
			System.out.println("pitchAtts -->");
			pitchAtts.print();
			System.out.println("yawAtts -->");
			yawAtts.print();
			System.out.println("rollMisalign -->");
			rollMisalign.print();
			System.out.println("pitchMisalign -->");

			pitchMisalign.print();


			System.out.println("imgDate -->" + imgDate);
			System.out.println("imgStart -->" + imgStart);
			System.out.println("instrument -->" + instrument);
			System.out.println("cyclesNS -->" + cyclesNS);
			System.out.println("cyclesEW -->" + cyclesEW);
			System.out.println("incrementNS -->" + incrementNS);
			System.out.println("incrementEW -->" + incrementEW);

			System.out.println("  more1: " + more1);
			System.out.println("  gvar1: " + gvar1);
			System.out.println("  more2: " + more2);
			System.out.println("  gvar2: " + gvar2);
			System.out.println("  more3: " + more3);
			System.out.println("  gvar3: " + gvar3);
*/		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public class ImgrRep{
		private int expMagnitude;
		private int expTimeConst;
		private int meanAttitudeAngleConst;
		private int numSinusoids;
		private int[] sinusoidMagnitude = new int[15];
		private int[] sinusoidPhaseAngle = new int[15];
		private int numMonoSinusoids;
		private int[] orderOfApplicableSinusoid = new int[4];
		private int[] orderOfMonomialSinusoid = new int[4];
		private int[] magOfMonomialSinusoid = new int[4];
		private int[] monoSinuPhaseAngle = new int[4];
		private int[] angFromEpoch = new int[4];
		
		public void readImgrRep(ucar.unidata.io.RandomAccessFile raf){
			try{
				expMagnitude = raf.readInt();
				expTimeConst = raf.readInt();
				meanAttitudeAngleConst = raf.readInt();
				numSinusoids = raf.readInt();
				for(int i=0;i<15;i++){
					sinusoidMagnitude[i] = raf.readInt();
					sinusoidPhaseAngle[i] = raf.readInt();					
				}
				numMonoSinusoids = raf.readInt();
				for(int i=0;i<4;i++){
					orderOfApplicableSinusoid[i] = raf.readInt();
					orderOfMonomialSinusoid[i] = raf.readInt();
					magOfMonomialSinusoid[i] = raf.readInt();
					monoSinuPhaseAngle[i] = raf.readInt();
					angFromEpoch[i] = raf.readInt();
				}
			}catch(Exception e){
				
			}
		}

		public int getExpMagnitude() {
			return expMagnitude;
		}

		public int getExpTimeConst() {
			return expTimeConst;
		}

		public int getMeanAttitudeAngleConst() {
			return meanAttitudeAngleConst;
		}

		public int getNumSinusoids() {
			return numSinusoids;
		}

		public int[] getSinusoidMagnitude() {
			return sinusoidMagnitude;
		}

		public int[] getSinusoidPhaseAngle() {
			return sinusoidPhaseAngle;
		}

		public int getNumMonoSinusoids() {
			return numMonoSinusoids;
		}

		public int[] getOrderOfApplicableSinusoid() {
			return orderOfApplicableSinusoid;
		}

		public int[] getOrderOfMonomialSinusoid() {
			return orderOfMonomialSinusoid;
		}

		public int[] getMagOfMonomialSinusoid() {
			return magOfMonomialSinusoid;
		}

		public int[] getMonoSinuPhaseAngle() {
			return monoSinuPhaseAngle;
		}

		public int[] getAngFromEpoch() {
			return angFromEpoch;
		}
		
		public void print(){
			System.out.println("expMagnitude---> " + expMagnitude);
			System.out.println("expTimeConst---> " + expTimeConst);
			System.out.println("meanAttitudeAngleConst---> " + meanAttitudeAngleConst);
			System.out.println("numSinusoids---> " + numSinusoids);

			for(int i=0;i<15;i++){
				System.out.println("sinusoidMagnitude-->" + sinusoidMagnitude[i]);
				System.out.println("sinusoidPhaseAngle-->" + sinusoidPhaseAngle[i]);
			}
			
			System.out.println("numMonoSinusoids -->" + numMonoSinusoids);
			for(int i=0;i<4;i++){
				System.out.println("orderOfApplicableSinusoid---> " + orderOfApplicableSinusoid[i] );
				System.out.println("orderOfMonomialSinusoid---> " + orderOfMonomialSinusoid[i]);
				System.out.println("magOfMonomialSinusoid---> " + magOfMonomialSinusoid[i]);
				System.out.println("monoSinuPhaseAngle---> " + monoSinuPhaseAngle[i]);				
				System.out.println("angFromEpoch---> " + angFromEpoch[i]);
			}
		}
	}
		
	public int getExpEpochStartTime() {
		return expEpochStartTime;
	}

	public int getSolarRate() {
		return solarRate;
	}

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public byte[] getScanStatus() {
		return scanStatus;
	}

	public int getRefLong() {
		return refLong;
	}

	public int getRefNominalDist() {
		return refNominalDist;
	}

	public int getRefLat() {
		return refLat;
	}

	public int getRefYaw() {
		return refYaw;
	}

	public int getRefAttitudeRoll() {
		return refAttitudeRoll;
	}

	public int getRefAttitudePitch() {
		return refAttitudePitch;
	}

	public int getRefAttitudeYaw() {
		return refAttitudeYaw;
	}

	public int getImcRoll() {
		return imcRoll;
	}

	public int getImcPitch() {
		return imcPitch;
	}

	public int getImcYaw() {
		return imcYaw;
	}

	public int[] getLongDeltas() {
		return longDeltas;
	}

	public int[] getRadialDeltas() {
		return radialDeltas;
	}

	public int[] getGeocentricLatDeltas() {
		return geocentricLatDeltas;
	}

	public int[] getOrbitYawDeltas() {
		return orbitYawDeltas;
	}

	public String getEpochTime() {
		return epochTime;
	}

	public ImgrRep getRollAtts() {
		return rollAtts;
	}

	public String getMore1() {
		return more1;
	}

	public String getGvar1() {
		return gvar1;
	}

	public ImgrRep getPitchAtts() {
		return pitchAtts;
	}

	public ImgrRep getYawAtts() {
		return yawAtts;
	}

	public String getMore2() {
		return more2;
	}

	public String getGvar2() {
		return gvar2;
	}
	
	public ImgrRep getRollMisalign() {
		return rollMisalign;
	}

	public ImgrRep getPitchMisalign() {
		return pitchMisalign;
	}

	public int getImgDate() {
		return imgDate;
	}

	public int getImgStart() {
		return imgStart;
	}

	public int getInstrument() {
		return instrument;
	}

	public int getCyclesNS() {
		return cyclesNS;
	}

	public int getCyclesEW() {
		return cyclesEW;
	}

	public int getIncrementNS() {
		return incrementNS;
	}

	public int getIncrementEW() {
		return incrementEW;
	}

	public String getMore3() {
		return more3;
	}

	public String getGvar3() {
		return gvar3;
	}

	public String getMore4() {
		return more4;
	}

	public String getGvar4() {
		return gvar4;
	}

	
	public static void main(String[] arg){
		String inFile="c:/goes-area/goes11.2007.200.000014.BAND_05";
		//open the file using same object as netcdf
		try {
			ucar.unidata.io.RandomAccessFile raf = new ucar.unidata.io.RandomAccessFile(inFile,"r");
			GvarNavBlock gnv = new GvarNavBlock();
			gnv.readNav(raf);
			AreaFile af = new AreaFile(inFile);
			GVARnav gn = (GVARnav)af.getNavigation();
			int blah = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
