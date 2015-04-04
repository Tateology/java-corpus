package gov.noaa.ncdc.wct.decoders;

import java.util.Map;

public class DecodeHintNotSupportedException extends Exception {
    
    private String offendingDecoder;
    private String offendingHintKey;
    private Map<String, Object> defaultSupportedHints;
    
    public DecodeHintNotSupportedException(String offendingDecoder, String offendingHintKey, Map<String, Object> defaultSupportedHints) {
        this.offendingDecoder = offendingDecoder;
        this.offendingHintKey = offendingHintKey;
        this.defaultSupportedHints = defaultSupportedHints;
    }

    public String getMessage() {
        return "Decoder: "+offendingDecoder.toString()+" does not supported the decode hint '"+offendingHintKey+"'."+
            "  Supported hints and default values are: "+defaultSupportedHints.toString();
    }
    
    public String toString() {
        return getMessage();
    }
}
