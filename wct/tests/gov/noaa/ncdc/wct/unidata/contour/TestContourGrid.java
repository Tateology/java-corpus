package gov.noaa.ncdc.wct.unidata.contour;

import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.geotools.feature.Feature;
import org.geotools.gc.GridCoverage;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.junit.Test;

import ucar.ma2.Range;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;

public class TestContourGrid {

	@Test
	public void testContour1() throws Exception {
		
//		Debug.set("contour/debugContours", true);
		
//        String fileIn = "testdata\\KLIX20050829_135451.Z";
		String fileIn = "testdata\\NCEP-NAM-CONUS_12km-noaaport_best.ncd-var9-z9-rt9-t0.nc";
//        RadialDatasetSweepRemappedRaster r = new RadialDatasetSweepRemappedRaster();
//        r.setVariableName("Reflectivity");
		GridDatasetRemappedRaster r = new GridDatasetRemappedRaster();
        r.process(fileIn);

        ArrayList<Double> contourList = new ArrayList<Double>();
        contourList.add(100.0);
        
        
        GridDataset gds = GridDataset.open(fileIn);
        GridDatatype grid = gds.getGrids().get(0).makeSubset(new Range(0), null, null, null, null, null);
        double[] xpos = new double[grid.getXDimension().getLength()];
        for (int x=0; x<xpos.length; x++) {
        	xpos[x] = x;
        }
        double[] ypos = new double[grid.getYDimension().getLength()];
        for (int y=0; y<ypos.length; y++) {
        	ypos[y] = y;
        }
        
        // grid.readVolumeData returns data in y,x order.  ContourGrid needs it in x,y order, so we transpose
        ContourGrid cg = new ContourGrid(grid.readVolumeData(0).transpose(0, 1), contourList, xpos, ypos, grid);
        ArrayList<ContourFeature> contourFeatureList = cg.getContourLines();
        
        System.out.println("contour feature count: "+contourFeatureList.size());
        for (ContourFeature cf : contourFeatureList) {
        	System.out.println("contour at value: "+cf.getContourValue());
        	Iterator<GisPart> iter = cf.getGisParts();
        	while (iter.hasNext()) {
        		GisPart gp = iter.next();
        		for (int n=0; n<gp.getNumPoints()-1; n++) {
        			System.out.println(gp.getX()[n]+","+gp.getY()[n]+" to "+gp.getX()[n+1]+","+gp.getY()[n+1]);
        		}
        	}
        }
        
        
        
//        WCTContourGrid wcg = new WCTContourGrid(r, contourList);        
//        WCTContourGrid wcg = new WCTContourGrid(grid.readVolumeData(0).transpose(0, 1), contourList, xpos, ypos, r);        
        WCTContourGrid wcg = new WCTContourGrid(contourList, xpos, ypos, r);        
        ArrayList<ContourFeature> wctContourFeatureList = wcg.getContourLines();
        
        System.out.println("wct contour feature count: "+wctContourFeatureList.size());
        for (ContourFeature cf : wctContourFeatureList) {
        	System.out.println("wct contour at value: "+cf.getContourValue());
        	Iterator<GisPart> iter = cf.getGisParts();
        	while (iter.hasNext()) {
        		GisPart gp = iter.next();
        		for (int n=0; n<gp.getNumPoints()-1; n++) {
        			System.out.println(gp.getX()[n]+","+gp.getY()[n]+" to "+gp.getX()[n+1]+","+gp.getY()[n+1]);
        		}
        	}
        }
        
        System.out.println("CONTOUR FEATURE RESULTS::::::");
        WCTContourFeatures contourFeatures = new WCTContourFeatures();
        StreamingProcess process = new StreamingProcess() {
			@Override
			public void addFeature(Feature feature)
					throws StreamingProcessException {

				System.out.println(feature);
			}
			@Override
			public void close() throws StreamingProcessException {
			}
        	
        };
        contourFeatures.process(wcg, new StreamingProcess[] { process });
        
        
        System.out.println("CONTOUR IMAGE RESULTS::::::");
        WCTContourImageRaster contourImageRaster = new WCTContourImageRaster();
        contourImageRaster.process(wcg);
        
//		bimage = WMSData.getTransparentImage(bimage, 150, Color.GRAY);
		JLabel iconLabel = new JLabel( new ImageIcon(contourImageRaster.getBufferedImage()) );
		JFrame frame = new JFrame();
		frame.getContentPane().add(iconLabel);
		frame.pack();
		frame.setVisible(true);		
		JOptionPane.showConfirmDialog(frame, "Done?");

        GridCoverage gc = contourImageRaster.getGridCoverage();
        RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
        
        
	}
}
