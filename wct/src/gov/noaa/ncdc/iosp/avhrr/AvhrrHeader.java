package gov.noaa.ncdc.iosp.avhrr;

import java.io.IOException;

public interface AvhrrHeader {
	
	public void readGeneralInformation(ucar.unidata.io.RandomAccessFile raf) throws IOException;
	
	public void readQualityIndicators(ucar.unidata.io.RandomAccessFile raf) throws IOException;
	
	public void readCalibration(ucar.unidata.io.RandomAccessFile raf) throws IOException;
	
	public void readRadianceConversion(ucar.unidata.io.RandomAccessFile raf) throws IOException;
	
	public void readNavigation(ucar.unidata.io.RandomAccessFile raf) throws IOException;
	
	public void readAnalogTelemetry(ucar.unidata.io.RandomAccessFile raf) throws IOException;	

}
