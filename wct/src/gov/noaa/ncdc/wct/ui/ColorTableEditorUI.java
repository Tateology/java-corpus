package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;

public class ColorTableEditorUI extends JDialog {

    private WCTViewer viewer = null;
    private JTextArea textArea = new JTextArea(25, 60);

    private boolean isProcessCanceled = false;

    public final static ImageIcon LOADING_ICON = new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif"));     

    public ColorTableEditorUI(WCTViewer viewer) {
        super(viewer, "Color Table Editor");
        this.viewer = viewer;
        createUI();
    }

    private void createUI() {

    	this.setLayout(new RiverLayout());
    	
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);

//        SampleDimension sd = viewer.getRadarGridCoverage().getSampleDimensions()[0];
        
        
        
        String palName = NexradSampleDimensionFactory.getDefaultPaletteName(viewer.getNexradHeader().getProductCode());
		BufferedReader br = null;
		try {
			URL url = ResourceUtils.getInstance().getJarResource(
					new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
					"/config/colormaps/"+palName, null);

			br = new BufferedReader(new InputStreamReader(url.openStream()));

			String str = null;
			while ( (str = br.readLine()) != null) {
				textArea.append(str+"\n");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

    
//		viewer.addRenderCompleteListener(new RenderCompleteListener() {
//			@Override
//			public void renderComplete() {
//				try {
//					update();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void renderProgress(int progressPercent) {
//			}
//		});
		
        
        
        
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					update();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WCTException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        });
        
        
        
        
        this.add(new JScrollPane(textArea), "p hfill vfill");
        this.add(updateButton, "p left");
    }
    
    
    
    public void update() throws Exception {
    	
		ColorsAndValues[] cavArray = 
			ColorLutReaders.parseWCTPal(new BufferedReader(new StringReader(textArea.getText())));
		
		SampleDimensionAndLabels sd = ColorLutReaders.convertToSampleDimensionAndLabels(
				cavArray[0], cavArray[1]);
	
		viewer.getRadarLegendImageProducer().setSampleDimensionAndLabels(sd);
		
		GridCoverage gc = viewer.getRadarGridCoverage();
        WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(gc.getRenderedImage());
        WritableRaster data = (WritableRaster)img.getData();

		viewer.setRadarGridCoverage(new GridCoverage(
				gc.getName(null), data, GeographicCoordinateSystem.WGS84, null, gc.getEnvelope(), 
				new SampleDimension[] { sd.getSampleDimension() }));
				
		viewer.getLargeLegendPanel().setLegendImage(viewer.getRadarLegendImageProducer());
		
		gc.dispose();

		
		
		
        String palName = NexradSampleDimensionFactory.getDefaultPaletteName(viewer.getNexradHeader().getProductCode());
        NexradSampleDimensionFactory.setPaletteOverride(palName, cavArray);
		
		
//		WCTGridCoverageSupport.setSampleDimensionAlpha(new SampleDimension[] { sd }, alpha);

    }
}
