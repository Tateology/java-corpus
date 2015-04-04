package gov.noaa.ncdc.wct.ui.table;

import javax.swing.table.AbstractTableModel;


public class DbSummaryTableModel extends AbstractTableModel {

    
    
    
    
    
    public int getColumnCount() {
        // TODO Auto-generated method stub
        return 5;
    }

    public int getRowCount() {
        // TODO Auto-generated method stub
        return 5;
    }

    public Object getValueAt(int row, int col) {

        if (col < 4) {
            return new Integer(row*col);
        }
        else {
            return new Boolean(true);
        }
    }
    
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }


    
    
}
