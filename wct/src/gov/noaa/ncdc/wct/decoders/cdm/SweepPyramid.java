package gov.noaa.ncdc.wct.decoders.cdm;

import java.io.IOException;
import java.util.Date;

import ucar.nc2.Variable;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.RadialDatasetSweep.Type;
import ucar.unidata.geoloc.EarthLocation;

public class SweepPyramid {

    
    private RadialDatasetSweep.Sweep origSweep;
    
    
    /**
     * Instantiate with the original sweep object
     * @param sweep
     */
    public SweepPyramid(RadialDatasetSweep.Sweep sweep) {
        this.origSweep = sweep;    
    }
    
    
    /**
     * Get a new Sweep object representing a 'downsampled' range/azimuth grid.  The maximum value
     * from any cell is kept.
     * @param numGates  Number of range gates to combine into one
     * @param numRays  Number of azimuths (rays) to combine into one
     * @return
     * @throws IOException
     */
    public RadialDatasetSweep.Sweep getDownsampledSweep(int numGates, int numRays) throws IOException {
        
        return new DownsampledSweep(numGates, numRays, this.origSweep);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    class DownsampledSweep implements RadialDatasetSweep.Sweep {
        
        private int numGates;
        private int numAzimuth;
        private RadialDatasetSweep.Sweep originalSweep;
        
        private float[] azimuthData;
        private float[] elevationData;
        private float[] data;
        
        private float newGateSize;
        private int newGateNumber;
        private int newRadialNumber;
        
        
        /**
         * 
         * @param numGates  The number of 
         * @param numAzimuth
         * @param originalSweep
         * @throws IOException 
         */
        public DownsampledSweep(int numGates, int numAzimuth, RadialDatasetSweep.Sweep originalSweep) throws IOException {
            this.numGates = numGates;
            this.numAzimuth = numAzimuth;
            this.originalSweep = originalSweep;
            process();
        }

        
        private void process() throws IOException {
//            this.newRadialNumber = (int) Math.round(originalSweep.getRadialNumber() / (double)numAzimuth);
//            this.newGateNumber = (int) Math.round(originalSweep.getGateNumber() / (double)numGates);
            this.newRadialNumber = (int) Math.floor(originalSweep.getRadialNumber() / (double)numAzimuth);
            this.newGateNumber = (int) Math.floor(originalSweep.getGateNumber() / (double)numGates);
            
            this.newGateSize = originalSweep.getGateSize() * numGates;
            
            
            
            System.out.println("Sweep Pyramid:   gate number: "+originalSweep.getGateNumber()+" -> "+newGateNumber);
            System.out.println("Sweep Pyramid:     gate size: "+originalSweep.getGateSize()+" -> "+newGateSize);
            System.out.println("Sweep Pyramid: radial number: "+originalSweep.getRadialNumber()+" -> "+newRadialNumber);

            
            float[] origData = originalSweep.readData();
            data = new float[newGateNumber * newRadialNumber];
            
            
            
            // 1. Create new data array by taking max value from neighboring cells
            int newIndex = 0;
            for (int i=0; i<originalSweep.getRadialNumber()-numAzimuth+1; i=i+numAzimuth) {
                for (int j=0; j<originalSweep.getGateNumber()-numGates+1; j=j+numGates) {

                    float value = -999.0f;

                    for (int x=0; x<numAzimuth; x++) {
                        for (int y=0; y<numGates; y++) {
                            
                            int index = (i+x)*originalSweep.getGateNumber() + (j+y);
                            if (origData[index] > value) {
                                value = origData[index];
                            }
                            
                        }
                    }
                    
                    data[newIndex++] = value;
                }
            }
            
            // 2. Create new azimuth array by taking center azimuth of combined rays 
            azimuthData = new float[newRadialNumber];
            newIndex = 0;
            
            for (int i=0; i<originalSweep.getRadialNumber()-numAzimuth+1; i=i+numAzimuth) {
                
                float firstVal = originalSweep.getAzimuth(i);
                float lastVal = originalSweep.getAzimuth(i+numAzimuth-1);
                
                float diff = firstVal - lastVal;
                float centerVal;
                if (diff > 300) {
                    // firstVal > lastVal, such as 359.5, 0.5
                    centerVal = ((lastVal+360)+firstVal)/2.0f;
                    if (centerVal > 360) {
                        centerVal = centerVal - 360;
                    }
                }
                else if (diff < -300) {
                    // firstVal < lastVal, such as 0.5, 359.5
                    centerVal = ((firstVal+360)+lastVal)/2.0f;
                    if (centerVal > 360) {
                        centerVal = centerVal - 360;
                    }
                }
                else {
                    // normal: such as 145.2, 146.2
                    centerVal = (firstVal+lastVal)/2.0f;
                }
                azimuthData[newIndex++] = centerVal;
            }

            
            // 3. Create new elevation array by taking average elevation of combined rays 
            elevationData = new float[newRadialNumber];
            newIndex = 0;
            
            for (int i=0; i<originalSweep.getRadialNumber()-numAzimuth+1; i=i+numAzimuth) {
                
                float total = 0;
                for (int x=0; x<numAzimuth; x++) {
                    total += originalSweep.getElevation(i+x);
                }
                
                elevationData[newIndex++] = total/numAzimuth;
            }

        }
        
        
        
        
        public void clearSweepMemory() {
        }

        public float[] getAzimuth() throws IOException {
            return azimuthData;
        }

        public float getAzimuth(int ray) throws IOException {
            return azimuthData[ray];
        }

        public float getBeamWidth() {
            return originalSweep.getBeamWidth()*numAzimuth;
        }

        public float[] getElevation() throws IOException {
            return elevationData;
        }

        public float getElevation(int ray) throws IOException {
            return elevationData[ray];
        }

        public Date getEndingTime() {
            return originalSweep.getEndingTime();
        }

        public int getGateNumber() {
            return this.newGateNumber;
        }

        public float getGateSize() {
            return this.newGateSize;
        }

        public float getMeanAzimuth() {
            return originalSweep.getMeanAzimuth();
        }

        public float getMeanElevation() {
            return originalSweep.getMeanElevation();
        }

        public float getNyquistFrequency() {
            return originalSweep.getNyquistFrequency();
        }

        public EarthLocation getOrigin(int ray) {
            return originalSweep.getOrigin(ray);
        }

        public int getRadialNumber() {
            return this.newRadialNumber;
        }

        public float getRangeToFirstGate() {
            return originalSweep.getRangeToFirstGate();
        }

        public Date getStartingTime() {
            return originalSweep.getStartingTime();
        }

        public int getSweepIndex() {
            return originalSweep.getSweepIndex();
        }

        public float getTime(int ray) throws IOException {
            return originalSweep.getTime(ray);
        }

        public Type getType() {
            return originalSweep.getType();
        }

        public Variable getsweepVar() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * read 2d sweep data: size = nradials * ngates
         */
        public float[] readData() throws IOException {
            return data;
        }

        /**
         * read 1d ray data: size = ngates
         */
        public float[] readData(int ray) throws IOException {
            float[] rayData = new float[getGateNumber()];
            int rayIndexOffset = ray*getGateNumber();
            for (int n=0; n<rayData.length; n++) {
                rayData[n] = data[rayIndexOffset+n];
            }
            return rayData;
        }


        
    }
    
}
