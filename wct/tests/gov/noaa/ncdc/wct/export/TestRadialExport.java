package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class TestRadialExport {

    @Test
    public void testNexradL2Export() {
        try {

            WCTExport exporter = new WCTExport();
            exporter.setExportVariable("Reflectivity");
            exporter.setExportCut(0);
            exporter.setExportUseRF(false);
            exporter.setExportClassify(false);
            exporter.setExportPoints(false);
            exporter.setExportAllPoints(false);
            exporter.setExportReducePolys(false);
            
            exporter.setOutputFormat(ExportFormat.SHAPEFILE);

            URL nexradURL = new File("testdata\\KLIX20050829_135451.Z").toURI().toURL();
            File file = new File("E:\\work\\ndit-tests\\KLIX20050829_135451.shp");

            exporter.exportData(nexradURL, file);
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
