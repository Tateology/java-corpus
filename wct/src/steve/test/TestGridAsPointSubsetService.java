package steve.test;

import ucar.ma2.Array;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.ProjectionPointImpl;

public class TestGridAsPointSubsetService {

	public static void main(String[] args) {
		
		try {
			
			String dodsURL = "http://data.ncdc.noaa.gov/thredds/dodsC/cdr/seaice/seaice_north.ncml";
			
			GridDataset gds = GridDataset.open(dodsURL);
			System.out.println(gds.getBoundingBox());

			GridDatatype grid = gds.findGridDatatype("seaice_conc_cdr");
			System.out.println(grid.getCoordinateSystem().getLatLonBoundingBox());
			
			double minX = grid.getCoordinateSystem().getXHorizAxis().getMinValue();
			double maxX = grid.getCoordinateSystem().getXHorizAxis().getMaxValue();
			double minY = grid.getCoordinateSystem().getYHorizAxis().getMinValue();
			double maxY = grid.getCoordinateSystem().getYHorizAxis().getMaxValue();
			
			System.out.println("min x: "+minX);
			System.out.println("max x: "+maxX);
			System.out.println("min y: "+minY);
			System.out.println("max y: "+maxY);
			
			Projection proj = grid.getProjection();
			ProjectionPoint projPointLL = new ProjectionPointImpl(minX, minY);
			LatLonPoint llPointLL = proj.projToLatLon(projPointLL, new LatLonPointImpl());
			ProjectionPoint projPointUR = new ProjectionPointImpl(maxX, maxY);
			LatLonPoint llPointUR = proj.projToLatLon(projPointUR, new LatLonPointImpl());
			
			System.out.println(llPointLL + " , "+llPointUR);
			
			
			Array xAxisData = grid.getCoordinateSystem().getXHorizAxis().read();
			Array yAxisData = grid.getCoordinateSystem().getYHorizAxis().read();
			double minLat = Double.POSITIVE_INFINITY;
			double maxLat = Double.NEGATIVE_INFINITY;
			double minLon = Double.POSITIVE_INFINITY;
			double maxLon = Double.NEGATIVE_INFINITY;			
			LatLonPointImpl llp = new LatLonPointImpl();
			ProjectionPointImpl ppt = new ProjectionPointImpl();
			float[] xData = (float[])xAxisData.copyTo1DJavaArray();
			float[] yData = (float[])yAxisData.copyTo1DJavaArray();

			for (float y : yData) {
				for (float x : xData) {
					ppt.setLocation(x, y);
					llp = (LatLonPointImpl) proj.projToLatLon(ppt, llp);
					minLat = (llp.getLatitude() < minLat) ? llp.getLatitude() : minLat;
					maxLat = (llp.getLatitude() > maxLat) ? llp.getLatitude() : maxLat;
					minLon = (llp.getLongitude() < minLon) ? llp.getLongitude() : minLon;
					maxLon = (llp.getLongitude() > maxLon) ? llp.getLongitude() : maxLon;
					
				}
			}
			
			
			// brute force range
			System.out.println("brute force lat range: "+minLat+" , "+maxLat + " | lon range: " + minLon + " , " + maxLon);
			
			// subset service TDS URL:
			// http://data.ncdc.noaa.gov/thredds/ncss/grid/cdr/seaice/seaice_north.ncml?var=seaice_conc_cdr&latitude=80.33203&longitude=-138.69141&temporal=range&time_start=2007-01-31T00:00:00Z&time_end=2007-12-31T00:00:00Z&vertCoord=0&accept=xml&point=true
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
