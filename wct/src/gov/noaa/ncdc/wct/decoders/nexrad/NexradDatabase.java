package gov.noaa.ncdc.wct.decoders.nexrad;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class NexradDatabase {

    
    private static NexradDatabase instance = null;
    private static HashMap<String, String> longNameMap = null;
    private static HashMap<Integer, String> longNameCodeMap = null;
    private static ArrayList<String> longNameList = null;

    private Connection connection;
    private Statement stmt;
    

    
    /**
     * Initializes an in-memory database instance - DON'T USE - USE 'getInstance' instead for singleton'.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    protected NexradDatabase() throws ClassNotFoundException, SQLException {
        init();
    }
    
    
    /**
     * Initializes a singleton in-memory database instance - USE this instead of constructor for singleton'.
     * The singleton instance is shared between all classes in the application.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static NexradDatabase getSharedInstance() throws ClassNotFoundException, SQLException {
        if (instance == null) {
            instance = new NexradDatabase();
        }
        return instance;
    }

    
    

    private void init() throws ClassNotFoundException, SQLException {
    
        // Use following for HSQLDB database
        Class.forName("org.hsqldb.jdbcDriver");
        this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:nexprod", "sa", "");
        this.stmt = connection.createStatement();
        
        // create table
        String createTableSQL = 
            "CREATE TABLE NEXRAD_LEVEL3_PRODUCTS ("+
            " DISPLAY_ORDER INT, "+
            " PID VARCHAR(255), "+
            " CODE INT, "+
            " NWSDIR VARCHAR(255), "+
            " LONGNAME VARCHAR(255), "+
            " TYPE VARCHAR(255) "+
            ", "+
            " UNIQUE(DISPLAY_ORDER, PID, LONGNAME)" +
            ");";
        stmt.execute(createTableSQL);
        

        // populate data
        String[] insertValues = new String[] {
                "0,   'N0R', 19, 'DS.p19r0/', 'Base Reflectivity ~0.5 deg (230 km)', 'Graphic'      ",
                "1,   'N1R', 19, 'DS.p19r1/', 'Base Reflectivity ~1.5 deg (230 km)', 'Graphic'      ",
                "2,   'N2R', 19, 'DS.p19r2/', 'Base Reflectivity ~2.5 deg (230 km)', 'Graphic'      ",
                "3,   'N3R', 19, 'DS.p19r3/', 'Base Reflectivity ~3.5 deg (230 km)', 'Graphic'      ",
                "4,   'N0Z', 20, 'DS.p20-r/', 'Base Reflectivity ~0.5 deg (460 km)', 'Graphic'      ",
                "11,  'N0V', 27, 'DS.p27v0/', 'Base Velocity ~0.5 deg (230 km)', 'Graphic'          ",
                "12,  'N1V', 27, 'DS.p27v1/', 'Base Velocity ~1.5 deg (230 km)', 'Graphic'          ",
                "13,  'N2V', 27, 'DS.p27v2/', 'Base Velocity ~2.5 deg (230 km)', 'Graphic'          ",
                "14,  'N3V', 27, 'DS.p27v3/', 'Base Velocity ~3.5 deg (230 km)', 'Graphic'          ",
                "15,  'N0W', 25, 'DS.p25-v/', 'Base Velocity ~0.5 deg (32 km)', 'Graphic'           ",
                "5, 'NCO', 36, 'DS.p36cr/', 'Composite Reflectivity 8 Levels (460 km)', 'Graphic'   ",
                "6, 'NCR', 37, 'DS.p37cr/', 'Composite Reflectivity 16 Levels (230 km)', 'Graphic'  ",
                "7, 'NCZ', 38, 'DS.p38cr/', 'Composite Reflectivity 16 Levels (460 km)', 'Graphic'  ",
                "16,'N0S', 56, 'DS.56rm0/', 'Storm Relative Velocity ~0.5 deg (230 km)', 'Graphic'  ",
                "17, 'N1S', 56, 'DS.56rm1/', 'Storm Relative Velocity ~1.5 deg (230 km)', 'Graphic' ",
                "18, 'N2S', 56, 'DS.56rm2/', 'Storm Relative Velocity ~2.4 deg (230 km)', 'Graphic' ",
                "19, 'N3S', 56, 'DS.56rm3/', 'Storm Relative Velocity ~3.4 deg (230 km)', 'Graphic' ",
                "8, 'NLL', 65, 'DS.65lrm/', 'Low Layer Composite Reflectivity (230 km)', 'Graphic'  ",
                "9, 'NML', 66, 'DS.66lrm/', 'Mid Layer Composite Reflectivity (230 km)', 'Graphic'  ",
                "10, 'NHL', 90, 'DS.90lrm/', 'High Layer Composite Reflectivity (230 km)', 'Graphic' ",
                "20, 'NVL', 57, 'DS.57vil/', 'Vertical Integrated Liquid (230 km)', 'Graphic'       ",
                "21, 'NET', 41, 'DS.p41et/', 'Echo Tops (230 km)', 'Graphic'                        ",
                "22, 'NST', 58, 'DS.58sti/', 'Storm Tracking Information (230 km)', 'Alphanumeric'  ",
                "23, 'NSS', 62, 'DS.p62ss/', 'Storm Structure (460 km)', 'Alphanumeric'             ",
                "24, 'NME', 60, 'DS.p60-m/', 'Mesocyclone (230 km)', 'Alphanumeric'                 ",
                "24, 'NMD', 141, '', 'Mesocyclone Detection (230 km)', 'Alphanumeric'                 ",
                "25, 'NTV', 61, 'DS.61tvs/', 'Tornadic Vortex Signature (230 km)', 'Alphanumeric'   ",
                "26, 'NHI', 59, 'DS.p59hi/', 'Hail Index (230 km)' , 'Alphanumeric'                 ",
                "27, 'N1P', 78, 'DS.78ohp/', 'One Hour Precipitation Total (230 km)', 'Graphic'     ",
                "28, 'N3P', 79, 'DS.79thp/', 'Three Hour Precipitation Total (230 km)', 'Graphic'   ",
                "29, 'NTP', 80, 'DS.80stp/', 'Storm Total Precipitation (230 km)', 'Graphic'        ",
                "30, 'DPA', 81, 'DS.81dpr/', 'Digital Precipitation Array (230 km)', 'Graphic'      ",
                "31, 'SPD', 82, 'DS.82spd/', 'Supplemental Precipitation Data', 'Unknown'           ",
                "32, 'NVW', 48, 'DS.48vwp/', 'VAD Wind Profile', 'Graphic'                          ",
                "33, 'NWP', 47, 'DS.47swp/', 'Severe Weather Probability', 'Unknown'                ",
                "34, 'NSP', 28, 'DS.p28sw/', 'Base Spectrum Width (60 km)', 'Graphic'               ",
                "35, 'NSW', 30, 'DS.p30sw/', 'Base Spectrum Width (230 km)', 'Graphic'              ",
                "36, 'NCF', 34, 'DS.34cfc/', 'Clutter Filter Control (230 km)', 'Unknown'           ",
                "37, 'GSM', 2, 'DS.p2gsm/', 'General Status Message', 'Alphanumeric'                ",
                "38, 'RCM', 74, 'DS.74rcm/', 'Radar Coded Message', 'Alphanumeric'                  ",
                "39, 'RSL', 00, 'DS.00rsl/', 'Radar Status Log', 'Alphanumeric'                  ",
                
                "-1, 'V01', -1, '', 'NEXRAD Level-II - Legacy', 'Graphic'    ",
                "-1, 'V02', -1, '', 'NEXRAD Level-II - Legacy', 'Graphic'    ",
                "-1, 'V03', -1, '', 'NEXRAD Level-II - Super Resolution', 'Graphic'    ",
                "-1, 'V04', -1, '', 'NEXRAD Level-II - Legacy, Recombined', 'Graphic'    "
        };
        
        for (int n=0; n<insertValues.length; n++) {
            String insertSQL = "insert into NEXRAD_LEVEL3_PRODUCTS "+
                "(DISPLAY_ORDER, PID, CODE, NWSDIR, LONGNAME, TYPE)"+
                " values ("+insertValues[n]+")";

            stmt.executeUpdate(insertSQL);
        }
        
        
    }
    
    
    
    
    public ArrayList<String> getRadarProductLongNamesList() throws SQLException {
        
        if (longNameList != null) {
            return longNameList;
        }
        
        longNameList = new ArrayList<String>();
        String selectSQL = "SELECT LONGNAME FROM NEXRAD_LEVEL3_PRODUCTS";
        ResultSet rs = stmt.executeQuery(selectSQL);
        while (rs.next()) {
            longNameList.add(rs.getString(1));
        }
        return longNameList;        
    }
    
    public HashMap<String, String> getRadarProductLongNamesMap() throws SQLException {
        
        if (longNameMap != null) {
            return longNameMap;
        }
        
        longNameMap = new HashMap<String, String>();
        String selectSQL = "SELECT PID, LONGNAME FROM NEXRAD_LEVEL3_PRODUCTS";
        ResultSet rs = stmt.executeQuery(selectSQL);
        while (rs.next()) {
            longNameMap.put(rs.getString(1), rs.getString(2));
        }
        rs.close();
        return longNameMap;        
    }
    
    public HashMap<Integer, String> getRadarCodeLongNamesMap() throws SQLException {
        
        if (longNameCodeMap != null) {
            return longNameCodeMap;
        }
        
        longNameCodeMap = new HashMap<Integer, String>();
        String selectSQL = "SELECT CODE, LONGNAME FROM NEXRAD_LEVEL3_PRODUCTS";
        ResultSet rs = stmt.executeQuery(selectSQL);
        while (rs.next()) {
            longNameCodeMap.put(rs.getInt(1), rs.getString(2));
        }
        rs.close();
        return longNameCodeMap;        
    }
    
    public String getLevel3ProductNWSDir(String longname) throws SQLException {
        String selectSQL = "SELECT NWSDIR FROM NEXRAD_LEVEL3_PRODUCTS WHERE LONGNAME='"+longname+"'";
        ResultSet rs = stmt.executeQuery(selectSQL);
        if (rs.next()) {
            return rs.getString(1);
        }
        else {
            return "ERROR";
        }
    }
    
}
