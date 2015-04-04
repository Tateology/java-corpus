package gov.noaa.ncdc.swath;

import java.util.Formatter;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordTransBuilderIF;
import ucar.nc2.dataset.CoordinateTransform;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.ProjectionCT;
import ucar.nc2.dataset.TransformType;
import ucar.nc2.dataset.VerticalCT;
import ucar.unidata.geoloc.vertical.VerticalTransform;

public class NcdcSwathIF implements CoordTransBuilderIF {

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
		//System.out.println(ds.toString());

		Variable ncdcSwath = (Variable) ds.findVariable("NcdcSwath");
		Attribute dirAtt = ncdcSwath.findAttributeIgnoreCase("AreaHeader");
		if (dirAtt == null) System.out.println("AreaHeader attribute not found.");
		Array dirArray = dirAtt.getValues();

		Attribute navAtt = ncdcSwath.findAttributeIgnoreCase("NavHeader");
		if (navAtt == null) System.out.println("NavHeader attribute not found.");
		Array navArray = navAtt.getValues();
		String type = "GVAR";
		if(128 ==  navAtt.getLength()){
			type="GOES";
		}
		
		NcdcSwathTransform proj = new NcdcSwathTransform(dirArray, navArray,type);
		return new ProjectionCT(ctv.getShortName(),"ToBeNamedLater",proj);
	}
		

	
	
	
	
	
	
	public VerticalTransform makeMathTransform(NetcdfDataset ds, Dimension timeDim, VerticalCT vCT) {
		// TODO Auto-generated method stub
		System.out.println("in "+getClass()+".makeMathTransform");
		return null;
	}


    public void setErrorBuffer(Formatter sb) {
        // TODO Auto-generated method stub
        
    }

}
