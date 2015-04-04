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
package org.geotools.gui.swing;

// J2SE dependencies
import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.nexradiv.UnitsChooser;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.ui.Viewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.IllegalComponentStateException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.geotools.ct.MathTransform;
import org.geotools.cv.SampleDimension;
import org.geotools.measure.AngleFormat;
import org.geotools.pt.CoordinatePoint;
import org.geotools.renderer.j2d.GeoMouseEvent;
import org.geotools.renderer.j2d.MouseCoordinateFormat;
import org.geotools.resources.XArray;
import org.geotools.resources.gui.ResourceKeys;
import org.geotools.resources.gui.Resources;

import ucar.units.ConversionException;
import ucar.units.Converter;
import ucar.units.Unit;
import ucar.units.UnitFormat;
import ucar.units.UnitFormatManager;



/**
 * A status bar. This status bar contains three parts: 1) an arbitrary message
 * 2) A progress bar and 3) The mouse coordinates.   The mouse coordinates are
 * automatically filled if this component is registered into a {@link MapPane}
 * as below:
 *
 * <blockquote><pre>
 * mapPane.addMouseMotionListener(statusBar);
 * </pre></blockquote>
 *
 * Coordinates can be formatted in any coordinate system, as long as a transform
 * exists from the {@linkplain MapPane#getCoordinateSystem map pane's coordinate
 * system} and the {@linkplain MouseCoordinateFormat#getCoordinateSystem format's
 * coordinate system}. If no transformation path is found, the coordinate will be
 * formatted as "ERROR". The status bar CS can be set to matches the map pane CS
 * as below:
 *
 * <blockquote><pre>
 * {@link #getCoordinateFormat}.setCoordinateSystem(mapPane.getCoordinateSystem());
 * </pre></blockquote>
 *
 * @version $Id: StatusBar.java,v 1.7 2003/11/12 14:14:25 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class WCTStatusBar extends JComponent implements MouseMotionListener {
    /**
     * Chaîne de caractères représentant un texte nul. Ce sera en général un
     * espace afin que l'étiquette conserve quand même une certaine hauteur.
     */
    private static final String NULL = " ";

    /**
     * Texte à afficher dans la barre d'état lorsqu'aucune opération n'est en cours.
     * S'il n'y a pas de texte à afficher, alors cette chaîne devrait être la constante
     * <code>StatusBar.NULL</code> plutôt que <code>null</code>.
     */
    private String text = NULL;

    /**
     * Composante dans lequel écrire des messages.
     */
    private final JLabel message = new JLabel(NULL);

    /**
     * Composante dans lequel écrire les coordonnées
     * pointées par le curseur de la souris.
     */
    private final JLabel coordinate = new JLabel(NULL);

    /**
     * The object to use for formatting coordinates.
     * Will be created only when first needed.
     */
    private MouseCoordinateFormat format;

    /**
     * The contextual menu for the "coordinate" area of status bar.
     * Will be created only when first needed.
     */
    private transient JPopupMenu coordinateMenu;

    /**
     * Progression d'une opération quelconque. Ce sera
     * souvent la progression de la lecture d'une image.
     */
    private final BoundedRangeModel progress;



    // Steve Ansari additions below --------------------------------------
    public final static double NEXRAD_SITE_UNDEFINED = -998.0; 
    public final static double NEXRAD_ELEVATION_UNDEFINED = -999.0; 

    private final JProgressBar progressBar;
    private NexradHeader nexradHeader;
    private Point2D coordpt;
    private double nexradElevationAngle = NEXRAD_ELEVATION_UNDEFINED;
    private double nexradElevationSin;
    private double nexradElevationCos;
    private WCTProjections wCTProjections = new WCTProjections();
    private MathTransform mathTransform;
    private CoordinatePoint outpnt;
    private DecimalFormat fmt2 = new DecimalFormat("0.00");
    private DecimalFormat fmt3 = new DecimalFormat("0.000");
    private double range, azimuth, height, range_in_nmi, work;
    private AngleFormat azimuthFormat;
    private UnitsChooser rangeUnitsChooser, zUnitsChooser;
    private String rangeUnits, zUnits;
    private String rangeUnitsAbbreviation = "nmi";
    private String zUnitsAbbreviation = "km";
    private UnitFormat unitFormat = UnitFormatManager.instance();
    private Unit meters, kilometers, miles, nauticalMiles, feet, kilofeet;
    private Converter rangeConverter, zConverter;
    private final JLabel azRanLabel = new JLabel(NULL);
    private final JLabel heightLabel = new JLabel(NULL);

    // -------------------------------------------------------------------    

    private Viewer viewer = null;
    private double[] elevAngles;
    private List<Integer> sweepsToUseList;
    private double rangeToLookInMeters;
    private double[] heightArrayToReuse;
    private int[] cappiInfoReturnArrayToReuse = new int[3];




    /**
     * Liste de numéros (<strong>en ordre croissant</code>) identifiant les objets
     * qui veulent écrire leur progression dans la barre des progrès. Chaque objet
     * {@link ProgressListener} a un numéro unique.  Le premier numéro de la liste
     * est celui de l'objet {@link ProgressListener} qui possède la barre des progrès.
     * On ne retient pas des références directes afin de ne pas nuire au travail du
     * ramasse-miettes.
     */
    private transient int[] progressQueue = new int[0]; // must be transient

    /**
     * Listen for {@link MouseListener#mouseExited} event. This is used
     * in order to erase the coordinates when the mouse exit the map pane.
     */
    private final MouseListener listener = new MouseAdapter() {
        public void mouseExited(final MouseEvent e) {
            setCoordinate(null);
            setAzRan(null);
            setZHeight(null);
        }
        public void mousePressed(final MouseEvent event) {
            if (event.isPopupTrigger()) {
                trigPopup(event);
            }
        }
        public void mouseReleased(final MouseEvent event) {
            if (event.isPopupTrigger()) {
                trigPopup(event);
            }
        }
    };

    /**
     * Construct a new status bar.
     */
    public WCTStatusBar(Viewer viewer) {
    	this.viewer = viewer;
    	
        final JProgressBar progress = new JProgressBar();
        config(message);
        config(coordinate);
        config(azRanLabel);
        config(heightLabel);


        setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        /*        
        c.gridy=0; c.fill=c.BOTH;
        c.gridx=0; c.weightx=1; add(message,    c);
        c.gridx=1; c.weightx=0; add(progress,   c);
        c.gridx=2;              add(coordinate, c);
        c.gridx=3;              add(azRanLabel, c);
        c.gridx=4;              add(heightLabel, c);
         */

        c.gridy=0; c.fill=c.BOTH;
        c.gridx=0; c.weightx=4; add(message,    c);
        c.gridx=1; c.weightx=3; add(progress,   c);
        c.gridx=2; c.weightx=2;              add(coordinate, c);
        c.gridx=3; c.weightx=1;              add(azRanLabel, c);
        c.gridx=4; c.weightx=0;              add(heightLabel, c);



        final Dimension size = message.getPreferredSize();
        size.width=75; message.setPreferredSize(size);
        final Dimension size1 = progress.getPreferredSize();
        size1.width=125; progress.setPreferredSize(size1);
        final Dimension size2 = coordinate.getPreferredSize();
        size2.width=200; coordinate.setPreferredSize(size2);
        final Dimension size3 = azRanLabel.getPreferredSize();
        size3.width=125; azRanLabel.setPreferredSize(size3);
        final Dimension size4 = heightLabel.getPreferredSize();
        size4.width=135; heightLabel.setPreferredSize(size4);


        progress.setBorder(BorderFactory.createLoweredBevelBorder());
        this.progress = progress.getModel();
        coordinate.addMouseListener(listener);
        azRanLabel.addMouseListener(listener);
        heightLabel.addMouseListener(listener);


        // Steve Ansari - assign progress bar
        this.progressBar = progress;
        setupUnitConversion();
    }

    /**
     * Construct a new status bar and register listeners.
     *
     * @param mapPane The map pane (usually a {@link MapPane} instance).
     */
    public WCTStatusBar(final Component mapPane, final Viewer viewer) {
        this(viewer);
        mapPane.addMouseListener(listener);
        mapPane.addMouseMotionListener(this);
    }

    /**
     * Configure la zone de texte spécifiée.
     */
    private static void config(final JLabel label)  {
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(0/*top*/, 6/*left*/, 0/*bottom*/,0/*right*/)));
    }

    /**
     * Registers a map pane for status bar management.  This will register listeners to track mouse
     * motion events occuring in the map pane area. Mouse locations will be formatted as geographic
     * coordinates according the current {@linkplain #getCoordinateFormat formatter} and {@linkplain
     * #setCoordinate written} in the status's bar coordinate area.
     *
     * @param mapPane The map pane (usually a {@link MapPane} instance).
     */
    public void registerMapPane(final Component mapPane) {
        mapPane.removeMouseMotionListener(this);
        mapPane.removeMouseListener(listener);
        mapPane.addMouseListener(listener);
        mapPane.addMouseMotionListener(this);
    }

    /**
     * Removes a map pane from status bar control.
     *
     * @param mapPane The map pane previously given to {@link #registerMapPane}.
     */
    public void unregisterMapPane(final Component mapPane) {
        mapPane.removeMouseMotionListener(this);
        mapPane.removeMouseListener(listener);
    }

    /**
     * Returns the text to display in the status bar. This is the text visible in the
     * message area, at the left. A <code>null</code> values means that no message are
     * currently displayed.
     */
    public String getText() {
        return (NULL.equals(text)) ? null : text;
    }

    /**
     * Set the text to display in the status bar. This is the
     * text to show in the message area, at the left side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     */
    public void setText(String text) {
        final String old = message.getText();
        if (text==null || text.length()==0) {
            text = NULL;
        }
        message.setText(this.text=text);
        firePropertyChange("text", old, text);
    }

    /**
     * Set the coordinate text to display in the status bar.
     * This is the text to show in the coordinate area, at the right side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     *
     * @task REVISIT: This method doesn't fire a 'PropertyChangeEvent' for
     *       performance reason (this method is invoked very often). Should we?
     */
    public void setCoordinate(String text) {
        if (text==null || text.length()==0) {
            text = NULL;
        }
        coordinate.setText(text);
        // Do not fire event for performance reason. Should we?
    }

    /**
     * Gets the coordinate text displayed in the status bar.
     * This is the text to show in the coordinate area, at the right side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     *
     * @task REVISIT: This method doesn't fire a 'PropertyChangeEvent' for
     *       performance reason (this method is invoked very often). Should we?
     */
    public String getCoordinate() {
        return coordinate.getText();
    }

    /**
     * Set the azimuth/range text to display in the status bar.
     * This is the text to show in the coordinate area, at the right side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     *
     * @task REVISIT: This method doesn't fire a 'PropertyChangeEvent' for
     *       performance reason (this method is invoked very often). Should we?
     */
    public void setAzRan(String text) {
        if (text==null || text.length()==0) {
            text = NULL;
        }
        azRanLabel.setText(text);
        // Do not fire event for performance reason. Should we?
    }

    /**
     * Set the beam height text to display in the status bar.
     * This is the text to show in the coordinate area, at the right side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     *
     * @task REVISIT: This method doesn't fire a 'PropertyChangeEvent' for
     *       performance reason (this method is invoked very often). Should we?
     */
    public void setZHeight(String text) {
        if (text==null || text.length()==0) {
            text = NULL;
        }
        heightLabel.setText(text);
        // Do not fire event for performance reason. Should we?
    }

    /**
     * Get the azimuth/range text to display in the status bar.
     * This is the text to show in the coordinate area, at the right side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     *
     * @task REVISIT: This method doesn't fire a 'PropertyChangeEvent' for
     *       performance reason (this method is invoked very often). Should we?
     */
    public String getAzRan() {
        return azRanLabel.getText();
    }

    /**
     * Get the beam height text to display in the status bar.
     * This is the text to show in the coordinate area, at the right side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     *
     * @task REVISIT: This method doesn't fire a 'PropertyChangeEvent' for
     *       performance reason (this method is invoked very often). Should we?
     */
    public String getZHeight() {
        return heightLabel.getText();
    }



    /**
     * Returns the format to use for formatting coordinates. The output coordinate
     * system can be se with <code>getCoordinateFormat().setCoordinateSystem(...)</code>.
     *
     * @see #setCoordinate(String)
     * @see MouseCoordinateFormat#setCoordinateSystem
     */
    public MouseCoordinateFormat getCoordinateFormat() {
        if (format == null) try {
            format = new MouseCoordinateFormat(getLocale());
        } catch (IllegalComponentStateException exception) {
            // The component doesn't have a parent.
            // Construct a format using the default locale.
            format = new MouseCoordinateFormat();
        }
        return format;
    }

    /**
     * ADDED BY STEVE ANSARI - Copied from setCoordinateFormat method
     * Returns the format to use for formatting coordinates. The output coordinate
     * system can be se with <code>getCoordinateFormat().setCoordinateSystem(...)</code>.
     *
     * @see #setCoordinate(String)
     * @see MouseCoordinateFormat#setCoordinateSystem
     */
    public AngleFormat getAzimuthCoordinateFormat() {
        if (azimuthFormat == null) try {
            azimuthFormat = new AngleFormat("D.dd°");
        } catch (IllegalComponentStateException exception) {
            // The component doesn't have a parent.
            // Construct a format using the default locale.
            azimuthFormat = new AngleFormat("D.dd°");
        }
        return azimuthFormat;
    }

    /**
     * Set the format to use for formatting coordinates.
     * A null value reset the default format.
     */
    public void setCoordinateFormat(final MouseCoordinateFormat format) {
        final MouseCoordinateFormat old = this.format;
        this.format = format;
        firePropertyChange("coordinateFormat", old, format);
    }

    /**
     * ADDED BY STEVE ANSARI - Copied from setCoordinateFormat method
     * Set the format to use for formatting coordinates.
     * A null value reset the default format.
     */
    public void setAzimuthCoordinateFormat(final AngleFormat format) {
        final Format old = this.azimuthFormat;
        this.azimuthFormat = format;
        firePropertyChange("azimuthCoordinateFormat", old, format);
    }

    private void setupUnitConversion() {
        try {
            meters = unitFormat.parse("meter");
            kilometers = unitFormat.parse("kilometer");
            miles = unitFormat.parse("mile");
            nauticalMiles = unitFormat.parse("nmi");
            feet = unitFormat.parse("feet");
            kilofeet = unitFormat.parse("kilofeet");
            setRangeUnits(rangeUnits, rangeUnitsAbbreviation);
            setZUnits(zUnits, zUnitsAbbreviation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setRangeUnits(String units, String unitsAbbreviation) throws ConversionException {
        this.rangeUnits = units;
        this.rangeUnitsAbbreviation = unitsAbbreviation;
        if (unitsAbbreviation.equals("mi")) {
            rangeConverter = meters.getConverterTo(miles);
        }
        else if (unitsAbbreviation.equals("nmi")) {
            rangeConverter = meters.getConverterTo(nauticalMiles);
        }
        else if (unitsAbbreviation.equals("km")) {
            rangeConverter = meters.getConverterTo(kilometers);
        }
        else if (unitsAbbreviation.equals("ft")) {
            rangeConverter = meters.getConverterTo(feet);
        }
        else if (unitsAbbreviation.equals("kft")) {
            rangeConverter = meters.getConverterTo(kilofeet);
        }
        else {
            rangeConverter = null;
        }
    }

    public void setZUnits(String units, String unitsAbbreviation) throws ConversionException {
        this.zUnits = units;
        this.zUnitsAbbreviation = unitsAbbreviation;
        if (unitsAbbreviation.equals("mi")) {
            zConverter = meters.getConverterTo(miles);
        }
        else if (unitsAbbreviation.equals("nmi")) {
            zConverter = meters.getConverterTo(nauticalMiles);
        }
        else if (unitsAbbreviation.equals("km")) {
            zConverter = meters.getConverterTo(kilometers);
        }
        else if (unitsAbbreviation.equals("ft")) {
            zConverter = meters.getConverterTo(feet);
        }
        else if (unitsAbbreviation.equals("kft")) {
            zConverter = meters.getConverterTo(kilofeet);
        }
        else {
            zConverter = null;
        }
    }


    /**
     * Sets the NexradHeader object that contains Radar lat/lon/elev info
     * needed to calculate the elevation of the mouse location. <br>
     * Added by Steve Ansari for Java NEXRAD Project.
     */
    public void setNexradHeader(NexradHeader header) {
        nexradHeader = header;         
        if (nexradHeader != null && nexradHeader.getLat() != NexradHeader.NO_SITE_DEFINED) {
            try {
                mathTransform = wCTProjections.getRadarTransform(header.getLon(), header.getLat()).inverse();
                
                sweepsToUseList = null;
                elevAngles = null;
                
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Set Elevation Angle of NEXRAD data in degrees
     */
    public void setNexradElevationAngle(double elevationAngle) {
        nexradElevationAngle = elevationAngle;
        nexradElevationSin = Math.sin(Math.toRadians(elevationAngle)); 
        nexradElevationCos = Math.cos(Math.toRadians(elevationAngle)); 
    }

    private String getNexradCursorAzRanString(GeoMouseEvent event) {
        if (nexradHeader != null) {
            return getNexradCursorAzRanString(event.getMapCoordinate(coordpt));
        }
        else {
            return "";
        }
    }

    private String getNexradCursorAzRanString(java.awt.geom.Point2D coordpt) {
        try {
            if (nexradHeader != null && nexradHeader.getLat() != NexradHeader.NO_SITE_DEFINED) {

                outpnt = mathTransform.transform(new CoordinatePoint(coordpt), outpnt);
                range = Math.sqrt(outpnt.ord[0]*outpnt.ord[0] + outpnt.ord[1]*outpnt.ord[1]); // in meters
                // convert to proper units
                work = (rangeConverter != null) ? rangeConverter.convert(range) : range;                 
                azimuth = Math.toDegrees(Math.atan(outpnt.ord[0]/outpnt.ord[1])); // reverse for meteorlogical coord system
                if (outpnt.ord[1] < 0) {
                    azimuth += 180;
                }
                if (outpnt.ord[1] > 0 && outpnt.ord[0] < 0) {
                    azimuth += 360;
                }
                return (azimuthFormat.format(azimuth)+" | "+fmt3.format(work)+" "+rangeUnitsAbbreviation);
            }
            else {
                return "";
            }
        } catch (Exception e) {
            return "ER1";
        }
    }


    private String getNexradCursorElevationString(GeoMouseEvent event) {
        if (nexradHeader != null) {
            return getNexradCursorElevationString(event.getMapCoordinate(coordpt));
        }
        else {
            return "";
        }
    }

    private String getNexradCursorElevationString(java.awt.geom.Point2D coordpt) {
        try {
            if (nexradHeader != null && nexradHeader.getLat() != NexradHeader.NO_SITE_DEFINED) {

                if (nexradHeader.getProductType() == NexradHeader.LEVEL2 || 
                		nexradHeader.getProductType() == NexradHeader.GENERIC_RADIAL) {
                	if (viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters() != null &&
                			! Double.isNaN(viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters()[0])) {
                		
                		// lazy init
                		if (sweepsToUseList == null) {
                    		sweepsToUseList = RadialDatasetSweepRemappedRaster.getSweepsToUseList( 
                            		viewer.getRadialRemappedRaster().getLastDecodedRadialVariable()
                            	);
                        	elevAngles = RadialDatasetSweepRemappedRaster.getElevAngles( 
                            		viewer.getRadialRemappedRaster().getLastDecodedRadialVariable()
                            	);
                        	
                        	double[] cappiHeightsInMeters = 
                        		viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters();
                        	
                			rangeToLookInMeters = Double.POSITIVE_INFINITY;
                			if (cappiHeightsInMeters.length > 1) {
                				rangeToLookInMeters = Math.abs(
                					cappiHeightsInMeters[cappiHeightsInMeters.length-1]-
                					cappiHeightsInMeters[cappiHeightsInMeters.length-2] );
                			}
                			heightArrayToReuse = new double[sweepsToUseList.size()];

                		}
                		
                		
                		int sweepIndex = RadialDatasetSweepRemappedRaster.getClosestSweepIndexAtHeight(
                				elevAngles, sweepsToUseList, range, 
                				viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters()[0],
                				rangeToLookInMeters, heightArrayToReuse, cappiInfoReturnArrayToReuse)[0];
                		if (sweepIndex == -1) {
                			return "z= No Data";
                		}
                		height = NexradEquations.getRelativeBeamHeight(elevAngles[sweepIndex], range);
                		
                        if (zConverter != null) {
                            height = zConverter.convert(height);
                        }
                        return ("z="+fmt3.format(height)+" "+zUnitsAbbreviation+" ("+fmt2.format(elevAngles[sweepIndex])+"°)");
                        
                	}
                }
            	
                if (nexradElevationAngle != NEXRAD_ELEVATION_UNDEFINED) {
                    height = NexradEquations.getRelativeBeamHeight(nexradElevationCos, nexradElevationSin, range);
//                    double height2 = NexradEquations.getRelativeBeamHeight2(nexradElevationCos, nexradElevationSin, range);
//                    double height3 = NexradEquations.getRelativeBeamHeight_old(nexradElevationCos, nexradElevationSin, range);

                    if (zConverter != null) {
                        height = zConverter.convert(height);
//                        height2 = zConverter.convert(height2);
//                        height3 = zConverter.convert(height3);
                    }                    

//                    progressBar.setString(fmt3.format(height)+" "+fmt3.format(height2)+" "+fmt3.format(height3));

                    return ("z="+fmt3.format(height)+" "+zUnitsAbbreviation);
                    //                    return ("z="+fmt3.format(height)+" "+fmt3.format(height2));
                }
                else {           
                    return ("");
                }
            }
            else {
                return "";
            }
        } catch (Exception e) {
        	e.printStackTrace();
            return "ER2";
        }
    }



    /**
     * Invoked when the mouse cursor has been moved onto a component. The default
     * implementation format the coordinate in the status bar coordinate area (at
     * the right side).
     */
    public void mouseMoved(final MouseEvent event) {
        if (event instanceof GeoMouseEvent) {
            if (format == null) {
                format = getCoordinateFormat();
                // Ansari addition - default to decimal degrees w/4 decimal places
                format.setAnglePattern("D.dddd°");
            }
            if (azimuthFormat == null) {
                azimuthFormat = getAzimuthCoordinateFormat();
            }

            
            String[] text = format.format((GeoMouseEvent) event).split("\\s+");
            String value = (text.length == 3) ? text[2] : "No Data";
            if (text.length == 3 && text[2].equals("(Untitled)")) {
            	value = "No Data";
            }
            else if (text.length == 3) {
            	value = value.substring(1, value.length()-1);
            }
            SampleDimension sd = viewer.getSampleDimension();
            if (sd != null && ! value.equals("No Data")) {
            	
            	String catName = sd.getCategory(Double.parseDouble(value)).getName(null);
            	if (catName.startsWith("Unique:")) {
            		setCoordinate(text[0]+" "+text[1]+"  ("+catName.substring(catName.indexOf(":")+1)+")");
            	}
            	else {
//                	setCoordinate(format.format((GeoMouseEvent) event));
            		setCoordinate(text[0]+" "+text[1]+"  ("+value+")");
            	}
            }
            else {
//            	setCoordinate(format.format((GeoMouseEvent) event));
        		setCoordinate(text[0]+" "+text[1]+"  ("+value+")");
            }
            
            
            setAzRan(getNexradCursorAzRanString((GeoMouseEvent) event));
            setZHeight(getNexradCursorElevationString((GeoMouseEvent) event));
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged. The default
     * implementation invokes <code>{@link #mouseMoved mouseMoved}(event)}</code> in order
     * to continue to format mouse's coordinate during the drag.
     */
    public void mouseDragged(final MouseEvent event) {
        mouseMoved(event);
    }

    /**
     * Invoked when a contextual menu has been trigged on a component.
     *
     * @param component The component on which the popup menu was trigged.
     */
    final void trigPopup(final MouseEvent event) {
        final Component component = event.getComponent();
        if (component == coordinate || component == azRanLabel ||
                component == heightLabel) {
        	
        	showPopup(component, event.getX(), event.getY());
        }
    }

    
    public void showPopup(Component parent, int xLocation, int yLocation) {
        if (coordinateMenu == null) {
            coordinateMenu = new JPopupMenu();
            final MenuListener listener = new MenuListener();
            JMenuItem item = coordinateMenu.add("Coordinate Format");
            item.addActionListener(listener);
            addPropertyChangeListener("coordinateFormat", listener);
            JMenuItem azitem = coordinateMenu.add("Azimuth Format");
            azitem.addActionListener(listener);
            addPropertyChangeListener("azimuthCoordinateFormat", listener);
            JMenuItem rangeitem = coordinateMenu.add("Range Units");
            rangeitem.addActionListener(listener);
            JMenuItem zitem = coordinateMenu.add("Height (Z) Units");
            zitem.addActionListener(listener);
            addPropertyChangeListener("heightFormat", listener);
        }
        coordinateMenu.show(parent, xLocation, yLocation);
    }
    
    
    /**
     * Listeners for the popup menus.
     */
    private final class MenuListener implements ActionListener, PropertyChangeListener {
        /**
         * The format chooser. Will be constructed only when first needed.
         */
        private transient FormatChooser chooser, azimuthChooser;

        /**
         * Invoked when the user select the "Format" menu item.
         */
        public void actionPerformed(final ActionEvent event) {
            JMenuItem source = (JMenuItem) event.getSource();
            if (source.getText().equals("Coordinate Format")) {
                if (chooser == null) {
                    chooser = new FormatChooser(getCoordinateFormat());
                }
                final Resources resources = Resources.getResources(getLocale());
                if (chooser.showDialog(WCTStatusBar.this,
                        resources.getString(ResourceKeys.COORDINATE_FORMAT)))
                {
                    setCoordinateFormat((MouseCoordinateFormat) chooser.getFormat());
                }
            }
            else if (source.getText().equals("Azimuth Format")) {
                if (azimuthChooser == null) {
                    azimuthChooser = new FormatChooser(getAzimuthCoordinateFormat());
                }
                final Resources resources = Resources.getResources(getLocale());
                if (azimuthChooser.showDialog(WCTStatusBar.this,
                        resources.getString(ResourceKeys.COORDINATE_FORMAT)))
                {
                    setAzimuthCoordinateFormat((AngleFormat) azimuthChooser.getFormat());
                }
            }
            else if (source.getText().equals("Range Units")) {
                try {
                    if (rangeUnitsChooser == null) {
                        rangeUnitsChooser = new UnitsChooser("Nautical Mi.");
                    }
                    if (rangeUnitsChooser.showDialog(WCTStatusBar.this, "Range Units Chooser")) {
                        setRangeUnits(rangeUnitsChooser.getUnits(), rangeUnitsChooser.getUnitsAbbreviation());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    javax.swing.JOptionPane.showMessageDialog(null, e.toString(), 
                            "RANGE UNITS EXCEPTION", javax.swing.JOptionPane.ERROR_MESSAGE);                   
                }
            }
            else if (source.getText().equals("Height (Z) Units")) {
                try {
                    if (zUnitsChooser == null) {
                        zUnitsChooser = new UnitsChooser();
                    }
                    if (zUnitsChooser.showDialog(WCTStatusBar.this, "Height (Z) Units Chooser")) {
                        setZUnits(zUnitsChooser.getUnits(), zUnitsChooser.getUnitsAbbreviation());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    javax.swing.JOptionPane.showMessageDialog(null, e.toString(), 
                            "HEIGHT (Z) UNITS EXCEPTION", javax.swing.JOptionPane.ERROR_MESSAGE);                   
                }
            }
        }

        /**
         * Invoked when a status bar property changed.
         */
        public void propertyChange(final PropertyChangeEvent event) {
            if (chooser != null) {
                chooser.setFormat(getCoordinateFormat());
            }
        }
    }






    public void resetProgress(int initialPercent) {
        progress.setRangeProperties(initialPercent, 1, 0, 100, false);
    }
    public void setProgress(int percent) {
        progress.setValue(percent);
    }
    public BoundedRangeModel getBoundedRangeModel() {
        return progress;
    }
    public void setProgressText(String text) {
        progressBar.setStringPainted(true);
        progressBar.setString(text);
    }























    /**
     * Returns a image I/O progress listener. This object can be used for updating the progress
     * bare in this status bar. This method can be invoked from any thread, and the progress
     * listener's methods can be invoked from any thread too (it doesn't need to be the
     * <i>Swing</i> thread).
     *
     * @param name The name of the image to be loaded. This name will appears
     *             in the status bar when the loading will start.
     */
    public IIOReadProgressListener getIIOReadProgressListener(final String name) {
        return new ProgressListener(Resources.getResources(getLocale()).
                getString(ResourceKeys.LOADING_$1, name));
    }




    /**
     * Classe chargée de réagir au progrès de la lecture.
     *
     * @version $Id: StatusBar.java,v 1.7 2003/11/12 14:14:25 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class ProgressListener implements IIOReadProgressListener, Runnable {
        /** No operation     */ private static final byte   NOP      = 0;
        /** Start loading    */ private static final byte   START    = 1;
        /** Loading progress */ private static final byte   PROGRESS = 2;
        /** End loading      */ private static final byte   END      = 3;

        /** Id number for {@link StatusBar#progressQueue}. */
        private int ID;

        /** Image name. */
        private final String name;

        /** Image name. */
        private String toWrite;

        /** Percent done. */
        private int percent;

        /** Operation to apply (@link #NOP}, {@link #START}, {@link #PROGRESS}, {@link #END}). */
        private byte operation = NOP;

        /** Ignored */ public void thumbnailStarted (ImageReader source, int im, int th) {}
        /** Ignored */ public void thumbnailProgress(ImageReader source, float percent ) {}
        /** Ignored */ public void thumbnailComplete(ImageReader source                ) {}
        /** Ignored */ public void sequenceStarted  (ImageReader source, int minIndex  ) {}
        /** Ignored */ public void sequenceComplete (ImageReader source                ) {}

        /** Setup the progress bar/ */
        public void imageStarted(ImageReader source, int imageIndex) {
            invokeLater(START, 0);
        }

        /** Update the progress bar. */
        public void imageProgress(ImageReader source, float percent) {
            invokeLater(PROGRESS, (int)percent);
        }

        /** Hide the progress bar. */
        public void imageComplete(ImageReader source) {
            invokeLater(END, 100);
        }

        /** Hide the progress bar. */
        public void readAborted(ImageReader source) {
            invokeLater(END, 0);
        }

        /**
         * Construit un objet chargé d'informer des progrès de la lecture d'une image.
         */
        protected ProgressListener(final String name) {
            this.name = name;
            toWrite   = name;
        }

        /**
         * Prépare une opération à exécuter dans le thread de <i>Swing</i>.
         * Cette opération sera décrite par le champ {@link #operation} et
         * consistera typiquement à initialiser la barre des progrès ou
         * afficher son pourcentage ({@link #percent}).
         *
         * @param nextOp  Code de l'opération ({@link #START}, {@link #PROGRESS} ou {@link #END}).
         * @param percent Pourcentage des progrès accomplis.
         */
        private void invokeLater(final byte nextOp, final int percent) {
            synchronized (progress) {
                final byte currentOp = this.operation;
                if (this.percent!=percent || currentOp!=nextOp) {
                    this.percent = percent;
                    switch (currentOp) {
                    case START: {
                        if (nextOp == END) {
                            this.operation = NOP;
                        }
                        // Sinon, on continue avec 'START'.
                        break;
                    }
                    case NOP: {
                        EventQueue.invokeLater(this);
                        // fall through
                    }
                    case PROGRESS: {
                        this.operation = nextOp;
                        break;
                    }
                    }
                }
            }
        }

        /**
         * Exécute une opération préparée par {@link #invokeLater}. Cette opération peut
         * constiter à initialiser la barre des progrès ({@link #START}), informer des
         * progrès accomplis ({@link #PROGRESS}) ou informer que la tâche est terminée
         * ({@link #END}). Cette méthode doit obligatoirement être appelée dans le thread
         * de <i>Swing</i>.
         */
        public void run() {
            synchronized (progress) {
                try {
                    switch (operation) {
                    /*
                     * Si on démarre la lecture d'une nouvelle image, tente de
                     * prendre possession de la barre d'état.  Si on n'est pas
                     * le premier à demander la possession de la barre d'état,
                     * cet objet 'ProgressListener' sera placé dans une liste
                     * d'attente.
                     */
                    case START: {
                        toWrite = name;
                        if (lock()) {
                            flush();
                            progress.setRangeProperties(percent, 1, 0, 100, false);
                        }
                        break;
                    }
                    /*
                     * Si la lecture de l'image a avancé, on écrira les progrès dans la barre d'état
                     * à la condition que cette barre d'état nous appartient. On écrira le nom de
                     * l'opération si ce n'était pas déjà fait (c'est le cas si on n'avait pas pu
                     * prendre possession de la barre d'état au moment ou START avait été exécuté).
                     */
                    case PROGRESS: {
                        if (hasLock()) {
                            flush();
                            progress.setValue(percent);
                        }
                        break;
                    }
                    /*
                     * A la fin de la lecture, relâche la barre d'état. Elle
                     * pourra être récupérée par d'autres 'ProgressListener'
                     * qui étaient dans la liste d'attente.
                     */
                    case END: {
                        if (hasLock()) {
                            progress.setRangeProperties(0, 1, 0, 100, false);
                            message.setText(text);
                        }
                        unlock();
                        break;
                    }
                    }
                } catch (RuntimeException exception) {
                    ExceptionMonitor.show(WCTStatusBar.this, exception);
                } finally {
                    operation = NOP;
                }
            }
        }

        /**
         * Ecrit dans la barre d'état la description de cet objet <code>ProgressListener</code>, si
         * ce n'était pas déjà fait.  Cette méthode ne doit être appelée que lorsque les conditions
         * suivantes ont été remplises:
         *
         * <ul>
         *   <li>Cette méthode est appelée dans le thread de Swing.</li>
         *   <li>Cette méthode est appelée dans un bloc synchronisé sur
         *       <code>StatusBar.progress</code>.</li>
         *   <li>La méthode {@link #lock} ou {@link #hasLock} a retourné <code>true</code>.</li>
         * </ul>
         */
        private void flush() {
            //            assert Thread.holdsLock(progress);
            //            assert EventQueue.isDispatchThread();
            if (toWrite != null) {
                message.setText(toWrite);
                toWrite = null;
            }
        }

        /**
         * Vérifie si cet objet <code>ProgressBar</code> possède la barre d'état. Cette
         * méthode ne doit être appelée que lorsque les conditions suivantes ont été remplises:
         *
         * <ul>
         *   <li>Cette méthode est appelée dans un bloc synchronisé sur
         *       <code>StatusBar.progress</code>.</li>
         * </ul>
         */
        private boolean hasLock() {
            //            assert Thread.holdsLock(progress);
            final int[] progressQueue = WCTStatusBar.this.progressQueue;
            return (progressQueue.length>=1 && progressQueue[0]==ID);
        }

        /**
         * tente de prendre possession de la barre d'état. Cette méthode retourne <code>true</code>
         * si elle a effectivement réussie à en prendre possession, ou <code>false</code> si elle
         * s'est placée dans une liste d'attente. Cette méthode ne doit être appelée que lorsque
         * les conditions suivantes ont été remplises:
         *
         * <ul>
         *   <li>Cette méthode est appelée dans un bloc synchronisé sur
         *       <code>StatusBar.progress</code>.</li>
         * </ul>
         */
        private boolean lock() {
            //            assert Thread.holdsLock(progress);
            final int index = Arrays.binarySearch(progressQueue, ID);
            if (index >= 0) {
                return index == 0;
            }
            final int length = progressQueue.length;
            if (length != 0) {
                ID = progressQueue[length-1]+1;
                if (ID <= 0) {
                    return false; // Too many ProgressListener
                }
                progressQueue = XArray.resize(progressQueue, length+1);
                progressQueue[length]=ID;
                return false;
            } else {
                progressQueue = new int[] {ID=1};
                return true;
            }
        }

        /**
         * Déclare que cet objet <code>ProgressBar</code> n'est plus intéressé
         * a posséder la barre d'état. Cette méthode ne doit être appelée que
         * lorsque les conditions suivantes ont été remplises:
         *
         * <ul>
         *   <li>Cette méthode est appelée dans un bloc synchronisé sur
         *       <code>StatusBar.progress</code>.</li>
         * </ul>
         */
        private void unlock() {
            //            assert Thread.holdsLock(progress);
            final int index = Arrays.binarySearch(progressQueue, ID);
            if (index >= 0) {
                progressQueue = XArray.remove(progressQueue, index, 1);
            }
            ID=0;
        }

        /**
         * Déclare que cet objet <code>ProgressListener</code>
         * n'est plus intéressé a posséder la barre d'état.
         */
        protected void finalize() throws Throwable {
            synchronized (progress) {
                unlock();
            }
            super.finalize();
        }
    }
}

