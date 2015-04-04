package gov.noaa.ncdc.wct.io;

public class FileInfo {
    private String name;
    private String path;
    private String timestamp;
    private long size;

    public FileInfo(String name, String path, String timestamp, long size) {    	
        this.name = name;
        this.path = path;
        this.timestamp = timestamp;
        this.size = size;
    }

    public String getName() {
        return name;
    }
    
    public String getPath() {
    	return path;
    }

    public String getTimestamp() {
        return timestamp;
    }
    
    public long getSize() {
        return size;
    }
    
    public String toString() {
        return name+" ::: "+timestamp+" ::: "+size;
    }

}
