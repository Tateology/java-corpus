package gov.noaa.ncdc.wct.ui.event;

import gov.noaa.ncdc.wct.export.ScanResultsManager;
import gov.noaa.ncdc.wct.io.ScanResults;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class SortByListener implements ActionListener {

    private final ScanResultsManager parent;

    public SortByListener(ScanResultsManager parent) {
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ScanResults[] results = this.parent.getScanResults();
        
        if (results != null) {
            Arrays.sort(results, parent.getSortByComparator());
            this.parent.setScanResults(results);
        }
        
    }

}
