package gov.noaa.ncdc.wct.io;


public class FilenamePattern {

	private String dataType;
	private String description;
	private String filePattern;
	private String fileTimestampLocation;
	private String sourceID;
	private String productCode;
	private String productLookupTable;
	private String idAliasLookupTable;
	
	
	
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFilePattern() {
		return filePattern;
	}
	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}
	public String getFileTimestampLocation() {
		return fileTimestampLocation;
	}
	public void setFileTimestampLocation(String fileTimestampLocation) {
		this.fileTimestampLocation = fileTimestampLocation;
	}
	public String getSourceID() {
		return sourceID;
	}
	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
    public void setProductLookupTable(String productLookupTable) {
        this.productLookupTable = productLookupTable;
    }
    public String getProductLookupTable() {
        return productLookupTable;
    }
	public void setIdAliasLookupTable(String idAliasLookupTable) {
        this.idAliasLookupTable = idAliasLookupTable;
    }
    public String getIdAliasLookupTable() {
        return idAliasLookupTable;
    }
    
    
    public String toString() {
		return "DataType="+dataType+", Description="+description+
		", FilePattern="+filePattern+", FileTimestamp="+fileTimestampLocation+
		", SourceID="+sourceID;
	}
}

