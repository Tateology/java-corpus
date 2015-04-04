package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.wct.io.ScanResults;
import gov.noaa.ncdc.wct.io.ScanResultsComparator;

public interface ScanResultsManager {

    public ScanResults[] getScanResults();
    public void setScanResults(ScanResults[] scanResults);
    
    public ScanResultsComparator getSortByComparator();
}
