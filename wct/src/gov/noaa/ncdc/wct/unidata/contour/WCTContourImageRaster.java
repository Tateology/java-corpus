package gov.noaa.ncdc.wct.unidata.contour;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.unidata.contour.smoothing.DouglasPeuckerLineSimplifier;
import gov.noaa.ncdc.wct.unidata.contour.smoothing.Line;
import gov.noaa.ncdc.wct.unidata.contour.smoothing.McMasterLineSmoother;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;

import com.vividsolutions.jts.geom.Coordinate;


public class WCTContourImageRaster {

	private BufferedImage bimage = null;
	private WCTContourGrid lastProcessedContourGrid = null;

	private Color contourColor = Color.BLACK;
	private Stroke contourStroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);

	private Color outlineColor = new Color(30, 30, 30);
	private Stroke outlineStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);

	
	
	public WCTContourImageRaster() {
		init();
	}
	
	private void init() {
		
	}
	

	public void process(final WCTContourGrid contourGrid) {
		process(contourGrid, 
				contourGrid.getInputRaster().getWritableRaster().getWidth(), 
				contourGrid.getInputRaster().getWritableRaster().getHeight());
	}
	
	public void process(final WCTContourGrid contourGrid, final int width, final int height) {
		
		this.lastProcessedContourGrid = contourGrid;

		final int gridWidth = contourGrid.getInputRaster().getWritableRaster().getWidth();
		final int gridHeight = contourGrid.getInputRaster().getWritableRaster().getHeight();
		// put in some logic to just clear raster if dimensions are the same

		
		bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		final double xRatio = width/(double)gridWidth;
		final double yRatio = height/(double)gridHeight;
		
		final Graphics2D g2d = (Graphics2D)bimage.getGraphics();
//		g2d.setColor(new Color(255, 255, 255, 0));
//		g2d.fillRect(0, 0, width, height);
		
//		final Color whiteColor = new Color(255, 255, 255, 40);
//		final Stroke whiteStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		
		RenderingHints renderHints =
			  new RenderingHints(RenderingHints.KEY_ANTIALIASING,
			                     RenderingHints.VALUE_ANTIALIAS_ON);
			renderHints.put(RenderingHints.KEY_RENDERING,
			                RenderingHints.VALUE_RENDER_QUALITY);
			renderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
			renderHints.put(RenderingHints.KEY_FRACTIONALMETRICS,
	                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			renderHints.put(RenderingHints.KEY_DITHERING,
	                RenderingHints.VALUE_DITHER_ENABLE);

		g2d.setRenderingHints(renderHints);	
		
        final FontRenderContext fc = g2d.getFontRenderContext();
        
        
        final ArrayList<Double> labelCenterXList = new ArrayList<Double>();
        final ArrayList<Double> labelCenterYList = new ArrayList<Double>();
        final ArrayList<String> labelTextList = new ArrayList<String>();

		
		StreamingContourProcess contourProcess = new StreamingContourProcess() {
			@Override
			public void processContourFeature(ContourFeature cf) {
	        	System.out.println("wct contour at value: "+cf.getContourValue());
	        	Iterator<ContourLine> iter = cf.getGisParts();
	        	int lineCnt = 0;
	        	while (iter.hasNext()) {
	        		ContourLine gp = iter.next();
	        		
	        		for (int n=0; n<gp.getNumPoints()-1; n++) {
	        			
	        			
//	        			System.out.println(gp.getX()[n]+","+gp.getY()[n]+" to "+gp.getX()[n+1]+","+gp.getY()[n+1]);
//	        			g2d.setColor(whiteColor);
//	        			g2d.setStroke(whiteStroke);
//	        			g2d.drawLine((int)Math.round(gp.getX()[n]),   height-1-(int)Math.round(gp.getY()[n]), 
//	        					     (int)Math.round(gp.getX()[n+1]), height-1-(int)Math.round(gp.getY()[n+1]));
//	        			g2d.setColor(blackColor);
//	        			g2d.setStroke(blackStroke);
//	        			g2d.drawLine((int)Math.round(gp.getX()[n]),   height-1-(int)Math.round(gp.getY()[n]), 
//	        					     (int)Math.round(gp.getX()[n+1]), height-1-(int)Math.round(gp.getY()[n+1]));
	        		}
	        		
	        		
	        		if (gp.getNumPoints() == 0) {
	        			continue;
	        		}
	        		
	        		if (gp.getNumPoints() < 20) {
	        			continue;
	        		}
	        		
	        		
//	        		Coordinate[] simpCoords = simplifyContourLine(gp);
//	        		System.out.println("SIMPLIFICATION -- BEFORE: "+gp.getNumPoints()+"  AFTER: "+simpCoords.length);
//	        		for (int n=0; n<simpCoords.length-1; n++) {
////	        			System.out.println(gp.getX()[n]+","+gp.getY()[n]+" to "+gp.getX()[n+1]+","+gp.getY()[n+1]);
//	        			g2d.setColor(blackColor);
//	        			g2d.setStroke(blackStroke);
//	        			g2d.drawLine((int)Math.round(simpCoords[n].x),   height-1-(int)Math.round(simpCoords[n].y), 
//	        					     (int)Math.round(simpCoords[n+1].x), height-1-(int)Math.round(simpCoords[n+1].y));
//	        		}
	        		
	        		Coordinate[] simpCoords = smoothContourLine(convertToCoordinateArray(gp));
//	        		simpCoords = simplifyContourLine(simpCoords);
//	        		System.out.println("SMOOTHING -- BEFORE: "+gp.getNumPoints()+"  AFTER: "+simpCoords.length);
	        		
//	        		Coordinate[] simpCoords2 = DouglasPeuckerLineSimplifier.simplify(simpCoords, 1);
//	        		System.out.println("SIMPLIFYING -- BEFORE: "+simpCoords.length+"  AFTER: "+simpCoords2.length);
	        		for (int n=0; n<simpCoords.length-1; n++) {
//	        		for (int n=0; n<simpCoords.length-1; n=n+6) {
	        			
//	        			g2d.setColor(whiteColor);
//	        			g2d.setStroke(whiteStroke);
//	        			g2d.drawLine((int)Math.round(xRatio*simpCoords[n].x+2),   height-1-(int)Math.round(yRatio*simpCoords[n].y-2), 
//       					     (int)Math.round(xRatio*simpCoords[n+1].x+2), height-1-(int)Math.round(yRatio*simpCoords[n+1].y-2));

//	        			int span = (simpCoords.length-1-n > 6) ? 6 : simpCoords.length-1-n;
	        			int span = 1;
	        			
	        			
//	        			System.out.println(gp.getX()[n]+","+gp.getY()[n]+" to "+gp.getX()[n+1]+","+gp.getY()[n+1]);
	        			g2d.setColor(contourColor);
	        			g2d.setStroke(contourStroke);
	        			g2d.drawLine((int)Math.round(xRatio*simpCoords[n].x),      height-1-(int)Math.round(yRatio*simpCoords[n].y), 
	        					     (int)Math.round(xRatio*simpCoords[n+span].x), height-1-(int)Math.round(yRatio*simpCoords[n+span].y));
	        			
//	        			if (n == 0) {
//	        				g2d.drawString("S"+lineCnt, (int)Math.round(xRatio*simpCoords[n].x), height-1-(int)Math.round(yRatio*simpCoords[n].y));
//	        			}
//	        			if (span < 6 || n == simpCoords.length-1) {
//	        				g2d.drawString("E"+lineCnt, (int)Math.round(xRatio*simpCoords[n+span].x), height-1-(int)Math.round(yRatio*simpCoords[n+span].y));
//	        			}
	        		}
	        		
	        		// close contour if we are not at the edge
//	        		int x1 = (int)Math.round(xRatio*simpCoords[0].x);
//	        		int y1 = (int)Math.round(yRatio*simpCoords[0].y);
//	        		int x2 = (int)Math.round(xRatio*simpCoords[simpCoords.length-1].x);
//	        		int y2 = (int)Math.round(yRatio*simpCoords[simpCoords.length-1].y);
//	        		
//	        		if (! (x1 == 0 || x1 == width || x2 == 0 || x2 == width ||
//	        			y1 == 0 || y1 == height || y2 == 0 || y2 == height)) {
//
//	        			g2d.drawLine(x1, height-1-y1, x2, height-1-y2);
//	        		}

	        		if (gp.getNumPoints() > 80) {
	        		
	        			double rx1 = (int)(xRatio*simpCoords[simpCoords.length/2].x);
	        			double ry1 = height-1-(int)(yRatio*simpCoords[simpCoords.length/2].y);
	        		
	        			labelCenterXList.add(rx1);
	        			labelCenterYList.add(ry1);
	        			labelTextList.add(WCTUtils.DECFMT_0D00.format(gp.getContourLevel()));
	        		}
	        		lineCnt++;
	        	}
			}
			@Override
			public void finish() {
				
				for (int n=0; n<labelTextList.size(); n++) {
					
					double rx1 = labelCenterXList.get(n);
					double ry1 = labelCenterYList.get(n);
					
	        		GlyphVector glyph = g2d.getFont().createGlyphVector(fc, labelTextList.get(n));
	                int textHeight = (int)glyph.getVisualBounds().getHeight();
	                int textWidth = (int)glyph.getVisualBounds().getWidth();

	                // label background
	        		g2d.setColor(new Color(255, 255, 255, 220));
	        		g2d.fillRoundRect((int)Math.round(rx1-4-textWidth/2.0f), 
	        						  (int)Math.round(ry1-textHeight-4+textHeight/2.0f), 
	        						  textWidth+8, textHeight+8, 4, 4);
	        		// label background
	        		g2d.setColor(outlineColor);
	        		g2d.setStroke(outlineStroke);
	        		g2d.drawRoundRect((int)Math.round(rx1-4-textWidth/2.0f), 
	        						  (int)Math.round(ry1-textHeight-4+textHeight/2.0f), 
	        						  textWidth+8, textHeight+8, 4, 4);
	        		
	        		// label
	        		g2d.setColor(contourColor);
	                g2d.drawGlyphVector(glyph, (float)rx1-textWidth/2.0f, (float)ry1+textHeight/2.0f);

				}
				
				labelTextList.clear();
				labelCenterXList.clear();
				labelCenterYList.clear();
				g2d.dispose();
			}
		};


		contourGrid.processContourLines(new StreamingContourProcess[] { contourProcess });
		
	}


	public static Coordinate[] convertToCoordinateArray(ContourLine contourLine) {
		double[] vertexX = contourLine.getX();
		double[] vertexY = contourLine.getY();
		Coordinate[] vertexCoords = new Coordinate[contourLine.getNumPoints()];

		for (int n=0; n<contourLine.getNumPoints(); n++) {
			//				System.out.println(vertexX[n]+" , "+vertexY[n]);
			//				reversed because this ncTools thinks this is lat/lon
			//				double x = vertexX[n]*cellWidth + llXCorner;
			//				double y = vertexY[n]*cellHeight + llYCorner;
			//				vertexCoords[n] = new Coordinate(x, y);
			vertexCoords[n] = new Coordinate(vertexX[n], vertexY[n]);
			//				System.out.println("geo contour coord: "+vertexCoords[n]);
		}

		return vertexCoords;
	}
	
	
	public static Coordinate[] simplifyContourLine(Coordinate[] vertexCoords) {
		return DouglasPeuckerLineSimplifier.simplify(vertexCoords, 1);
	}
	

	
	public static Coordinate[] smoothContourLine(Coordinate[] vertexCoords) {

//		double[] vertexX = contourLine.getX();
//		double[] vertexY = contourLine.getY();

		ArrayList<Line> lineList = new ArrayList<Line>();
		for (int n=0; n<vertexCoords.length-1; n++) {
			lineList.add(new Line((int)vertexCoords[n].x, (int)vertexCoords[n].y, 
					(int)vertexCoords[n+1].x, (int)vertexCoords[n+1].y));
		}
		
		List<Line> smoothedLineSegments = McMasterLineSmoother.smoothLine(lineList);
		
		Coordinate[] smoothedVertexCoords = new Coordinate[smoothedLineSegments.size()+1];
		for (int n=0; n<smoothedLineSegments.size(); n++) {
			smoothedVertexCoords[n] = new Coordinate(smoothedLineSegments.get(n).x1, smoothedLineSegments.get(n).y1);
		}
		smoothedVertexCoords[smoothedVertexCoords.length-1] = new Coordinate(
				smoothedLineSegments.get(smoothedLineSegments.size()-1).x2, 
				smoothedLineSegments.get(smoothedLineSegments.size()-1).y2);

		return smoothedVertexCoords;
	}
	

	
	
	
	/**
	 * process(contourGrid) must be called first!
	 */
	public GridCoverage getGridCoverage() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bimage, "png", baos);
			bimage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new GridCoverage("hidden__ContourGC", bimage, GeographicCoordinateSystem.WGS84, new Envelope(this.lastProcessedContourGrid.getInputRaster().getBounds()));
	}


	/**
	 * Gets underlying BufferedImage
	 * @return
	 */
	public BufferedImage getBufferedImage() {
		return bimage;
	}
}
