package gov.noaa.ncdc.swath;

import java.io.IOException;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordSysBuilder;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.ProjectionCT;
import ucar.nc2.util.CancelTask;

public class NcdcCoordBuilder extends CoordSysBuilder{

	private ProjectionCT projCT = null;

	public void addUserAdvice(String advice) {
		// TODO Auto-generated method stub
		System.out.println("in "+getClass()+".addUserAdvice");
		
	}

	public void augmentDataset(NetcdfDataset ds, CancelTask cancelTask) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("in "+getClass()+".augmentDataset");
/*
		ProjectionImpl proj = null;
	    proj = new LambertConformal();
	    projCT = new ProjectionCT("Projection", "FGDC", proj);
	    //proj = new NcdcSwathTransform(0.1,0.1);
		//projCT = new ProjectionCT("Projection", "FGDC", proj);
		
	    //Make coordinate transform variable
        VariableDS v = makeCoordinateTransformVariable(ds, projCT);
        v.addAttribute( new Attribute(_Coordinate.AxisTypes, "GeoX GeoY"));
        ds.addVariable(null, v);
	    
	    //Add coordinate variables
		if (ds.findVariable("GeoX") != null)
			ds.addCoordinateAxis( makeCoordAxis( ds, "GeoX"));
		if (ds.findVariable("GeoY") != null)
			ds.addCoordinateAxis( makeCoordAxis( ds, "GeoY"));
		
		ds.finish();
*/
		}

	private CoordinateAxis makeCoordAxis(NetcdfDataset ds, String axisName) throws IOException {
		// TODO Auto-generated method stub
		Variable var = ds.findVariable(axisName);
		Array data_stag = var.read();
		int n = (int) data_stag.getSize() - 1;
		Array data = Array.factory( data_stag.getElementType(), new int[] { n} );
		Index dataIndex = data.getIndex();
		for (int i=0; i<n; i++) {
			data.setDouble( dataIndex.set(i), (double) (i) );
		}

		String units = null;
		CoordinateAxis v = new CoordinateAxis1D( ds, null, axisName, DataType.DOUBLE, axisName, units, "synthesized non-staggered "+axisName+" coordinate");
		v.setCachedData( data, true);
		return v;
	}

/*	public void buildCoordinateSystems(NetcdfDataset ncDataset) {
		// TODO Auto-generated method stub
		System.out.println("in "+getClass()+".buildCoordinateSystems");
		
	}
*/
/*	public void setConventionUsed(String convName) {
		// TODO Auto-generated method stub
		System.out.println("in "+getClass()+".setConventionUsed");
		convName="NcdcSwath";
	}
*/
}
