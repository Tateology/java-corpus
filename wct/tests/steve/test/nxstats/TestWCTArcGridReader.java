package steve.test.nxstats;

import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestWCTArcGridReader {

    
    @Test
    public void testReader() throws IOException, WCTExportNoDataException, WCTExportException {
        WCTRaster raster = WCTArcGridReader.read(new File("E:\\work\\nxstat\\KGSP20080628.tar.Z-herz.zip-max.asc"));
        WCTRasterExport export = new WCTRasterExport();
        export.saveAsciiGrid(new File("E:\\work\\nxstat\\KGSP20080628.tar.Z-herz.zip-max-2.asc"), raster);
    }
}
