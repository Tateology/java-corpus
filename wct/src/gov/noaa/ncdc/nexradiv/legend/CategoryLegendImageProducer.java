package gov.noaa.ncdc.nexradiv.legend;

import gov.noaa.ncdc.common.color.SimpleColorMap;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.geotools.cv.Category;

public class CategoryLegendImageProducer {



    private String dataType;
    private String[] dataDescription;
    private String dateTimeInfo;
    private String[] mainMetadata;
    private String[] specialMetadata;

    private String[] legendTitle;
    private String[] categoryLabels;
    private Color[] categoryColors;
    private SampleDimensionAndLabels sampleDimensionAndLabels = null;

    private boolean drawBorder = false;

    private Color mapBackgroundColor = Color.BLACK;
    private Color backgroundColor = new Color(220, 220, 220);
    private Color foregroundColor = Color.BLACK;
    private Font font = new Font("Verdana", Font.PLAIN, 11);


    private int colorBoxWidth = 35;
    private int smallColorBoxWidth = 8;

    private boolean drawColorMap = true;
    private boolean drawColorMapTransparentBackgroundPattern = false;

    private int labelEveryOtherN = 1;

    private boolean interpolateBetweenCategories = false;
    private SimpleColorMap[] colorMapArray;
    private boolean drawLabels = true; 

    private String[] supplementalCategoryLabels;
    private Color[] supplementalCategoryColors;
    private String labelOverride = "";



    public BufferedImage createLargeLegendImage() throws WCTException {
    	return createLargeLegendImage(null);
    }
    
    public BufferedImage createLargeLegendImage(Dimension size) throws WCTException {

        int marginLeft = 10;
        int marginTop = 2;
        int textSpacing = 5;
//        int colorMapSpacing = 15;
        int colorMapSpacing = 4;
        
        
        int numCats = 0;
        if (sampleDimensionAndLabels != null) {
        	numCats = sampleDimensionAndLabels.getSampleDimension().getCategories().size();
        }
        else if (categoryColors != null) {
        	numCats = categoryColors.length;
        }
        else {
        	numCats = 0;
        	drawColorMap = false;
        }
        
        
        if (numCats > 75) {
        	colorMapSpacing = 1;
        	labelEveryOtherN = 4;
        }
        else if (numCats > 50) {
        	colorMapSpacing = 1;
        	labelEveryOtherN = 3;
        }
        else if (numCats > 25) {
        	colorMapSpacing = 2;
        	labelEveryOtherN = 2;
        }
        else if (numCats > 16) {
        	colorMapSpacing = 4;
        	labelEveryOtherN = 1;
        }
//        else if (numCats > 12) {
//        	colorMapSpacing = 6;
//        	labelEveryOtherN = 1;
//        }
//        else if (numCats > 8) {
////        	colorMapSpacing = 8;
//        	labelEveryOtherN = 1;
//        }
//        else if (numCats > 6) {
//        	colorMapSpacing = 12;
//        	labelEveryOtherN = 1;
//        }

        
        
//        BufferedImage image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        // junk image so we can measure text size in pixels
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();

        // ------- SET UP FONT AND RENDERING ---------
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_DITHERING,
        //      RenderingHints.VALUE_DITHER_ENABLE);



        // ------- DETERMINE MAX GLYPH WIDTH/HEIGHT OF METADATA TEXT ---------
        int[] maxGlyphHeightWidth = getMaxGlyphWidthAndHeight(g, fc);
        int maxGlyphHeight = maxGlyphHeightWidth[1];
        int maxGlyphWidth = maxGlyphHeightWidth[0];

        if (size == null) {
        	size = new Dimension(maxGlyphWidth+40, 520);
        }
        image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        g = (Graphics2D)image.getGraphics();
        fc = g.getFontRenderContext();

        // ------- SET UP FONT AND RENDERING ---------
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_DITHERING,
        //      RenderingHints.VALUE_DITHER_ENABLE);



        // ------- SET UP BACKGROUND ---------
        if (drawBorder) {
            g.setColor(backgroundColor);
            g.fillRoundRect(1, 1, image.getWidth()-2, image.getHeight()-2, 10, 10);
            g.setColor(foregroundColor);
            g.drawRoundRect(1, 1, image.getWidth()-2, image.getHeight()-2, 10, 10);
        }
        else {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(foregroundColor);
        }

        

        // ------- START DRAWING METADATA TEXT ---------

        float cursor = 0;
        if (dataType != null){
            GlyphVector glyphs = g.getFont().createGlyphVector(fc, dataType);
            g.setColor(foregroundColor);
            cursor += maxGlyphHeight+marginTop+textSpacing;
            g.drawGlyphVector(glyphs, marginLeft, cursor);
        }

        if (dataDescription != null) {
            for (int n=0; n<dataDescription.length; n++) {
                if (dataDescription[n] != null) {
                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, dataDescription[n]);
                    g.setColor(foregroundColor);
                    cursor += maxGlyphHeight+textSpacing;
                    g.drawGlyphVector(glyphs, marginLeft, cursor);                    
                }
            }
        }

        if (dateTimeInfo != null){
            GlyphVector glyphs = g.getFont().createGlyphVector(fc, dateTimeInfo);
            g.setColor(foregroundColor);
            cursor += maxGlyphHeight+textSpacing;
            g.drawGlyphVector(glyphs, marginLeft, cursor);
        }

        if (mainMetadata != null) {
            for (int n=0; n<mainMetadata.length; n++) {
                if (mainMetadata[n] != null) {
                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, mainMetadata[n]);
                    g.setColor(foregroundColor);
                    cursor += maxGlyphHeight+textSpacing;
                    g.drawGlyphVector(glyphs, marginLeft, cursor);
                }
            }
        }

        // Add space before special metadata
        if (cursor > 0) {
            cursor += 2*textSpacing;
        }


        if (specialMetadata != null) {
            for (int n=0; n<specialMetadata.length; n++) {
                if (specialMetadata[n] != null) {
                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, specialMetadata[n]);
                    g.setColor(foregroundColor);
                    cursor += maxGlyphHeight+textSpacing;
                    g.drawGlyphVector(glyphs, marginLeft, cursor);

//                    System.out.println("-- "+specialMetadata[n]);
                }
            }
        }




        // Add space before legend
        if (cursor > 0) {
            cursor += 15;
        }
        else {
            cursor = 4;
        }

        if (legendTitle != null) {
            for (int n=0; n<legendTitle.length; n++) {
                if (legendTitle[n] != null) {

                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, legendTitle[n]);
                    g.setColor(foregroundColor);
                    cursor += maxGlyphHeight+textSpacing;
                    g.drawGlyphVector(glyphs, 2*marginLeft, cursor);
                }
            }
        }


        cursor += 2*textSpacing;




        // if we are not drawing the legend, return
        if (! drawColorMap) {
            return image;
        }


        if (sampleDimensionAndLabels == null) {
        	if (categoryColors == null || categoryLabels == null) {
        		throw new WCTException("Category labels and colors must not be null");
        	}

        	if (categoryColors.length != categoryLabels.length) {
        		throw new WCTException("Number of category colors must equal number of category labels: \n" +
        				"categoryColors.length="+categoryColors.length+"  categoryLabels.length="+categoryLabels.length);
        	}
        }


        // -------------- DRAW COLOR MAP ------------------------



        int colorMapStartCursor = (int)cursor;

        int maxLabelGlyphHeight = 0;
        if (sampleDimensionAndLabels == null) {
        	for (int n=0; n<categoryLabels.length; n++) {
        		if (categoryLabels[n] != null) {
        			GlyphVector glyphs = g.getFont().createGlyphVector(fc, categoryLabels[n]);
        			Rectangle2D bounds = glyphs.getVisualBounds();
        			if (bounds.getHeight() > maxLabelGlyphHeight) {
        				maxLabelGlyphHeight = (int)bounds.getHeight();
        			}
        		}
        	}
        }
        else {
        	List<Category> catList = (List<Category>)sampleDimensionAndLabels.getSampleDimension().getCategories();
        	for (int n=0; n<catList.size(); n++) {
//    			GlyphVector glyphs = g.getFont().createGlyphVector(fc, WCTUtils.DECFMT_0D0000.format(catList.get(n).getRange().getMinimum()));
    			GlyphVector glyphs = g.getFont().createGlyphVector(fc, sampleDimensionAndLabels.getLabels()[n]);
    			Rectangle2D bounds = glyphs.getVisualBounds();
    			if (bounds.getHeight() > maxLabelGlyphHeight) {
    				maxLabelGlyphHeight = (int)bounds.getHeight();
    			}
        	}
        }
        
        maxLabelGlyphHeight /= labelEveryOtherN;

        
        
        
        
        
        
        
        
        
        
        
        if (sampleDimensionAndLabels != null) {
        	
        	List<Category> catList = sampleDimensionAndLabels.getSampleDimension().getCategories();
        	
        	
        	boolean flip = false;
        	if (catList.get(0).getRange().getMinimum() < 
        		catList.get(1).getRange().getMinimum()) {
        		
        		flip = true;
        	}       	
        	
        	// flip list if needed, and remove NO DATA from list
        	ArrayList<Category> newCatList = new ArrayList<Category>();
        	for (int n=0; n<catList.size(); n++) {
        		Category cat = null;
        		if (flip) {
        			cat = catList.get(catList.size()-n-1);
        		}
        		else {
        			cat = catList.get(n);
        		}
        		if (cat.getColors().length > 1) {
        			newCatList.add(cat);
        		}
        	}
        	catList = newCatList;
        	
        	
    		colorMapArray = new SimpleColorMap[catList.size()];
    		for (int n=0; n<colorMapArray.length; n++) {
    			if (catList.get(n).getColors().length == 2) {
    				colorMapArray[n] = new SimpleColorMap(0, maxLabelGlyphHeight+colorMapSpacing, 
    					catList.get(n).getColors()[0], catList.get(n).getColors()[1]);
    			}
    			else if (catList.get(n).getColors().length == 1) {
    				colorMapArray[n] = new SimpleColorMap(0, maxLabelGlyphHeight+colorMapSpacing, 
    					catList.get(n).getColors()[0], catList.get(n).getColors()[0]);
    			}
    		}
    		

    		ArrayList<Color> supColorList = new ArrayList<Color>();
    		ArrayList<String> supLabelList = new ArrayList<String>();
        	for (int n=0; n<catList.size(); n++) {
    			if (catList.get(n).getName(null).startsWith("Unique:")) {
    				supColorList.add(catList.get(n).getColors()[0]);
    				String s = catList.get(n).getName(null);
    				supLabelList.add(s.substring("Unique:".length()));
    			}
        	}
    		supplementalCategoryColors = supColorList.toArray(new Color[supColorList.size()]);
    		supplementalCategoryLabels = supLabelList.toArray(new String[supLabelList.size()]);
        	
        	

        	for (int n=0; n<catList.size(); n++) {
    			String label = catList.get(n).getName(null).split("\\:")[0];
    			GlyphVector glyphs = g.getFont().createGlyphVector(fc, label);    			
    			
    			if (catList.get(n).getColors().length == 1 ||
        			catList.get(n).getName(null).startsWith("Unique:")) {
    				continue;
    			}
    			
    			
    			
    			
				if (catList.get(n).getColors().length > 1 &&
						catList.get(n).getColors()[1].getAlpha() > 0) {				

					for (int i=0; i<(maxLabelGlyphHeight+colorMapSpacing); i++) {
						if (n < colorMapArray.length) {

							int yPixel = (int)cursor+i+colorMapSpacing*2;

							g.setColor(colorMapArray[n].getColor(i));
							g.drawLine(2*marginLeft, yPixel, 2*marginLeft+colorBoxWidth, yPixel);

						}
					}				
				}
//				else if (catList.get(n).getColors().length == 1 &&
//						catList.get(n).getColors()[0].getAlpha() > 0) {
//					
//					g.setColor(catList.get(n).getColors()[0]);
//					for (int i=0; i<(maxLabelGlyphHeight+colorMapSpacing); i++) {
//						int yPixel = (int)cursor+i+colorMapSpacing*2;
//						g.drawLine(2*marginLeft, yPixel, 2*marginLeft+colorBoxWidth, yPixel);
//					}
//				}

				
				
				
				
    			if (n % labelEveryOtherN == 0) {
    				float yPixel = cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+colorMapSpacing*2;
    				g.setColor(foregroundColor);
    				g.drawLine(2*marginLeft, (int)yPixel-((maxLabelGlyphHeight+colorMapSpacing)/2), 
    						2*marginLeft+colorBoxWidth, (int)yPixel-((maxLabelGlyphHeight+colorMapSpacing)/2));
    				g.drawGlyphVector(glyphs, 2*marginLeft+colorBoxWidth+15, yPixel-colorMapSpacing/2);
    			}

    			cursor += maxLabelGlyphHeight+colorMapSpacing;
        	}
        	
        	if ((catList.size()-supplementalCategoryColors.length) > 1) {

        	// draw last label
//			String label = sampleDimensionAndLabels.getLabels()[catList.size()-1];
			String label = catList.get(catList.size()-1).getName(null).split("\\:")[1];
////			label = "last";
			GlyphVector glyphs = g.getFont().createGlyphVector(fc, label);
			
			if ((catList.size()-1) % labelEveryOtherN == 0) {
//				float yPixel = cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+colorMapSpacing*2;
//				g.setColor(foregroundColor);
////				g.drawLine(2*marginLeft, (int)yPixel, 2*marginLeft+colorBoxWidth, (int)yPixel);
				g.drawGlyphVector(glyphs, 2*marginLeft+colorBoxWidth+15, 
						cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+(int)(colorMapSpacing*1.5));
			}
        	
			
			
			
        	
        	// draw border around color map
    		g.setColor(foregroundColor);
    		g.drawRect(2*marginLeft, colorMapStartCursor+colorMapSpacing*2, colorBoxWidth, 
    				(maxLabelGlyphHeight+colorMapSpacing)*(catList.size()-supplementalCategoryColors.length));

        	}
        }
        else {

        	
        	
        	
        	
        	
        	
        	
        	if (interpolateBetweenCategories) {            
        		colorMapArray = new SimpleColorMap[categoryColors.length-1];
        		for (int n=0; n<categoryColors.length-1; n++) {
        			colorMapArray[n] = new SimpleColorMap(0, maxLabelGlyphHeight+colorMapSpacing, categoryColors[n], categoryColors[n+1]);
        		}
        	}

        	//        System.out.println("maxGlyphHeight="+maxGlyphHeight+" colorMapSpacing="+colorMapSpacing);

        	for (int n=0; n<categoryColors.length; n++) {
        		if (categoryColors[n] != null &&
        				categoryLabels[n] != null &&
        				categoryLabels[n].trim().length() > 0) {

        			GlyphVector glyphs = g.getFont().createGlyphVector(fc, categoryLabels[n]);



        			if (interpolateBetweenCategories) {
        				for (int i=0; i<(maxLabelGlyphHeight+colorMapSpacing); i++) {
        					if (n < colorMapArray.length) {
        						int yPixel = (int)cursor+i+colorMapSpacing*2;

        						g.setColor(colorMapArray[n].getColor(i));
        						g.drawLine(2*marginLeft, yPixel, 2*marginLeft+colorBoxWidth, yPixel);
        					}
        				}

        			}
        			else {

        				g.setColor(categoryColors[n]);
        				g.fillRect(2*marginLeft, (int)cursor+colorMapSpacing/2, colorBoxWidth, (int)maxLabelGlyphHeight+colorMapSpacing);

        				g.setColor(foregroundColor);
        				g.drawRect(2*marginLeft, (int)cursor+colorMapSpacing/2, colorBoxWidth, (int)maxLabelGlyphHeight+colorMapSpacing);

        			}

        			if (n % labelEveryOtherN == 0) {
        				float yPixel = cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+colorMapSpacing*2;
        				g.setColor(foregroundColor);
        				if (interpolateBetweenCategories) {
        					g.drawLine(2*marginLeft, (int)yPixel-((maxLabelGlyphHeight+colorMapSpacing)/2), 
        						2*marginLeft+colorBoxWidth, (int)yPixel-((maxLabelGlyphHeight+colorMapSpacing)/2));
        				}
        				g.drawGlyphVector(glyphs, 2*marginLeft+colorBoxWidth+15, (int)yPixel-colorMapSpacing/2);
        				
//        				g.drawLine(2*marginLeft, (int)yPixel-colorMapSpacing/2-maxLabelGlyphHeight+1, 
//        						2*marginLeft+colorBoxWidth+15, (int)yPixel-colorMapSpacing/2-maxLabelGlyphHeight+1);
//        				g.drawLine(2*marginLeft, (int)yPixel-colorMapSpacing/2+1, 
//        						2*marginLeft+colorBoxWidth+15, (int)yPixel-colorMapSpacing/2+1);
        				
        			}
        			
        			cursor += maxLabelGlyphHeight+colorMapSpacing;

        		}
        	}


        	if (interpolateBetweenCategories) {
        		g.setColor(foregroundColor);
        		g.drawRect(2*marginLeft, colorMapStartCursor+colorMapSpacing*2, colorBoxWidth, (maxLabelGlyphHeight+colorMapSpacing)*(categoryColors.length-1));
        		//            System.out.println(2*marginLeft+","+ colorMapSpacing*2+", "+ colorBoxWidth+","+ colorMapSpacing*categoryColors.length);
        	}

        }

        
        boolean addSpacer = true;
        if (sampleDimensionAndLabels != null) {        	
        	List<Category> catList = sampleDimensionAndLabels.getSampleDimension().getCategories();
        	if (catList.size() - supplementalCategoryColors.length < 2) {
        		addSpacer = false;
        	}
        }
        
        
        if (supplementalCategoryColors != null && supplementalCategoryLabels != null) {
        	if (addSpacer) {
        		cursor += 1*(maxLabelGlyphHeight+colorMapSpacing);
        	}
            for (int n=0; n<supplementalCategoryColors.length; n++) {
                if (supplementalCategoryLabels[n] != null) {
                    cursor += maxLabelGlyphHeight+colorMapSpacing;
                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, supplementalCategoryLabels[n]);
                    g.setColor(foregroundColor);
                    g.drawGlyphVector(glyphs, 2*marginLeft+colorBoxWidth+15, cursor+maxLabelGlyphHeight+colorMapSpacing);

                    g.setColor(supplementalCategoryColors[n]);
                    g.fillRect(2*marginLeft, (int)cursor+colorMapSpacing/2, colorBoxWidth, (int)maxLabelGlyphHeight+colorMapSpacing);

                    g.setColor(foregroundColor);
                    g.drawRect(2*marginLeft, (int)cursor+colorMapSpacing/2, colorBoxWidth, (int)maxLabelGlyphHeight+colorMapSpacing);
                    
                }                
            }

        }

        return image;
    }


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


    public BufferedImage createMediumLegendImage() throws WCTException {
    	return createMediumLegendImage(null);    	
    }

    /**
     * Create a smaller, simpler legend with a horizontal color ramp. 
     * @param size
     * @return
     * @throws WCTException 
     * @throws Exception
     */
    public BufferedImage createMediumLegendImage(Dimension size) throws WCTException  {

        double titleTextSpacingRatio = 1.25;
        int titleToColorMapSpacing = 6;
        int categoryLabelOffset = 15;
        int borderBufferX = 20;

        
        
//        BufferedImage image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        // junk image so we can measure text size in pixels
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();

        // ------- SET UP FONT AND RENDERING ---------
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        //      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_DITHERING,
        //      RenderingHints.VALUE_DITHER_ENABLE);





        double cursorX = 0;
        int cursorY = 0;

        //      labelEveryOtherN = 2;


        if (categoryColors == null || categoryLabels == null) {
            throw new WCTException("Category labels and colors must not be null");
        }

        if (categoryColors.length != categoryLabels.length) {
            throw new WCTException("Number of category colors must equal number of category labels: \n" +
                    "categoryColors.length="+categoryColors.length+"  categoryLabels.length="+categoryLabels.length);
        }


        // ------- DETERMINE MAX GLYPH HEIGHT OF METADATA TEXT ---------
        int[] maxGlyphHeightWidth = getMaxGlyphWidthAndHeight(g, fc);
        int maxGlyphHeight = maxGlyphHeightWidth[1];
        int maxGlyphWidth = maxGlyphHeightWidth[0];

        
        // special width test for horizontal labels for 'medium' legend
        StringBuilder labelString = new StringBuilder();
        for (int n=0; n<categoryLabels.length; n=n+labelEveryOtherN) {
            labelString.append(categoryLabels[n]).append("    ");
        }
        int height = (int) g.getFont().createGlyphVector(fc, labelString.toString()).getVisualBounds().getHeight();
        int width = (int) g.getFont().createGlyphVector(fc, labelString.toString()).getVisualBounds().getWidth();
        maxGlyphHeight = Math.max(height, maxGlyphHeight);
        maxGlyphWidth = Math.max(width, maxGlyphWidth);

        
        int legendHeight = maxGlyphHeight;
        if (legendTitle != null) {
            for (int n=0; n<legendTitle.length; n++) {
                if (legendTitle[n] != null && legendTitle[n].trim().length() > 0) {                	
                	maxGlyphWidth = Math.max(maxGlyphWidth,
                		(int)WCTUiUtils.getTextBounds(g.getFont(), legendTitle[n]).getWidth());
                	legendHeight += titleTextSpacingRatio*(int)WCTUiUtils.getTextBounds(g.getFont(), legendTitle[n]).getHeight();
                }
            }
        }
        // add the height of the color bar
        legendHeight += maxGlyphHeight;
        // multiply by two to account for line spacing and margins
        legendHeight *= 1.5;
        // add/remove a little extra from testing
        legendHeight += 4-(legendTitle.length*2);
        
        // -- DRAW STUFF --

        if (size == null) {
        	size = new Dimension(maxGlyphWidth+20, legendHeight);
        }
        image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        g = (Graphics2D)image.getGraphics();
        fc = g.getFontRenderContext();


        // ------- SET UP BACKGROUND ---------
        if (drawBorder) {             
            g.setColor(backgroundColor);
            g.fillRoundRect(1, 1, image.getWidth()-2, image.getHeight()-2, 10, 10);
            g.setColor(foregroundColor);
            g.drawRoundRect(1, 1, image.getWidth()-2, image.getHeight()-2, 10, 10);
        }
        else {
            g.setColor(backgroundColor);
            g.fillRect(1, 1, image.getWidth()-2, image.getHeight()-2);
            g.setColor(foregroundColor);
        }
        // ------- SET UP FONT AND RENDERING ---------
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        //      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_DITHERING,
        //      RenderingHints.VALUE_DITHER_ENABLE);
        
        // ------- START WRITING TEXT ------------
        if (legendTitle != null) {
            for (int n=0; n<legendTitle.length; n++) {
                if (legendTitle[n] != null && legendTitle[n].trim().length() > 0) {

                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, legendTitle[n]);
                    Rectangle2D bounds = glyphs.getVisualBounds();

                    g.setColor(foregroundColor);
                    g.drawGlyphVector(glyphs, 
                            (float)(size.getWidth()/2-bounds.getWidth()/2), 
                            cursorY+Math.round(titleTextSpacingRatio*maxGlyphHeight));
                    cursorY += Math.round(titleTextSpacingRatio*maxGlyphHeight);
                }
            }
        }



        // if we are not drawing the legend, return
        if (! drawColorMap) {
            return image;
        }


      

        
        
        
        
        if (sampleDimensionAndLabels != null) {
        	
        	  // -------------- DRAW COLOR MAP ------------------------

//
//            if (interpolateBetweenCategories) {            
//                colorMapArray = new SimpleColorMap[categoryColors.length-1];
//                for (int n=0; n<categoryColors.length-1; n++) {
//                    colorMapArray[n] = new SimpleColorMap(0, Math.round(colorMapSpacing), categoryColors[n], categoryColors[n+1]);
//                }
//            }
//
//            cursorX += xBorderSpacing;
//            cursorY += titleToColorMapSpacing;

            //        System.out.println("cursorX: "+cursorX);
            //        System.out.println("colorMapSpacing*(categoryColors.length-1): "+colorMapSpacing*(categoryColors.length-1));     
//            System.out.println("------------------------ using sample dimension for legend --------------------------");

            
        	List<Category> catList = sampleDimensionAndLabels.getSampleDimension().getCategories();
        	
        	
        	boolean flip = false;
        	if (catList.get(0).getRange().getMinimum() > 
        		catList.get(1).getRange().getMinimum()) {
        		
        		flip = true;
        	}       	
        	
        	// flip list if needed, and remove NO DATA from list
        	ArrayList<Category> newCatList = new ArrayList<Category>();
        	for (int n=0; n<catList.size(); n++) {
        		Category cat = null;
        		if (flip) {
        			cat = catList.get(catList.size()-n-1);
        		}
        		else {
        			cat = catList.get(n);
        		}
        		if (cat.getColors().length > 1) {
        			newCatList.add(cat);
        		}
        	}
        	catList = newCatList;
        	

        	
            // spacing correction for edge
            int xBorderSpacing = 20;
            double colorMapSpacing = (size.getWidth()-2*xBorderSpacing -0)/(catList.size()-0);
            
            
            
//            int xBorderSpacing = 0;
//
//            double colorMapSpacing = (size.getWidth()-2*xBorderSpacing -0)/(catList.size()-1);
//            {
//                GlyphVector glyphs = g.getFont().createGlyphVector(fc, catList.get(0).getName(null));
//                Rectangle2D bounds = glyphs.getVisualBounds();
//                if (bounds.getWidth()/2 > xBorderSpacing) {
//                    xBorderSpacing = borderBufferX +(int)Math.round(bounds.getWidth()/2);
//                }
//            }
//            colorMapSpacing = (size.getWidth()-2*xBorderSpacing)/(catList.size()-1);
            
//            colorMapSpacing = colorMapSpacing/2;
//            xBorderSpacing = 8;
            
            cursorX += xBorderSpacing;
            cursorY += titleToColorMapSpacing;
            
            int colorMapWidth = (int)(colorMapSpacing*(catList.size()-1));
            int colorMapHeight = maxGlyphHeight;
            
            // draw transparent background pattern
            if (drawColorMapTransparentBackgroundPattern) {
            	int patternSquareSize = colorMapHeight/2;
            	for (int i=0; i<colorMapWidth; i=i+patternSquareSize) {
            		int x = (int)Math.round(cursorX) + i;
            		g.setColor( i%2 == 0 ? Color.gray : Color.white );
            		g.fillRect(x, cursorY, (colorMapWidth-i < patternSquareSize) ? colorMapWidth-i : patternSquareSize, (int)Math.round(colorMapHeight/2.0));
            		g.setColor( i%2 == 0 ? Color.white : Color.gray );
            		g.fillRect(x, cursorY+(int)(Math.round(colorMapHeight/2.0)), (colorMapWidth-i < patternSquareSize) ? colorMapWidth-i : patternSquareSize, (int)Math.round(colorMapHeight/2.0));
            	}
            }
        	
    		colorMapArray = new SimpleColorMap[catList.size()];
    		for (int n=0; n<colorMapArray.length; n++) {
    			if (catList.get(n).getColors().length == 2) {
    				colorMapArray[n] = new SimpleColorMap(0, colorMapSpacing, 
//    				colorMapArray[n] = new SimpleColorMap(0, maxGlyphWidth+colorMapSpacing, 
    					catList.get(n).getColors()[0], catList.get(n).getColors()[1]);
    			}
    			else if (catList.get(n).getColors().length == 1) {
      				colorMapArray[n] = new SimpleColorMap(0, colorMapSpacing, 
//      					colorMapArray[n] = new SimpleColorMap(0, maxGlyphWidth+colorMapSpacing, 
    					catList.get(n).getColors()[0], catList.get(n).getColors()[0]);
    			}
    		}
    		

    		ArrayList<Color> supColorList = new ArrayList<Color>();
    		ArrayList<String> supLabelList = new ArrayList<String>();
        	for (int n=0; n<catList.size(); n++) {
    			if (catList.get(n).getName(null).startsWith("Unique:")) {
    				supColorList.add(catList.get(n).getColors()[0]);
    				String s = catList.get(n).getName(null);
    				supLabelList.add(s.substring("Unique:".length()));
    			}
        	}
    		supplementalCategoryColors = supColorList.toArray(new Color[supColorList.size()]);
    		supplementalCategoryLabels = supLabelList.toArray(new String[supLabelList.size()]);
        	
        	

        	for (int n=0; n<catList.size(); n++) {
    			String label = catList.get(n).getName(null).split("\\:")[0];
    			GlyphVector glyphs = g.getFont().createGlyphVector(fc, label);  
                Rectangle2D glyphBounds = glyphs.getVisualBounds();

                
//				System.out.println(n+"/"+catList.size()+" : "+catList.get(n)+ "getColors().length="+catList.get(n).getColors().length);
                
                
                
    			if (catList.get(n).getColors().length == 1 ||
        			catList.get(n).getName(null).startsWith("Unique:")) {
    				continue;
    			}
    			
    			
    			
    			
//				if (catList.get(n).getColors().length > 1 &&
//						catList.get(n).getColors()[1].getAlpha() > 0) {				
				if (catList.get(n).getColors().length > 1) {				

					
//					System.out.println(n+": "+catList.get(n)+ "cursorX="+cursorX+" "+(maxGlyphWidth+colorMapSpacing));
//					System.out.println("label: "+label+"  maxGlyphWidth: "+maxGlyphWidth+" colorMapSpacing: "+colorMapSpacing);
					
//					for (int i=0; i<(maxGlyphWidth+colorMapSpacing); i++) {
					for (int i=0; i<(colorMapSpacing); i++) {
						if (n < colorMapArray.length) {

//							int yPixel = (int)cursor+i+colorMapSpacing*2;

//							g.setColor(colorMapArray[n].getColor(i));
//							g.drawLine(2*marginLeft, yPixel, 2*marginLeft+colorBoxWidth, yPixel);

							

                        	if (drawColorMapTransparentBackgroundPattern) {
                            	Color c = colorMapArray[n].getColor(i);
                                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
                        	}
                        	else {
                        		g.setColor(new Color(colorMapArray[n].getColor(i).getRGB()));
                        	}
                        	                                
                            g.drawLine((int)Math.round(cursorX+i), 
                                    cursorY, 
                                    (int)Math.round(cursorX+i), 
                                    cursorY+maxGlyphHeight);							
						}
					}
					
					
	                if (getLabelOverride().length() == 0 && drawLabels && n % labelEveryOtherN == 0) {
	                    g.setColor(foregroundColor);
	                    g.drawGlyphVector(glyphs, 
	                            (float)(cursorX-glyphBounds.getWidth()/2), 
	                            cursorY+maxGlyphHeight+categoryLabelOffset);
	                    
	                    
//	                    System.out.println("Drawing label: "+categoryLabels[n]);
	                }


	                cursorX += colorMapSpacing;
				}
								
        	}
        	
        	if ((catList.size()-supplementalCategoryColors.length) > 1) {

        	// draw last label
//			String label = sampleDimensionAndLabels.getLabels()[catList.size()-1];
			String label = catList.get(catList.size()-1).getName(null).split("\\:")[1];
////			label = "last";
			GlyphVector glyphs = g.getFont().createGlyphVector(fc, label);
			
			if ((catList.size()-1) % labelEveryOtherN == 0) {
//				temp comment
//				g.drawGlyphVector(glyphs, 2*marginLeft+colorBoxWidth+15, 
//						cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+(int)(colorMapSpacing*1.5));
                Rectangle2D glyphBounds = glyphs.getVisualBounds();
                g.drawGlyphVector(glyphs, 
                        (float)(cursorX-glyphBounds.getWidth()/2), 
                        cursorY+maxGlyphHeight+categoryLabelOffset);

			}
        	
			
			
			
        	
        	// draw border around color map
    		g.setColor(foregroundColor);
//    		temp comment
//    		g.drawRect(2*marginLeft, colorMapStartCursor+colorMapSpacing*2, colorBoxWidth, 
//    				(maxLabelGlyphHeight+colorMapSpacing)*(catList.size()-supplementalCategoryColors.length));

        	}
        }
        else {

        	// use old category colors and labels arrays
        	
        	
        	
        	
        	
        	
        	
        	
        	
        
        
        	  // -------------- DRAW COLOR MAP ------------------------
            // spacing correction for edge
            int xBorderSpacing = 0;

            double colorMapSpacing = (size.getWidth()-2*xBorderSpacing -0)/(categoryColors.length-1);
            //        if (colorMapSpacing == 0) { colorMapSpacing = 1; }


            {
                GlyphVector glyphs = g.getFont().createGlyphVector(fc, categoryLabels[0]);
                Rectangle2D bounds = glyphs.getVisualBounds();
                if (bounds.getWidth()/2 > xBorderSpacing) {
                    xBorderSpacing = borderBufferX +(int)Math.round(bounds.getWidth()/2);
                }
            }

            colorMapSpacing = (size.getWidth()-2*xBorderSpacing)/(categoryColors.length-1);

            if (interpolateBetweenCategories) {            
                colorMapArray = new SimpleColorMap[categoryColors.length-1];
                for (int n=0; n<categoryColors.length-1; n++) {
                    colorMapArray[n] = new SimpleColorMap(0, Math.round(colorMapSpacing), categoryColors[n], categoryColors[n+1]);
                }
            }

            cursorX += xBorderSpacing;
            cursorY += titleToColorMapSpacing;

            //        System.out.println("cursorX: "+cursorX);
            //        System.out.println("colorMapSpacing*(categoryColors.length-1): "+colorMapSpacing*(categoryColors.length-1));     
            
            int colorMapWidth = (int)(colorMapSpacing*(categoryColors.length-1));
            int colorMapHeight = maxGlyphHeight;
            
            // draw transparent background pattern
            if (drawColorMapTransparentBackgroundPattern) {
            	int patternSquareSize = colorMapHeight/2;
            	for (int i=0; i<colorMapWidth; i=i+patternSquareSize) {
            		int x = (int)Math.round(cursorX) + i;
            		g.setColor( i%2 == 0 ? Color.gray : Color.white );
            		g.fillRect(x, cursorY, (colorMapWidth-i < patternSquareSize) ? colorMapWidth-i : patternSquareSize, (int)Math.round(colorMapHeight/2.0));
            		g.setColor( i%2 == 0 ? Color.white : Color.gray );
            		g.fillRect(x, cursorY+(int)(Math.round(colorMapHeight/2.0)), (colorMapWidth-i < patternSquareSize) ? colorMapWidth-i : patternSquareSize, (int)Math.round(colorMapHeight/2.0));
            	}
            }
              
        
        
        // THIS IS HARDED CODED BECAUSE OF BUG DURING INTERPOLATION - COLOR BAR IS NOT LONG ENOUGH!
        boolean outlineCategories = ! interpolateBetweenCategories;
//        interpolateBetweenCategories = false;

        int minX = 999999999;
        int maxX = -999999999;
        
        for (int n=0; n<categoryColors.length; n++) {
            //            System.out.println("catColor["+n+"]="+categoryColors[n]+" alpha="+categoryColors[n].getAlpha());
//                        System.out.println("catLabel["+n+"]="+categoryLabels[n]);


            if (categoryColors[n] != null &&
                    categoryLabels[n] != null &&
                    categoryLabels[n].trim().length() > 0) {

                GlyphVector glyphs = g.getFont().createGlyphVector(fc, categoryLabels[n]);
                Rectangle2D glyphBounds = glyphs.getVisualBounds();


//                 ansari test
                if (interpolateBetweenCategories) {
//                    int maxWidth = (int)(colorMapSpacing*(categoryColors.length-1))+1;

                    for (int i=0; i<(int)(colorMapSpacing+1); i++) {
                        if (n < colorMapArray.length) {
                        	
                        	if (drawColorMapTransparentBackgroundPattern) {
                            	Color c = colorMapArray[n].getColor(i);
                                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
                        	}
                        	else {
                        		g.setColor(new Color(colorMapArray[n].getColor(i).getRGB()));
                        	}
                        	                                
                            g.drawLine((int)Math.round(cursorX+i), 
                                    cursorY, 
                                    (int)Math.round(cursorX+i), 
                                    cursorY+maxGlyphHeight);
                            
                        }
                    }

                }
                else {

                    minX = (minX > (int)Math.round(cursorX)) ? (int)Math.round(cursorX) : minX;  
                    maxX = (maxX < (int)Math.round(cursorX)) ? (int)Math.round(cursorX) : maxX;  
                    
                    g.setColor(categoryColors[n]);
                    g.setColor(new Color(categoryColors[n].getRGB()));
                    if (outlineCategories) {
                        g.fillRect((int)Math.round(cursorX), cursorY, 
                            (int)Math.round(colorMapSpacing), maxGlyphHeight);

                        g.setColor(foregroundColor);
                        g.drawRect((int)Math.round(cursorX), cursorY, 
                            (int)Math.round(colorMapSpacing), maxGlyphHeight);
                    }
                    else {
                        g.fillRect((int)Math.round(cursorX), cursorY, 
                                (int)Math.round(colorMapSpacing)+1, maxGlyphHeight);
                    }
                }

                if (getLabelOverride().length() == 0 && drawLabels && n % labelEveryOtherN == 0) {
                    g.setColor(foregroundColor);
                    g.drawGlyphVector(glyphs, 
                            (float)(cursorX-glyphBounds.getWidth()/2), 
                            cursorY+maxGlyphHeight+categoryLabelOffset);
                    
                    
//                    System.out.println("Drawing label: "+categoryLabels[n]);
                }

                cursorX += colorMapSpacing;

            }
            else {
                System.out.println("skipping... catColor["+n+"]="+categoryColors[n]);
                System.out.println("skipping... catLabel["+n+"]="+categoryLabels[n]);

            }
        }

        //        System.out.println("cursorX: "+cursorX);

        if (getLabelOverride().length() > 0) {
            GlyphVector glyphs = g.getFont().createGlyphVector(fc, getLabelOverride());
            Rectangle2D bounds = glyphs.getVisualBounds();

            g.setColor(foregroundColor);
            g.drawGlyphVector(glyphs, 
                    (float)(size.getWidth()/2-bounds.getWidth()/2), 
                    cursorY+maxGlyphHeight+categoryLabelOffset);
        }
        

        
        
        

//        if (interpolateBetweenCategories || ! outlineCategories) {
        if (interpolateBetweenCategories) {
            g.setColor(foregroundColor);
//            g.setColor(Color.RED);
//            g.drawRect(xBorderSpacing, 
//                    cursorY, 
//                    (int)(colorMapSpacing*(categoryColors.length-1))+3, 
//                    maxGlyphHeight);
            g.drawRect(minX, 
                    cursorY, 
                    maxX-minX+1, 
                    maxGlyphHeight);
            
//            System.out.println("\n\nCOLOR SCALE LOC: "+xBorderSpacing+" TO "+(xBorderSpacing+(int)(colorMapSpacing*(categoryColors.length-1))));
        }

    }

        return image;
    }





    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


    private int[] getMaxGlyphWidthAndHeight(Graphics2D g, FontRenderContext fc) {
    	
        // ------- DETERMINE MAX GLYPH HEIGHT OF METADATA TEXT ---------
        int maxGlyphHeight = 0;
        int maxGlyphWidth = 0;
        int height;
        int width;

        if (dataType != null) {
    		Rectangle2D visBounds = g.getFont().createGlyphVector(fc, dataType).getVisualBounds();
            maxGlyphHeight = Math.max((int) visBounds.getHeight(), maxGlyphHeight);
            maxGlyphWidth = Math.max((int) visBounds.getWidth(), maxGlyphWidth);
        }
        if (dataDescription != null) {
            for (int n=0; n<dataDescription.length; n++) {
                if (dataDescription[n] != null) {
            		Rectangle2D visBounds = g.getFont().createGlyphVector(fc, dataDescription[n]).getVisualBounds();
                    maxGlyphHeight = Math.max((int) visBounds.getHeight(), maxGlyphHeight);
                    maxGlyphWidth = Math.max((int) visBounds.getWidth(), maxGlyphWidth);
                }
            }
        }
        if (dateTimeInfo != null) {
    		Rectangle2D visBounds = g.getFont().createGlyphVector(fc, dateTimeInfo).getVisualBounds();
            maxGlyphHeight = Math.max((int) visBounds.getHeight(), maxGlyphHeight);
            maxGlyphWidth = Math.max((int) visBounds.getWidth(), maxGlyphWidth);
        }
        if (mainMetadata != null) {
            for (int n=0; n<mainMetadata.length; n++) {
                if (mainMetadata[n] != null) {
            		Rectangle2D visBounds = g.getFont().createGlyphVector(fc, mainMetadata[n]).getVisualBounds();
                    maxGlyphHeight = Math.max((int) visBounds.getHeight(), maxGlyphHeight);
                    maxGlyphWidth = Math.max((int) visBounds.getWidth(), maxGlyphWidth);
                }
            }
        }
        if (specialMetadata != null) {
            for (int n=0; n<specialMetadata.length; n++) {
                if (specialMetadata[n] != null) {
            		Rectangle2D visBounds = g.getFont().createGlyphVector(fc, specialMetadata[n]).getVisualBounds();
                    maxGlyphHeight = Math.max((int) visBounds.getHeight(), maxGlyphHeight);
                    maxGlyphWidth = Math.max((int) visBounds.getWidth(), maxGlyphWidth);
                }
            }
        }
        if (legendTitle != null) {
            for (int n=0; n<legendTitle.length; n++) {
                if (legendTitle[n] != null) {
            		Rectangle2D visBounds = g.getFont().createGlyphVector(fc, legendTitle[n]).getVisualBounds();
                    maxGlyphHeight = Math.max((int) visBounds.getHeight(), maxGlyphHeight);
                    maxGlyphWidth = Math.max((int) visBounds.getWidth(), maxGlyphWidth);
                }
            }
        }
        
        
        if (sampleDimensionAndLabels != null) {        	
        	List<Category> catList = sampleDimensionAndLabels.getSampleDimension().getCategories();  
        	for (Category cat : catList) {
        		Rectangle2D visBounds = g.getFont().createGlyphVector(fc, cat.getName(null)).getVisualBounds();
                maxGlyphHeight = Math.max((int) visBounds.getHeight(), maxGlyphHeight);
                maxGlyphWidth = Math.max((int) visBounds.getWidth(), maxGlyphWidth);
        	}
        }
        else {
        	if (categoryLabels != null) {
        		for (String label : categoryLabels) {        			
        			if (label != null) { 
        				Rectangle2D visBounds = g.getFont().createGlyphVector(fc, label).getVisualBounds();
        				maxGlyphHeight = Math.max((int) visBounds.getHeight(), maxGlyphHeight);
        				maxGlyphWidth = Math.max((int) visBounds.getWidth(), maxGlyphWidth);
        			}
        		}
        	}
        }
        
        return new int[] { maxGlyphWidth, maxGlyphHeight };
	}





	public BufferedImage createSmallLegendImage() throws WCTException {
    	return createSmallLegendImage(null);    	
    }

    /**
     * Create a much smaller, simpler legend with a horizontal color ramp. 
     * @param size
     * @return
     * @throws WCTException 
     * @throws Exception
     */
    public BufferedImage createSmallLegendImage(Dimension size) throws WCTException  {

        double titleTextSpacingRatio = 1.25;
        int titleToColorMapSpacing = 6;
        int categoryLabelOffset = 15;
        int borderBufferX = 10;

        
        
//        BufferedImage image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        // junk image so we can measure text size in pixels
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();

        // ------- SET UP FONT AND RENDERING ---------
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        //      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_DITHERING,
        //      RenderingHints.VALUE_DITHER_ENABLE);





        double cursorX = 0;
        int cursorY = 0;

        //      labelEveryOtherN = 2;


        if (categoryColors == null || categoryLabels == null) {
            throw new WCTException("Category labels and colors must not be null");
        }

        if (categoryColors.length != categoryLabels.length) {
            throw new WCTException("Number of category colors must equal number of category labels: \n" +
                    "categoryColors.length="+categoryColors.length+"  categoryLabels.length="+categoryLabels.length);
        }


        // ------- DETERMINE MAX GLYPH HEIGHT OF METADATA TEXT ---------
        int maxGlyphHeight = 0;
        int maxGlyphWidth = 0;
        int height;
        int width;

        if (dataType != null) {
            height = (int) g.getFont().createGlyphVector(fc, dataType).getVisualBounds().getHeight();
            width = (int) g.getFont().createGlyphVector(fc, dataType).getVisualBounds().getWidth();
            maxGlyphHeight = Math.max(height, maxGlyphHeight);
            maxGlyphWidth = Math.max(width, maxGlyphWidth);
        }
        if (dataDescription != null) {
            for (int n=0; n<dataDescription.length; n++) {
                if (dataDescription[n] != null) {
                    height = (int) g.getFont().createGlyphVector(fc, dataDescription[n]).getVisualBounds().getHeight();
                    width = (int) g.getFont().createGlyphVector(fc, dataDescription[n]).getVisualBounds().getWidth();
                    maxGlyphHeight = Math.max(height, maxGlyphHeight);
                    maxGlyphWidth = Math.max(width, maxGlyphWidth);
                }
            }
        }
        if (dateTimeInfo != null) {
            height = (int) g.getFont().createGlyphVector(fc, dateTimeInfo).getVisualBounds().getHeight();
            width = (int) g.getFont().createGlyphVector(fc, dateTimeInfo).getVisualBounds().getWidth();
            maxGlyphHeight = Math.max(height, maxGlyphHeight);
            maxGlyphWidth = Math.max(width, maxGlyphWidth);
        }
        if (mainMetadata != null) {
            for (int n=0; n<mainMetadata.length; n++) {
                if (mainMetadata[n] != null) {
                    height = (int) g.getFont().createGlyphVector(fc, mainMetadata[n]).getVisualBounds().getHeight();
                    width = (int) g.getFont().createGlyphVector(fc, mainMetadata[n]).getVisualBounds().getWidth();
                    maxGlyphHeight = Math.max(height, maxGlyphHeight);
                    maxGlyphWidth = Math.max(width, maxGlyphWidth);
                }
            }
        }
        if (specialMetadata != null) {
            for (int n=0; n<specialMetadata.length; n++) {
                if (specialMetadata[n] != null) {
                    height = (int) g.getFont().createGlyphVector(fc, specialMetadata[n]).getVisualBounds().getHeight();
                    width = (int) g.getFont().createGlyphVector(fc, specialMetadata[n]).getVisualBounds().getWidth();
                    maxGlyphHeight = Math.max(height, maxGlyphHeight);
                    maxGlyphWidth = Math.max(width, maxGlyphWidth);
                }
            }
        }
        if (legendTitle != null) {
            for (int n=0; n<legendTitle.length; n++) {
                if (legendTitle[n] != null) {
                    height = (int) g.getFont().createGlyphVector(fc, legendTitle[n]).getVisualBounds().getHeight();
                    width = (int) g.getFont().createGlyphVector(fc, legendTitle[n]).getVisualBounds().getWidth();
                    maxGlyphHeight = Math.max(height, maxGlyphHeight);
                    maxGlyphWidth = Math.max(width, maxGlyphWidth);
                }
            }
        }

        StringBuilder labelString = new StringBuilder();
        int startIndex = (categoryLabels.length%labelEveryOtherN)/2;
        for (int n=startIndex; n<categoryLabels.length; n=n+labelEveryOtherN) {
            labelString.append(categoryLabels[n]).append("  ");
        }
        height = (int) g.getFont().createGlyphVector(fc, labelString.toString()).getVisualBounds().getHeight();
        width = (int) g.getFont().createGlyphVector(fc, labelString.toString()).getVisualBounds().getWidth();
        maxGlyphHeight = Math.max(height, maxGlyphHeight);
        maxGlyphWidth = Math.max(width, maxGlyphWidth);
        
        // -- DRAW STUFF --

        if (size == null) {
        	size = new Dimension(maxGlyphWidth+20, 83);
        }
        image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        g = (Graphics2D)image.getGraphics();
        fc = g.getFontRenderContext();


        // ------- SET UP BACKGROUND ---------
        if (drawBorder) {             
            g.setColor(backgroundColor);
            g.fillRoundRect(1, 1, image.getWidth()-2, image.getHeight()-2, 10, 10);
            g.setColor(foregroundColor);
            g.drawRoundRect(1, 1, image.getWidth()-2, image.getHeight()-2, 10, 10);
        }
        else {
            g.setColor(backgroundColor);
            g.fillRect(1, 1, image.getWidth()-2, image.getHeight()-2);
            g.setColor(foregroundColor);
        }
        // ------- SET UP FONT AND RENDERING ---------
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        //      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_DITHERING,
        //      RenderingHints.VALUE_DITHER_ENABLE);
        
        // ------- START WRITING TEXT ------------
        if (legendTitle != null) {
            for (int n=0; n<legendTitle.length; n++) {
                if (legendTitle[n] != null) {

                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, legendTitle[n]);
                    Rectangle2D bounds = glyphs.getVisualBounds();

                    g.setColor(foregroundColor);
                    g.drawGlyphVector(glyphs, 
                            (float)(size.getWidth()/2-bounds.getWidth()/2), 
                            cursorY+Math.round(titleTextSpacingRatio*maxGlyphHeight));
                    cursorY += Math.round(titleTextSpacingRatio*maxGlyphHeight);
                }
            }
        }



        // if we are not drawing the legend, return
        if (! drawColorMap) {
            return image;
        }


        // -------------- DRAW COLOR MAP ------------------------
        // spacing correction for edge
        int xBorderSpacing = 0;

        double colorMapSpacing = (size.getWidth()-2*xBorderSpacing -0)/categoryColors.length;
        //        if (colorMapSpacing == 0) { colorMapSpacing = 1; }


        {
            GlyphVector glyphs = g.getFont().createGlyphVector(fc, categoryLabels[0]);
            Rectangle2D bounds = glyphs.getVisualBounds();
            if (bounds.getWidth()/2 > xBorderSpacing) {
                xBorderSpacing = borderBufferX +(int)Math.round(bounds.getWidth()/2);
            }
        }

        colorMapSpacing = (size.getWidth()-2*xBorderSpacing)/categoryColors.length;

        if (interpolateBetweenCategories) {            
            colorMapArray = new SimpleColorMap[categoryColors.length-1];
            for (int n=0; n<categoryColors.length-1; n++) {
                colorMapArray[n] = new SimpleColorMap(0, Math.round(colorMapSpacing), categoryColors[n], categoryColors[n+1]);
            }
        }


        //        colorMapSpacing = (int)colorMapSpacing;

        cursorX += xBorderSpacing;

        cursorY += titleToColorMapSpacing;

        //        System.out.println("cursorX: "+cursorX);
        //        System.out.println("colorMapSpacing*(categoryColors.length-1): "+colorMapSpacing*(categoryColors.length-1));

        g.setColor(mapBackgroundColor);
        g.fillRect((int)Math.round(cursorX), 
                cursorY, 
                (int)(colorMapSpacing*(categoryColors.length-1)), 
                maxGlyphHeight);

        
        
        // THIS IS HARDED CODED BECAUSE OF BUG DURING INTERPOLATION - COLOR BAR IS NOT LONG ENOUGH!
        boolean outlineCategories = ! interpolateBetweenCategories;
        interpolateBetweenCategories = false;

        int minX = 999999999;
        int maxX = -999999999;
        
        for (int n=0; n<categoryColors.length; n++) {
            //            System.out.println("catColor["+n+"]="+categoryColors[n]+" alpha="+categoryColors[n].getAlpha());
//                        System.out.println("catLabel["+n+"]="+categoryLabels[n]);


            if (categoryColors[n] != null &&
                    categoryLabels[n] != null &&
                    categoryLabels[n].trim().length() > 0) {





                GlyphVector glyphs = g.getFont().createGlyphVector(fc, categoryLabels[n]);
                Rectangle2D glyphBounds = glyphs.getVisualBounds();


                if (interpolateBetweenCategories) {
                    int maxWidth = (int)(colorMapSpacing*(categoryColors.length-1))+1;

                    for (int i=0; i<Math.round(colorMapSpacing); i++) {
                        if (n < colorMapArray.length) {
//                            g.setColor(colorMapArray[n].getColor(i));
                            g.setColor(new Color(colorMapArray[n].getColor(i).getRGB()));
                            
//                            g.setColor(Color.RED);

                            if ((int)Math.round(cursorX+i+colorMapSpacing/2)+1 < maxWidth) {                                
                                g.fillRect((int)Math.round(cursorX+i+colorMapSpacing/2), 
                                        cursorY, 
                                        (int)Math.round(colorMapSpacing)+1, 
                                        maxGlyphHeight);
                            }
                            else if ((int)Math.round(cursorX+i+colorMapSpacing/2) < maxWidth) {
                                g.drawLine((int)Math.round(cursorX+i+colorMapSpacing/2), 
                                        cursorY, 
                                        (int)Math.round(cursorX+i+colorMapSpacing/2), 
                                        cursorY+maxGlyphHeight);
                            }
//                            System.out.println(colorMapArray[n].getColor(i)+":"+(int)Math.round(cursorX+i+colorMapSpacing/2)+" ");
                        }
                    }

                }
                else {

                    minX = (minX > (int)Math.round(cursorX)) ? (int)Math.round(cursorX) : minX;  
                    maxX = (maxX < (int)Math.round(cursorX)) ? (int)Math.round(cursorX) : maxX;  
                    
                    g.setColor(categoryColors[n]);
                    g.setColor(new Color(categoryColors[n].getRGB()));
                    if (outlineCategories) {
                        g.fillRect((int)Math.round(cursorX), cursorY, 
                            (int)Math.round(colorMapSpacing), maxGlyphHeight);

                        g.setColor(foregroundColor);
                        g.drawRect((int)Math.round(cursorX), cursorY, 
                            (int)Math.round(colorMapSpacing), maxGlyphHeight);
                    }
                    else {
                        g.fillRect((int)Math.round(cursorX), cursorY, 
                                (int)Math.round(colorMapSpacing)+1, maxGlyphHeight);
                    }
                }

                if (getLabelOverride().length() == 0 && drawLabels && n % labelEveryOtherN == 0) {
                    g.setColor(foregroundColor);
                    g.drawGlyphVector(glyphs, 
                            (float)(cursorX-glyphBounds.getWidth()/2), 
                            cursorY+maxGlyphHeight+categoryLabelOffset);
                    
                    
//                    System.out.println("Drawing label: "+categoryLabels[n]);
                }

                cursorX += colorMapSpacing;

            }
            else {
                System.out.println("skipping... catColor["+n+"]="+categoryColors[n]);
                System.out.println("skipping... catLabel["+n+"]="+categoryLabels[n]);

            }
        }

        //        System.out.println("cursorX: "+cursorX);

        if (getLabelOverride().length() > 0) {
            GlyphVector glyphs = g.getFont().createGlyphVector(fc, getLabelOverride());
            Rectangle2D bounds = glyphs.getVisualBounds();

            g.setColor(foregroundColor);
            g.drawGlyphVector(glyphs, 
                    (float)(size.getWidth()/2-bounds.getWidth()/2), 
                    cursorY+maxGlyphHeight+categoryLabelOffset);
        }
        


//        if (interpolateBetweenCategories || ! outlineCategories) {
        if (interpolateBetweenCategories) {
            g.setColor(foregroundColor);
//            g.setColor(Color.RED);
//            g.drawRect(xBorderSpacing, 
//                    cursorY, 
//                    (int)(colorMapSpacing*(categoryColors.length-1))+3, 
//                    maxGlyphHeight);
            
            
            g.drawRect(minX, 
                    cursorY, 
                    maxX-minX+1, 
                    maxGlyphHeight);
            
//            System.out.println("\n\nCOLOR SCALE LOC: "+xBorderSpacing+" TO "+(xBorderSpacing+(int)(colorMapSpacing*(categoryColors.length-1))));
        }


        return image;
    }




    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public BufferedImage createSmallVertLegendImage() throws WCTException {
    	return createSmallVertLegendImage(null);
    }
    
    public BufferedImage createSmallVertLegendImage(Dimension size) throws WCTException {

        int marginLeft = 7;
        int marginTop = 2;
        int textSpacing = 5;
//        int colorMapSpacing = 15;
        int colorMapSpacing = 4;
        
        
        int numCats = 0;
        if (sampleDimensionAndLabels != null) {
        	numCats = sampleDimensionAndLabels.getSampleDimension().getCategories().size();
        }
        else if (categoryColors != null) {
        	numCats = categoryColors.length;
        }
        else {
        	numCats = 0;
        	drawColorMap = false;
        }
        
        
        if (numCats > 75) {
        	colorMapSpacing = 1;
        	labelEveryOtherN = 4;
        }
        else if (numCats > 50) {
        	colorMapSpacing = 1;
        	labelEveryOtherN = 3;
        }
        else if (numCats > 25) {
        	colorMapSpacing = 2;
        	labelEveryOtherN = 2;
        }
        else if (numCats > 16) {
        	colorMapSpacing = 4;
        	labelEveryOtherN = 1;
        }
//        else if (numCats > 12) {
//        	colorMapSpacing = 6;
//        	labelEveryOtherN = 1;
//        }
//        else if (numCats > 8) {
////        	colorMapSpacing = 8;
//        	labelEveryOtherN = 1;
//        }
//        else if (numCats > 6) {
//        	colorMapSpacing = 12;
//        	labelEveryOtherN = 1;
//        }

        
        
//        BufferedImage image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        // junk image so we can measure text size in pixels
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();

        // ------- SET UP FONT AND RENDERING ---------
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_DITHERING,
        //      RenderingHints.VALUE_DITHER_ENABLE);



        // ------- DETERMINE MAX GLYPH WIDTH/HEIGHT OF METADATA TEXT ---------
        int[] maxGlyphHeightWidth = getMaxGlyphWidthAndHeight(g, fc);
        int maxGlyphHeight = maxGlyphHeightWidth[1];
        int maxGlyphWidth = maxGlyphHeightWidth[0];

        int numSupps = (supplementalCategoryColors == null) ? 1 : supplementalCategoryColors.length+1;
        
        if (size == null) {
        	size = new Dimension(maxGlyphWidth+10, (maxGlyphHeight+colorMapSpacing)*(numCats + numSupps + 1));
        }
        image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        g = (Graphics2D)image.getGraphics();
        fc = g.getFontRenderContext();

        // ------- SET UP FONT AND RENDERING ---------
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        //      g.setRenderingHint(RenderingHints.KEY_DITHERING,
        //      RenderingHints.VALUE_DITHER_ENABLE);



        // ------- SET UP BACKGROUND ---------
        if (drawBorder) {
            g.setColor(backgroundColor);
            g.fillRoundRect(1, 1, image.getWidth()-2, image.getHeight()-2, 10, 10);
            g.setColor(foregroundColor);
            g.drawRoundRect(1, 1, image.getWidth()-2, image.getHeight()-2, 10, 10);
        }
        else {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(foregroundColor);
        }

        


        float cursor = 0;

        if (legendTitle != null) {
            for (int n=0; n<legendTitle.length; n++) {
                if (legendTitle[n] != null) {

                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, legendTitle[n]);
                    g.setColor(foregroundColor);
                    cursor += maxGlyphHeight+textSpacing;
                    g.drawGlyphVector(glyphs, 2*marginLeft, cursor);
                }
            }
        }


        cursor += 2*textSpacing;

        
        
        // if we are not drawing the legend, return
        if (! drawColorMap) {
            return image;
        }


        if (sampleDimensionAndLabels == null) {
        	if (categoryColors == null || categoryLabels == null) {
        		throw new WCTException("Category labels and colors must not be null");
        	}

        	if (categoryColors.length != categoryLabels.length) {
        		throw new WCTException("Number of category colors must equal number of category labels: \n" +
        				"categoryColors.length="+categoryColors.length+"  categoryLabels.length="+categoryLabels.length);
        	}
        }


        // -------------- DRAW COLOR MAP ------------------------



        int colorMapStartCursor = (int)cursor;

        int maxLabelGlyphHeight = 0;
        if (sampleDimensionAndLabels == null) {
        	for (int n=0; n<categoryLabels.length; n++) {
        		if (categoryLabels[n] != null) {
        			GlyphVector glyphs = g.getFont().createGlyphVector(fc, categoryLabels[n]);
        			Rectangle2D bounds = glyphs.getVisualBounds();
        			if (bounds.getHeight() > maxLabelGlyphHeight) {
        				maxLabelGlyphHeight = (int)bounds.getHeight();
        			}
        		}
        	}
        }
        else {
        	List<Category> catList = (List<Category>)sampleDimensionAndLabels.getSampleDimension().getCategories();
        	for (int n=0; n<catList.size(); n++) {
//    			GlyphVector glyphs = g.getFont().createGlyphVector(fc, WCTUtils.DECFMT_0D0000.format(catList.get(n).getRange().getMinimum()));
    			GlyphVector glyphs = g.getFont().createGlyphVector(fc, sampleDimensionAndLabels.getLabels()[n]);
    			Rectangle2D bounds = glyphs.getVisualBounds();
    			if (bounds.getHeight() > maxLabelGlyphHeight) {
    				maxLabelGlyphHeight = (int)bounds.getHeight();
    			}
        	}
        }
        
        maxLabelGlyphHeight /= labelEveryOtherN;

        
        
        
        
        
        
        
        
        
        
        
        if (sampleDimensionAndLabels != null) {
        	
        	List<Category> catList = sampleDimensionAndLabels.getSampleDimension().getCategories();
  
        	
        	
//        	for (int n=0; n<catList.size(); n++) {
//        		Category cat = catList.get(n);
//    			String label = sampleDimensionAndLabels.getLabels()[n];

//    			System.out.println("Cat: "+cat+"   label: "+label);
//        	}
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	boolean flip = false;
        	if (catList.get(0).getRange().getMinimum() < 
        		catList.get(1).getRange().getMinimum()) {
        		
        		flip = true;
        	}
        	
        	
        	
        	// flip list if needed, and remove NO DATA from list
        	ArrayList<Category> newCatList = new ArrayList<Category>();
        	for (int n=0; n<catList.size(); n++) {
        		Category cat = null;
        		if (flip) {
        			cat = catList.get(catList.size()-n-1);
        		}
        		else {
        			cat = catList.get(n);
        		}
        		if (cat.getColors().length > 1) {
        			newCatList.add(cat);
        		}
        	}
        	catList = newCatList;
        	
        	
    		colorMapArray = new SimpleColorMap[catList.size()];
    		for (int n=0; n<colorMapArray.length; n++) {
    			if (catList.get(n).getColors().length == 2) {
    				colorMapArray[n] = new SimpleColorMap(0, maxLabelGlyphHeight+colorMapSpacing, 
    					catList.get(n).getColors()[0], catList.get(n).getColors()[1]);
    			}
    			else if (catList.get(n).getColors().length == 1) {
    				colorMapArray[n] = new SimpleColorMap(0, maxLabelGlyphHeight+colorMapSpacing, 
    					catList.get(n).getColors()[0], catList.get(n).getColors()[0]);
    			}
    		}
    		

    		ArrayList<Color> supColorList = new ArrayList<Color>();
    		ArrayList<String> supLabelList = new ArrayList<String>();
        	for (int n=0; n<catList.size(); n++) {
    			if (catList.get(n).getName(null).startsWith("Unique:")) {
    				supColorList.add(catList.get(n).getColors()[0]);
    				String s = catList.get(n).getName(null);
    				supLabelList.add(s.substring("Unique:".length()));
    			}
        	}
    		supplementalCategoryColors = supColorList.toArray(new Color[supColorList.size()]);
    		supplementalCategoryLabels = supLabelList.toArray(new String[supLabelList.size()]);
        	
        	

        	for (int n=0; n<catList.size(); n++) {
//        		String label = WCTUtils.DECFMT_0D00.format(catList.get(n).getRange().getMinimum());
//    			String label = sampleDimensionAndLabels.getLabels()[n];
//    			String label = catList.get(n).getName(null);
    			String label = catList.get(n).getName(null).split("\\:")[0];
    			GlyphVector glyphs = g.getFont().createGlyphVector(fc, label);

				// don't draw fully transparent categories
//    			System.out.println(catList.get(n).getColors()[1].getAlpha());
//    			System.out.println(catList.get(n).getColors()[1]);
    			
    			
    			
//    			System.out.println(catList.get(n).getColors()[0] + "    "+ sampleDimensionAndLabels.getLabels()[n]);
    			
    			
    			
    			
    			if (catList.get(n).getColors().length == 1 ||
        			catList.get(n).getName(null).startsWith("Unique:")) {
    				continue;
    			}
    			
    			
    			
    			
				if (catList.get(n).getColors().length > 1 &&
						catList.get(n).getColors()[1].getAlpha() > 0) {				

					for (int i=0; i<(maxLabelGlyphHeight+colorMapSpacing); i++) {
						if (n < colorMapArray.length) {

							int yPixel = (int)cursor+i+colorMapSpacing*2;

							g.setColor(colorMapArray[n].getColor(i));
							g.drawLine(2*marginLeft, yPixel, 2*marginLeft+smallColorBoxWidth, yPixel);

						}
					}				
				}
//				else if (catList.get(n).getColors().length == 1 &&
//						catList.get(n).getColors()[0].getAlpha() > 0) {
//					
//					g.setColor(catList.get(n).getColors()[0]);
//					for (int i=0; i<(maxLabelGlyphHeight+colorMapSpacing); i++) {
//						int yPixel = (int)cursor+i+colorMapSpacing*2;
//						g.drawLine(2*marginLeft, yPixel, 2*marginLeft+smallColorBoxWidth, yPixel);
//					}
//				}

				
				
				
				
    			if (n % labelEveryOtherN == 0) {
    				
    				System.out.println("drawing: "+glyphs);
    				
    				
    				float yPixel = cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+colorMapSpacing*2;
    				g.setColor(foregroundColor);
    				g.drawLine(2*marginLeft, (int)yPixel-((maxLabelGlyphHeight+colorMapSpacing)/2), 
    						2*marginLeft+smallColorBoxWidth, (int)yPixel-((maxLabelGlyphHeight+colorMapSpacing)/2));
    				g.drawGlyphVector(glyphs, 2*marginLeft+smallColorBoxWidth+15, yPixel-colorMapSpacing/2);
    			}

    			cursor += maxLabelGlyphHeight+colorMapSpacing;
        	}
        	
        	if ((catList.size()-supplementalCategoryColors.length) > 1) {

        	// draw last label
//			String label = sampleDimensionAndLabels.getLabels()[catList.size()-1];
			String label = catList.get(catList.size()-1).getName(null).split("\\:")[1];
////			label = "last";
			GlyphVector glyphs = g.getFont().createGlyphVector(fc, label);
			
			if ((catList.size()-1) % labelEveryOtherN == 0) {
//				float yPixel = cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+colorMapSpacing*2;
//				g.setColor(foregroundColor);
////				g.drawLine(2*marginLeft, (int)yPixel, 2*marginLeft+smallColorBoxWidth, (int)yPixel);
				g.drawGlyphVector(glyphs, 2*marginLeft+smallColorBoxWidth+15, 
						cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+(int)(colorMapSpacing*1.5));
			}
        	
			
			
			
        	
        	// draw border around color map
    		g.setColor(foregroundColor);
    		g.drawRect(2*marginLeft, colorMapStartCursor+colorMapSpacing*2, smallColorBoxWidth, 
    				(maxLabelGlyphHeight+colorMapSpacing)*(catList.size()-supplementalCategoryColors.length));

        	}
        }
        else {

        	
        	
        	
        	
        	
        	
        	
        	if (interpolateBetweenCategories) {            
        		colorMapArray = new SimpleColorMap[categoryColors.length-1];
        		for (int n=0; n<categoryColors.length-1; n++) {
        			colorMapArray[n] = new SimpleColorMap(0, maxLabelGlyphHeight+colorMapSpacing, categoryColors[n], categoryColors[n+1]);
        		}
        	}

        	//        System.out.println("maxGlyphHeight="+maxGlyphHeight+" colorMapSpacing="+colorMapSpacing);

        	for (int n=0; n<categoryColors.length; n++) {
        		if (categoryColors[n] != null &&
        				categoryLabels[n] != null &&
        				categoryLabels[n].trim().length() > 0) {

        			GlyphVector glyphs = g.getFont().createGlyphVector(fc, categoryLabels[n]);



        			if (interpolateBetweenCategories) {
        				for (int i=0; i<(maxLabelGlyphHeight+colorMapSpacing); i++) {
        					if (n < colorMapArray.length) {
        						int yPixel = (int)cursor+i+colorMapSpacing*2;

        						g.setColor(colorMapArray[n].getColor(i));
        						g.drawLine(2*marginLeft, yPixel, 2*marginLeft+smallColorBoxWidth, yPixel);
        					}
        				}

        			}
        			else {

        				g.setColor(categoryColors[n]);
        				g.fillRect(2*marginLeft, (int)cursor+colorMapSpacing/2, smallColorBoxWidth, (int)maxLabelGlyphHeight+colorMapSpacing);

        				g.setColor(foregroundColor);
        				g.drawRect(2*marginLeft, (int)cursor+colorMapSpacing/2, smallColorBoxWidth, (int)maxLabelGlyphHeight+colorMapSpacing);

        			}

        			if (n % labelEveryOtherN == 0) {
        				float yPixel = cursor+((maxLabelGlyphHeight+colorMapSpacing)/2)+colorMapSpacing*2;
        				g.setColor(foregroundColor);
        				if (interpolateBetweenCategories) {
        					g.drawLine(2*marginLeft, (int)yPixel-((maxLabelGlyphHeight+colorMapSpacing)/2), 
        						2*marginLeft+smallColorBoxWidth, (int)yPixel-((maxLabelGlyphHeight+colorMapSpacing)/2));
        				}
        				g.drawGlyphVector(glyphs, 2*marginLeft+smallColorBoxWidth+15, (int)yPixel-colorMapSpacing/2);
        				
//        				g.drawLine(2*marginLeft, (int)yPixel-colorMapSpacing/2-maxLabelGlyphHeight+1, 
//        						2*marginLeft+smallColorBoxWidth+15, (int)yPixel-colorMapSpacing/2-maxLabelGlyphHeight+1);
//        				g.drawLine(2*marginLeft, (int)yPixel-colorMapSpacing/2+1, 
//        						2*marginLeft+smallColorBoxWidth+15, (int)yPixel-colorMapSpacing/2+1);
        				
        			}
        			
        			cursor += maxLabelGlyphHeight+colorMapSpacing;

        		}
        	}


        	if (interpolateBetweenCategories) {
        		g.setColor(foregroundColor);
        		g.drawRect(2*marginLeft, colorMapStartCursor+colorMapSpacing*2, smallColorBoxWidth, (maxLabelGlyphHeight+colorMapSpacing)*(categoryColors.length-1));
        		//            System.out.println(2*marginLeft+","+ colorMapSpacing*2+", "+ smallColorBoxWidth+","+ colorMapSpacing*categoryColors.length);
        	}

        }

        
        boolean addSpacer = true;
        if (sampleDimensionAndLabels != null) {        	
        	List<Category> catList = sampleDimensionAndLabels.getSampleDimension().getCategories();
        	if (catList.size() - supplementalCategoryColors.length < 2) {
        		addSpacer = false;
        	}
        }
        
        
        if (supplementalCategoryColors != null && supplementalCategoryLabels != null) {
        	if (addSpacer) {
        		cursor += 1*(maxLabelGlyphHeight+colorMapSpacing);
        	}
            for (int n=0; n<supplementalCategoryColors.length; n++) {
                if (supplementalCategoryLabels[n] != null) {
                    cursor += maxLabelGlyphHeight+colorMapSpacing;
                    GlyphVector glyphs = g.getFont().createGlyphVector(fc, supplementalCategoryLabels[n]);
                    g.setColor(foregroundColor);
                    g.drawGlyphVector(glyphs, 2*marginLeft+smallColorBoxWidth+15, cursor+maxLabelGlyphHeight+colorMapSpacing);

                    g.setColor(supplementalCategoryColors[n]);
                    g.fillRect(2*marginLeft, (int)cursor+colorMapSpacing/2, smallColorBoxWidth, (int)maxLabelGlyphHeight+colorMapSpacing);

                    g.setColor(foregroundColor);
                    g.drawRect(2*marginLeft, (int)cursor+colorMapSpacing/2, smallColorBoxWidth, (int)maxLabelGlyphHeight+colorMapSpacing);
                    
                }                
            }

        }

        return image;
    }



    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    









    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String[] getDataDescription() {
        return dataDescription;
    }

    public void setDataDescription(String[] dataDescription) {
        this.dataDescription = dataDescription;
    }

    public String getDateTimeInfo() {
        return dateTimeInfo;
    }

    public void setDateTimeInfo(String dateTimeInfo) {
        this.dateTimeInfo = dateTimeInfo;
    }

    public String[] getMainMetadata() {
        return mainMetadata;
    }

    public void setMainMetadata(String[] mainMetadata) {
        this.mainMetadata = mainMetadata;
    }

    public String[] getSpecialMetadata() {
        return specialMetadata;
    }

    public void setSpecialMetadata(String[] specialMetadata) {
        this.specialMetadata = specialMetadata;
    }

    public String[] getLegendTitle() {
        return legendTitle;
    }

    public void setLegendTitle(String[] legendTitle) {
        this.legendTitle = legendTitle;
    }

    public String[] getCategoryLabels() {
        return categoryLabels;
    }

    public void setCategoryLabels(String[] categoryLabels) {
        this.categoryLabels = categoryLabels;
    }

    public Color[] getCategoryColors() {
        return categoryColors;
    }

    public void setCategoryColors(Color[] categoryColors) {
        this.categoryColors = categoryColors;
    }

    public boolean isDrawColorMap() {
        return drawColorMap;
    }
    public void setDrawColorMap(boolean drawColorMap) {
        this.drawColorMap = drawColorMap;
    }


    public boolean isDrawColorMapTransparentBackgroundPattern() {
		return drawColorMapTransparentBackgroundPattern;
	}

	public void setDrawColorMapTransparentBackgroundPattern(
			boolean drawColorMapTransparentBackgroundPattern) {
		this.drawColorMapTransparentBackgroundPattern = drawColorMapTransparentBackgroundPattern;
	}

	public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public int getLabelEveryOtherN() {
        return labelEveryOtherN;
    }

    /**
     * Sets the number of categories to label.  Value of '1' (default), means label every category.
     * Value of '2' means every other category.  Value of '3' means every 3rd category, etc...
     * @param labelEveryOtherN
     */
    public void setLabelEveryOtherN(int labelEveryOtherN) {
        this.labelEveryOtherN = labelEveryOtherN;
    }


    public boolean isInterpolateBetweenCategories() {
        return interpolateBetweenCategories;
    }

    public void setInterpolateBetweenCategories(boolean interpolateBetweenCategories) {
        this.interpolateBetweenCategories = interpolateBetweenCategories;
    }

    public Color getMapBackgroundColor() {
        return mapBackgroundColor;
    }

    public void setMapBackgroundColor(Color mapBackgroundColor) {
        this.mapBackgroundColor = mapBackgroundColor;
    }
    public boolean isDrawBorder() {
        return drawBorder;
    }
    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
    }
    public boolean isDrawLabels() {
        return drawLabels;
    }
    public void setDrawLabels(boolean drawLabels) {
        this.drawLabels = drawLabels;
    }

    public void setSupplementalCategoryLabels(String[] supplementalCategoryLabels) {
        this.supplementalCategoryLabels = supplementalCategoryLabels;
    }
    public String[] getSupplementalCategoryLabels() {
        return supplementalCategoryLabels;
    }

    public void setSupplementalCategoryColors(Color[] supplementalCategoryColors) {
        this.supplementalCategoryColors = supplementalCategoryColors;
    }
    public Color[] getSupplementalCategoryColors() {
        return supplementalCategoryColors;
    }

    public void setLabelOverride(String labelOverride) {
        this.labelOverride = labelOverride;
    }

    public String getLabelOverride() {
        return labelOverride;
    }





	public void setSampleDimensionAndLabels(SampleDimensionAndLabels sampleDimensionAndLabels) {
		this.sampleDimensionAndLabels = sampleDimensionAndLabels;
	}
	
	public SampleDimensionAndLabels getSampleDimensionAndLabels() {
		return sampleDimensionAndLabels;
	}

}
