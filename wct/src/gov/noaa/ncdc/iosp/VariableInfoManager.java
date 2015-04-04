/**
*      Copyright (c) 2007-2008 Work of U.S. Government.
*      No rights may be assigned.
*
* LIST OF CONDITIONS
* Redistribution and use of this program in source and binary forms, with or
* without modification, are permitted for any purpose (including commercial purposes) 
* provided that the following conditions are met:
*
* 1.  Redistributions of source code must retain the above copyright notice,
*     this list of conditions, and the following disclaimer.
*
* 2.  Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions, and the following disclaimer in the documentation
*    and/or materials provided with the distribution.
*
* 3.  In addition, redistributions of modified forms of the source or binary
*     code must carry prominent notices stating that the original code was
*     changed, the author of the revisions, and the date of the change.
*
* 4.  All publications or advertising materials mentioning features or use of
*     this software are asked, but not required, to acknowledge that it was
*     developed at the NOAA's National Climatic Data Center in Asheville, NC and to
*     credit the contributors.
*
* 5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS  "AS IS"
*     WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event
*     shall the Government or the Contributors be liable for any damages
*     suffered by the users arising out of the use of this software, even if
*     advised of the possibility of such damage.
*/

package gov.noaa.ncdc.iosp;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants._Coordinate;

public class VariableInfoManager {

    private java.util.List<VariableInfo> list = new java.util.ArrayList<VariableInfo>();
    private java.util.HashMap<String, VariableInfo> hash = new java.util.HashMap<String, VariableInfo>();

    public VariableInfo addVariableInfo( String name, String long_name, String units, DataType dataType, List<Dimension> dimList) {
        VariableInfo varInfo = new VariableInfo(name, long_name, units, dataType, dimList);
        list.add( varInfo );
        hash.put( name, varInfo);
        return varInfo;
    }
    //Constructor - 6 parameters - short using coordinate
    public VariableInfo addVariableInfo( String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates) {
        VariableInfo varInfo = new VariableInfo(name, long_name, units, dataType, dimList, coordinates);
        list.add( varInfo );
        hash.put( name, varInfo);
        return varInfo;
    }
    //Constructor - 9 parameters - All values present, except coordinate
    public VariableInfo addVariableInfo( String name, String long_name, String units, DataType dataType, List<Dimension> dimList, Float scale_factor, 
            Float add_offset, Integer valid_max, Integer recordNumber) {
        VariableInfo varInfo = new VariableInfo(name, long_name, units, dataType, dimList, scale_factor, add_offset, valid_max, recordNumber);
        list.add( varInfo );
        hash.put( name, varInfo);
        return varInfo;
    }
    //Constructor - 10 parameters
    public VariableInfo addVariableInfo( String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates, 
            Float scale_factor, Float add_offset, Integer valid_max, Integer recordNumber) {
        VariableInfo varInfo = new VariableInfo(name, long_name, units, dataType, dimList, coordinates, scale_factor, 
                add_offset, valid_max, recordNumber); 
        list.add( varInfo );
        hash.put( name, varInfo);
        return varInfo;
    }
    //Constructor - 11 parameters
    public VariableInfo addVariableInfo(String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates, 
            Float scale_factor, Float add_offset, Integer valid_max, Integer recordNumber, String coordinateTransform) {
        
        VariableInfo varInfo = new VariableInfo(name, long_name, units, dataType, dimList, coordinates,
                scale_factor, add_offset, valid_max, recordNumber, coordinateTransform);
        list.add( varInfo );
        hash.put( name, varInfo);
        return varInfo;
    }
    //Constructor - 12 parameters - Includes derivedNotes
    public VariableInfo addVariableInfo(String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates, 
            Float scale_factor, Float add_offset, Integer valid_max, Integer recordNumber, String coordinateTransform, String derivedNotes) {
        
        VariableInfo varInfo = new VariableInfo(name, long_name, units, dataType, dimList, coordinates, 
                scale_factor, add_offset, valid_max, recordNumber, coordinateTransform, derivedNotes);
        list.add( varInfo );
        hash.put( name, varInfo);
        return varInfo;
    }

    
    
    
    public VariableInfo findByName( String name) {
        if ( name == null ) {
            return( null);
        }
        return (VariableInfo) hash.get( name );
    }

    public void clearAll() {
        list.clear();
        hash.clear();
    }

    
    public void add( String name, String long_name, String units, DataType dataType, List<Dimension> dimList) {
        VariableInfo varInfo = new VariableInfo(name, long_name, units, dataType, dimList);
        list.add( varInfo );
        hash.put( name, varInfo);
    }
    public void add( String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates) {
        VariableInfo varInfo = new VariableInfo(name, long_name, units, dataType, dimList, coordinates);
        list.add( varInfo );
        hash.put( name, varInfo);
    }
    
    
    public List<VariableInfo> getAll()     {return(list); }
    public Set<String> getAllNames() {return(hash.keySet()); }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void setVariables(NetcdfFile ncfile) {
        Iterator<VariableInfo> varInfoIter = getAll().iterator();
        VariableInfo curVarInfo = null;
        while (varInfoIter.hasNext()) {
            curVarInfo = (VariableInfo) varInfoIter.next();
        
            // System.out.println(curVarInfo);
            Variable var = new Variable(ncfile, null, null, curVarInfo.getName());
            var.setDataType(curVarInfo.getDataType());
            var.setDimensions(curVarInfo.getDimensions());
            ncfile.addVariable(null, var);
            ncfile.addVariableAttribute(var, new Attribute("long_name", curVarInfo.getLongName()));
            //missing value & _FillValue (will provide both)
//          ncfile.addVariableAttribute(var,new Attribute("missing_value",curVarInfo.getMissingValue()));
//          ncfile.addVariableAttribute(var,new Attribute("_FillValue",curVarInfo.getMissingValue()));
            
            if (curVarInfo.getUnits() != null) {
                ncfile.addVariableAttribute(var, new Attribute("units", curVarInfo.getUnits()));
            }
            if (curVarInfo.getCoordinates() != null) {
                ncfile.addVariableAttribute(var, new Attribute(_Coordinate.Axes, curVarInfo.getCoordinates()));
            }
            if (curVarInfo.getTransform() != null) {
                ncfile.addVariableAttribute(var, new Attribute("_CoordinateTransforms", curVarInfo.getTransform()));
            }
            
            if(curVarInfo.getCoordinates() != null){
                ncfile.addVariableAttribute(var, new Attribute("_CoordinateSystems", curVarInfo.getCoordinates()));
            }
            if (curVarInfo.getScaleFactor() != null) {
                ncfile.addVariableAttribute(var, new Attribute("scale_factor", curVarInfo.getScaleFactor()));
            }
            if (curVarInfo.getAddOffset() != null) {
                ncfile.addVariableAttribute(var, new Attribute("add_offset", curVarInfo.getAddOffset()));
            }
            if (curVarInfo.getValidMax() != null) {
                ncfile.addVariableAttribute(var, new Attribute("valid_max", curVarInfo.getValidMax()));
            }

            if(curVarInfo.getName().equalsIgnoreCase("time")){
                ncfile.addVariableAttribute(var,new Attribute(_Coordinate.AxisType, "Time"));
            }
            if (curVarInfo.getName().equalsIgnoreCase("GeoX"))
                ncfile.addVariableAttribute(var, new Attribute(_Coordinate.AxisType, "GeoX"));
            if (curVarInfo.getName().equalsIgnoreCase("GeoY"))
                ncfile.addVariableAttribute(var, new Attribute(_Coordinate.AxisType, "GeoY"));
            if (curVarInfo.getDerivedNotes() != null) {
                ncfile.addVariableAttribute(var, new Attribute("derived", curVarInfo.getDerivedNotes()));
            }
            //add any attributes
            if(!curVarInfo.getAttributes().isEmpty()){
                List<Attribute> list = curVarInfo.getAttributes();
                for(Attribute att:list){
                    ncfile.addVariableAttribute(var, att);
                }
            }
        }
    }


    
    
    
    
    
}
