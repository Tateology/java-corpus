package gov.noaa.ncdc.wct.ui.table;

import ftp.FtpException;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.io.WCTDataSourceDB;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public class DbSourceTableModel extends AbstractTableModel {


    //private String[] columns = new String[] { "Source", "Location", "Files", "Date Added" };

    private WCTDataSourceDB db;
    private ResultSet sourceRs;
    private ResultSetMetaData sourceRsMeta;
    private ArrayList<String[]> data = new ArrayList<String[]>();

    public DbSourceTableModel() throws SQLException, ClassNotFoundException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {

        String tempdir = System.getProperty("java.io.tmpdir");
        if ( !(tempdir.endsWith(File.separator)) ) {
            tempdir = tempdir + File.separator;
        }
        tempdir += "jnx"+File.separator+"datadb";

        this.db = WCTDataSourceDB.getSharedInstance(tempdir);
        refresh();

    }

    public void refresh() throws SQLException {
        String sqlQuery = "SELECT sr.SOURCE_ID, ss.SOURCE_DESC, count(sr.DISPLAY_NAME) as ENTRY_COUNT, ss.SOURCE_TYPE, ss.SOURCE, ss.DATETIME_ADDED "+
        "FROM SCAN_RESULTS sr, SCAN_SOURCE ss "+
        "WHERE sr.SOURCE_ID = ss.SOURCE_ID "+
        "GROUP BY sr.SOURCE_ID, ss.SOURCE_DESC, ss.SOURCE_TYPE, ss.SOURCE, ss.DATETIME_ADDED "+
        "ORDER BY DATETIME_ADDED DESC";

        this.sourceRs = db.executeQuery(sqlQuery);
        this.sourceRsMeta = sourceRs.getMetaData();

        data.clear();
        while (sourceRs.next()) {
            String[] row = new String[getColumnCount()];
            for (int n=0; n<getColumnCount(); n++) {
                row[n] = sourceRs.getString(n+1);
                System.out.print(row[n]+"\t");
            }
            this.data.add(row);
            System.out.println();
        }
        System.out.println("ROWS: "+data.size()+" COLS: "+sourceRsMeta.getColumnCount());
    }
    
    
    public void closeDb() throws SQLException {
        db.shutdown();
    }
    
    
    public void addEntry(String type, String description, String location) 
    	throws SQLException, IOException, FtpException, DecodeException, ParseException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {
        
    	if (type.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            db.addHASJob(description, location);
        }
        else if (type.equals(WCTDataSourceDB.LOCAL_DISK)) {
            db.addLocalDirectory(description, new File(location));
        }
        else if (type.equals(WCTDataSourceDB.URL_DIRECTORY)) {
            db.addURLDirectory(description, new URL(location));
        }
        else if (type.equals(WCTDataSourceDB.THREDDS)) {
            db.addTHREDDSCatalog(description, new URL(location));
        }
    }
    
    public void removeEntry(int[] entryIndices) throws SQLException {
        for (int n=0; n<entryIndices.length; n++) {
            int sourceID = Integer.parseInt(getValueAt(entryIndices[n], 0).toString());
            System.out.println("need to remove id="+sourceID);
            db.deleteID(sourceID);
        }
    }
    
    
    
    
    
    

    public int getColumnCount() {
        try {
            return sourceRsMeta.getColumnCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getRowCount() {
//        try {
//            String sqlQuery = "SELECT COUNT(*) FROM SCAN_SOURCE";
//            ResultSet rs = db.executeQuery(sqlQuery);
//            if (rs.next()) {
//                return Integer.parseInt(rs.getString(1));
//            }
//            else {
//                return 0;
//            }
//        } catch (SQLException e) {
//            return 0;
//        }
        return data.size();
    }

    public Object getValueAt(int row, int col) {
        return data.get(row)[col];
    }


    public String getColumnName(int col) {
        try {
            return sourceRsMeta.getColumnName(col+1);
        } catch (SQLException e) {
            return "Unknown";
        }
    }
    
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
