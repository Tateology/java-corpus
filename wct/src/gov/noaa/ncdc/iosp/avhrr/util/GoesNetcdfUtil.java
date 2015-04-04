package gov.noaa.ncdc.iosp.avhrr.util;

import gov.noaa.ncdc.iosp.area.AreaCalibration;

import java.io.EOFException;
import java.io.IOException;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class GoesNetcdfUtil {
	

	public static Array getCountsData(NetcdfDataset ds) throws IOException,EOFException{
		Variable dataVar = ds.findVariable("data");
		Array da = dataVar.read();
		
		int[] blah = (int[])da.get1DJavaArray(int.class);
		int[] xy = dataVar.getShape();
		int pos = 0;
		ucar.ma2.Array dataArray = Array.factory(int.class, xy);
		Index dataIndex = dataArray.getIndex();
		for(int i=0;i<xy[1];i++){
			for(int j=0;j<xy[2];j++){
				dataArray.setInt(dataIndex.set(0,i,j),blah[pos]/32);
				pos++;
			}
		}
		return dataArray;
	}

	
	public static Array getCalibratedData(NetcdfDataset ds) throws IOException,EOFException{
		Variable sensorVar = ds.findVariable("sensorID");
		Array sa = sensorVar.read();
		int sensorId = Integer.parseInt(sa.toString().trim());
		Variable chanVar = ds.findVariable("bands");
		sa = chanVar.read();
		int chanNum = Integer.parseInt(sa.toString().trim()); 
		Variable widthVar = ds.findVariable("dataWidth");
		sa = widthVar.read();
		int datawidth = Integer.parseInt(sa.toString().trim()); 
		Variable dataVar = ds.findVariable("data");
		AreaCalibration ac = new AreaCalibration(sensorId,chanNum,datawidth);
		int[] xy = dataVar.getShape();
		Array a = dataVar.read();
		int[] blah = (int[])a.get1DJavaArray(int.class);
		float[] caldata = ac.calibrate(blah, 1);
		ucar.ma2.Array dataArray = Array.factory(float.class, xy);
		Index dataIndex = dataArray.getIndex();
		int pos = 0;
		if(caldata != null){
			for(int i=0;i<xy[1];i++){
				for(int j=0;j<xy[2];j++){
					dataArray.setFloat(dataIndex.set(0,i,j),caldata[pos]);
					pos++;
				}
			}
		}else{
			for(int i=0;i<xy[1];i++){
				for(int j=0;j<xy[2];j++){
					dataArray.setFloat(dataIndex.set(0,i,j),0);
					pos++;
				}
			}			
		}
		return dataArray;
	}
	

	
	public static Array getRadianceData(NetcdfDataset ds) throws IOException,EOFException{
		Variable sensorVar = ds.findVariable("sensorID");
		Array sa = sensorVar.read();
		int sensorId = Integer.parseInt(sa.toString().trim());
		Variable chanVar = ds.findVariable("bands");
		sa = chanVar.read();
		int chanNum = Integer.parseInt(sa.toString().trim()); 
		Variable widthVar = ds.findVariable("dataWidth");
		sa = widthVar.read();
		int datawidth = Integer.parseInt(sa.toString().trim()); 
		Variable dataVar = ds.findVariable("data");
		AreaCalibration ac = new AreaCalibration(sensorId,chanNum,datawidth);
		int[] xy = dataVar.getShape();
		Array a = dataVar.read();
		int[] blah = (int[])a.get1DJavaArray(int.class);
		float[] caldata = ac.calculateRadiance(blah);
		ucar.ma2.Array dataArray = Array.factory(float.class, xy);
		Index dataIndex = dataArray.getIndex();
		int pos = 0;
		if(caldata != null){
			for(int i=0;i<xy[1];i++){
				for(int j=0;j<xy[2];j++){
					dataArray.setFloat(dataIndex.set(0,i,j),caldata[pos]);
					pos++;
				}
			}
		}else{
			for(int i=0;i<xy[1];i++){
				for(int j=0;j<xy[2];j++){
					dataArray.setFloat(dataIndex.set(0,i,j),0);
					pos++;
				}
			}			
		}
		return dataArray;
	}

}
