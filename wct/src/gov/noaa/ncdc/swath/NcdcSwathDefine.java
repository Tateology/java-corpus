package gov.noaa.ncdc.swath;

import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateTransform;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.TransformType;

public class NcdcSwathDefine extends ucar.nc2.dataset.transform.AbstractCoordTransBuilder{

	public String getTransformName() {
		// TODO Auto-generated method stub
		System.out.println("in "+getClass()+".getTransformName");
		return "NcdcSwath";
	}

	public TransformType getTransformType() {
		// TODO Auto-generated method stub
		System.out.println("in "+getClass()+".getTransformType");
		return TransformType.Projection;
	}

	public CoordinateTransform makeCoordinateTransform(NetcdfDataset ds, Variable ctv) {
		// TODO Auto-generated method stub
		System.out.println("in "+getClass()+".makeCoordinateTransform");
		return null;
	}

}
