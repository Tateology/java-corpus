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

package gov.noaa.ncdc.wct.export.raster;


import java.awt.image.WritableRaster;

/**
 *  Perform math operations on WritableRaster objects<BR><BR>
 *  <B>IMPORTANT: </B> All rasters must have identical dimensions or WCTMathOpException
 * will be thrown.
 *
 * @author     steve.ansari
 * @created    August 31, 2004
 */
public class RasterMathOp {
    
	
	

	/**
	 *  Returns raster1-raster2
	 *
	 * @param  returnRaster               Raster that is populated with return value
	 * @param  raster1                    Input raster 1
	 * @param  raster2                    Input raster 2
	 * @param  noData                     No Data value for all rasters
	 * @exception  RasterMathOpException  Thrown if raster dimensions do not match
	 */
	public static void diff(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, int noData)
	throws RasterMathOpException {
		checkRasters(returnRaster, raster1, raster2);

		for (int i = 0; i < returnRaster.getWidth(); i++) {
			for (int j = 0; j < returnRaster.getHeight(); j++) {
				if (raster1.getSample(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
				}
				else if (raster2.getSample(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
				}
				else {
					returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0)-raster2.getSample(i, j, 0));
				}
			}
		}
	}


	/**
	 *  Returns raster1-raster2
	 *
	 * @param  returnRaster               Raster that is populated with return value
	 * @param  raster1                    Input raster 1
	 * @param  raster2                    Input raster 2
	 * @param  noData                     No Data value for all rasters
	 * @exception  RasterMathOpException  Thrown if raster dimensions do not match
	 */
	public static void diff(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, float noData)
	throws RasterMathOpException {
		checkRasters(returnRaster, raster1, raster2);

		for (int i = 0; i < returnRaster.getWidth(); i++) {
			for (int j = 0; j < returnRaster.getHeight(); j++) {
				if (Float.isNaN(raster1.getSampleFloat(i, j, 0)) ||
						raster1.getSampleFloat(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster2.getSampleFloat(i, j, 0));
				}
				else if (Float.isNaN(raster2.getSampleFloat(i, j, 0)) || 
						raster2.getSampleFloat(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0));
				}
				else {
					returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0)-raster2.getSampleFloat(i, j, 0));
				}
			}
		}
	}


	/**
	 *  Returns raster1-raster2
	 *
	 * @param  returnRaster               Raster that is populated with return value
	 * @param  raster1                    Input raster 1
	 * @param  raster2                    Input raster 2
	 * @param  noData                     No Data value for all rasters
	 * @exception  RasterMathOpException  Thrown if raster dimensions do not match
	 */
	public static void diff(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, double noData)
	throws RasterMathOpException {
		checkRasters(returnRaster, raster1, raster2);

		for (int i = 0; i < returnRaster.getWidth(); i++) {
			for (int j = 0; j < returnRaster.getHeight(); j++) {
				if (Double.isNaN(raster1.getSampleDouble(i, j, 0)) ||
						raster1.getSampleDouble(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster2.getSampleDouble(i, j, 0));
//					System.out.println("raster 1 has no data");
				}
				else if (Double.isNaN(raster2.getSampleDouble(i, j, 0)) ||
						raster2.getSampleDouble(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0));
//					System.out.println("raster 2 has no data");
				}
				else {
					returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0)-raster2.getSampleDouble(i, j, 0));
//					System.out.println("doing the diff! "+(raster1.getSampleDouble(i, j, 0)-raster2.getSampleDouble(i, j, 0)));
				}
			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 *  Returns raster1*raster2
	 *
	 * @param  returnRaster               Raster that is populated with return value
	 * @param  raster1                    Input raster 1
	 * @param  raster2                    Input raster 2
	 * @param  noData                     No Data value for all rasters
	 * @exception  RasterMathOpException  Thrown if raster dimensions do not match
	 */
	public static void multiply(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, int noData)
	throws RasterMathOpException {
		checkRasters(returnRaster, raster1, raster2);

		for (int i = 0; i < returnRaster.getWidth(); i++) {
			for (int j = 0; j < returnRaster.getHeight(); j++) {
				if (raster1.getSample(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
				}
				else if (raster2.getSample(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
				}
				else {
					returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0)*raster2.getSample(i, j, 0));
				}
			}
		}
	}


	/**
	 *  Returns raster1*raster2
	 *
	 * @param  returnRaster               Raster that is populated with return value
	 * @param  raster1                    Input raster 1
	 * @param  raster2                    Input raster 2
	 * @param  noData                     No Data value for all rasters
	 * @exception  RasterMathOpException  Thrown if raster dimensions do not match
	 */
	public static void multiply(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, float noData)
	throws RasterMathOpException {
		checkRasters(returnRaster, raster1, raster2);

		for (int i = 0; i < returnRaster.getWidth(); i++) {
			for (int j = 0; j < returnRaster.getHeight(); j++) {
				if (raster1.getSampleFloat(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster2.getSampleFloat(i, j, 0));
				}
				else if (raster2.getSampleFloat(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0));
				}
				else {
					returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0)*raster2.getSampleFloat(i, j, 0));
				}
			}
		}
	}


	/**
	 *  Returns raster1*raster2
	 *
	 * @param  returnRaster               Raster that is populated with return value
	 * @param  raster1                    Input raster 1
	 * @param  raster2                    Input raster 2
	 * @param  noData                     No Data value for all rasters
	 * @exception  RasterMathOpException  Thrown if raster dimensions do not match
	 */
	public static void multiply(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, double noData)
	throws RasterMathOpException {
		checkRasters(returnRaster, raster1, raster2);

		for (int i = 0; i < returnRaster.getWidth(); i++) {
			for (int j = 0; j < returnRaster.getHeight(); j++) {
				if (raster1.getSampleDouble(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster2.getSampleDouble(i, j, 0));
				}
				else if (raster2.getSampleDouble(i, j, 0) == noData) {
					returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0));
				}
				else {
					returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0)*raster2.getSampleDouble(i, j, 0));
				}
			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	

   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void max(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, int noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (raster1.getSample(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
            }
            else if (raster2.getSample(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
            }
            else {
               if (raster1.getSample(i, j, 0) > raster2.getSample(i, j, 0)) {
                  returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
               }
               else {
                  returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
               }
            }
         }
      }
   }


   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void max(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, float noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (raster1.getSampleFloat(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster2.getSampleFloat(i, j, 0));
            }
            else if (raster2.getSampleFloat(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0));
            }
            else {
               if (raster1.getSampleFloat(i, j, 0) > raster2.getSampleFloat(i, j, 0)) {
                  returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0));
               }
               else {
                  returnRaster.setSample(i, j, 0, raster2.getSampleFloat(i, j, 0));
               }
            }
         }
      }
   }



   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void max(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, double noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      
      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (Double.isNaN(raster1.getSampleDouble(i, j, 0)) || 
            		raster1.getSampleDouble(i, j, 0) == noData) {
            	
               returnRaster.setSample(i, j, 0, raster2.getSampleDouble(i, j, 0));
            }
            else if (Double.isNaN(raster2.getSampleDouble(i, j, 0)) ||
            		raster2.getSampleDouble(i, j, 0) == noData) {
            	
               returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0));
            }
            else {
               if (raster1.getSampleDouble(i, j, 0) > raster2.getSampleDouble(i, j, 0)) {
                  returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0));
               }
               else {
                  returnRaster.setSample(i, j, 0, raster2.getSampleDouble(i, j, 0));
               }
            }
         }
      }
   }


//=============================================================================
//=============================================================================
//=============================================================================

   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void min(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, int noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (raster1.getSample(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
            }
            else if (raster2.getSample(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
            }
            else {
               if (raster1.getSample(i, j, 0) < raster2.getSample(i, j, 0)) {
                  returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
               }
               else {
                  returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
               }
            }
         }
      }
   }


   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void min(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, float noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (raster1.getSampleFloat(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster2.getSampleFloat(i, j, 0));
            }
            else if (raster2.getSampleFloat(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0));
            }
            else {
               if (raster1.getSampleFloat(i, j, 0) < raster2.getSampleFloat(i, j, 0)) {
                  returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0));
               }
               else {
                  returnRaster.setSample(i, j, 0, raster2.getSampleFloat(i, j, 0));
               }
            }
         }
      }
   }



   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void min(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, double noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (raster1.getSampleDouble(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster2.getSampleDouble(i, j, 0));
            }
            else if (raster2.getSampleDouble(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0));
            }
            else {
               if (raster1.getSampleDouble(i, j, 0) < raster2.getSampleDouble(i, j, 0)) {
                  returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0));
               }
               else {
                  returnRaster.setSample(i, j, 0, raster2.getSampleDouble(i, j, 0));
               }
            }
         }
      }
   }


//=============================================================================
//=============================================================================
//=============================================================================

   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void absMax(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, int noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (raster1.getSample(i, j, 0) == noData) {
               if (raster2.getSample(i, j, 0) == noData) {
                  returnRaster.setSample(i, j, 0, noData);
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster2.getSample(i, j, 0)));
               }
            }
            else if (raster2.getSample(i, j, 0) == noData) {
               if (raster1.getSample(i, j, 0) == noData) {
                  returnRaster.setSample(i, j, 0, noData);
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster1.getSample(i, j, 0)));
               }
            }
            else {
               if (Math.abs(raster1.getSample(i, j, 0)) > Math.abs(raster2.getSample(i, j, 0))) {
                  returnRaster.setSample(i, j, 0, Math.abs(raster1.getSample(i, j, 0)));
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster2.getSample(i, j, 0)));
               }
            }
         }
      }
   }


   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void absMax(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, float noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (raster1.getSampleFloat(i, j, 0) == noData) {
               if (raster2.getSampleFloat(i, j, 0) == noData) {
                  returnRaster.setSample(i, j, 0, noData);
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster2.getSampleFloat(i, j, 0)));
               }
            }
            else if (raster2.getSampleFloat(i, j, 0) == noData) {
               if (raster1.getSampleFloat(i, j, 0) == noData) {
                  returnRaster.setSample(i, j, 0, noData);
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster1.getSampleFloat(i, j, 0)));
               }
            }
            else {
               if (Math.abs(raster1.getSampleFloat(i, j, 0)) > Math.abs(raster2.getSampleFloat(i, j, 0))) {
                  returnRaster.setSample(i, j, 0, Math.abs(raster1.getSampleFloat(i, j, 0)));
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster2.getSampleFloat(i, j, 0)));
               }
            }
         }
      }
   }



   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void absMax(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, double noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            
            
            
            if (raster2.getSampleDouble(i, j, 0) == -100) {
               raster2.setSample(i, j, 0, noData);
            }
            if (raster2.getSampleDouble(i, j, 0) == -200) {
               raster2.setSample(i, j, 0, noData);
            }
            
            
            
            
            
            if (Double.isNaN(raster1.getSampleDouble(i, j, 0)) ||
            		raster1.getSampleDouble(i, j, 0) == noData) {
               if (Double.isNaN(raster2.getSampleDouble(i, j, 0)) ||
            		   raster2.getSampleDouble(i, j, 0) == noData) {
                  returnRaster.setSample(i, j, 0, noData);
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster2.getSampleDouble(i, j, 0)));
               }
            }
            else if (Double.isNaN(raster2.getSampleDouble(i, j, 0)) ||
            		raster2.getSampleDouble(i, j, 0) == noData) {
               if (Double.isNaN(raster1.getSampleDouble(i, j, 0)) ||
            		   raster1.getSampleDouble(i, j, 0) == noData) {
                  returnRaster.setSample(i, j, 0, noData);
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster1.getSampleDouble(i, j, 0)));
               }
            }
            else {
               if (Math.abs(raster1.getSampleDouble(i, j, 0)) > Math.abs(raster2.getSampleDouble(i, j, 0))) {
                  returnRaster.setSample(i, j, 0, Math.abs(raster1.getSampleDouble(i, j, 0)));
               }
               else {
                  returnRaster.setSample(i, j, 0, Math.abs(raster2.getSampleDouble(i, j, 0)));
               }
            }
         }
      }
   }


   
   
   
   
   
   
   
   
   
   
   
   
   
   
   


 //=============================================================================
 //=============================================================================
 //=============================================================================

    /**
     *  Description of the Method
     *
     * @param  returnRaster               Raster that is populated with return value
     * @param  raster1                    Input raster 1
     * @param  raster2                    Input raster 2
     * @param  noData                     No Data value for all rasters
     * @exception  RasterMathOpException  Thrown if raster dimensions do not match
     */
    public static void absMaxSigned(WritableRaster returnRaster, WritableRaster raster1, 
    		WritableRaster raster2, int noData)
           throws RasterMathOpException {
       checkRasters(returnRaster, raster1, raster2);

       for (int i = 0; i < returnRaster.getWidth(); i++) {
          for (int j = 0; j < returnRaster.getHeight(); j++) {
             if (raster1.getSample(i, j, 0) == noData) {
                if (raster2.getSample(i, j, 0) == noData) {
                   returnRaster.setSample(i, j, 0, noData);
                }
                else {
                   returnRaster.setSample(i, j, 0, Math.abs(raster2.getSample(i, j, 0)));
                }
             }
             else if (raster2.getSample(i, j, 0) == noData) {
                if (raster1.getSample(i, j, 0) == noData) {
                   returnRaster.setSample(i, j, 0, noData);
                }
                else {
                   returnRaster.setSample(i, j, 0, Math.abs(raster1.getSample(i, j, 0)));
                }
             }
             else {
                if (Math.abs(raster1.getSample(i, j, 0)) > Math.abs(raster2.getSample(i, j, 0))) {
                   returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
                }
                else {
                   returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
                }
             }
          }
       }
    }


    /**
     *  Description of the Method
     *
     * @param  returnRaster               Raster that is populated with return value
     * @param  raster1                    Input raster 1
     * @param  raster2                    Input raster 2
     * @param  noData                     No Data value for all rasters
     * @exception  RasterMathOpException  Thrown if raster dimensions do not match
     */
    public static void absMaxSigned(WritableRaster returnRaster, WritableRaster raster1, 
    		WritableRaster raster2, float noData)
           throws RasterMathOpException {
       checkRasters(returnRaster, raster1, raster2);

       for (int i = 0; i < returnRaster.getWidth(); i++) {
          for (int j = 0; j < returnRaster.getHeight(); j++) {
             if (raster1.getSampleFloat(i, j, 0) == noData
            		 || Float.isNaN(raster1.getSampleFloat(i, j, 0))) {
                if (raster2.getSampleFloat(i, j, 0) == noData
                		|| Float.isNaN(raster2.getSampleFloat(i, j, 0))) {
                   returnRaster.setSample(i, j, 0, noData);
                }
                else {
                   returnRaster.setSample(i, j, 0, Math.abs(raster2.getSampleFloat(i, j, 0)));
                }
             }
             else if (raster2.getSampleFloat(i, j, 0) == noData 
            		 || Float.isNaN(raster2.getSampleFloat(i, j, 0))) {
                if (raster1.getSampleFloat(i, j, 0) == noData
                		|| Float.isNaN(raster1.getSampleFloat(i, j, 0))) {
                   returnRaster.setSample(i, j, 0, noData);
                }
                else {
                   returnRaster.setSample(i, j, 0, Math.abs(raster1.getSampleFloat(i, j, 0)));
                }
             }
             else {
                if (Math.abs(raster1.getSampleFloat(i, j, 0)) > Math.abs(raster2.getSampleFloat(i, j, 0))) {
                   returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0));
                }
                else {
                   returnRaster.setSample(i, j, 0, raster2.getSampleFloat(i, j, 0));
                }
             }
          }
       }
    }



    /**
     *  Description of the Method
     *
     * @param  returnRaster               Raster that is populated with return value
     * @param  raster1                    Input raster 1
     * @param  raster2                    Input raster 2
     * @param  noData                     No Data value for all rasters
     * @exception  RasterMathOpException  Thrown if raster dimensions do not match
     */
    public static void absMaxSigned(WritableRaster returnRaster, WritableRaster raster1, 
    		WritableRaster raster2, double noData)
           throws RasterMathOpException {
       checkRasters(returnRaster, raster1, raster2);

       for (int i = 0; i < returnRaster.getWidth(); i++) {
          for (int j = 0; j < returnRaster.getHeight(); j++) {
             
             
             
             if (raster2.getSampleDouble(i, j, 0) == -100) {
                raster2.setSample(i, j, 0, noData);
             }
             if (raster2.getSampleDouble(i, j, 0) == -200) {
                raster2.setSample(i, j, 0, noData);
             }
             
             
             
             
             
             if (Double.isNaN(raster1.getSampleDouble(i, j, 0)) ||
             		raster1.getSampleDouble(i, j, 0) == noData) {
                if (Double.isNaN(raster2.getSampleDouble(i, j, 0)) ||
             		   raster2.getSampleDouble(i, j, 0) == noData) {
                   returnRaster.setSample(i, j, 0, noData);
                }
                else {
                   returnRaster.setSample(i, j, 0, Math.abs(raster2.getSampleDouble(i, j, 0)));
                }
             }
             else if (Double.isNaN(raster2.getSampleDouble(i, j, 0)) ||
             		raster2.getSampleDouble(i, j, 0) == noData) {
                if (Double.isNaN(raster1.getSampleDouble(i, j, 0)) ||
             		   raster1.getSampleDouble(i, j, 0) == noData) {
                   returnRaster.setSample(i, j, 0, noData);
                }
                else {
                   returnRaster.setSample(i, j, 0, Math.abs(raster1.getSampleDouble(i, j, 0)));
                }
             }
             else {
                if (Math.abs(raster1.getSampleDouble(i, j, 0)) > Math.abs(raster2.getSampleDouble(i, j, 0))) {
                   returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0));
                }
                else {
                   returnRaster.setSample(i, j, 0, raster2.getSampleDouble(i, j, 0));
                }
             }
          }
       }
    }

    
    
    
    
    
    
    
    
//=============================================================================
//=============================================================================
//=============================================================================

   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void sum(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, int noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (raster1.getSample(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster2.getSample(i, j, 0));
            }
            else if (raster2.getSample(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster1.getSample(i, j, 0));
            }
            else {
               returnRaster.setSample(i, j, 0, (raster1.getSample(i, j, 0) + raster2.getSample(i, j, 0)));
            }
         }
      }
   }


   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void sum(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, float noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (Float.isNaN(raster1.getSampleFloat(i, j, 0)) || 
            		raster1.getSampleFloat(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster2.getSampleFloat(i, j, 0));
            }
            else if (Float.isNaN(raster2.getSampleFloat(i, j, 0)) ||
            		raster2.getSampleFloat(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster1.getSampleFloat(i, j, 0));
            }
            else {
               returnRaster.setSample(i, j, 0, (raster1.getSampleFloat(i, j, 0) + raster2.getSampleFloat(i, j, 0)));
            }
         }
      }
   }



   /**
    *  Description of the Method
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void sum(WritableRaster returnRaster, WritableRaster raster1, WritableRaster raster2, double noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            if (Double.isNaN(raster1.getSampleDouble(i, j, 0)) || 
            		raster1.getSampleDouble(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster2.getSampleDouble(i, j, 0));
            }
            else if (Double.isNaN(raster2.getSampleDouble(i, j, 0)) ||
            		raster2.getSampleDouble(i, j, 0) == noData) {
               returnRaster.setSample(i, j, 0, raster1.getSampleDouble(i, j, 0));
            }
            else {
               returnRaster.setSample(i, j, 0, (raster1.getSampleDouble(i, j, 0) + raster2.getSampleDouble(i, j, 0)));
            }
         }
      }
   }


//=============================================================================
//=============================================================================
//=============================================================================


   /**
    *  Calculates average -- If NoData is present in any grid cell, it is treated as a zero value!
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @param  weight1                    Weight that raster1 receives in average (allows for reaveraging to get average of many grids)
    * @param  weight2                    Weight that raster2 receives in average
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void average(WritableRaster returnRaster, WritableRaster raster1, int weight1, WritableRaster raster2, int weight2, int noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      int value, input1, input2;
      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            input1 = raster1.getSample(i, j, 0); 
            input2 = raster2.getSample(i, j, 0); 
            if (input1 == noData) {
               input1 = 0;
            }
            if (input2 == noData) {
               input2 = 0;
            }
            value = Math.round((input1 * weight1 + input2 * weight2) / (float) (weight1 + weight2));
            returnRaster.setSample(i, j, 0, value);
         }
      }
   }





   /**
    *  Calculates average -- If NoData is present in any grid cell, it is treated as a zero value!
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @param  weight1                    Weight that raster1 receives in average (allows for reaveraging to get average of many grids)
    * @param  weight2                    Weight that raster2 receives in average
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void average(WritableRaster returnRaster, WritableRaster raster1, int weight1, WritableRaster raster2, int weight2, float noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);

      float value, input1, input2;
      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            input1 = raster1.getSampleFloat(i, j, 0); 
            input2 = raster2.getSampleFloat(i, j, 0); 
            if (Float.isNaN(input1) || input1 == noData) {
               input1 = 0.0f;
            }
            if (Float.isNaN(input2) || input2 == noData) {
               input2 = 0.0f;
            }
            value = (input1 * weight1 + input2 * weight2) / (float) (weight1 + weight2);
            returnRaster.setSample(i, j, 0, value);
         }
      }
   }




   /**
    *  Calculates average -- If NoData is present in any grid cell, it is treated as a zero value!
    *
    * @param  returnRaster               Raster that is populated with return value
    * @param  raster1                    Input raster 1
    * @param  raster2                    Input raster 2
    * @param  noData                     No Data value for all rasters
    * @param  weight1                    Weight that raster1 receives in average (allows for reaveraging to get average of many grids)
    * @param  weight2                    Weight that raster2 receives in average
    * @exception  RasterMathOpException  Thrown if raster dimensions do not match
    */
   public static void average(WritableRaster returnRaster, WritableRaster raster1, int weight1, WritableRaster raster2, int weight2, double noData)
          throws RasterMathOpException {
      checkRasters(returnRaster, raster1, raster2);
      
      double value, input1, input2;
      for (int i = 0; i < returnRaster.getWidth(); i++) {
         for (int j = 0; j < returnRaster.getHeight(); j++) {
            
            
            if (raster2.getSampleDouble(i, j, 0) == -100) {
               raster2.setSample(i, j, 0, noData);
            }
            if (raster2.getSampleDouble(i, j, 0) == -200) {
               raster2.setSample(i, j, 0, noData);
            }
            
            
            input1 = raster1.getSampleDouble(i, j, 0); 
            input2 = raster2.getSampleDouble(i, j, 0); 
            if (Double.isNaN(input1) || input1 == noData) {
//               input1 = 0.0;
            	returnRaster.setSample(i, j, 0, input2);
            }
            else if (Double.isNaN(input2) || input2 == noData) {
//               input2 = 0.0;
            	returnRaster.setSample(i, j, 0, input1);
            }
            else {
            	value = (input1 * weight1 + input2 * weight2) / (double) (weight1 + weight2);
            	returnRaster.setSample(i, j, 0, value);
            }
         }
      }
   }


//=============================================================================
//=============================================================================
//=============================================================================







   /**
    *  Checks the bounds of each raster.  Throws RasterMathOpException if bounds do not match.
    *
    * @param  raster1                    Description of the Parameter
    * @param  raster2                    Description of the Parameter
    * @param  raster3                    Description of the Parameter
    * @exception  RasterMathOpException  Thrown if the bounds do not match.
    */
   private static void checkRasters(WritableRaster raster1, WritableRaster raster2, WritableRaster raster3)
          throws RasterMathOpException {

      // Check dimensions of rasters -- All 3 MUST match
      if (raster1.getBounds().equals(raster2.getBounds()) &&
            raster1.getBounds().equals(raster3.getBounds())) {

         return;
      }
      else {
         throw new RasterMathOpException("RASTER BOUNDS DO NOT MATCH");
      }

   }

}

