package gov.noaa.ncdc.swath;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.ProjectionPointImpl;
import edu.wisc.ssec.mcidas.AREAnav;
import edu.wisc.ssec.mcidas.GOESnav;
import edu.wisc.ssec.mcidas.GVARnav;

public class NcdcSwathTransform extends ProjectionImpl {

    /** Projection Name */
    public static final String ATTR_NAME = "Projection_Name";
	private double lat_scale, lon_scale;
	
	private int[] dir;
	private int[] nav;
    private AREAnav  ng;

    //Parameterless constructor to keep toolsUI from throwing Exceptions
    public NcdcSwathTransform() {
    	super("NcdcSwathTransform", true);
    }
    
    public NcdcSwathTransform(Array dirArray, Array navArray,String type) {
    	super("NcdcSwathTransform", true);
    	
		Index dirIndex = dirArray.getIndex();
		Index navIndex = navArray.getIndex();
		
		dir = new int[64];
		for (int i=0; i<64; i++) {
			dir[i] = dirArray.getInt(dirIndex.set(i));
		}
		nav = new int[(int)navArray.getSize()];
		for (int i=0; i<navArray.getSize(); i++) {
			nav[i] = navArray.getInt(navIndex.set(i));
		}
		
		try { 
			if("GVAR".equals(type)){
				ng = new GVARnav(nav);  // XXXXnav is the specific implementation
			}else{
				ng = new GOESnav(nav);
			}
		} catch (IllegalArgumentException excp) {
			System.out.println(excp);
			return;
		}
		ng.setImageStart(dir[5], dir[6]);
		ng.setRes(dir[11], dir[12]);
		ng.setStart(1,1);
		addParameter(ATTR_NAME, "NcdcSwath");
		addParameter("AreaHeader",dir.toString());
		addParameter("NavHeader", nav.toString());
	}

/*	public NcdcSwathTransform(double lat, double lon) {
		this.lat_scale = lat;
		this.lon_scale = lon;
        addParameter(ATTR_NAME, "NcdcSwath");
		addParameter("Latitude_scale",lat_scale);
		addParameter("Longitude_scale",lon_scale);
	}
*/	
	
	
	public boolean crossSeam(ProjectionPoint pt1, ProjectionPoint pt2) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getClassName() {
		// TODO Auto-generated method stub
		return "NcdcSwath";
	}

	/**
	public ProjectionRect getDefaultMapArea() {
		// TODO Auto-generated method stub
		
		System.out.println(getClass()+".getDefaultMapArea");
		
		return null;
	}
	*/

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}


	public String paramsToString() {
		// TODO Auto-generated method stub
		return null;
	}

	public ProjectionPoint latLonToProj(LatLonPoint latLon, ProjectionPointImpl result) {
        double toX, toY;
        double fromLat = (latLon.getLatitude());
        double fromLon = (latLon.getLongitude());
//        double fromLat = Math.toDegrees(latLon.getLatitude());
//        double fromLon = Math.toDegrees(latLon.getLongitude());

        //test method
/*        toX = fromLon * lon_scale;
        toY = fromLat * lat_scale;
        result.setLocation(toX, toY);
        */
        
        //Convert Lat/Lon --> i/j
		double [][] latlon = new double [2][1];
		latlon[0][0] = fromLat;
		latlon[1][0] = fromLon;
		double [][] point; 
        point = ng.toLinEle(latlon);

        //System.out.println("LLtoIJ "+latlon[0][0]+" "+latlon[1][0]+" to "+   point[0][0] +" "+point[1][0]);

        result.setLocation(point[0][0], point[1][0]);
        return result;
	}

	public LatLonPoint projToLatLon(ProjectionPoint world, LatLonPointImpl result) {
        double fromX = world.getX();
        double fromY = world.getY();

        /*
        double toLat, toLon;
        toLat = Math.toRadians(fromY / lat_scale);
        toLon = Math.toRadians(fromX / lon_scale);
		 */
		double [][] pixel = new double [2][1];
		double [][] latLon;
		//Initialize navigation variables
		ng.setImageStart(dir[5], dir[6]);
		ng.setRes(dir[11], dir[12]);
		ng.setStart(1,1);
		pixel[0][0] = fromX;
		pixel[1][0] = fromY;
		latLon = ng.toLatLon(pixel); 
		//latLonValues[i][j][0] = latLon[0][0];
		//latLonValues[i][j][1] = latLon[1][0];
		//System.out.println("IJtoLL "+pixel[0][0] +" "+pixel[1][0]+" to "+   latLon[0][0]+" "+latLon[1][0]);
//		result.setLatitude( Math.toRadians(latLon[0][0]));
//      result.setLongitude(Math.toRadians(latLon[1][0]));
		result.setLatitude( (latLon[0][0]));
        result.setLongitude((latLon[1][0]));
        return result;
	}

	@Override
	public boolean equals(Object proj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ProjectionImpl constructCopy() {
		NcdcSwathTransform st = new NcdcSwathTransform();
		st.setDir(getDir());
		st.setNav(getNav());
		st.setNg(getNg());
		return st;
	}

	public int[] getDir() {
		return dir;
	}

	public void setDir(int[] dir) {
		this.dir = dir;
	}


	public int[] getNav() {
		return nav;
	}

	public void setNav(int[] nav) {
		this.nav = nav;
	}

	public AREAnav getNg() {
		return ng;
	}

	public void setNg(AREAnav ng) {
		this.ng = ng;
	}
	
}

