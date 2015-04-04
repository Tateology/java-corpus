package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.common.SwingWorker;
import gov.noaa.ncdc.nexradiv.SnapshotLayer;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.map.MapLayer;
import org.geotools.pt.CoordinatePoint;

import com.vividsolutions.jts.geom.Geometry;

public class IdentifyUI extends JDialog {

    private WCTViewer viewer = null;
    private JTextArea textArea = new JTextArea(20, 80);

    private ArrayList<ArrayList<Identification>> idenCollection = new ArrayList<ArrayList<Identification>>();
    
    private boolean isProcessCanceled = false;

    public final static ImageIcon LOADING_ICON = new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif"));     

    public IdentifyUI(WCTViewer viewer) {
        super(viewer, "Identification Results");
        this.viewer = viewer;
        createUI();
    }

    private void createUI() {

        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        Vector<MapLayer> mapLayers = viewer.getBaseMapLayers();
        Vector<String> listItems = new Vector<String>();
        listItems.add("Custom Markers");
        listItems.add("--------------");
        for (MapLayer mapLayer : mapLayers) {
            if (! mapLayer.getTitle().startsWith("_")) {
                listItems.add(mapLayer.getTitle());
            }
        }
        final JComboBox jcomboLocations = new JComboBox(listItems);
        final JButton jbProcessLocations = new JButton("Go");
        jbProcessLocations.setPreferredSize(new Dimension(65, jbProcessLocations.getPreferredSize().height));
        final JLabel jlLoadingIcon = new JLabel();
        final JTextField jtfMatches = new JTextField(10);
        jbProcessLocations.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jbProcessLocations.getText().equals("Cancel")) {
                    setIsProcessCanceled(true);
                    jbProcessLocations.setText("Go");
                    jlLoadingIcon.setIcon(null);
                    return;
                }
                
                SwingWorker worker = new SwingWorker() {
                    public Object construct() {
                        jbProcessLocations.setText("Cancel");
                        jlLoadingIcon.setIcon(LOADING_ICON);
                        processLocation(jcomboLocations.getSelectedItem().toString(), jtfMatches.getText());
                        jbProcessLocations.setText("Go");
                        jlLoadingIcon.setIcon(null);
                        return "Done";
                    }
                };
                worker.start();
            }
        });
        
        final JButton jbClear = new JButton("Clear");
        jbClear.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
                idenCollection.clear();
            }
        });
        
        JButton jbCopy = new JButton("Copy");
        jbCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TextUtils.getInstance().copyToClipboard(textArea.getText());
            }
        });
        
        JButton jbPrint = new JButton("Print");
        jbPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                try {
//                    TextUtils.getInstance().print("Identification Results", textArea.getText());
//                } catch (JetException e1) {
//                    e1.printStackTrace();
//                }
                try {
					TextUtils.getInstance().print(textArea);
				} catch (PrinterException e1) {
					e1.printStackTrace();
				}
            }
        });
        
        JButton jbSave = new JButton("Save");
        jbSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TextUtils.getInstance().save(getContentPane(), textArea.getText(), "txt", "Text File");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        
        
//        textArea.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//            }
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//            }
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                validateTree();
//            }
//        });
        
        
        JPanel panel = new JPanel(new RiverLayout());
        panel.add("center", new JLabel("Identify From Existing Locations: "));
        panel.add("br", jcomboLocations);
        panel.add(new JLabel("Matches: "));
        panel.add(jtfMatches);
        panel.add(jbProcessLocations);
        panel.add(jlLoadingIcon);
        panel.add(jbClear, "p");
        panel.add(jbCopy);
//        panel.add(jbPrint);  PRINT NOT WORKING - not worth time to debug, users can save or copy and print from something else
        panel.add(jbSave);
        
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        this.getContentPane().add(panel, BorderLayout.SOUTH);

//        validateTree();

    }

    private void setIsProcessCanceled(boolean isProcessCanceled) {
        this.isProcessCanceled = isProcessCanceled;
    }
    
    private void processLocation(String name) {
        processLocation(name, null);
    }
    private void processLocation(String name, String matches) {
        

        StringBuilder patternString = new StringBuilder();
        patternString.append(".*");
        if (matches != null) {
            for (int n=0; n<matches.length(); n++) {
                patternString.append("[").append(matches.substring(n, n+1).toLowerCase());
                patternString.append(matches.substring(n, n+1).toUpperCase()).append("]");
            }
            patternString.append(".*");
        }
//        Pattern regexPattern = Pattern.compile(patternString.toString());

        
        if (name.equals("Custom Markers")) {
            
            processFeatures(viewer.getMarkerFeatures(), name, matches);
            return;
        }
        
        for (MapLayer mapLayer : viewer.getBaseMapLayers()) {
            if (mapLayer.getTitle().equals(name)) {
                
                try {                                        
                    
                    FeatureCollection fc = mapLayer.getFeatureSource().getFeatures().collection();
                    processFeatures(fc, name, matches);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        }
        
    }
    
    public void processFeatures(FeatureCollection fc, String name, String matches) {

        FeatureIterator iter = fc.features();
        
//        System.out.println(mapLayer.getFeatureSource().getSchema());
        
        while (iter.hasNext()) {
            Feature f = iter.next();
            Geometry geom = f.getDefaultGeometry();
            String id = null;
            if (name.contains("Cities")) {
                id = f.getAttribute(1).toString() + " ["+f.getAttribute(2).toString()+"]";
            }
            else {
                id = f.getAttribute(1).toString();
            }
            
            if (isProcessCanceled) {
                isProcessCanceled = false;
                break;
            }
        
            
//            if (regexPattern.matcher(id).matches()) {
            if (id.toUpperCase().contains(matches.toUpperCase())) {
                identify(new Point2D.Double(geom.getCoordinate().x, geom.getCoordinate().y), id);
            }
        }
        
    }

    public void identify(Point2D center) {
        identify(center, null);
    }
    
    public void identify(Point2D center, String id) {

        final FileScanner scannedFile = viewer.getFileScanner();
        
        if (scannedFile == null || scannedFile.getLastScanResult() == null) {
            return;
        }
        
        
        ArrayList<Identification> idenList = new ArrayList<Identification>();

        String dataName = "";
        if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED ||
                scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
            for (String s : viewer.getLastDecodedLegendImageProducer().getLegendTitle()) {
                dataName += s + " ";
            }            
        }
        else {
            dataName = scannedFile.getLastScanResult().getLongName();
        }


        if (viewer.getGridSatelliteGridCoverage() != null && viewer.getGridSatelliteRenderedGridCoverage().isVisible()) {
            double[] results = null;
            if (viewer.getGridSatelliteGridCoverage().getEnvelope().contains(new CoordinatePoint(center))) {
                results = viewer.getGridSatelliteGridCoverage().evaluate(center, new double[1]);
            }
            else {
                results = new double[] { Double.NaN };
            }
            try {
                Identification iden = new Identification();

                if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
                    iden.setUnits(viewer.getGoesRaster().getUnits());
//                    System.out.println(dataName + " : "+Arrays.toString(results)+ " : " + viewer.getGoesRaster().getUnits());
                    iden.setResampled(true);
                }
                else {
                    iden.setUnits(viewer.getGridDatasetRaster().getUnits());
//                    System.out.println(dataName + " : "+Arrays.toString(results)+ " : " + viewer.getGridDatasetRaster().getUnits());
                    iden.setResampled(! viewer.getGridDatasetRaster().isNative());
                    
                    
//                    StringBuilder errlog = new StringBuilder();
//                    ucar.nc2.dt.GridDataset gds = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(
//                            FeatureType.GRID, viewer.getCurrentDataURL().toString(), WCTUtils.getEmptyCancelTask(), errlog);
//                    if (gds == null) { 
//                        throw new Exception("Can't open Grid Dataset at location= "+
//                                viewer.getCurrentDataURL()+"; error message= "+errlog);
//                    }
//                    if (gds.getGrids().size() == 0) { 
//                        throw new Exception("No Grids found in file.  Check CF-compliance for gridded data");
//                    }
//
//                    GridDatatype gd = gds.findGridDatatype(viewer.getGridDatasetRaster().getLastProcessedGridDatatype().getName());
//                    GridDatasetRemappedRaster gdr = viewer.getGridDatasetRaster();
//                    int[] cellIndices = gdr.getLastProcessedGridCoordSystem().findXYindexFromLatLon(center.getY(), center.getX(), new int[2]); 
//                    
////                    double val = gd.readDataSlice(gdr.getRuntimeIndex(), -1, gdr.getTimeIndex(), gdr.getZIndex(), cellIndices[1], cellIndices[0]).getDouble(0);
//                    double val = gd.readDataSlice(-1, -1, 0, -1, cellIndices[1], cellIndices[0]).getDouble(0);
//                    
//                    System.out.println(center.getY() + " , " + center.getX() + " , " + 
//                            gd.getName() + ": t="+gdr.getTimeIndex()+" rt="+gdr.getRuntimeIndex()+" z="+gdr.getZIndex()+" ::: value="+val);
//                    
//                    gds.close();
                    
                }
                iden.setValue(String.valueOf(results[0]));
                iden.setLocation(center);
                iden.setName(dataName);
                iden.setId(id);

                idenList.add(iden);

            } catch (Exception e) {
                e.printStackTrace();
            }
            //            }
        }

        if (viewer.getRadarGridCoverage() != null && viewer.getRadarRenderedGridCoverage().isVisible()) {
            double[] results = null;
            boolean withinCoverageBounds = true;
            if (viewer.getRadarGridCoverage().getEnvelope().contains(new CoordinatePoint(center))) {
                results = viewer.getRadarGridCoverage().evaluate(center, new double[1]);
            }
            else {
                results = new double[] { Double.NaN };
                withinCoverageBounds = false;                 
            }
            try {
                Identification iden = new Identification();

                if ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
                        scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) &&
                        viewer.getLevel3Header().getProductType() != NexradHeader.L3RADIAL_8BIT) {

                    if (withinCoverageBounds && (int)results[0] >= 0) {
                        iden.setValue(viewer.getLevel3Header().getDataThresholdStringArray()[(int)results[0]].replaceAll(" ", ""));
                    }
                    else {
                        iden.setValue("NaN");
                    }

//                    System.out.println(dataName + " : "+Arrays.toString(results)+ " : " + 
//                            iden.getValue() + " " + 
//                            viewer.getWCTRasterizer().getUnits());

                }
                else {

                    iden.setValue(String.valueOf(results[0]));

                    // override if lookup of value yields a unique category
                    SampleDimension sd = viewer.getSampleDimension();
                    if (sd != null && ! Double.isNaN(results[0])) {
                    	Category cat = sd.getCategory(results[0]);
                    	if (cat != null) {
                    		String catName = sd.getCategory(results[0]).getName(null);
                    		if (catName.startsWith("Unique:")) {
                    			iden.setValue(catName.substring(catName.indexOf(":")+1));
                    		}
                    	}
                    }
                    
//                    System.out.println(dataName + " : "+Arrays.toString(results)+ " : " + 
//                            results[0] + " " + viewer.getWCTRasterizer().getUnits());
                }
                iden.setUnits(viewer.getWCTRasterizer().getUnits());
                iden.setName(dataName);
                iden.setLocation(center);
                iden.setResampled(true);
                iden.setId(id);
                
                idenList.add(iden);


            } catch (Exception e) {
                e.printStackTrace();
            }
            //            }
        }



        for (SnapshotLayer l : viewer.getSnapshotLayers()) {
        	
        	if (! l.getRenderedGridCoverage().isVisible()) {
        		continue;
        	}
        	
            Identification iden = new Identification();

            double[] results = null;
            boolean withinCoverageBounds = true;
            if (l.getRenderedGridCoverage().getGridCoverage().getEnvelope().contains(new CoordinatePoint(center))) {
                results = l.getRenderedGridCoverage().getGridCoverage().evaluate(center, new double[1]);
            }
            else {
                results = new double[] { Double.NaN };
                withinCoverageBounds = false;
            }

            iden.setName(l.getName());
            iden.setUnits(l.getUnits());
            iden.setLocation(center);
            iden.setResampled(l.isResampled());
            iden.setId(id);

            if (withinCoverageBounds && (int)results[0] >= 0) {
                iden.setValue(((l.getCategoryToValueArray() != null) ? 
                        l.getCategoryToValueArray()[(int)results[0]].replaceAll(" ", "") : String.valueOf(results[0])));
            }
            else {
                iden.setValue("NaN");
            }

            
            idenList.add(iden);

//            System.out.println(l.getName() + " : "+Arrays.toString(results) + "  " +
//                    ((l.getCategoryToValueArray() != null) ? l.getCategoryToValueArray()[(int)results[0]] : "") +
//                    ((l.getUnits() != null) ? l.getUnits() : "") );
        }


        // 1. check for same layers in past identifications
        boolean layersChanged = false;
        for (int n=0; n<idenCollection.size(); n++) {
            for (int i=0; i<idenCollection.get(n).size(); i++) {
                if (idenCollection.get(n).size() != idenList.size()) {
                    layersChanged = true;
                }
                else if (! idenCollection.get(n).get(i).getName().equals(idenList.get(i).getName())) {
                    layersChanged = true;
                }
            }
        }
        if (layersChanged) {
            idenCollection.clear();
        }

        idenCollection.add(idenList);


        StringBuilder keySb = new StringBuilder();
        StringBuilder headerSb = new StringBuilder();
        StringBuilder dataSb = new StringBuilder();

        keySb.append("Column 1 = ID \nColumn 2 = Latitude\nColumn 3 = Longitude\n");
        for (int n=0; n<idenList.size(); n++) {
            keySb.append("Column "+(n+4)+" = Val "+(n+1)+" "+((idenList.get(n).isResampled()) ? "*** " : "") +
                    idenList.get(n).getName()+" ["+idenList.get(n).getUnits()+"]\n");
        }
        keySb.append("\n*** [Note: These values (marked with '***') are based on the resampled grid used for \n" +
        		"visualization. The value for a given lat/lon may change depending on the zoom extent and \n" +
        		"resulting resampled grid dimensions and cell size.] ***\n");


        headerSb.append("ID, Latitude, Longitude, ");
        for (int n=0; n<idenList.size(); n++) {
            if (n > 0) {
                headerSb.append(", ");
            }
//            headerSb.append(((idenList.get(n).isResampled()) ? "*** " : "") + 
//                    idenList.get(n).getName()+" ["+idenList.get(n).getUnits()+"]");
            headerSb.append(((idenList.get(n).isResampled()) ? "*** " : "") + 
                    "Val "+(n+1)+" ["+idenList.get(n).getUnits()+"]");
        }

        for (int n=0; n<idenCollection.size(); n++) {

            ArrayList<Identification> ilist = idenCollection.get(n);
            
            boolean printIt = false;
            for (int i=0; i<ilist.size(); i++) {
                if (ilist.get(i).getId() != null && ! ilist.get(i).getValue().equals("NaN")) {
                    printIt = true;
                }
                else if (ilist.get(i).getId() == null) {
                    printIt = true;
                }
            }


            if (printIt) {
                
                dataSb.append(((ilist.get(0).getId() == null) ? n : ilist.get(0).getId()) + ", ");
                dataSb.append(WCTUtils.DECFMT_0D0000.format(ilist.get(0).getLocation().getY())+", ");
                dataSb.append(WCTUtils.DECFMT_0D0000.format(ilist.get(0).getLocation().getX())+", ");

                for (int i=0; i<ilist.size(); i++) {
                    if (i > 0) {
                        dataSb.append(", ");
                    }
                    dataSb.append(ilist.get(i).getValue());
                }
                dataSb.append("\n");
            
            }
        }


        textArea.setText(keySb.toString()+"\n"+headerSb.toString()+"\n"+dataSb.toString());
    }





    class Identification {
        private String id;
        private String name;
        private String units;
        private String value;
        private Point2D location;
        private boolean isResampled;

        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public void setUnits(String units) {
            this.units = units;
        }
        public String getUnits() {
            return units;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public void setLocation(Point2D location) {
            this.location = location;
        }
        public Point2D getLocation() {
            return location;
        }
        public void setResampled(boolean isResampled) {
            this.isResampled = isResampled;
        }
        public boolean isResampled() {
            return isResampled;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getId() {
            return id;
        }
    }
}
