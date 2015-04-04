package gov.noaa.ncdc.wct.export.raster;

import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.cdm.SmoothingOperation;
import gov.noaa.ncdc.wct.decoders.cdm.SmoothingOperation.SmoothingInfo;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradValueFactory;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileCache;
import javax.media.jai.WritableRenderedImageAdapter;

import org.apache.commons.lang.ArrayUtils;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.geotools.util.NumberRange;

public class WCTGridCoverageSupport {



    private double smoothFactor = 0.0;
    private boolean isPowerSmoothing = false;
    private WCTFilter wctFilter = new WCTFilter();

    public static enum AlphaInterpolationType { LINEAR_ASCENDING, NONE };


    private URL paletteOverrideURL = null;




    public RenderedImage getRenderedImage(WritableRaster raster, SampleDimension[] bands) {
        return PlanarImage.wrapRenderedImage(
                new BufferedImage(bands[0].getColorModel(0, bands.length), raster, false, null));
    }

    
    
    
    /**
     * RF values are ignored
     * @param wctRaster
     * @param header
     * @param alpha
     * @return
     */
    public GridCoverage getGridCoverage(WCTRaster wctRaster, NexradHeader header, int alpha) {
    	
		SampleDimensionAndLabels sd = 
			NexradSampleDimensionFactory.getSampleDimensionAndLabels(
					header.getProductCode(), false);
		
		if (sd == null) {
			return getGridCoverage(wctRaster, header, false, 
					NexradColorFactory.getTransparentColors(header.getProductCode(), false, alpha));
		}
		
		WritableRaster raster = wctRaster.getWritableRaster();
		if (smoothFactor > 0) {
			raster = getSmoothedRaster(wctRaster, (int)smoothFactor, true, sd.getSampleDimension());
		}
			
			
		SampleDimension[] sdArray = setSampleDimensionAlpha(new SampleDimension[] { sd.getSampleDimension() }, alpha);

		return new GridCoverage(wctRaster.getVariableName(), raster, 
				GeographicCoordinateSystem.WGS84, null, 
				new Envelope(wctRaster.getBounds()), sdArray);

    }
    
    
    
    
    
    
    
    
    
    
    
    


    /**
     *  Create GridCoverage from the WritableRaster object
     *
     * @param  header  NexradHeader of input to FeatureRasterizer
     * @return         The gridCoverage value
     */
    public GridCoverage getGridCoverage(WCTRaster wctRaster, NexradHeader header) {
        return getGridCoverage(wctRaster, header, true);
    }



    /**
     *  Create GridCoverage from the WritableRaster object
     *
     * @param  header    NexradHeader of input to FeatureRasterizer
     * @param  classify  Is data classified?
     * @return           The gridCoverage value
     */
    public GridCoverage getGridCoverage(WCTRaster wctRaster, NexradHeader header, boolean classify) {
        Color[] colors = NexradColorFactory.getColors(header.getProductCode(), classify);
        double[] vals = NexradValueFactory.getProductMaxMinValues(header, classify);

        return getGridCoverage(wctRaster, header, classify, colors, vals);
    }

    /**
     *  Create GridCoverage from the WritableRaster object
     *
     * @param  header    NexradHeader of input to FeatureRasterizer
     * @param  classify  Is data classified?
     * @param  colors    Color array to use as ColorMap
     * @return           The gridCoverage value
     */
    public GridCoverage getGridCoverage(WCTRaster wctRaster, NexradHeader header, boolean classify, Color[] colors) {
        double[] vals = NexradValueFactory.getProductMaxMinValues(header, classify);

        return getGridCoverage(wctRaster, header, classify, colors, vals);
    }

    /**
     *  Create GridCoverage from the WritableRaster object <br><br>
     *  colors[0] = color at vals[0] (min value) <br>
     *  colors[colors.length-1] = color at vals[1] (max value) <br>
     *
     * @param  header    NexradHeader of input to FeatureRasterizer
     * @param  classify  Is data classified?
     * @param  colors    Color array to use as ColorMap
     * @param  vals      Min/Max values that used to color the GridCoverage
     * @return           The gridCoverage value
     */
    public GridCoverage getGridCoverage(WCTRaster wctRaster, NexradHeader header, boolean classify,
            Color[] colors, double[] vals) {

    	
    	if (paletteOverrideURL != null) {
    		try {
    			
    			
//System.out.println("\n\nold vals/colors: "+Arrays.toString(vals)+" / "+Arrays.toString(colors));    			
    	
//    			System.out.println("palette override url: "+paletteOverrideURL);
    			
    			BufferedReader br = new BufferedReader(new InputStreamReader(paletteOverrideURL.openStream()));
    			ColorsAndValues cav1 = ColorLutReaders.parseWCTPal(br)[0];
    			br.close();
    			ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(cav1, 50);
    			colors = cav.getColors();
    			vals = new double[] { cav.getValues()[0], cav.getValues()[cav.getValues().length-1] };
    			
//System.out.println("new vals/colors: "+Arrays.toString(vals)+" / "+Arrays.toString(colors));
    			
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	

//        System.out.println("vals : "+Arrays.toString(vals));



        java.awt.geom.Rectangle2D.Double bounds = wctRaster.getBounds();
        int width = wctRaster.getWritableRaster().getWidth();
        int height = wctRaster.getWritableRaster().getHeight();
        double cellWidth = wctRaster.getCellWidth();
        double cellHeight = wctRaster.getCellHeight();
        boolean emptyGrid = wctRaster.isEmptyGrid();

        WritableRaster raster = wctRaster.getWritableRaster();


//        System.out.println("gcSupport: BOUNDS="+bounds);
//        System.out.println("gcSupport: height="+height+" width="+width);
//        System.out.println("gcSupport: cellWidth="+cellWidth);
//        System.out.println("gcSupport: cellHeight="+cellHeight);
//        System.out.println("gcSupport: emptyGrid="+emptyGrid);

        
        
//      for (int i = 0; i < width; i++) {
//      for (int j = 0; j < height; j++) {
//      if (raster.getSampleFloat(i, j, 0) != wctRaster.getNoDataValue()) {
//      System.out.println(raster.getSampleFloat(i, j, 0));
//      }
//      }
//      }




        double[] minValues = new double[1];
        double[] maxValues = new double[1];
        if (classify) {
            minValues[0] = -0.1;
            maxValues[0] = (double) (header.getNumberOfLevels(classify) - 1);
        }
        else {
            minValues[0] = vals[0] - 0.000001;
            maxValues[0] = vals[1];
        }

        
        java.awt.geom.Rectangle2D.Double rect = null;
        if (bounds.getWidth() == 0) {
            System.err.println("THIS GRID IS EMPTY");
            emptyGrid = true;
            rect = MaxGeographicExtent.getNexradExtent(header);
        }
        else {
        	rect = new java.awt.geom.Rectangle2D.Double(bounds.x, bounds.y, width * cellWidth, height * cellHeight); 
        }
        
        
            






        // change NoData values in Raster
    	double gridMinValue = Double.POSITIVE_INFINITY;
    	double gridMaxValue = Double.NEGATIVE_INFINITY;
    	
        if (smoothFactor > 0) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (raster.getSampleFloat(i, j, 0) != wctRaster.getNoDataValue() &&
                    		! Float.isNaN(raster.getSampleFloat(i, j, 0))) {
                    	gridMinValue = Math.min(gridMinValue, raster.getSampleFloat(i, j, 0));
                    	gridMaxValue = Math.max(gridMaxValue, raster.getSampleFloat(i, j, 0));
                    }
                }
            }

        	
        	
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (raster.getSampleFloat(i, j, 0) == wctRaster.getNoDataValue() 
                    		|| Float.isNaN(raster.getSampleFloat(i, j, 0))) {
                        // Set nodata values to 0.0 for Level-2 velocity when smoothing
                        if (header.getProductCode()== NexradHeader.LEVEL2_VELOCITY) {
                            if (classify) {
                                raster.setSample(i, j, 0, 7); // cat 7 is 0 for velocity
                            }
                            else {
                                raster.setSample(i, j, 0, 0.0);
                            }
                        }
                        else {
                            raster.setSample(i, j, 0, gridMinValue - (vals[1]-vals[0])/15.0);
                        }
                    }
                }
            }
        }




        Envelope envelope = new Envelope(rect);
        GridCoverage gc = new GridCoverage(wctRaster.getLongName(), raster, GeographicCoordinateSystem.WGS84,
                envelope, minValues, maxValues, null, new Color[][]{colors}, null);

        
        if (smoothFactor > 0) {

            // CONVERT dBZ to Gain
            if (isPowerSmoothing) {
                if (header.getProductCode() == NexradHeader.LEVEL2_REFLECTIVITY ||
                        header.getProductCode() == NexradHeader.L3PC_COMPOSITE_REFLECTIVITY_248NM_8LVL ||
                        header.getProductCode() == NexradHeader.L3PC_COMPOSITE_REFLECTIVITY_124NM_16LVL ||
                        header.getProductCode() == NexradHeader.L3PC_COMPOSITE_REFLECTIVITY_248NM_16LVL ||
                        header.getProductCode() == NexradHeader.L3PC_LOW_LAYER_COMP_REFLECTIVITY ||
                        header.getProductCode() == NexradHeader.L3PC_MID_LAYER_COMP_REFLECTIVITY ||
                        header.getProductCode() == NexradHeader.L3PC_HIGH_LAYER_COMP_REFLECTIVITY ||
                        header.getProductCode() == NexradHeader.L3PC_BASE_REFLECTIVITY_124NM ||
                        header.getProductCode() == NexradHeader.L3PC_BASE_REFLECTIVITY_248NM ||
                        header.getProductCode() == NexradHeader.Q2_NATL_MOSAIC_3DREFL
                ) {

//                    System.out.println("POWER SMOOTHING!");

                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {


                            double val = raster.getSampleFloat(i, j, 0);
                            val = Math.pow(10, val/10.0);
                            raster.setSample(i, j, 0, val);
                        }
                    }
                }
            }




            SmoothingOperation smoothingOp = new SmoothingOperation();
            SmoothingInfo sminfo = smoothingOp.getSmoothingInfo(rect, 
            		wctRaster.getWritableRaster().getWidth(), 
            		wctRaster.getWritableRaster().getHeight(), (int)smoothFactor);
//            KernelJAI kernel = new KernelJAI(kernelSize, kernelSize, kernelMatrix);        
            KernelJAI kernel = new KernelJAI(sminfo.getKernelSize(), sminfo.getKernelSize(), sminfo.getKernelMatrix());        
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(gc.getRenderedImage());
            pb.add(kernel);      
            PlanarImage output = JAI.create("convolve", pb, null);




            // CONVERT back to dBZ
            if (isPowerSmoothing) {
                if (header.getProductCode() == NexradHeader.LEVEL2_REFLECTIVITY ||
                        header.getProductCode() == NexradHeader.L3PC_COMPOSITE_REFLECTIVITY_248NM_8LVL ||
                        header.getProductCode() == NexradHeader.L3PC_COMPOSITE_REFLECTIVITY_124NM_16LVL ||
                        header.getProductCode() == NexradHeader.L3PC_COMPOSITE_REFLECTIVITY_248NM_16LVL ||
                        header.getProductCode() == NexradHeader.L3PC_LOW_LAYER_COMP_REFLECTIVITY ||
                        header.getProductCode() == NexradHeader.L3PC_MID_LAYER_COMP_REFLECTIVITY ||
                        header.getProductCode() == NexradHeader.L3PC_HIGH_LAYER_COMP_REFLECTIVITY ||
                        header.getProductCode() == NexradHeader.L3PC_BASE_REFLECTIVITY_124NM ||
                        header.getProductCode() == NexradHeader.L3PC_BASE_REFLECTIVITY_248NM ||
                        header.getProductCode() == NexradHeader.Q2_NATL_MOSAIC_3DREFL
                ) {

                    raster = (WritableRaster)(output.getData());
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {

                            double val = raster.getSampleFloat(i, j, 0);

                            val = (float)(10*(Math.log(val)/Math.log(10)));
                            raster.setSample(i, j, 0, val);

                        }
                    }
                }
            }            



            boolean scrubRaster = true;

            double maxVal = -99999;
            double minVal = 99999;
            int kernelSize = sminfo.getKernelSize();
            
            WritableRaster smoothedRaster = (WritableRaster)(output.getData());
            if (scrubRaster) {

                float val;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {

                        val = smoothedRaster.getSampleFloat(i, j, 0);

                        if (val <= gridMinValue) {
                            //System.out.println("111 "+(minValues[0] - 5.0)+"   :::   "+val+"  :: "+NO_DATA+"  :: "+kernelSize);
                            smoothedRaster.setSample(i, j, 0, wctRaster.getNoDataValue());
                        }
                        

                        if (i <= kernelSize/2 || width-i <= kernelSize/2 ||
                                j <= kernelSize/2 || height-j <= kernelSize/2 ) {
//                        	System.out.println(i+","+j);
                            smoothedRaster.setSample(i, j, 0, wctRaster.getNoDataValue());
                            //System.out.println("222  "+(minValues[0] - 5.0)+"   :::   "+val+"  :: "+NO_DATA+"  :: "+kernelSize);
                        }


                        maxVal = (val > maxVal) ? val : maxVal;
                        minVal = (val < minVal) ? val : minVal;

                    }
                }

//                System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//                System.out.println("MAX VAL: "+maxVal);
//                System.out.println("MIN VAL: "+minVal);
//                System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

            }

            gc = new GridCoverage(wctRaster.getLongName(), smoothedRaster, GeographicCoordinateSystem.WGS84,
                    envelope, minValues, maxValues, null, new Color[][]{colors}, null);            
        }




        return gc;
    }

    
    






    /**
     * Process the underlying raster with the provided smoothing factor.  This will
     * automatically call "setSmoothing(true)" and "setSmoothFactor(smoothFactor)".
     * The "baseline" value is set to the minimum value of the raster minus 0.0001 
     * (min - 0.0001) - this is equivalent to setting the valueOffset to 0.0001.
     *
     * @param smoothFactor  The general factor to smooth the data.  This is dependent
     *   on the size of the underlying raster.  For example, a smooth factor of 8 is
     *   a nominal smoothing on a 800 x 800 raster.  A smooth factor of 16 is the 
     *   proportional amount of smoothing for a 1600 x 1600 raster.
     *
     */   
    public WritableRaster getSmoothedRaster(WCTRaster wctRaster, int smoothFactor, double[] ignoreValues) {

        this.setSmoothFactor(smoothFactor);

        return getSmoothedRaster(wctRaster, ignoreValues);
    }





    /**
     * Process the underlying raster with the provided smoothing factor.  This will
     * automatically call "setSmoothing(true)" and "setSmoothFactor(smoothFactor)".
     * The "baseline" value is set to the minimum value of the raster minus 0.0001 
     * (min - 0.0001) - this is equivalent to setting the valueOffset to 0.0001.
     *
     */   
    public WritableRaster getSmoothedRaster(WCTRaster wctRaster, double[] ignoreValues) {

        return getSmoothedRaster(wctRaster, 0.0001, ignoreValues);
    }



    
    
    
    
    /**
     * Process the underlying raster with the provided smoothing factor.  This will
     * automatically call "setSmoothing(true)" and "setSmoothFactor(smoothFactor)".
     * The "baseline" value is set to the minimum value of the raster minus 0.0001 (min - 0.0001)
     *
     * @param smoothFactor  The general factor to smooth the data.  This is dependent
     *   on the size of the underlying raster.  For example, a smooth factor of 8 is
     *   a nominal smoothing on a 800 x 800 raster.  A smooth factor of 16 is the 
     *   proportional amount of smoothing for a 1600 x 1600 raster.
     *
     * @param useMinVal   valueOffsetPercentage of 5.0 and useMinVal of true
     *   means that the baseline will be the minimum grid value minus 5% (minVal - 5%)
     *   If useMinVal is true, the max value is used (maxVal + 5%).
     *   
     * @param sampleDimension   The SampleDimension from the GridCoverage
     *   used to render this raster.  The min/max values will be read from
     *   this sampleDimension.  Categories with a name starting with "Unique:"
     *   will not be included.
     */   
    public WritableRaster getSmoothedRaster(WCTRaster wctRaster, int smoothFactor, 
    		boolean useMinVal, SampleDimension sampleDimension) {


    	double maxval = Double.NEGATIVE_INFINITY;
        double minval = Double.POSITIVE_INFINITY;

    	List<Category> catList = sampleDimension.getCategories();
    	ArrayList<Double> ignoreList = new ArrayList<Double>();
    	
    	for (Category cat : catList) {
    		NumberRange range = cat.getRange();

    		if (cat.getName(null).startsWith("Unique:")) {
    			ignoreList.add((range.getMaximum()-range.getMinimum())/2.0);
    			continue;
    		}
    		
    		if (range.getMaximum() > maxval) {
    			maxval = range.getMaximum();
    		}
    		if (range.getMinimum() < minval) {
    			minval = range.getMinimum();
    		}
    	}

    	
    	
    	
        double valueOffset = (maxval - minval)*0.1; 
    	
//        System.out.println("min/max: "+minval+"/"+maxval+"   valueOffset:::: "+valueOffset);
        
        double[] ignoreValues = ArrayUtils.toPrimitive( ignoreList.toArray(new Double[ignoreList.size()]) );
        
    	
//        System.out.println("valueOffset: "+valueOffset+"  ignoreValues:"+Arrays.toString(ignoreValues));
        
        
        this.setSmoothFactor(smoothFactor);
        return getSmoothedRaster(wctRaster, useMinVal, valueOffset, ignoreValues);

    }
    

    
    
    
    
    /**
     * Process the underlying raster with the provided smoothing factor.  This will
     * automatically call "setSmoothing(true)" and "setSmoothFactor(smoothFactor)".
     * The "baseline" value is set to the minimum value of the raster minus 0.0001 (min - 0.0001)
     *
     * @param smoothFactor  The general factor to smooth the data.  This is dependent
     *   on the size of the underlying raster.  For example, a smooth factor of 8 is
     *   a nominal smoothing on a 800 x 800 raster.  A smooth factor of 16 is the 
     *   proportional amount of smoothing for a 1600 x 1600 raster.
     *
     * @param useMinVal   valueOffsetPercentage of 5.0 and useMinVal of true
     *   means that the baseline will be the minimum grid value minus 5% (minVal - 5%)
     *   If useMinVal is true, the max value is used (maxVal + 5%).
     *   
     * @param ignoreValues   values which will be converted to NaN and excluded
     *   from the smoothing operation.     *   
     *   
     * @param valueOffsetPercentage  The offset from the minimum grid value which will be
     *   used for the baseline.  For example, a valueOffsetPercentage of 5% and useMinVal of true
     *   means that the baseline will be the minimum grid value minus 5% of difference
     *   between the min and max value. (minVal - 5%)
     *   If useMinVal is true, the max value is used (maxVal + 5%).
     */   
    public WritableRaster getSmoothedRaster(WCTRaster wctRaster, int smoothFactor, 
    		boolean useMinVal, double[] ignoreValues, int valueOffsetPercentage) {

        double maxval = Double.NEGATIVE_INFINITY;
        double minval = Double.POSITIVE_INFINITY;
        float val = 0.0f;

        int width = wctRaster.getWritableRaster().getWidth();
        int height = wctRaster.getWritableRaster().getHeight();
        double noDataValue = wctRaster.getNoDataValue();
        WritableRaster raster = wctRaster.getWritableRaster();

        for (int i = 0; i < width; i++) {
        	for (int j = 0; j < height; j++) {
        		val = raster.getSampleFloat(i, j, 0);

        		boolean ignore = false;
        		for (int n=0; n<ignoreValues.length; n++) {
        			if (val == ignoreValues[n]) {
        				ignore = true;
        			}
        		}
        		
        		if (ignore) {
        			continue;
        		}
        		
        		
        		if (val < minval && val != noDataValue && ! Float.isNaN(val)) {
        			minval = val;
        		}
        		if (val > maxval && val != noDataValue && ! Float.isNaN(val)) {
        			maxval = val;
        		}
        	}         
        }

        double valueOffset = (maxval - minval)*0.01*valueOffsetPercentage; 
    	
//        System.out.println("min/max: "+minval+"/"+maxval+"   valueOffset:::: "+valueOffset);
    	
        this.setSmoothFactor(smoothFactor);
        return getSmoothedRaster(wctRaster, useMinVal, valueOffset, ignoreValues);

    }
    

    /**
     * Process the underlying raster with the provided smoothing factor.  This will
     * automatically call "setSmoothing(true)" and "setSmoothFactor(smoothFactor)".
     * The "baseline" value is set to the minimum value of the raster minus 0.0001 (min - 0.0001)
     *
     * @param smoothFactor  The general factor to smooth the data.  This is dependent
     *   on the size of the underlying raster.  For example, a smooth factor of 8 is
     *   a nominal smoothing on a 800 x 800 raster.  A smooth factor of 16 is the 
     *   proportional amount of smoothing for a 1600 x 1600 raster.
     *
     * @param useMinVal   valueOffset of 5.0 and useMinVal of true
     *   means that the baseline will be the minimum grid value minus 5.0 (minVal - 5.0)
     *   If useMinVal is true, the max value is used (maxVal + 5.0).
     *   
     * @param valueOffset  The offset from the minimum grid value which will be
     *   used for the baseline.  For example, a valueOffset of 5.0 and useMinVal of true
     *   means that the baseline will be the minimum grid value minus 5.0 (minVal - 5.0)
     *   If useMinVal is true, the max value is used (maxVal + 5.0).
     */   
    public WritableRaster getSmoothedRaster(WCTRaster wctRaster, int smoothFactor, boolean useMinVal, double valueOffset, double[] ignoreValues) {

        this.setSmoothFactor(smoothFactor);
        return getSmoothedRaster(wctRaster, useMinVal, valueOffset, ignoreValues);
    }





    /**
     * Process the underlying raster with the provided smoothing factor.  This will
     * automatically call "setSmoothing(true)" and "setSmoothFactor(smoothFactor)".
     * The "baseline" value is set to the minimum value of the raster minus 0.0001 
     * (min - 0.0001) - this is equivalent to setting the valueOffset to 0.0001.
     *
     * @param valueOffset  The offset from the minimum grid value which will be
     *   used for the baseline.  For example, a valueOffset of 5.0 and useMinVal of true
     *   means that the baseline will be the minimum grid value minus 5.0 (minVal - 5.0).
     *   If useMinVal is true, the max value is used (maxVal + 5.0).
     */   
    public WritableRaster getSmoothedRaster(WCTRaster wctRaster, boolean useMinVal, double valueOffset, double[] ignoreValues) {

        // Get min value of raster
        double maxval = Double.NEGATIVE_INFINITY;
        double minval = Double.POSITIVE_INFINITY;
        float val = 0.0f;
        double baseline;


        int width = wctRaster.getWritableRaster().getWidth();
        int height = wctRaster.getWritableRaster().getHeight();
        double noDataValue = wctRaster.getNoDataValue();
        WritableRaster raster = wctRaster.getWritableRaster();

        if (useMinVal) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    val = raster.getSampleFloat(i, j, 0);
                    if (val < minval && val != noDataValue && ! Float.isNaN(val)) {
                        minval = val;
                    }
                }         
            }
            baseline = minval-valueOffset;
        }
        else {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    val = raster.getSampleFloat(i, j, 0);
                    if (val > maxval && val != noDataValue && ! Float.isNaN(val)) {
                        maxval = val;
                    }
                }         
            }
            baseline = maxval+valueOffset;
        }

        return getSmoothedRaster(wctRaster, baseline, ignoreValues);
    }




    /**
     * Process the underlying raster with the provided smoothing factor.  This will
     * automatically call "setSmoothing(true)" and "setSmoothFactor(smoothFactor)".
     *
     * @param smoothFactor  The general factor to smooth the data.  This is dependant
     *   on the size of the underlying raster.  For example, a smooth factor of 8 is
     *   a nominal smoothing on a 800 x 800 raster.  A smooth factor of 16 is the 
     *   proportional amount of smoothing for a 1600 x 1600 raster.
     *
     * @param baseline  The value which the data will be smoothed towards.  This 
     * could be something like "0.0" or the min value of the raster.  Because 
     * NO_DATA could be numerically far (such as -999), the baseline factor
     * should be something closer to the data.
     */   
    public WritableRaster getSmoothedRaster(WCTRaster wctRaster, int smoothFactor, double baseline, double[] ignoreValues) {

        this.setSmoothFactor(smoothFactor);

        return getSmoothedRaster(wctRaster, baseline, ignoreValues);
    }





    /**
     * Process the underlying raster with the set smoothing values (setSmoothing(boolean) and 
     *  setSmoothFactor(int)).  
     *
     * @param baseline  The value which the data will be smoothed towards.  This 
     * could be something like "0.0" or the min value of the raster.  Because 
     * NO_DATA could be numerically far (such as -999), the baseline factor
     * should be something closer to the data.
     */   
    public WritableRaster getSmoothedRaster(WCTRaster wctRaster, double baseline, double[] ignoreValues) {

        double maxval = Double.NEGATIVE_INFINITY;
        double minval = Double.POSITIVE_INFINITY;

        int width = wctRaster.getWritableRaster().getWidth();
        int height = wctRaster.getWritableRaster().getHeight();
        double noDataValue = wctRaster.getNoDataValue();
        WritableRaster raster = wctRaster.getWritableRaster();


        java.awt.geom.Rectangle2D.Double rect = wctRaster.getBounds();
        
        float val;

        // change NoData values in Raster 
        int cnt = 0;
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
            	
            	val = raster.getSampleFloat(i, j, 0);
            	
            	boolean ignore = false;
            	for (int n=0; n<ignoreValues.length; n++) {
            		if (val == ignoreValues[n]) {
            			ignore = true;
            		}
            	}

        		if (val < minval && val != noDataValue && ! Float.isNaN(val) && ! ignore) {
        			minval = val;
        		}
        		if (val > maxval && val != noDataValue && ! Float.isNaN(val) && ! ignore) {
        			maxval = val;
        		}

            	
                if (val == noDataValue || ignore || Float.isNaN(val)) {
                    raster.setSample(i, j, 0, baseline);
                    cnt++;
                }
            }
        }
        System.out.println(cnt+" no data records set to baseline");
        
        
        
        // create temp. grid coverage
        Envelope envelope = new Envelope(rect);
        java.awt.Color[] colors = new Color[] { Color.black };
        double[] minmax = new double[] { 0.0, 1.0 };
        GridCoverage tempGC = new GridCoverage(wctRaster.getLongName(), raster, GeographicCoordinateSystem.WGS84,
                envelope, new double[] { minmax[0] }, new double[] { minmax[1] }, 
                null, new Color[][]{colors}, null);            

        
        
        // get dynamic smoothing info
        SmoothingOperation smoothingOp = new SmoothingOperation();
        SmoothingInfo sminfo = smoothingOp.getSmoothingInfo(rect, 
        		wctRaster.getWritableRaster().getWidth(), 
        		wctRaster.getWritableRaster().getHeight(), (int)smoothFactor);
//        KernelJAI kernel = new KernelJAI(kernelSize, kernelSize, kernelMatrix);        
        KernelJAI kernel = new KernelJAI(sminfo.getKernelSize(), sminfo.getKernelSize(), sminfo.getKernelMatrix());        

        // do smoothing
        java.awt.image.renderable.ParameterBlock pb = new java.awt.image.renderable.ParameterBlock();
        pb.addSource(tempGC.getRenderedImage());
        pb.add(kernel);      
        javax.media.jai.PlanarImage output = javax.media.jai.JAI.create("convolve", pb, null);


        // reset 'baseline' data back to 'NoData'
        // baseline value can be changed slightly by smoothing operation.
        // let's check for baseline value between 1% (based on min/max value) from baseline
        double baselineBuffer = (maxval - minval)*0.01;
        
        WritableRaster smoothedRaster = (WritableRaster)(output.getData());
        
        System.out.println("BASELINE:    "+baseline +" buffer("+baselineBuffer);
        
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
            	
            	val = smoothedRaster.getSampleFloat(i, j, 0);
            	if (wctFilter != null && wctFilter.getMinValue()[0] != WCTFilter.NO_MIN_VALUE) {
					if (val < wctFilter.getMinValue()[0]) {
						smoothedRaster.setSample(i, j, 0, Double.NaN);
					}
				}
				if (wctFilter != null && wctFilter.getMaxValue()[0] != WCTFilter.NO_MAX_VALUE) {
					if (val > wctFilter.getMaxValue()[0]) {
						smoothedRaster.setSample(i, j, 0, Double.NaN);
					}
				}
				
            	if (val >= (baseline-baselineBuffer) &&
            			val <= (baseline+baselineBuffer)) {
            		
            		smoothedRaster.setSample(i, j, 0, Double.NaN);
            	}
            }
        }
        
        // trim the edges based on the smoothing kernel used
        smoothedRaster = trimSmoothedEdges(smoothedRaster, sminfo.getKernelSize());

        return smoothedRaster;
    }



    /**
     * Sets the edges of the raster to the NO_DATA value, based on the kernel size.
     * These pixels are worthless following a smoothing operation anyway, so they 
     * should not be used.
     */
    public WritableRaster trimSmoothedEdges(WritableRaster raster, int kernelSize) {

    	int width = raster.getWidth();
    	int height = raster.getHeight();
    	
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
            	
            	if (i<kernelSize/2 || i>(width-kernelSize/2-1) ||
            			j<kernelSize/2 || j>(height-kernelSize/2-1)) {
            		raster.setSample(i, j, 0, Double.NaN);
            	}
            	
            }
        }
        
        return raster;
    }








    public double getSmoothFactor() {
        return smoothFactor;
    }
    public void setSmoothFactor(double smoothFactor) {
        this.smoothFactor = smoothFactor;
    }

    public boolean isPowerSmoothing() {
        return isPowerSmoothing;
    }
    public void setPowerSmoothing(boolean isPowerSmoothing) {
        this.isPowerSmoothing = isPowerSmoothing;
    }




    public static Color[] getColors(GridCoverage gc) {
        SampleDimension[] sdArray = gc.getSampleDimensions();
        Color[] colorArray = null;
        
        for (int n=0; n<sdArray.length; n++) {
            List<Category> catList = sdArray[n].getCategories();
            for (int i=0; i<1; i++) {
                return catList.get(i).getColors();
            }
        }    
        return null;
    }
    
    public static GridCoverage setColors(GridCoverage gc, Color[] newColors) {
        SampleDimension[] sdArray = gc.getSampleDimensions();
        Color[][] colorArrays = new Color[sdArray.length][];
        
        
//        for (Color c : newColors) {
//            System.out.println(c + " , " + c.getAlpha());
//        }
        
        
//        System.out.println("Sample Dimension size: "+sdArray.length);
        
        for (int n=0; n<sdArray.length; n++) {
//            System.out.println( sdArray[n].getMinimumValue() + " - " + sdArray[n].getMaximumValue() );
            
            List<Category> catList = sdArray[n].getCategories();
//            ArrayList<Category> newCatList = new ArrayList<Category>();

//            System.out.println("Category List: "+catList.size());

//            for (int i=0; i<catList.size(); i++) {
            for (int i=0; i<1; i++) {
//                Color[] colors = catList.get(i).getColors();
//                Color[] newColors = new Color[colors.length];
//                for (int j=0; j<colors.length; j++) {
////                    System.out.println("old color: "+colors[j] + " , alpha="+colors[j].getAlpha());
//                    newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), colors[j].getBlue(), alpha);
//                }
//                System.out.println("  --- "+Arrays.toString(catList.get(i).getColors()));
//                catList.get(i).recolor(newColors);
                colorArrays[n] = newColors;
                
//                System.out.println("cat min/max: "+catList.get(i).getRange().getMinimum()+"/"+catList.get(i).getRange().getMaximum());
                
//                Category cat = new Category(catList.get(i).getName(null), newColors, catList.get(i).getRange(), catList.get(i).getSampleToGeophysics());
//                cat.getRange().getMinimum()
//                newCatList.add(cat);
            }
            
//            sdArray[n] = new SampleDimension((Category[])(newCatList.toArray()), null);
        }    

        WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(gc.getRenderedImage());
        Raster data = img.getData();
//        double minValue = 999999999;
//        double maxValue = -999999999;
//        for (int j=0; j<data.getHeight(); j++) {
//            for (int i=0; i<data.getWidth(); i++) {
//                double value = data.getPixel(i, j, new double[1])[0];
//                if (value < minValue) minValue = value;
//                if (value > maxValue) maxValue = value;
//            }
//        }
//        System.out.println("min value = "+minValue+"  max value = "+maxValue);

        GridCoverage gc1 = new GridCoverage(gc.getName(null), (WritableRaster)data, gc.getCoordinateSystem(), gc.getEnvelope(),
                new double[] { sdArray[0].getMinimumValue() }, new double[] { sdArray[0].getMaximumValue() }, null, colorArrays, null);
        
        gc.dispose();

        return gc1;
    }
    
    
    
    
    
    
    
    
    public static GridCoverage applyAlpha(GridCoverage gc, int alpha) {
    	return applyAlpha(gc, alpha, AlphaInterpolationType.NONE);
    }
    
    public static GridCoverage applyAlpha(GridCoverage gc, int alpha, AlphaInterpolationType interpType) {
        
//        System.out.println("alpha = "+alpha);
        
        if (alpha < 0) {
            alpha = 0;
        }
        if (alpha > 255) {
            alpha = 255;
        }
        
        SampleDimension[] sdArray = gc.getSampleDimensions();
        Color[][] colorArrays = new Color[sdArray.length][];
        
        
//        System.out.println("Sample Dimension size: "+sdArray.length);
        
        for (int n=0; n<sdArray.length; n++) {
//            System.out.println( sdArray[n].getMinimumValue() + " - " + sdArray[n].getMaximumValue() );
            
            List<Category> catList = sdArray[n].getCategories();
//            ArrayList<Category> newCatList = new ArrayList<Category>();

//            System.out.println("Category List: "+catList.size());

            for (int i=0; i<catList.size(); i++) {
//            for (int i=0; i<1; i++) {
                Color[] colors = catList.get(i).getColors();
                Color[] newColors = new Color[colors.length];
                for (int j=0; j<colors.length; j++) {
//                    System.out.println("old color: "+colors[j] + " , alpha="+colors[j].getAlpha());
                    // this is band-aid
                    if (j==0 && colors[j].getAlpha() == 0) {
//                        alpha = (int)(colors[i].getAlpha()*(alpha/255.0));
                        newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), colors[j].getBlue(), 0);
                    }
                    else {
//                        System.out.println("new Color("+colors[j].getRed()+", "+colors[j].getGreen()+", "+colors[j].getBlue()+", "+(int)((j+1)*alpha/colors.length) + 
//                        		" --- j="+j+" alpha="+alpha+" colors.length="+colors.length);
                        
//                        newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), colors[j].getBlue(), alpha);
                    	if (interpType == AlphaInterpolationType.LINEAR_ASCENDING) {
                    		int denom = (j+1 < colors.length-4) ? colors.length-4 : j+1;
                    		newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), 
                    				colors[j].getBlue(), (int)((j+1)*alpha/(double)denom));
                    	}
                    	else {
                    		newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), 
                    				colors[j].getBlue(), alpha);
                    	}
                    }
                }
//                System.out.println("  --- "+Arrays.toString(catList.get(i).getColors()));
//                catList.get(i).recolor(newColors);
                colorArrays[n] = newColors;
                
            }
            
//            sdArray[n] = new SampleDimension((Category[])(newCatList.toArray()), null);
        }    

        WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(gc.getRenderedImage());
        Raster data = img.getData();
//        double minValue = 999999999;
//        double maxValue = -999999999;
//        for (int j=0; j<data.getHeight(); j++) {
//            for (int i=0; i<data.getWidth(); i++) {
//                double value = data.getPixel(i, j, new double[1])[0];
//                if (value < minValue) minValue = value;
//                if (value > maxValue) maxValue = value;
//            }
//        }
//        System.out.println("min value = "+minValue+"  max value = "+maxValue);

        GridCoverage gc1 = new GridCoverage(gc.getName(null), (WritableRaster)data, gc.getCoordinateSystem(), gc.getEnvelope(),
                new double[] { sdArray[0].getMinimumValue() }, new double[] { sdArray[0].getMaximumValue() }, null, colorArrays, null);
        
        gc.dispose();

        return gc1;

    }
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static GridCoverage applyAlpha2(GridCoverage gc, int alpha) {
    	return applyAlpha2(gc, alpha, AlphaInterpolationType.NONE);
    }

    public static GridCoverage applyAlpha2(GridCoverage gc, SampleDimension[] sdArrayOverride, int alpha) {
    	return applyAlpha2(gc, alpha, sdArrayOverride, AlphaInterpolationType.NONE);
    }

    public static GridCoverage applyAlpha2(GridCoverage gc, int alpha, AlphaInterpolationType interpType) {
    	return applyAlpha2(gc, alpha, gc.getSampleDimensions(), interpType);
    }
    public static GridCoverage applyAlpha2(GridCoverage gc, int alpha, SampleDimension[] sdArrayOverride, AlphaInterpolationType interpType) {

//        System.out.println("alpha = "+alpha);
        
        if (alpha < 0) {
            alpha = 0;
        }
        if (alpha > 255) {
            alpha = 255;
        }
        
//        SampleDimension[] sdArray = setSampleDimensionAlpha(sdArrayOverride, 255);
//        sdArray = setSampleDimensionAlpha(sdArrayOverride, alpha);
        SampleDimension[] sdArray = setSampleDimensionAlpha(sdArrayOverride, alpha);

        WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(gc.getRenderedImage());
        Raster data = img.getData();
        GridCoverage gc1 = new GridCoverage(gc.getName(null), (WritableRaster)data, 
        		GeographicCoordinateSystem.WGS84, null, gc.getEnvelope(), sdArray);
        
        gc.dispose();

        return gc1;

    }
    

    
    
    public static SampleDimension[] setSampleDimensionAlpha(SampleDimension[] sdArray, int alpha) {
    	
    	
    	
    	
        Color[][] colorArrays = new Color[sdArray.length][];
        
        
        for (int n=0; n<sdArray.length; n++) {
//            System.out.println( sdArray[n].getMinimumValue() + " - " + sdArray[n].getMaximumValue() );
            
            List<Category> catList = sdArray[n].getCategories();
            ArrayList<Category> newCatList = new ArrayList<Category>();

//            System.out.println("Category List: "+catList.size());

            
            for (int i=0; i<catList.size(); i++) {
            	
//            	System.out.println("cat: "+i+" = "+catList.get(i).getName(null)+
//            			" colors.length: "+catList.get(i).getColors().length);
            	
                if (catList.get(i).getName(null).trim().equalsIgnoreCase("NO DATA")) {
                	newCatList.add(Category.NODATA.geophysics(true));
                }
                else {

                	Color[] colors = catList.get(i).getColors();
                	Color[] newColors = new Color[colors.length];
                	for (int j=0; j<colors.length; j++) {

                		//                    else {
                		//                        System.out.println("new Color("+colors[j].getRed()+", "+colors[j].getGreen()+", "+colors[j].getBlue()+", "+(int)((j+1)*alpha/colors.length) + 
                		//                        		" --- j="+j+" alpha="+alpha+" colors.length="+colors.length);

                		//                        newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), colors[j].getBlue(), alpha);


                		//                    	if (interpType == AlphaInterpolationType.LINEAR_ASCENDING) {
                		//                    		int denom = (j+1 < colors.length-4) ? colors.length-4 : j+1;
                		//                    		newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), 
                		//                    				colors[j].getBlue(), (int)((j+1)*alpha/(double)denom));
                		//                    	}
                		//                    	else {
                		
//                		System.out.println("alpha: "+colors[j].getAlpha());
                		
                		double currentAlphaPercent = colors[j].getAlpha()/255.0;
//                		double currentAlphaPercent = 1;
                		
                		newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), 
                				colors[j].getBlue(), (int)(alpha*currentAlphaPercent));
                		//                    	}
                		//                    }
                	}


                	colorArrays[n] = newColors;

                	newCatList.add(catList.get(i).recolor(newColors));
                
                }
            }
            
            sdArray[n] = new SampleDimension((Category[])(newCatList.toArray(new Category[newCatList.size()])), null);
        }    

        return sdArray;
    }
    
    
    
    
    
    
    
    
    /**
     * Applies given alpha value to color array.  Also adjusts the alpha value based on an interpolated value 
     * between the '0' and 'alpha' range, either ascending or descending, with the purpose to fade out values at one end of the range. 
     * @param colors
     * @param alpha
     * @param interpType
     * @return
     */
    public Color[] applyAlphaInterpolation(Color[] colors, int alpha, AlphaInterpolationType interpType) {
    	
    	Color[] newColors = new Color[colors.length];
    	
    	for (int j=0; j<colors.length; j++) {
    		if (interpType == AlphaInterpolationType.LINEAR_ASCENDING) {
    			int denom = (j+1 < colors.length-4) ? colors.length-4 : j+1;
        		newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), 
        				colors[j].getBlue(), (int)((j+1)*alpha/(double)denom));
//    			newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), colors[j].getBlue(), (int)((j+1)*alpha/(double)colors.length));
    		}
    		else {
    			newColors[j] = new Color(colors[j].getRed(), colors[j].getGreen(), colors[j].getBlue(), alpha);
    		}
    	}

    	return newColors;
    }


    
    
    
    
    
    
    
    
    
    
    public static GridCoverage smooth(GridCoverage gc, int smoothFactor, float baseline) {
        SampleDimension[] sdArray = gc.getSampleDimensions();
        Color[][] colorArrays = new Color[sdArray.length][];
        
        
        for (int n=0; n<sdArray.length; n++) {
//            System.out.println( sdArray[n].getMinimumValue() + " - " + sdArray[n].getMaximumValue() );
            
            List<Category> catList = sdArray[n].getCategories();
            for (int i=0; i<1; i++) {
                colorArrays[n] = catList.get(i).getColors();
            }
        }    

        WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(gc.getRenderedImage());
        Raster data = img.getData();
        // change NoData values in Raster 
//        int cnt = 0;
        for (int i = 0; i < data.getWidth(); i++) {
            for (int j = 0; j < data.getHeight(); j++) {
            	
//            	System.out.println(data.getSampleFloat(i, j, 0) + " ___"+baseline);
            	
                if (data.getSampleFloat(i, j, 0) < baseline || 
                		Float.isNaN(data.getSampleFloat(i, j, 0))) {
                	
//                	System.out.println(" *****");
                	
                    ((WritableRaster) data).setSample(i, j, 0, baseline);
//                    cnt++;
                }
            }
        }
//        System.out.println(cnt+" no data records set to baseline");
        

        
        
		// config JAI
		TileCache cache = JAI.getDefaultInstance().getTileCache();
		cache.setMemoryCapacity(200000000L);
		cache.setMemoryThreshold(.75f);
		JAI.getDefaultInstance().setTileCache(cache);
		JAI.getDefaultInstance().getTileScheduler().setParallelism(0);

		
		
        
        
        
        
        SmoothingOperation smoothingOp = new SmoothingOperation();
        SmoothingInfo sminfo = smoothingOp.getSmoothingInfo(gc.getEnvelope(), 
        		gc.getRenderedImage().getWidth(), 
        		gc.getRenderedImage().getHeight(), (int)smoothFactor);
        KernelJAI kernel = new KernelJAI(sminfo.getKernelSize(), sminfo.getKernelSize(), sminfo.getKernelMatrix());        

        java.awt.image.renderable.ParameterBlock pb = new java.awt.image.renderable.ParameterBlock();
        pb.addSource(gc.getRenderedImage());
        pb.add(kernel);      
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        javax.media.jai.PlanarImage output = javax.media.jai.JAI.create("convolve", pb, hints);


        WritableRaster smoothedRaster = (WritableRaster)(output.getData());



        // clean smoothed data
        for (int i = 0; i < smoothedRaster.getWidth(); i++) {
            for (int j = 0; j < smoothedRaster.getHeight(); j++) {
            	
//            	System.out.println(data.getSampleFloat(i, j, 0) + " ___"+baseline);
            	
//                if (smoothedRaster.getSampleFloat(i, j, 0) < baseline || 
//                		Float.isNaN(smoothedRaster.getSampleFloat(i, j, 0))) {
                	
                if (smoothedRaster.getSampleFloat(i, j, 0) <= baseline) {

//                	System.out.println(" *****");
                	
                    ((WritableRaster) smoothedRaster).setSample(i, j, 0, Float.NaN);
//                    cnt++;
                }
            }
        }
//        System.out.println(cnt+" no data records set to baseline");

        
        
//        GridCoverage gc1 = new GridCoverage(gc.getName(null), smoothedRaster, gc.getCoordinateSystem(), gc.getEnvelope(),
//                new double[] { sdArray[0].getMinimumValue() }, new double[] { sdArray[0].getMaximumValue() }, null, colorArrays, null);
        
        GridCoverage gc1 = new GridCoverage(gc.getName(null), (WritableRaster)smoothedRaster, 
        		GeographicCoordinateSystem.WGS84, null, gc.getEnvelope(), sdArray);
        
        gc.dispose();

        return gc1;
    }
    
    
    
    
    /**
     * Produce a grid coverage rendered as defined in the .wctpal palette file
     * @param raster
     * @param wctpalURL
     * @return
     * @throws WCTException
     * @throws IOException
     */
    public static GridCoverage getGridCoverage(WCTRaster raster, URL wctpalURL) throws WCTException, IOException {
    	    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(wctpalURL.openStream()));
		ColorsAndValues[] cav = ColorLutReaders.parseWCTPal(br);
		br.close();
		SampleDimensionAndLabels sd = ColorLutReaders.convertToSampleDimensionAndLabels(cav[0], cav[1]);
	    GridCoverage gc = new GridCoverage(raster.getVariableName(), raster.getWritableRaster(), 
	        		GeographicCoordinateSystem.WGS84, null, new Envelope(raster.getBounds()), 
	        		new SampleDimension[] { sd.getSampleDimension() });
	    
	    return gc;
    }
    
    
    

	public void setPaletteOverrideURL(URL paletteOverrideURL) {
		this.paletteOverrideURL = paletteOverrideURL;
	}

	public URL getPaletteOverrideURL() {
		return paletteOverrideURL;
	}




	public WCTFilter getWctFilter() {
		return wctFilter;
	}




	public void setWctFilter(WCTFilter wctFilter) {
		this.wctFilter = wctFilter;
	}
    
    
    
    
    
    
    
    
    
    

}
