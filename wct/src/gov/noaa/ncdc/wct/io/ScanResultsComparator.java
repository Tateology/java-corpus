package gov.noaa.ncdc.wct.io;

import java.util.Comparator;




public class ScanResultsComparator implements Comparator<ScanResults> {
    
    public static enum CompareBy { FILE_NAME, PRODUCT_ID, DISPLAY_NAME, DATA_TYPE, TIMESTAMP_ASC, TIMESTAMP_DESC };
    private CompareBy compareBy;
    
    public ScanResultsComparator(CompareBy compareBy) {
        this.compareBy = compareBy;
    }

//    @Override
    public int compare(ScanResults sr1, ScanResults sr2) {
        
        try {

            if (compareBy == CompareBy.FILE_NAME) {
                return sr1.getFileName().compareTo(sr2.getFileName());
            }
            else if (compareBy == CompareBy.PRODUCT_ID) {
                return sr1.getProductID().compareTo(sr2.getProductID());
            }
            else if (compareBy == CompareBy.DISPLAY_NAME) {
                return sr1.getDisplayName().compareTo(sr2.getDisplayName());
            }
            else if (compareBy == CompareBy.DATA_TYPE) {
                return sr1.getDataType().compareTo(sr2.getDataType());
            }
            else if (compareBy == CompareBy.TIMESTAMP_ASC) {
           		return sr1.getTimestamp().compareTo(sr2.getTimestamp());
            }
            else if (compareBy == CompareBy.TIMESTAMP_DESC) {
           		return -1*sr1.getTimestamp().compareTo(sr2.getTimestamp());
            }
            else {            
                return 0;
            }
        
        } catch (Exception e) {
            return 0;
        }
    }

    public CompareBy getCompareBy() {
        return compareBy;
    }
  
}



