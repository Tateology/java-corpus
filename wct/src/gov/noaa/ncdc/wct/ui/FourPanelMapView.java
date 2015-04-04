package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentDataType;
import gov.noaa.ncdc.wct.ui.WCTViewer.RenderCompleteListener;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.gc.GridCoverage;
import org.geotools.gui.swing.WCTStatusBar;
import org.geotools.gui.swing.event.ZoomChangeEvent;
import org.geotools.gui.swing.event.ZoomChangeListener;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.pt.CoordinatePoint;
import org.geotools.renderer.j2d.GeoMouseEvent;
import org.geotools.renderer.j2d.LegendPosition;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.RenderedLogo;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class FourPanelMapView extends JPanel {

	private WCTViewer viewer = null;
	
	enum TargetPanel { ALL, PANEL1, PANEL2, PANEL3, PANEL4 };
	
	private RenderedGridCoverage[] panelRGC = new RenderedGridCoverage[4];
	private GridCoverage[] panelGridCoverage = new GridCoverage[4];
	private final WCTMapPane[] mapPanes = new WCTMapPane[4];
	private final WCTStatusBar[] statusBars = new WCTStatusBar[4];
	private final RenderedLogo[] legend = new RenderedLogo[4];
	private final JButton[] selectPanelButtons = new JButton[4];
	private final Image[] legendImage = new Image[4];
	private FourPanelGlassPane glassPane = null;
	
	private final RefreshInfo[] lastRefreshInfo = new RefreshInfo[4];
	
	private TargetPanel currentTargetPanel = TargetPanel.PANEL1;
	private boolean isZoomLinked = true;
	
	private boolean isZoomLinkedToWCTViewer = false;
	int legendBottomInset = 10;
	
	private final WCTViewer[] panelViewers = new WCTViewer[4];
	
	
	
	
	PanelRefreshListener panelRefreshListener = new PanelRefreshListener();
	
	/**
	 * Not linked to WCTViewer zoom changes by default.
	 * @param viewer
	 */
	public FourPanelMapView(WCTViewer viewer) {
		this(viewer, false);
	}
	
	public FourPanelMapView(WCTViewer viewer, boolean isZoomLinkedToWCTViewer) {
		this.viewer = viewer;
		this.isZoomLinkedToWCTViewer = isZoomLinkedToWCTViewer;
		
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	public void launchInFrame() {
		
		WCTFrame frame = new WCTFrame("4 panel test");
		frame.getContentPane().add(this);
		
		frame.setSize(800, 800);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	
	private void init() throws Exception {
		
		
		this.glassPane = new FourPanelGlassPane(viewer);
		
		for (int n=0; n<mapPanes.length; n++) {
			mapPanes[n] = new WCTMapPane();
			statusBars[n] = new WCTStatusBar(mapPanes[n], viewer);
			
			
			
			
			
			
//			panelViewers[n] = new WCTViewer();
//			panelViewers[n].getContentPane().removeAll();
//			mapPanes[n] = panelViewers[n].getMapPane();
			
			mapPanes[n].setMapContext(viewer.getMapContext());
			mapPanes[n].setBackground(viewer.getMapPane().getBackground());
			mapPanes[n].setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
			mapPanes[n].setVisibleArea(viewer.getMapPane().getVisibleArea());
			mapPanes[n].setPreferredArea(viewer.getMapPane().getPreferredArea());
			

			
			
			
			
			mapPanes[n].addZoomChangeListener(new ZoomChangeListener() {
				
				@Override
				public void zoomChanged(ZoomChangeEvent event) {
					
					
//					for (int n=0; n<mapPanes.length; n++) {
//						if (n == getCurrentSelectedPanelIndex()) {
//					
//							viewer.getMapPaneZoomChange().zoomChanged(event);
//							System.out.println("trigger zoom extent change event in viewer");
//							viewer.setCurrentExtent(mapPanes[n].getVisibleArea());
//						}
//					}
					
					
					
					
					
					// update zoom in other 3 windows
					if (isZoomLinked) {
						for (int n=0; n<mapPanes.length; n++) {
							if (event.getSource() == mapPanes[n]) {
								for (int i=0; i<mapPanes.length; i++) {
									if (i !=n ) {
										
										try {
											mapPanes[i].setVisibleArea(mapPanes[n].getVisibleArea(), false);
//											JOptionPane.showMessageDialog(null, "stop");
											
											
										} catch (Exception e) {
											
											JOptionPane.showMessageDialog(null, e.getMessage());
											
											e.printStackTrace();
										}
										
									}
								}
							}
						}
					}
				}
			});
			
			
			
			mapPanes[n].addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent evt) {
					for (int n=0; n<mapPanes.length; n++) {
						if (evt.getSource() == mapPanes[n]) {

							refreshLegendImage(n);
						}
					}
					
//					JOptionPane.showMessageDialog(null, " "+mapPanes[0].getWidth()+", "+mapPanes[0].getHeight());
					System.out.println(mapPanes[0].getWidth());
					System.out.println(mapPanes[0].getHeight());
					viewer.getMapPane().setSize(mapPanes[0].getWidth()*2, mapPanes[0].getHeight()*2);
					
				}				
			});
		}


		
		
		mapPanes[0].addZoomChangeListener(new ZoomChangeListener() {
			
			private final int waitTime = 1000;

			TimerTask task = null;
			Timer timer = new Timer();
		        
			@Override
			public void zoomChanged(ZoomChangeEvent event) {
				
						if (task != null) {
							try {
								task.cancel();
							} catch (Exception ex) {
							}
						}
						task = new TimerTask() {
							public void run() {


								System.out.println("TIME TO EXECUTE ZOOM CHANGE EVENT!");
								viewer.getMapPane().setVisibleArea(mapPanes[0].getVisibleArea(), false);
								
								for (int i=0; i<4; i++)
									
									try {
										if (lastRefreshInfo[i] != null) {
											
											
											
											if (i == getCurrentSelectedPanelIndex()) {
//											refreshViewer(lastRefreshInfo[i]);
//											updatePanel(i);
											}
											
											
											
											
										}
										else {
											System.out.println("NO DATA LOADED FOR PANEL "+i);
										}
										
									} catch (Exception e) {
										e.printStackTrace();
									}

							}


						};
						
						timer.schedule(task , waitTime);
						
				}
			});
		
		
		
		
		
		
		
	    AttributeType[] CURSOR_ATTRIBUTES = {
	        AttributeTypeFactory.newAttributeType("geom", Geometry.class),
	        AttributeTypeFactory.newAttributeType("label1", String.class),
	        AttributeTypeFactory.newAttributeType("label2", String.class),
	        AttributeTypeFactory.newAttributeType("label3", String.class),
	    };	    
		GeometryFactory geoFactory = new GeometryFactory();
		final Coordinate coord = new Coordinate(-95, 30);
        FeatureType schema = FeatureTypeFactory.newFeatureType(CURSOR_ATTRIBUTES, "Cursor Attributes");
        Feature feature = schema.create(new Object[] {
                geoFactory.createPoint(coord),
                " ", " ", " "
        }, new Integer(0).toString());
		FeatureCollection fc = FeatureCollections.newCollection();
		fc.add(feature);
		
        StyleBuilder sb = new StyleBuilder();
        Style style = sb.createStyle();
        Color defaultColor = new Color(30, 144, 255);
        Mark mark1 = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.WHITE, Color.CYAN, 7);
        Graphic gr1 = sb.createGraphic(null, mark1, null);
        PointSymbolizer pntSymbolizer1 = sb.createPointSymbolizer(gr1);
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("Cursor Attributes", 
                new Symbolizer[] { pntSymbolizer1 }));
        final MapLayer cursorMapLayer = new DefaultMapLayer(fc, style);
        
//        viewer.getSt
        
        
//		mapPanes[1].getRenderer().addLayer(new DefaultMapLayer(fc, style));
		mapPanes[1].getMapContext().addLayer(cursorMapLayer);
		
		
		
		mapPanes[0].addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {
				glassPane.setVisible(true);
				cursorMapLayer.setVisible(true);
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
				glassPane.setVisible(false);
				cursorMapLayer.setVisible(false);
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		
		
		
//		final Component finalThis = this;
		mapPanes[0].addMouseMotionListener(new MouseMotionListener() {

			Point[] glassPanePoints = new Point[4];
			int[] xLocations = new int[4];
			int[] yLocations = new int[4];
			String[][] dataValues = new String[][] { 
				new String[] { "", "", "" },
				new String[] { "", "", "" },
				new String[] { "", "", "" },
				new String[] { "", "", "" },
			};
			
			Point2D p2d = new Point2D.Double();
			Point2D coordPoint2d = new Point2D.Double();
			double[] vals = new double[1];
			
			@Override
			public void mouseMoved(final MouseEvent e) {

			
				
		        if (e instanceof GeoMouseEvent) {
//		        	System.out.println(((GeoMouseEvent)e).getMapCoordinate(p2d));
		        	
//		        	coord.x = ((GeoMouseEvent)e).getMapCoordinate(p2d).getX();
//		        	coord.y = ((GeoMouseEvent)e).getMapCoordinate(p2d).getY();
		        	
		        	
//		        	coordPoint2d = ((GeoMouseEvent)e).getMapCoordinate(new Point2D.Double());
		        	
		        }
				
		        int mpX = e.getX();
				int mpY = e.getY();
				int paneWidth = mapPanes[0].getWidth();
				int paneHeight = mapPanes[0].getHeight();
		        
		        
		        double geoX = mapPanes[0].getVisibleArea().getX() * ((double)mpX/paneWidth) * mapPanes[0].getVisibleArea().getWidth();
		        double geoY = mapPanes[0].getVisibleArea().getY() * ((double)mpY/paneHeight) * mapPanes[0].getVisibleArea().getHeight();
		        coordPoint2d = new Point2D.Double(geoX, geoY);
		        
		        
//				System.out.println(mapPanes[0].getVisibleArea().getWidth());
				

				
//				if (mpX > mapPanes[0].getWidth() || mpX < 0 || 
//						mpY > mapPanes[0].getHeight() || mpY < 0) {
//					
//					System.out.println("outside the area!");
//					return;
//				}
				
				for (int i=0; i<4; i++) {					
//					Point mapPanePoint = SwingUtilities.convertPoint(finalThis, mpX, mpY, mapPanes[i]);
//					glassPanePoints[i] = SwingUtilities.convertPoint(mapPanes[i], (int)mapPanePoint.getX(), (int)mapPanePoint.getY(), viewer);
					glassPanePoints[i] = SwingUtilities.convertPoint(mapPanes[i], mpX, mpY, viewer);
					xLocations[i] = (int)glassPanePoints[i].getX();
					yLocations[i] = (int)glassPanePoints[i].getY();
					dataValues[i][0] = "pixel "+i+" "+mpX+" "+mpY;
					dataValues[i][1] = "coords "+WCTUtils.DECFMT_0D000.format(coord.x)+" "+
							WCTUtils.DECFMT_0D000.format(coord.y)+" ";
					
					if (panelGridCoverage[i] != null) {
						coordPoint2d.setLocation(coord.x, coord.y);
						 if (panelGridCoverage[i].getEnvelope().contains(new CoordinatePoint(coordPoint2d))) {
							 vals = panelGridCoverage[i].evaluate(coordPoint2d, vals);			        
							 dataValues[i][2] = "value "+i+" "+vals[0]+" ";
							 
							 
							 System.out.println("value "+i+" "+vals[0]+" ");
						 }
						 else {

								dataValues[i][2] = "";
						 }
					}
					else {
						dataValues[i][2] = "";
					}
				}					

				glassPane.setCursorImitatorLocation(xLocations, yLocations);
				glassPane.setDataValues(dataValues);
				glassPane.repaint();
				
				
				
				
				
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				
			}
		});
		
		viewer.setGlassPane(glassPane);
//		glassPane.setVisible(true);
		
		
		
		// useful if in separate panel
		if (isZoomLinkedToWCTViewer) {
			viewer.getMapPane().addZoomChangeListener(new ZoomChangeListener() {
				@Override
				public void zoomChanged(ZoomChangeEvent event) {
					for (int n=0; n<mapPanes.length; n++) {
						mapPanes[n].setVisibleArea(viewer.getMapPane().getVisibleArea(), false);
					}
				}
			});
		}

		
		viewer.addRenderCompleteListener(panelRefreshListener);
		
		
		
		JPanel panel = new JPanel(new GridLayout(2, 2));
		for (int n=0; n<mapPanes.length; n++) {
			panel.add(mapPanes[n]);
		}
		
		JPanel controlPanel = getControlPanel();

		
		
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);

		
		selectPanel(0);
		updatePanel();
		
		
		
//		JOptionPane.showMessageDialog(this, " "+mapPanes[0].getWidth()+", "+mapPanes[0].getHeight());
		viewer.getMapPane().setSize(mapPanes[0].getWidth(), mapPanes[0].getHeight());

	}
	
	
	public int getCurrentSelectedPanelIndex() {
		if (currentTargetPanel == TargetPanel.PANEL1) {
			return 0;
		}
		else if (currentTargetPanel == TargetPanel.PANEL2) {
			return 1;
		} 
		else if (currentTargetPanel == TargetPanel.PANEL3) {
			return 2;
		} 
		else if (currentTargetPanel == TargetPanel.PANEL4) {
			return 3;
		} 
		else {
			return -1;
		}
	}
	
	public void selectPanel(int index) {
		
//		viewer.getStatusBar().set
		
		mapPanes[index].setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.CYAN));
		for (int n=0; n<mapPanes.length; n++) {
			if (n != index) {
				mapPanes[n].setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
			}
		}
		
		for (int n=0; n<selectPanelButtons.length; n++) {
			if (n == index) {
				selectPanelButtons[n].setFont(selectPanelButtons[n].getFont().deriveFont(Font.BOLD));
			}
			else {
				selectPanelButtons[n].setFont(selectPanelButtons[n].getFont().deriveFont(Font.PLAIN));
			}
		}
		if (index == 0) {
			setCurrentTargetPanel(TargetPanel.PANEL1);
		}
		else if (index == 1) {
			setCurrentTargetPanel(TargetPanel.PANEL2);
		}
		else if (index == 2) {
			setCurrentTargetPanel(TargetPanel.PANEL3);
		}
		else if (index == 3) {
			setCurrentTargetPanel(TargetPanel.PANEL4);
		}
	}
	
	
	public JPanel getControlPanel() {
		JPanel controlPanel = new JPanel();

		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(Integer.parseInt(e.getActionCommand())-1);
			}
		};
		
		KeyListener keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_1) {
					selectPanel(0);
				}
				else if (e.getKeyCode() == KeyEvent.VK_2) {
					selectPanel(1);
				}
				else if (e.getKeyCode() == KeyEvent.VK_3) {
					selectPanel(2);
				}
				else if (e.getKeyCode() == KeyEvent.VK_4) {
					selectPanel(3);
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
			@Override
			public void keyTyped(KeyEvent e) {
			}
		};



		controlPanel.add(new JLabel("Select View Panel: "));
		for (int n=0; n<selectPanelButtons.length; n++) {
			selectPanelButtons[n] = new JButton(String.valueOf(n+1));
			selectPanelButtons[n].addActionListener(listener);
			selectPanelButtons[n].addKeyListener(keyListener);
			controlPanel.add(selectPanelButtons[n]);
		}
		controlPanel.addKeyListener(keyListener);
		for (int n=0; n<mapPanes.length; n++) {
			mapPanes[n].addKeyListener(keyListener);
		}
		
		return controlPanel;
	}
	
	
	
	
	/**
	 * Updates the currently selected panel to updated as specified by the method
	 * 'setCurrentTargetPanel'.
	 */
	public void updatePanel() {
		updatePanel(currentTargetPanel);
	}
	
	
	
	public void updatePanel(TargetPanel panel) {
		if (panel == TargetPanel.ALL) {
			for (int n=0; n<mapPanes.length; n++) {
				updatePanel(n);
			}
		}
		else if (panel == TargetPanel.PANEL1) {
			updatePanel(0);
		}
		else if (panel == TargetPanel.PANEL2) {
			updatePanel(1);
		}
		else if (panel == TargetPanel.PANEL3) {
			updatePanel(2);
		}
		else if (panel == TargetPanel.PANEL4) {
			updatePanel(3);
		}
	}
	
	
	public void updatePanel(int panelIndex) {
		
		
		try {
			lastRefreshInfo[panelIndex] = populateRefreshInfo(viewer);
			
			
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		}
		
		
		if (panelRGC[panelIndex] != null) {
			mapPanes[panelIndex].getRenderer().removeLayer(panelRGC[panelIndex]);
		}
		if (legend[panelIndex] != null) {
			mapPanes[panelIndex].getRenderer().removeLayer(legend[panelIndex]);
		}


        if (viewer.getCurrentDataType() == CurrentDataType.RADAR) {
        	panelGridCoverage[panelIndex] = viewer.getRadarGridCoverage();
    		panelRGC[panelIndex] = new RenderedGridCoverage(viewer.getRadarGridCoverage());
    		panelRGC[panelIndex].setZOrder(viewer.getRadarRenderedGridCoverage().getZOrder());	
        }
        else {
        	panelGridCoverage[panelIndex] = viewer.getGridSatelliteGridCoverage();
        	panelRGC[panelIndex] = new RenderedGridCoverage(viewer.getGridSatelliteRenderedGridCoverage().getGridCoverage());
        	panelRGC[panelIndex].setZOrder(viewer.getGridSatelliteRenderedGridCoverage().getZOrder());
        }
		
		mapPanes[panelIndex].getRenderer().addLayer(panelRGC[panelIndex]);
		
		
		
		CategoryLegendImageProducer viewerLegendImgProducer = viewer.getLastDecodedLegendImageProducer();
		
        if (viewer.getCurrentDataType() == CurrentDataType.RADAR) {

		Color oldBkColor = viewerLegendImgProducer.getBackgroundColor();
//		viewerLegendImgProducer.setBackgroundColor(new Color(0, 0, 0, 0));
		
		CategoryLegendImageProducer legendImgProducer = new CategoryLegendImageProducer();
		legendImgProducer.setBackgroundColor(viewerLegendImgProducer.getBackgroundColor());
		legendImgProducer.setInterpolateBetweenCategories(true);
		legendImgProducer.setCategoryColors(viewerLegendImgProducer.getCategoryColors());
		legendImgProducer.setCategoryLabels(viewerLegendImgProducer.getCategoryLabels());
		legendImgProducer.setSampleDimensionAndLabels(viewerLegendImgProducer.getSampleDimensionAndLabels());
		legendImgProducer.setBackgroundColor(new Color(0, 0, 0, 210));
		legendImgProducer.setForegroundColor(new Color(255, 255, 255));
//		legendImgProducer.setLegendTitle(new String[] { "Units: " });
		try {
			legendImgProducer.setLegendTitle(new String[] {viewerLegendImgProducer.getLegendTitle()[0].replaceAll("Legend: ", "")});
		} catch (Exception e) {
			e.printStackTrace();
		}
		legendImgProducer.setLabelEveryOtherN(1);
		legendImgProducer.setFont(viewerLegendImgProducer.getFont().deriveFont(10f));
		legendImgProducer.setDrawBorder(true);
		
		
		try {
			
//			image = legendImgProducer.createSmallVertLegendImage(new Dimension(120, mapPanes[panelIndex].getHeight()-50));
			legendImage[panelIndex] = legendImgProducer.createSmallVertLegendImage();
//			image = viewerLegendImgProducer.createSmallVertLegendImage(new Dimension(40, mapPanes[panelIndex].getHeight()-20));
			
			
			Image image = null;
			if (legendImage[panelIndex].getHeight(this) > 
				mapPanes[panelIndex].getSize().getHeight()-legendBottomInset) {
				
				double scale = (double)(mapPanes[panelIndex].getSize().getHeight()-(2*legendBottomInset)) /
								legendImage[panelIndex].getHeight(this);
				
				image = legendImage[panelIndex].getScaledInstance(
						(int)(legendImage[panelIndex].getWidth(this)*scale), 
						(int)(legendImage[panelIndex].getHeight(this)*scale), Image.SCALE_SMOOTH);
			}
			else {
				image = legendImage[panelIndex];
			}
			
			
			
			legend[panelIndex] = new RenderedLogo();
			legend[panelIndex].setInsets(new Insets(0, 0, legendBottomInset, legendImage[panelIndex].getWidth(this)-10));
			legend[panelIndex].setImage(image);

			
			legend[panelIndex].setPosition(LegendPosition.SOUTH_EAST);
			legend[panelIndex].setZOrder(500.1f);                    
			mapPanes[panelIndex].getRenderer().addLayer(legend[panelIndex]);
			
			
			System.out.println(legendImage[panelIndex].getWidth(this)+" ||||||| "+legendImage[panelIndex].getHeight(this));
			System.out.println(image.getWidth(this)+" ||||||| "+image.getHeight(this));
			System.out.println(mapPanes[panelIndex].getSize());
			
			
			
			
			
		} catch (WCTException e) {
			e.printStackTrace();
		}

		
		viewerLegendImgProducer.setBackgroundColor(oldBkColor);

        }
        else {
			try {
				viewerLegendImgProducer.setInterpolateBetweenCategories(true);
				Image image;
				legendImage[panelIndex] = viewerLegendImgProducer.createMediumLegendImage();
				double scale = 0.8;
				image = legendImage[panelIndex].getScaledInstance(
						(int)(legendImage[panelIndex].getWidth(this)*scale), 
						(int)(legendImage[panelIndex].getHeight(this)*scale), Image.SCALE_SMOOTH);
				
				
				legend[panelIndex] = new RenderedLogo();
//				legend[panelIndex].setInsets(new Insets(0, 0, legendBottomInset, legendImage[panelIndex].getWidth(this)-10));
				legend[panelIndex].setInsets(new Insets(0, 0, 15, image.getWidth(this)));
				legend[panelIndex].setImage(image);


				legend[panelIndex].setPosition(LegendPosition.SOUTH_EAST);
				legend[panelIndex].setZOrder(500.1f);                    
				mapPanes[panelIndex].getRenderer().addLayer(legend[panelIndex]);
			} catch (WCTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
		
	}
	
	public void refreshLegendImage() {
		for (int panelIndex = 0; panelIndex < 4; panelIndex++) {
			refreshLegendImage(panelIndex);
		}
	}
	
	public void refreshLegendImage(int panelIndex) {
		if (legendImage[panelIndex] == null) {
			return;
		}

		if (legendImage[panelIndex].getHeight(this) > 
			mapPanes[panelIndex].getSize().getHeight()-legendBottomInset) {
		
			double scale = (double)(mapPanes[panelIndex].getSize().getHeight()-(2*legendBottomInset)) /
						legendImage[panelIndex].getHeight(this);
		
			Image image = legendImage[panelIndex].getScaledInstance(
				(int)(legendImage[panelIndex].getWidth(this)*scale), 
				(int)(legendImage[panelIndex].getHeight(this)*scale), Image.SCALE_SMOOTH);
		

			legend[panelIndex].setImage(image);
			legend[panelIndex].repaint();
			mapPanes[panelIndex].repaint();
		}
	}
	
	
		


	public void setCurrentTargetPanel(TargetPanel currentTargetPanel) {
		this.currentTargetPanel = currentTargetPanel;
	}

	public TargetPanel getCurrentTargetPanel() {
		return currentTargetPanel;
	}


	public void setZoomLinked(boolean isZoomLinked) {
		this.isZoomLinked = isZoomLinked;
	}


	public boolean isZoomLinked() {
		return isZoomLinked;
	}
	
	
	
	
	
	
	
	
	public RefreshInfo populateRefreshInfo(WCTViewer viewer) throws IllegalAccessException, InstantiationException {
		
		URL url = viewer.getCurrentDataURL();

		RefreshInfo ri = new RefreshInfo();
		ri.setUrl(url);
		ri.setDataType(viewer.getCurrentDataType());

		
		if (viewer.getRadialRemappedRaster() != null &&
				viewer.getCurrentDataType() == CurrentDataType.RADAR) {

			int radialSweepIndex = viewer.getRadialRemappedRaster().getLastDecodedSweepIndex();
			String radialVariableName = viewer.getRadialRemappedRaster().getLastDecodedVariableName();
			ri.setRadialSweepIndex(radialSweepIndex);
			ri.setRadialVariableName(radialVariableName);
		}
		
		
		if (viewer.getGridDatasetRaster() != null &&
				viewer.getCurrentDataType() == CurrentDataType.GRIDDED) {
			
			int gridRuntimeIndex = -1;
			if (viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getRunTimeAxis() != null) {
				viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getRunTimeAxis().findTimeIndexFromDate(
					viewer.getGridDatasetRaster().getLastProcessedRuntime());
			}
			
			
			int gridTimeIndex = -1;
			if (viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getTimeAxis1D() != null) {
				viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getTimeAxis1D().findTimeIndexFromDate(
					viewer.getGridDatasetRaster().getLastProcessedDateTime());
			}
			
			int gridZIndex = -1;
			if (viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis() != null) {
				viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis().findCoordElement(
					viewer.getGridDatasetRaster().getLastProcessedHeight());
			}

			ri.setGridRuntimeIndex(gridRuntimeIndex);
			ri.setGridTimeIndex(gridTimeIndex);
			ri.setGridZIndex(gridZIndex);
		}
		
		return ri;
	}
	
	public void refreshViewer(RefreshInfo ri) throws IllegalAccessException, InstantiationException {
		
		if (ri == null) {
			System.out.println("No data currently loaded for panel ");
			return;
		}
		
//		viewer.removeRenderCompleteListener(panelRefreshListener);
		
		if (ri.getDataType() == CurrentDataType.GRIDDED) {
			viewer.getGridDatasetRaster().setTimeIndex(ri.gridTimeIndex);
			viewer.getGridDatasetRaster().setRuntimeIndex(ri.gridRuntimeIndex);
			viewer.getGridDatasetRaster().setZIndex(ri.gridZIndex);
		}
		else if (ri.getDataType() == CurrentDataType.RADAR) {
			viewer.getRadialRemappedRaster().setVariableName(ri.radialVariableName);
			viewer.getRadialRemappedRaster().setSweepIndex(ri.radialSweepIndex);
		}

//		viewer.setLoad
		viewer.loadFile(ri.getUrl());
		
		
//		viewer.addRenderCompleteListener(panelRefreshListener);
	}
	
	
	
	
	public class RefreshInfo {
		private URL url;
		private CurrentDataType dataType;
		private int radialSweepIndex;
		private String radialVariableName;
		private int gridRuntimeIndex;
		private int gridTimeIndex;
		private int gridZIndex;
		
		
		public URL getUrl() {
			return url;
		}
		public void setUrl(URL url) {
			this.url = url;
		}
		public int getRadialSweepIndex() {
			return radialSweepIndex;
		}
		public void setRadialSweepIndex(int radialSweepIndex) {
			this.radialSweepIndex = radialSweepIndex;
		}
		public String getRadialVariableName() {
			return radialVariableName;
		}
		public void setRadialVariableName(String radialVariableName) {
			this.radialVariableName = radialVariableName;
		}
		public int getGridRuntimeIndex() {
			return gridRuntimeIndex;
		}
		public void setGridRuntimeIndex(int gridRuntimeIndex) {
			this.gridRuntimeIndex = gridRuntimeIndex;
		}
		public int getGridTimeIndex() {
			return gridTimeIndex;
		}
		public void setGridTimeIndex(int gridTimeIndex) {
			this.gridTimeIndex = gridTimeIndex;
		}
		public int getGridZIndex() {
			return gridZIndex;
		}
		public void setGridZIndex(int gridZIndex) {
			this.gridZIndex = gridZIndex;
		}
		public CurrentDataType getDataType() {
			return dataType;
		}
		public void setDataType(CurrentDataType dataType) {
			this.dataType = dataType;
		}
		
		
		public String toString() {
			return url+"\n  currentDataType: "+dataType+
					"\n  radialSweepIndex: "+radialSweepIndex+
					"\n  radialVariableName: "+radialVariableName+
					"\n  gridRuntimeIndex: "+gridRuntimeIndex+
					"\n  gridTimeIndex: "+gridTimeIndex+
					"\n  gridZIndex: "+gridZIndex;
		}
	}
	
	
	
	
	public class PanelRefreshListener implements RenderCompleteListener {
		
		@Override
		public void renderProgress(int progressPercent) {
		}
		@Override
		public void renderComplete() {
			updatePanel();
			

//			int currentSelectedPanelIndex = getCurrentSelectedPanelIndex();
			
			
			
			
			
			
			
			
			
//			for (int n=0; n<4; n++) {
//				try {
//
//					if (lastRefreshInfo[n] != null && n != currentSelectedPanelIndex) {
//						System.out.println("REFRESHING PANEL "+n+"::::::: "+lastRefreshInfo[n]);
//						JOptionPane.showMessageDialog(null, "REFRESHING PANEL "+n+"::::::: "+lastRefreshInfo[n]);
//						selectPanel(n);
//						refreshViewer(lastRefreshInfo[n]);
//					}
//				
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				} catch (InstantiationException e) {
//					e.printStackTrace();
//				}
//			}
//			selectPanel(currentSelectedPanelIndex);

		}
	}
	
	
	
	
	
	public class FourPanelGlassPane extends JPanel {
		
		private JFrame parentFrame;
		private Point point = new Point(); 
		  
		private int[] cursorImitatorX = new int[4];
		private int[] cursorImitatorY = new int[4];
		private String[][] dataValueString = new String[][] { 
				new String[] { "", "", "" },
				new String[] { "", "", "" },
				new String[] { "", "", "" },
				new String[] { "", "", "" },
			};;

		
		public FourPanelGlassPane(JFrame parentFrame) {
			super();
			this.parentFrame = parentFrame;
			
			setOpaque(false);
		}
		
		
		
		public void setCursorImitatorLocation(int[] x, int[] y) {
			for (int n=0; n<x.length; n++) {
				this.cursorImitatorX[n] = x[n];
				this.cursorImitatorY[n] = y[n];
			}
		}

		public void setDataValues(String[][] values) {
			for (int n=0; n<values.length; n++) {
				for (int i=0; i<values[n].length; i++) {
					this.dataValueString[n][i] = values[n][i];
				}
			}
		}
		
		
		public void paintComponent(Graphics g) {
			g.setColor(Color.CYAN);
			
			for (int n=0; n<cursorImitatorX.length; n++) {
				g.drawOval(cursorImitatorX[n]-10, cursorImitatorY[n]-10, 20, 20);
				g.fillOval(cursorImitatorX[n]-6, cursorImitatorY[n]-25, 4, 4);
				
			
				 
				((Graphics2D)g).setColor(Color.GREEN.darker()); 
				((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
				((Graphics2D)g).fillRoundRect(
						cursorImitatorX[n]+20, cursorImitatorY[n]-50, 
						150, 50, 5, 5);
				

				((Graphics2D)g).setColor(Color.WHITE); 
				((Graphics2D)g).drawString(dataValueString[n][0], cursorImitatorX[n]+20, cursorImitatorY[n]-50);
				((Graphics2D)g).drawString(dataValueString[n][1], cursorImitatorX[n]+20, cursorImitatorY[n]-20);
				((Graphics2D)g).drawString(dataValueString[n][2], cursorImitatorX[n]+20, cursorImitatorY[n]+10);
			}
			
			
			
			
			
		}

		public int[] getCursorImitatorX() {
			return cursorImitatorX;
		}

		public int[] getCursorImitatorY() {
			return cursorImitatorY;
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		// FROM:
		// http://weblogs.java.net/blog/alexfromsun/archive/2006/09/a_wellbehaved_g.html
		// http://download.java.net/javadesktop/blogs/alexfromsun/2006.09.20/FinalGlassPane.java.html
		
	    public void eventDispatched(AWTEvent event) { 
	        if (event instanceof MouseEvent) { 
	            MouseEvent me = (MouseEvent) event; 
	            if (!SwingUtilities.isDescendingFrom(me.getComponent(), parentFrame)) { 
	                return; 
	            } 
	            if (me.getID() == MouseEvent.MOUSE_EXITED && me.getComponent() == parentFrame) { 
	                point = null; 
	            } else { 
	                MouseEvent converted = SwingUtilities.convertMouseEvent(me.getComponent(), me, parentFrame.getGlassPane()); 
	                point = converted.getPoint(); 
	            } 
	            repaint(); 
	        } 
	    } 
	 
	    /** 
	     * If someone adds a mouseListener to the GlassPane or set a new cursor 
	     * we expect that he knows what he is doing 
	     * and return the super.contains(x, y) 
	     * otherwise we return false to respect the cursors 
	     * for the underneath components 
	     */ 
	    public boolean contains(int x, int y) { 
	        if (getMouseListeners().length == 0 && getMouseMotionListeners().length == 0 
	                && getMouseWheelListeners().length == 0 
	                && getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) { 
	            return false; 
	        } 
	        return super.contains(x, y); 
	    } 
	}
	
	
	
	
}
