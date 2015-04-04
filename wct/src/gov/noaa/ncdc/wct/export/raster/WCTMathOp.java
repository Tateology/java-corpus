package gov.noaa.ncdc.wct.export.raster;

import java.awt.geom.Rectangle2D.Double;
import java.awt.image.WritableRaster;
import java.util.HashMap;


public class WCTMathOp extends RasterMathOp {

	
	
    public static void diff(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, int noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        diff(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void diff(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, float noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        diff(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void diff(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, double noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        diff(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    
    

    
    public static void multiply(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, int noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        multiply(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void multiply(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, float noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        multiply(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void multiply(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, double noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        multiply(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }

	
	
	
	
	
	
    public static void max(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, int noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        max(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void max(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, float noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        max(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void max(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, double noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        max(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }

    
    
    
    public static void min(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, int noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        min(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void min(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, float noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        min(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void min(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, double noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        min(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }

    
    
    
    public static void absMax(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, int noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        absMax(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void absMax(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, float noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        absMax(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void absMax(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, double noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        absMax(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }

    
    
    
    
    
    public static void absMaxSigned(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, int noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        absMaxSigned(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void absMaxSigned(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, float noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        absMaxSigned(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void absMaxSigned(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, double noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        absMaxSigned(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }

    
    
    
    
    public static void sum(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, int noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        sum(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void sum(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, float noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        sum(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }
    public static void sum(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2, double noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        sum(returnRaster.getWritableRaster(), raster1.getWritableRaster(), raster2.getWritableRaster(), noData);
    }



    
    public static void average(WCTRaster returnRaster, WCTRaster raster1, int weight1, 
            WCTRaster raster2, int weight2, int noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        average(returnRaster.getWritableRaster(), raster1.getWritableRaster(), weight1, 
                raster2.getWritableRaster(), weight2, noData);
    }
    public static void average(WCTRaster returnRaster, WCTRaster raster1, int weight1, 
            WCTRaster raster2, int weight2, float noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        average(returnRaster.getWritableRaster(), raster1.getWritableRaster(), weight1, 
                raster2.getWritableRaster(), weight2, noData);
    }
    public static void average(WCTRaster returnRaster, WCTRaster raster1, int weight1, 
            WCTRaster raster2, int weight2, double noData)
    throws RasterMathOpException {
        if (! checkBounds(returnRaster, raster1, raster2)) {
            throw new RasterMathOpException(getCheckBoundsExceptionMessage(returnRaster, raster1, raster2));
        }
        average(returnRaster.getWritableRaster(), raster1.getWritableRaster(), weight1, 
                raster2.getWritableRaster(), weight2, noData);
    }





    public static boolean checkBounds(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2) {
        return returnRaster.getBounds().equals(raster1.getBounds()) && raster1.getBounds().equals(raster2.getBounds());
    }



    private static String getCheckBoundsExceptionMessage(WCTRaster returnRaster, WCTRaster raster1, WCTRaster raster2) {
        return "Geographic Bounds of rasters (return, raster1, raster2) do not match: \n"+
            "returnRaster: "+returnRaster.getBounds()+"\n"+
            "raster1: "+raster1.getBounds()+"\n"+
            "raster2: "+raster2.getBounds();
    }
    
    
    
    
    
    public static HashMap<String, java.lang.Double> getStatsIgnoreNoData(WCTRaster raster) {

    	double minValue = java.lang.Double.MAX_VALUE;
    	double maxValue = java.lang.Double.MIN_VALUE;
    	double noData = raster.getNoDataValue();
    	double val;
    	
        for (int i = 0; i < raster.getWritableRaster().getWidth(); i++) {
           for (int j = 0; j < raster.getWritableRaster().getHeight(); j++) {
        	   val = raster.getWritableRaster().getSample(i, j, 0);
               if (val != noData) {
            	   if (val > maxValue) {
            		   maxValue = val;
            	   }
            	   if (val < minValue) {
            		   minValue = val;
            	   }
               }              
           }
        }
        HashMap<String, java.lang.Double> hashMap = new HashMap<String, java.lang.Double>();
        hashMap.put("max", new java.lang.Double(maxValue));
        hashMap.put("min", new java.lang.Double(minValue));
        return hashMap;
    }
    
    
    
    
    
    
    public static WCTRaster createEmptyRasterCopy(final WCTRaster rasterTemplate) {
    	WCTRaster raster = new WCTRaster() {

    		private WritableRaster writableRaster = null;
    		private long dateInMillis;
    		private String longName;
    		private String units;
    	    private String standardName = "";
    	    private String variableName = "";

			@Override
			public Double getBounds() {
				return rasterTemplate.getBounds();
			}

			@Override
			public double getCellHeight() {
				return rasterTemplate.getCellHeight();
			}

			@Override
			public double getCellWidth() {
				return rasterTemplate.getCellWidth();
			}

			@Override
			public long getDateInMilliseconds() {
				return dateInMillis;
			}

			@Override
			public String getLongName() {
				return longName;
			}

			@Override
			public double getNoDataValue() {
				return rasterTemplate.getNoDataValue();
			}

			@Override
			public String getUnits() {
				return units;
			}

			@Override
			public WritableRaster getWritableRaster() {
				if (writableRaster == null) {
					initWritableRaster();
				}

				return writableRaster;
			}

			@Override
			public boolean isEmptyGrid() {
				return false;
			}

			@Override
			public boolean isNative() {
				return false;
			}

			@Override
			public void setDateInMilliseconds(long dateInMillis) {
				this.dateInMillis = dateInMillis;
			}

			@Override
			public void setLongName(String longName) {
				this.longName = longName;				
			}

			@Override
			public void setUnits(String units) {	
				this.units = units;
			}

			@Override
			public void setWritableRaster(WritableRaster raster) {
				this.writableRaster = raster;				
			}

		    @Override
		    public String getStandardName() {
		        return this.standardName;
		    }


		    @Override
		    public String getVariableName() {
		        return this.variableName;
		    }


		    @Override
		    public void setStandardName(String standardName) {
		        this.standardName = standardName;        
		    }


		    @Override
		    public void setVariableName(String variable) {
		        this.variableName = variable;        
		    }

			
			private void initWritableRaster() {
				WritableRaster wrTemplate = rasterTemplate.getWritableRaster();
	            writableRaster = wrTemplate.createCompatibleWritableRaster();

	            // initialize process raster to NoData value
	            for (int i = 0; i < wrTemplate.getWidth(); i++) {
	               for (int j = 0; j < wrTemplate.getHeight(); j++) {
	            	   writableRaster.setSample(i, j, 0, rasterTemplate.getNoDataValue());
	               }
	            }
			}
    		
    	};
    	return raster;
    }
}
