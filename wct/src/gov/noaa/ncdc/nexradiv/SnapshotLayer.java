package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;

import java.awt.Color;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.opengis.referencing.operation.TransformException;

public class SnapshotLayer {

    private RenderedGridCoverage rgc;
    private int alpha;
    private String name;
    private URL dataURL;
    private Color[] originalColors;
    private SampleDimension[] originalSampleDimensionArray;
    private Image legendImage;
    private String[] categoryToValueArray;
    private String units;
    private boolean isResampled;
    
    private int[][][] originalAlphaValues = null;
    
    
    public void setRenderedGridCoverage(RenderedGridCoverage rgc) {
        this.rgc = rgc;
        this.originalColors = WCTGridCoverageSupport.getColors(rgc.getGridCoverage());
        this.originalSampleDimensionArray = rgc.getGridCoverage().getSampleDimensions();
//        SampleDimension sd = new SampleDimension(originalSampleDimensionArray[0].getCategories().toArray(), null);
    
        this.originalAlphaValues = new int[this.originalSampleDimensionArray.length][][];
        
        SampleDimension[] sdArray = rgc.getGridCoverage().getSampleDimensions();
        for (int i=0; i<sdArray.length; i++) {
        	List<Category> catList = sdArray[i].getCategories();
        	
        	int[][] catAlphas = new int[catList.size()][];
        	
        	for (int j=0; j<catList.size(); j++) {
        		Color[] colors = catList.get(j).getColors();
        		int[] a = new int[colors.length];
        		for (int n=0; n<colors.length; n++) {
        			a[n] = colors[n].getAlpha();
        		}
        		catAlphas[j] = a;
        	}
        	this.originalAlphaValues[i] = catAlphas;
        }
        
//        System.out.println(Arrays.deepToString(this.originalAlphaValues));
        
    }
    
    public RenderedGridCoverage getRenderedGridCoverage() {
        return rgc;
    }
    
    public void resetColorsToDefault() {
        WCTGridCoverageSupport.setColors(rgc.getGridCoverage(), this.originalColors);
    }
    
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        try {
        	
//        	System.out.println(Arrays.toString(originalColors));
//        	System.out.println(originalAlphaValues);
        	

        	
        	
        	
            
            SampleDimension[] sdArray = rgc.getGridCoverage().getSampleDimensions();
            ArrayList<Category> newCatList = new ArrayList<Category>();
            for (int i=0; i<sdArray.length; i++) {
            	List<Category> catList = sdArray[i].getCategories();
            	
            	for (int j=0; j<catList.size(); j++) {
            		Color[] colors = catList.get(j).getColors();
            		Color[] newColors = new Color[colors.length];
            		for (int n=0; n<colors.length; n++) {
            			newColors[n] = new Color(colors[n].getRed(), colors[n].getGreen(), 
            					colors[n].getBlue(), originalAlphaValues[i][j][n]);
            		}
            		newCatList.add(catList.get(j).recolor(newColors));
            	}
            	sdArray[i] = new SampleDimension((Category[])(newCatList.toArray(new Category[newCatList.size()])), null);
            }

            
        	this.originalSampleDimensionArray = sdArray;
        	
        	
        	
        	
        	
            if (alpha == -1) {
            	// new sample dim -based stuff
            	if (originalAlphaValues[0].length > 2) {
            		rgc.setGridCoverage(WCTGridCoverageSupport.applyAlpha2(rgc.getGridCoverage(), originalSampleDimensionArray, 255));
            	}
            	else {
            		rgc.setGridCoverage(WCTGridCoverageSupport.setColors(rgc.getGridCoverage(), this.originalColors));
            	}
            }
            else {
                rgc.setGridCoverage(WCTGridCoverageSupport.applyAlpha2(rgc.getGridCoverage(), originalSampleDimensionArray, alpha));
            }
        } catch (TransformException e) {
            e.printStackTrace();
        }
        
        rgc.repaint();
    }
    
    public int getAlpha() {
        return alpha;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setDataURL(URL dataURL) {
        this.dataURL = dataURL;
    }
    public URL getDataURL() {
        return dataURL;
    }
    public void setLegendImage(Image legendImage) {
        this.legendImage = legendImage;
    }
    public Image getLegendImage() {
        return legendImage;
    }
    public void setCategoryToValueArray(String[] categoryToValueArray) {
        this.categoryToValueArray = categoryToValueArray;
    }
    public String[] getCategoryToValueArray() {
        return categoryToValueArray;
    }
    public void setUnits(String units) {
        this.units = units;
    }
    public String getUnits() {
        return units;
    }
    public void setResampled(boolean isResampled) {
        this.isResampled = isResampled;
    }
    public boolean isResampled() {
        return isResampled;
    }
    
}
