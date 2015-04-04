package gov.noaa.ncdc.wct.ui;

import java.io.Serializable;

public final class MarkerInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String label1;
    private String label2;
    private String label3;
    private String lat;
    private String lon;
    
    MarkerInfo(String label1, String label2, String label3, String lat, String lon) {
        this.label1 = label1;
        this.label2 = label2;
        this.label3 = label3;
        this.lat = lat;
        this.lon = lon;
    }

    public String getLabel1() {
        return label1;
    }

    public String getLabel2() {
        return label2;
    }

    public String getLabel3() {
        return label3;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }
    
    
    
    public String toString() {
        return "label1: "+label1+"\n"+
            "label2: "+label2+"\n"+
            "label3: "+label3+"\n"+
            "lat: "+lat+"\n"+
            "lon: "+lon+"\n";
    }
}