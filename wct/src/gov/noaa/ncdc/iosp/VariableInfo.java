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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

public class VariableInfo {
    

    
    private String   name = null;
    private String   long_name = null;
    private String   units = null;
    private DataType dataType = null;
    private List<Dimension>     dimList = null;
    private Float    scale_factor = null;
    private Float    add_offset = null;
    private Integer  valid_max = null;
    private Integer  recordNumber = null;
    private String   coordinates = null;
    private String   coordinateTransform = null;
    private String   derivedNotes=null;
    private double missingValue = -9999;
    private List<Attribute> attributes;

    //Constructor Summary: Currently 5 generic Constructors
    // a) name, long_name, units, dataType, dimList
    // b) name, long_name, units, dataType, dimList, coordinates
    // c) name, long_name, units, dataType, dimList, scale, offset, max, recordnumber
    // d) name, long_name, units, dataType, dimList, coordinates, scale, offset, max, recordnumber
    // e) name, long_name, units, dataType, dimList, coordinates, scale, offset, max, recordnumber, coordTransform
    // f) name, long_name, units, dataType, dimList, coordinates, scale, offset, max, recordnumber, coordTransform, derivedNotes
    //
    // scale and offset are Float
    // max and recordnumber are Integer
    // name, long_name, units, coordinates and coordTransform are String
    
    //Constructor - Nothing beyond dataType
    public VariableInfo( String name, String long_name, String units, DataType dataType, List<Dimension> dimList) {
    	this.name = name; this.long_name = long_name; this.units = units; this.dataType = dataType; this.dimList = dimList;
    }
    //Constructor - 6 parameters - short using coordinate
    public VariableInfo( String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates) {
    	this.name = name; this.long_name = long_name; this.units = units; this.dataType = dataType; this.dimList = dimList;
    	this.coordinates = coordinates;
    }
    //Constructor - 9 parameters - All values present, except coordinate
    public VariableInfo( String name, String long_name, String units, DataType dataType, List<Dimension> dimList, Float scale_factor, Float add_offset, Integer valid_max, Integer recordNumber) {
    	this.name = name; this.long_name = long_name; this.units = units; this.dataType = dataType; this.dimList = dimList;
    	this.scale_factor = scale_factor;
    	this.add_offset = add_offset;
    	this.valid_max = valid_max;
    	this.recordNumber = recordNumber;
    }
    //Constructor - 10 parameters
    public VariableInfo( String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates, Float scale_factor, Float add_offset, Integer valid_max, Integer recordNumber) {
    	this.name = name; this.long_name = long_name; this.units = units; this.dataType = dataType; this.dimList = dimList;
    	this.coordinates = coordinates;
    	this.scale_factor = scale_factor;
    	this.add_offset = add_offset;
    	this.valid_max = valid_max;
    	this.recordNumber = recordNumber;
    }
    //Constructor - 11 parameters
    public VariableInfo(String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates, Float scale_factor, Float add_offset, Integer valid_max, Integer recordNumber, String coordinateTransform) {
    	this.name = name; this.long_name = long_name; this.units = units; this.dataType = dataType; this.dimList = dimList;
    	this.coordinates = coordinates;
    	this.scale_factor = scale_factor;
    	this.add_offset = add_offset;
    	this.valid_max = valid_max;
    	this.recordNumber = recordNumber;
    	this.coordinateTransform = coordinateTransform;
	}
    //Constructor - 12 parameters - Includes derivedNotes
    public VariableInfo(String name, String long_name, String units, DataType dataType, List<Dimension> dimList, String coordinates, Float scale_factor, Float add_offset, Integer valid_max, Integer recordNumber, String coordinateTransform, String derivedNotes) {
    	this.name = name; this.long_name = long_name; this.units = units; this.dataType = dataType; this.dimList = dimList;
    	this.coordinates = coordinates;
    	this.scale_factor = scale_factor;
    	this.add_offset = add_offset;
    	this.valid_max = valid_max;
    	this.recordNumber = recordNumber;
    	this.coordinateTransform = coordinateTransform;
    	this.derivedNotes=derivedNotes;
	}
    
    public String toString() {
	StringBuffer retVal = new StringBuffer();
	DecimalFormat formatter = new DecimalFormat("#####0.00##");
	retVal.append("Variable (")
	    .append(this.getName()).append(",")
	    .append(this.getLongName()).append(",")
	    .append(this.getUnits()).append(",")
	    .append(this.getDataType()).append(",")
	    .append(formatter.format((double) (this.getScaleFactor())) ).append(",")
	    .append(formatter.format((double) (this.getAddOffset()))   ).append(",")
	    .append(formatter.format((double) (this.getValidMax()))).append(")");
	return( retVal.toString() );
    }


    
   
    public String getName() {
		return (this.name);
	}

	public String getLongName() {
		return (this.long_name);
	}

	public String getUnits() {
		return (this.units);
	}

	public String getCoordinates() {
		return (this.coordinates);
	}

	public DataType getDataType() {
		return (this.dataType);
	}

	public List<Dimension> getDimensions() {
		return (this.dimList);
	}

	public Float getScaleFactor() {
		return (this.scale_factor);
	}

	public Float getAddOffset() {
		return (this.add_offset);
	}
	
    public Integer getValidMax() {
		return (this.valid_max);
	}
    
    public Integer getRecordNumber() {
		return (this.recordNumber);
	}
    
    public String getTransform() {
		return (this.coordinateTransform);
	}
    
    public String getDerivedNotes() {
		return (this.derivedNotes);
	}
    
    public double getMissingValue() {
		return (this.missingValue);
	}   
    
    public List<Attribute> getAttributes(){
    	if(this.attributes == null){
    		attributes = new ArrayList<Attribute>();
    	}
    	return attributes;
    }
    
    
    public void addAttribute(Attribute att){
    	List<Attribute> list = this.getAttributes();
    	list.add(att);
    }
}
