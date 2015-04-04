package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;

import java.util.Map;

public class ExportHintNotSupportedException extends Exception {
    
    private ExportFormat exportFormat;
    private String offendingHintKey;
    private Map<String, Object> defaultSupportedHints;
    
    public ExportHintNotSupportedException(ExportFormat exportFormat, String offendingHintKey, Map<String, Object> defaultSupportedHints) {
        this.exportFormat = exportFormat;
        this.offendingHintKey = offendingHintKey;
        this.defaultSupportedHints = defaultSupportedHints;
    }

    public String getMessage() {
        return "Export Format: "+exportFormat.toString()+" does not supported the decode hint '"+offendingHintKey+"'."+
            "  Supported hints and default values are: "+defaultSupportedHints.toString();
    }
    
    public String toString() {
        return getMessage();
    }
}
