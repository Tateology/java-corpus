package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.WCTUtils;

import java.io.IOException;

import org.junit.Test;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.TypedDatasetFactory;

public class NcmlTest {

	@Test
	public void testNcmlRead() throws IOException {

		String source = "dods://localhost:18080/thredds/dodsC/cdr/gridsat/gridsat.ncml_dataset.ncml";
		
        StringBuilder errlog = new StringBuilder();
        GridDataset gds = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(FeatureType.GRID, source, WCTUtils.getSharedCancelTask(), errlog);
        if (gds == null) { 
            throw new IOException("Can't open Grid Dataset at location= "+source+"; error message= "+errlog);
        }
        
	}
}
