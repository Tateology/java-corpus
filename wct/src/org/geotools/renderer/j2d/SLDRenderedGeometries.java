/*
 * Geotools2 - OpenSource GIS mapping toolkit http://geotools.org
 * (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * File may contain documentation and or interfaces derived from Open Geospatial 
 * Consortium, Inc. (OGC) specifications. The work of the OGC is acknowledged.
 */
/*
 * SLDRenderedGeometries.java
 *
 * Created on 11 giugno 2003, 7.35
 */
package org.geotools.renderer.j2d;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.renderer.geom.GeometryCollection;
import org.geotools.renderer.style.GraphicStyle2D;
import org.geotools.renderer.style.LineStyle2D;
import org.geotools.renderer.style.MarkStyle2D;
import org.geotools.renderer.style.PolygonStyle2D;
import org.geotools.renderer.style.Style2D;
import org.geotools.renderer.style.TextStyle2D;
import org.geotools.resources.XMath;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.resources.geometry.XRectangle2D;
import org.opengis.referencing.operation.TransformException;


/**
 * A RenderedGeometries layer that can process styles (will use device space
 * coordinates for  style rendering)
 *
 * @author aaime
 */
public class SLDRenderedGeometries extends RenderedGeometries {
    private static AffineTransform IDENTITY_TRANSFORM = new AffineTransform();
    
    /** Observer for image loading */
    private static Canvas imgObserver = new Canvas();
    
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger(SLDRenderedGeometries.class.getName());

    /** The current map scale. */
    protected double currentScale;

    // steve ansari addition
    private ArrayList<XRectangle2D> rectList = new ArrayList<XRectangle2D>(10000);
    private int rectIndex = 0;
    private double[] origDisplacementVals;
    // end ansari addition
    
    /**
     * Construct a layer for the specified geometry.
     *
     * @param geometry The geometry, or <code>null</code> if none.
     *
     * @see #setGeometry
     */
    public SLDRenderedGeometries(final GeometryCollection geometry) {
        super(geometry);

        // this layer works directly with device space coordinates
        setRenderUsingMapCS(false);

        /*
         * Set a default "pixel" size in order to avoid automatic (and costly) resolution
         * computation. We assume that the geometry shape is close to an ellipse, i.e. we
         * estimate the perimeter using PI*sqrt(2*(a² + b²)) where a and b are semi-axis.
         */
        Rectangle2D bounds = geometry.getBounds2D();
        double size = (THICKNESS * 2.2214414690791831235079404950303) * XMath
            .hypot(bounds.getWidth(), bounds.getHeight());
        size /= geometry.getPointCount();
        setPreferredPixelSize(new XDimension2D.Double(size, size));
    }

    /**
     * Draw the geometry.
     *
     * @param context The set of transformations needed for transforming
     *        geographic coordinates
     *        (<var>longitude</var>,<var>latitude</var>) into pixels
     *        coordinates.
     *
     * @throws TransformException If a transformation failed.
     *
     * @task REVISIT: The scale computation done here is inacurate. It uses only the scale along
     *                X axis, fails under rotation, ignore the physical device size and do not
     *                comply to the usual definition of map scale (for example 1:10000 meaning
     *                "10000 meters in the real world = 1 meter on the screen").
     *                A more accurate scale would be Renderer.getScale().
     */
    protected void paint(final RenderingContext context)
        throws TransformException {
        // Overridden to get the current scale even if rendering in device space coordinates.
        // Use only the scaleX since this is the behaviour of Java2DRendering 
        // (reference implementation)
        currentScale = context.getAffineTransform(context.mapCS, context.textCS)
                              .getScaleX();

        // steve ansari addition
//        SLDRenderedGeometriesLabelManager.registerObject(this);        
        rectList.clear();
        rectIndex = 0;
        
        origDisplacementVals = null;
        // end ansari addition
        
        
        //        System.out.println("Current scale: " + currentScale);
        super.paint(context);
    }

    /**
     * Invoked automatically when a polyline is about to be draw. This
     * implementation paints the polyline according to the rendered style
     *
     * @param graphics The graphics in which to draw.
     * @param polyline The polyline to draw.
     * @param style The style to apply, or <code>null</code> if none.
     */
    protected void paint(final Graphics2D graphics, final Shape shape,
        final Style2D style) {
        if (style == null) {
            System.out.println("Null style!"); // TODO: what's going on? Should not be reached...

            return;
        }

        // Is the current scale within the style scale range? 
        if (!style.isScaleInRange(currentScale)) {
//            System.out.println("Out of scale");

            return;
        }

        if (style instanceof MarkStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            MarkStyle2D ms2d = (MarkStyle2D) style;
            Shape transformedShape = ms2d.getTransformedShape(coords[0],
                    coords[1]);

            if (transformedShape != null) {
                if (ms2d.getFill() != null) {
                    graphics.setPaint(ms2d.getFill());
                    graphics.setComposite(ms2d.getFillComposite());
                    graphics.fill(transformedShape);
                }

                if (ms2d.getContour() != null) {
                    graphics.setPaint(ms2d.getContour());
                    graphics.setStroke(ms2d.getStroke());
                    graphics.setComposite(ms2d.getContourComposite());
                    graphics.draw(transformedShape);
                }
            } 
        } else if (style instanceof GraphicStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);
            
            GraphicStyle2D gs2d = (GraphicStyle2D) style;
            
            renderImage(graphics, coords[0], coords[1], (Image) gs2d.getImage(), 
                        gs2d.getRotation(), gs2d.getOpacity());
        } else if (style instanceof TextStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            AffineTransform old = graphics.getTransform();
            AffineTransform temp = new AffineTransform(old);
            TextStyle2D ts2d = (TextStyle2D) style;
            GlyphVector textGv = ts2d.getTextGlyphVector(graphics);
            Rectangle2D bounds = textGv.getVisualBounds();
            

            // steve ansari addition - special for wct labels
            int declutterType = 0;  // 0 = full, 1 = simple, 2 = none
            if (Math.round(Math.toDegrees(ts2d.getRotation())) == 360) {
            	declutterType = 1;
            }
            if (Math.round(Math.toDegrees(ts2d.getRotation())) == 720) {
            	declutterType = 2;
            }
            
//           System.out.println(ts2d.getRotation()+"  "+declutterType+" "+Math.toDegrees(ts2d.getRotation()));
            if (declutterType == 0 || declutterType == 1) {

            	if (origDisplacementVals == null) {
            		origDisplacementVals = new double[] { ts2d.getDisplacementX(), ts2d.getDisplacementY() };
            	}           

            	ts2d.setDisplacementX(origDisplacementVals[0]);
            	ts2d.setDisplacementY(origDisplacementVals[1]);
            	XRectangle2D rect = XRectangle2D.createFromExtremums(coords[0]+ts2d.getDisplacementX(), 
            			coords[1]+ts2d.getDisplacementY(), coords[0]+bounds.getWidth(), coords[1]+bounds.getHeight());
            	
            	
//            	ArrayList<XRectangle2D> rectList = SLDRenderedGeometriesLabelManager.getRectList();
            	for (int n=0; n<rectList.size(); n++) {
            		if (rectList.get(n) != null && XRectangle2D.intersectInclusive(rect, rectList.get(n))) {
            			//            		System.out.println("found overlapping labels (num labels = "+rectList.size());

            			// check a second time with upper left location
            			if (declutterType == 0) {
            				rect = XRectangle2D.createFromExtremums(coords[0]-bounds.getWidth()-origDisplacementVals[0], 
            					coords[1]+ts2d.getDisplacementY(), coords[0], coords[1]+bounds.getHeight());
            				if (XRectangle2D.intersectInclusive(rect, rectList.get(n))) {
            					//                    	System.out.println("found overlapping labels again... (num labels = "+rectList.size());
            					return;
            				}
            				else {
            					// move from upper right to upper left
            					ts2d.setDisplacementX(-1*(bounds.getWidth()+origDisplacementVals[0]));
            					ts2d.setDisplacementY(origDisplacementVals[1]);
            					//                		System.out.println("setting left displacement  ");
            				}
            			}
            			else {
            				return;
            			}
            		}
            	}
//            	SLDRenderedGeometriesLabelManager.addRectangle(rect);
            	if (rectIndex >= rectList.size()) {
            		rectList.ensureCapacity(rectList.size()+1000);
            	}
            	rectList.add(rectIndex++, rect);
            	//            System.out.println(coords[0]+","+coords[1]);
            
            	
            }
            // end ansari addition

            
            
            
            
            temp.translate(coords[0], coords[1]);
            
            double x = 0;
            double y = 0;
            if (ts2d.isAbsoluteLineDisplacement()) {
                double offset = ts2d.getDisplacementY();
                
                if (offset > 0.0) { // to the left of the line
                    y = -offset;
                } else if(offset < 0) {
                    y = -offset + bounds.getHeight();
                } else {
                    y = bounds.getHeight() / 2;
                }
                x = -bounds.getWidth() / 2;
            } else {
                x = ts2d.getAnchorX() * (-bounds.getWidth()) + ts2d.getDisplacementX();
                y = ts2d.getAnchorY() * (bounds.getHeight()) + ts2d.getDisplacementY();
            }
            temp.rotate(ts2d.getRotation());
            temp.translate(x, y);
            

            graphics.setTransform(temp);
            if(ts2d.getHaloFill() != null) {
                float radious = ts2d.getHaloRadius();
                // graphics.translate(radious, -radious);
                graphics.setPaint(ts2d.getHaloFill());
                graphics.setComposite(ts2d.getHaloComposite());
                graphics.fill(ts2d.getHaloShape(graphics));
                // graphics.translate(radious, radious);
            }
            if(ts2d.getFill() != null) {
                graphics.setPaint(ts2d.getFill());
                graphics.setComposite(ts2d.getComposite());
                graphics.drawGlyphVector(textGv, 0, 0);
            }
            graphics.setTransform(old);
        } else {
            // if the style is a polygon one, process it even if the polyline is not
            // closed (by SLD specification)
            if (style instanceof PolygonStyle2D) {
                PolygonStyle2D ps2d = (PolygonStyle2D) style;

                if (ps2d.getFill() != null) {
                    Paint paint = ps2d.getFill();
                    if(paint instanceof TexturePaint) {
                        TexturePaint tp = (TexturePaint) paint;
                        BufferedImage image = tp.getImage();
                        Rectangle2D rect = tp.getAnchorRect();
                        AffineTransform at = graphics.getTransform();
                        double width = rect.getWidth() * at.getScaleX();
                        double height = rect.getHeight() * at.getScaleY();
                        Rectangle2D scaledRect = new Rectangle2D.Double(0, 0, width, height);
                        paint = new TexturePaint(image, scaledRect);
                    } 
                    graphics.setPaint(paint);
                    graphics.setComposite(ps2d.getFillComposite());
                    graphics.fill(shape);
                }
            }

            if (style instanceof LineStyle2D) {
                LineStyle2D ls2d = (LineStyle2D) style;

                if (ls2d.getStroke() != null) {
                    // see if a graphic stroke is to be used, the drawing method is completely
                    // different in this case
                    if(ls2d.getGraphicStroke() != null) {
                        drawWithGraphicsStroke(graphics, shape, ls2d.getGraphicStroke());
                    } else {
                        Paint paint  = ls2d.getContour();
                        if(paint instanceof TexturePaint) {
                            TexturePaint tp = (TexturePaint) paint;
                            BufferedImage image = tp.getImage();
                            Rectangle2D rect = tp.getAnchorRect();
                            AffineTransform at = graphics.getTransform();
                            double width = rect.getWidth() * at.getScaleX();
                            double height = rect.getHeight() * at.getScaleY();
                            Rectangle2D scaledRect = new Rectangle2D.Double(0, 0, width, height);
                            paint = new TexturePaint(image, scaledRect);
                        } 
                        graphics.setPaint(paint);
                        graphics.setStroke(ls2d.getStroke());
                        graphics.setComposite(ls2d.getContourComposite());
                        graphics.draw(shape);
                    }
                }
            }
        }
    }
    
    // draws the image along the path
    private void drawWithGraphicsStroke(Graphics2D graphics, Shape shape, BufferedImage image) {
        PathIterator pi = shape.getPathIterator(null, 10.0);
        double[] coords = new double[2];
        int type;
        
        // I suppose the image has been already scaled and its square
        int imageSize = image.getWidth();

        double[] first = new double[2];
        double[] previous = new double[2];
        type = pi.currentSegment(coords);
        first[0] = coords[0];
        first[1] = coords[1];
        previous[0] = coords[0];
        previous[1] = coords[1];

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("starting at " + first[0] + "," + first[1]);
        }

        pi.next();

        while (!pi.isDone()) {
            type = pi.currentSegment(coords);

            switch (type) {
            case PathIterator.SEG_MOVETO:

                // nothing to do?
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("moving to " + coords[0] + "," + coords[1]);
                }

                break;

            case PathIterator.SEG_CLOSE:

                // draw back to first from previous
                coords[0] = first[0];
                coords[1] = first[1];

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("closing from " + previous[0] + "," + previous[1] + " to "
                        + coords[0] + "," + coords[1]);
                }

            // no break here - fall through to next section
            case PathIterator.SEG_LINETO:

                // draw from previous to coords
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("drawing from " + previous[0] + "," + previous[1] + " to "
                        + coords[0] + "," + coords[1]);
                }

                double dx = coords[0] - previous[0];
                double dy = coords[1] - previous[1];
                double len = Math.sqrt((dx * dx) + (dy * dy)); // - imageWidth;

                double theta = Math.atan2(dx, dy);
                dx = (Math.sin(theta) * imageSize);
                dy = (Math.cos(theta) * imageSize);

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("dx = " + dx + " dy " + dy + " step = "
                        + Math.sqrt((dx * dx) + (dy * dy)));
                }

                double rotation = -(theta - (Math.PI / 2d));
                double x = previous[0] + (dx / 2.0);
                double y = previous[1] + (dy / 2.0);

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("len =" + len + " imageSize " + imageSize);
                }

                double dist = 0;

                for (dist = 0; dist < (len - imageSize); dist += imageSize) {
                    /*graphic.drawImage(image2,(int)x-midx,(int)y-midy,null); */
                    renderImage(graphics, x, y, image, rotation, 1);

                    x += dx;
                    y += dy;
                }

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("loop end dist " + dist + " len " + len + " " + (len - dist));
                }

                double remainder = len - dist;
                int remainingWidth = (int) remainder;
                if (remainingWidth > 0) {

                    //clip and render image
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("about to use clipped image " + remainder);
                    }

                    BufferedImage img = new BufferedImage(remainingWidth, imageSize, image.getType());
                    Graphics2D ig = img.createGraphics();
                    ig.drawImage(image, 0, 0, imgObserver);

                    renderImage(graphics, x, y, img, rotation, 1);
                }

                break;

            default:
                LOGGER.warning("default branch reached in drawWithGraphicStroke");
            }

            previous[0] = coords[0];
            previous[1] = coords[1];
            pi.next();
        }
    }
    
    /**
     * Renders an image on the device
     *
     * @param tx the image location on the screen, x coordinate
     * @param ty the image location on the screen, y coordinate
     * @param img the image
     * @param rotation the image rotatation
     */
    private void renderImage(Graphics2D graphics, double x, double y, Image image, double rotation, float opacity) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("drawing Image @" + x + "," + y);
        }

        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Point2D mapCentre = new java.awt.geom.Point2D.Double(x, y);
        Point2D graphicCentre = new java.awt.geom.Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        markAT.rotate(rotation);
        graphics.setTransform(markAT);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        // we moved the origin to the centre of the image.
        graphics.drawImage(image, -image.getWidth(imgObserver) / 2, -image.getHeight(imgObserver) / 2, imgObserver);

        graphics.setTransform(temp);

        return;
    }
}
