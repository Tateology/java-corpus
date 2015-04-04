package gov.noaa.ncdc.wct.unidata.contour;

import gov.noaa.ncdc.wct.decoders.cdm.SmoothingOperation;
import gov.noaa.ncdc.wct.decoders.cdm.SmoothingOperation.SmoothingInfo;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentDataType;

import java.awt.image.WritableRaster;
import java.util.ArrayList;

public class ContourManager {

	
	
	
	public static void contour(WCTViewer viewer) throws IllegalAccessException, InstantiationException {
		WCTRaster raster = null;
		int kernelSize = 0;
		if (viewer.getCurrentDataType() == CurrentDataType.RADAR) {
			raster = viewer.getRadialRemappedRaster();
			// check for the empty 'first time' rendered grid coverage, which means that this is first time
			// that the radar data is loaded and the post-render event has not been generated yet.
			if (viewer.getRadarRenderedGridCoverage().getGridCoverage().getEnvelope().getLength(0) < 0.0001) {
				return;
			}
			raster.setWritableRaster((WritableRaster)viewer.getRadarRenderedGridCoverage().getGridCoverage().getRenderedImage().getData());
	    	SmoothingOperation smop = new SmoothingOperation();
	    	SmoothingInfo info = smop.getSmoothingInfo(viewer.getCurrentExtent(), 
	    			raster.getWritableRaster().getWidth(), 
	    			raster.getWritableRaster().getHeight(), 
	    			(int)viewer.getRadarSmoothFactor());    	
	    	kernelSize = info.getKernelSize();
		}
		else if (viewer.getCurrentDataType() == CurrentDataType.GRIDDED) {
			raster = viewer.getGridDatasetRaster();
		}
		else if (viewer.getCurrentDataType() == CurrentDataType.SATELLITE) {
			raster = viewer.getGoesRaster();
		}
		
		double[] xpos = new double[raster.getWritableRaster().getWidth()];
		for (int x=0; x<xpos.length; x++) {
			xpos[x] = x;
		}
		double[] ypos = new double[raster.getWritableRaster().getHeight()];
		for (int y=0; y<ypos.length; y++) {
			ypos[y] = y;
		}
//		WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();
//		gcSupport.setSmoothFactor(10);
//		raster.setWritableRaster( gcSupport.getSmoothedRaster(raster) );

		WritableRaster wr = raster.getWritableRaster();
		double minVal = Double.POSITIVE_INFINITY;
		double maxVal = Double.NEGATIVE_INFINITY;
		for (int j=0; j<wr.getHeight(); j++) {
			for (int i=0; i<wr.getWidth(); i++) {
				double val = wr.getSampleDouble(i, j, 0);
				minVal = val < minVal ? val : minVal;
				maxVal = val > maxVal ? val : maxVal;
			}
		}
		
		
        ArrayList<Double> contourList = new ArrayList<Double>();
        int numContours = 15;
        for (int n=0; n<numContours; n++) {
        	double conVal = minVal + (n+1)*(maxVal-minVal)/(numContours+1);
        	System.out.println("---- adding contour: "+conVal+" n="+n+"  minVal="+minVal+"  maxVal="+maxVal);
        	contourList.add(conVal);
        }
        WCTContourGrid wcg = new WCTContourGrid(contourList, xpos, ypos, raster);        

        

        
        
        
		
        
        WCTContourImageRaster contourImageRaster = new WCTContourImageRaster();
        contourImageRaster.process(wcg, viewer.getMapPane().getWidth()+kernelSize+1, viewer.getMapPane().getHeight()+kernelSize+1);
        viewer.setContourGridCoverage(contourImageRaster.getGridCoverage());
		viewer.getContourRenderedGridCoverage().setVisible(true);

        
//		JLabel iconLabel = new JLabel( new ImageIcon(contourImageRaster.getBufferedImage()) );
//		JFrame frame = new JFrame();
//		frame.getContentPane().add(iconLabel);
//		frame.pack();
//		frame.setVisible(true);		
		
//		JFrame frame = new JFrame();
//		frame.getContentPane().add(new ContourPlot(contourList, raster));
//		frame.pack();
//		frame.setVisible(true);		

        
//		int width = wcg.getInputRaster().getWritableRaster().getWidth();
//		int height = wcg.getInputRaster().getWritableRaster().getHeight();
//		// put in some logic to just clear raster if dimensions are the same
//        WCTRasterizer rasterizer = new WCTRasterizer(height, width, Float.NaN);
//        rasterizer.setBounds(wcg.getInputRaster().getBounds());
//        WCTContourFeatures contourFeatures = new WCTContourFeatures();
//        contourFeatures.process(wcg, new StreamingProcess[] { rasterizer });
//        
//        double[] minValues = new double[] { 0 };
//        double[] maxValues = new double[] { 100 };
//        Color[] colors = new Color[] { Color.WHITE, Color.WHITE };
//        viewer.setContourGridCoverage( 
//        		new GridCoverage("Contour GC", rasterizer.getWritableRaster(), GeographicCoordinateSystem.WGS84,
//                new Envelope(wcg.getInputRaster().getBounds()), minValues, maxValues, null, new Color[][]{colors}, null)
//        	);
        
        

	}
}
