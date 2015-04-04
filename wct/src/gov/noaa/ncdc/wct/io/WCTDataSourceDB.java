package gov.noaa.ncdc.wct.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import ftp.FtpException;
import gov.noaa.ncdc.wct.decoders.DecodeException;

/**
 * Creates an HSQLDB in-memory or persistant file-based database.  If the 
 * database is file-based, the method '.shutdown()' must be called
 * before the JVM exits.
 *  
 * @author steve.ansari
 *
 */
public class WCTDataSourceDB {
    
    private static WCTDataSourceDB instance = null;

    public final static String LOCAL_DISK = "Local Disk";
    public final static String NCDC_HAS_FTP = "NCDC HAS Order";
    public final static String URL_DIRECTORY = "URL Directory";
    public final static String THREDDS = "THREDDS Catalog";
    public final static String SSH_DIRECTORY = "SSH Directory";
    public final static String CLASS_ORDER = "CLASS Order";
    public final static String SINGLE_FILE = "Single File";
    public final static String FAVORITES = "Favorites";


    private WCTDirectoryScanner wctDirScanner;
    private int sourceSequence = 0;

    private String dbLocation;
    
    private Connection connection = null;
    private Statement stmt = null;
    private PreparedStatement prepStmtScanResults = null;
    private PreparedStatement prepStmtScanSource = null;

    /**
     * Initializes an in-memory database instance - DON'T USE - USE 'getInstance' instead for singleton'.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    protected WCTDataSourceDB() throws ClassNotFoundException, SQLException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        this("mem:wrt");
    }
    
    /**
     * Initializes the database, which can be in-memory or file based.
     * @param dbLocation "mem:wrt" for In-Memory, or "file:/path/to/db" for persistant
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    protected WCTDataSourceDB(String dbLocation) throws ClassNotFoundException, SQLException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        this.dbLocation = dbLocation;
       
        this.wctDirScanner = new WCTDirectoryScanner();
        
        System.out.println("BEGIN DB INIT");
        init();
        System.out.println("END DB INIT");
    }
    
    
    /**
     * Initializes a singleton in-memory database instance - USE this instead of constructor for singleton'.
     * The singleton instance is shared between all classes in the application.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public static WCTDataSourceDB getSharedInstance() throws ClassNotFoundException, SQLException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        if (instance == null) {
            instance = new WCTDataSourceDB();
        }
        return instance;
    }

    
    /**
     * Initializes the database, which can be in-memory or file based - USE this instead of constructor for 'singleton'.
     * The singleton instance is shared between all classes in the application.  If the dbLocation has changed,
     * this method create a new singleton instance.
     * @param dbLocation "mem:wrt" for In-Memory, or "file:/path/to/db" for persistant
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public static WCTDataSourceDB getSharedInstance(String dbLocation) throws ClassNotFoundException, SQLException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        if (instance == null || (! dbLocation.equals(instance.getDbLocation())) ) {
            instance = new WCTDataSourceDB(dbLocation);
        }
        return instance;
    }
    
    
    
    /**
     * Initializes a new in-memory database instance.  
     * Using the shared instance is preferred (see 'getSharedInstance').
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public static WCTDataSourceDB getNewInstance() throws ClassNotFoundException, SQLException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        return new WCTDataSourceDB();
    }

    
    /**
     * Initializes the database, which can be in-memory or file based.
     * Using the shared instance is preferred (see 'getSharedInstance').
     * @param dbLocation "mem:wrt" for In-Memory, or "file:/path/to/db" for persistant
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public static WCTDataSourceDB getNewInstance(String dbLocation) throws ClassNotFoundException, SQLException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        return new WCTDataSourceDB(dbLocation);
    }
    
    
    public String getDbLocation() {
        return this.dbLocation;
    }

    private void createTables() throws SQLException {
    	String createTableScript = 
            "CREATE CACHED TABLE SCAN_RESULTS("+
            " SOURCE_ID INT, "+
            " URL VARCHAR(255), "+
            " DATA_TYPE VARCHAR(255), "+
            " DISPLAY_NAME VARCHAR(255), "+
            " PRODUCT_ID VARCHAR(255), "+
            " TIMESTAMP_STRING VARCHAR(255) "+
            ");";
        stmt.execute(createTableScript);

        createTableScript = 
            "CREATE CACHED TABLE SCAN_SOURCE("+
            " SOURCE_ID INT PRIMARY KEY, "+
            " SOURCE_DESC VARCHAR(255), "+
            " SOURCE_TYPE VARCHAR(255), "+
            " SOURCE VARCHAR(255), "+
            " DATETIME_ADDED VARCHAR(255) "+
            ");";
        stmt.execute(createTableScript);

        connection.commit();
    }
    
    
    private void init() throws ClassNotFoundException, SQLException {

        System.out.println("INIT DATABASE AT: "+dbLocation);
        
        // Use following for H2 database
        //Class.forName("org.h2.Driver");
        //this.connection = DriverManager.getConnection("jdbc:h2:mem:wrt", "sa", "");
        
        // Use following for HSQLDB database
        Class.forName("org.hsqldb.jdbcDriver");
        this.connection = DriverManager.getConnection("jdbc:hsqldb:"+dbLocation+"", "sa", "");
        connection.setAutoCommit(false); 

        this.stmt = connection.createStatement();

        // this will fail if table already exists
        try {
            
            createTables();

            connection.commit();
        
        } catch (SQLException e) {
            System.out.println(e);
        }
        
        // get sequence id - we are doing it this way to avoid a database specific sequence
        String querySequenceString = "select max(SOURCE_ID) as SEQ_LAST_VALUE from SCAN_SOURCE";
        ResultSet rs = stmt.executeQuery(querySequenceString);
        if (rs.next()) {
            this.sourceSequence = rs.getInt("SEQ_LAST_VALUE");
            System.out.println("SETTING SEQUENCE LOCATION TO: "+sourceSequence);
        }
        
        this.prepStmtScanResults = connection.prepareStatement("insert into SCAN_RESULTS "+
                "(SOURCE_ID, URL, DATA_TYPE, DISPLAY_NAME, PRODUCT_ID, TIMESTAMP_STRING)"+
                " values (?, ?, ?, ?, ?, ?)");

        this.prepStmtScanSource = connection.prepareStatement("insert into SCAN_SOURCE "+
                "(SOURCE_ID, SOURCE_DESC, SOURCE_TYPE, SOURCE, DATETIME_ADDED)"+
                " values (?, ?, ?, ?, ?)");
        
    }

    
    /**
     * Add a HAS Job order number to the database.  
     * This currently represents an FTP folder on the 2-week temporary NCDC FTP site.
     * @param description A user-supplied description of this order
     * @param hasJobString
     * @throws SQLException
     * @throws IOException
     * @throws FtpException
     * @throws DecodeException
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public void addHASJob(String description, String hasJobString) throws SQLException, IOException, FtpException, DecodeException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {
        prepStmtScanSource.setInt(1, ++sourceSequence);
        prepStmtScanSource.setString(2, description);
        prepStmtScanSource.setString(3, NCDC_HAS_FTP);
        prepStmtScanSource.setString(4, hasJobString);
        prepStmtScanSource.setString(5, new Date().toString());
        addScanResults(sourceSequence, wctDirScanner.scanHASDirectory(hasJobString));
        // only execute if no errors in scanning
        prepStmtScanSource.execute();
        connection.commit();
    }
    /**
     * Add a local directory to the database.
     * @param localDir
     * @throws SQLException
     * @throws IOException
     * @throws DecodeException
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public void addLocalDirectory(String description, File localDir) throws SQLException, IOException, DecodeException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {
        prepStmtScanSource.setInt(1, ++sourceSequence);
        prepStmtScanSource.setString(2, description);
        prepStmtScanSource.setString(3, LOCAL_DISK);
        prepStmtScanSource.setString(4, localDir.toString());
        prepStmtScanSource.setString(5, new Date().toString());
        addScanResults(sourceSequence, wctDirScanner.scanLocalDirectory(localDir, false));
        // only execute if no errors in scanning
        prepStmtScanSource.execute();  
        connection.commit();
    }
    /**
     * Add a URL to the database.  If this is ftp://, the FTP functions will be
     * used to get a directory listing.  If this is http://, the default
     * apache directory listing will be parsed.  Beware, this is not 100%
     * effective.
     * @param urlDir
     * @throws SQLException
     * @throws IOException
     * @throws FtpException
     * @throws DecodeException
     * @throws ParseException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public void addURLDirectory(String description, URL urlDir) throws SQLException, IOException, FtpException, DecodeException, ParseException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {
        prepStmtScanSource.setInt(1, ++sourceSequence);
        prepStmtScanSource.setString(2, description);
        prepStmtScanSource.setString(3, URL_DIRECTORY);
        prepStmtScanSource.setString(4, urlDir.toString());
        prepStmtScanSource.setString(5, new Date().toString());
        addScanResults(sourceSequence, wctDirScanner.scanUrlDirectory(urlDir, false));
        // only execute if no errors in scanning
        prepStmtScanSource.execute();  
        connection.commit();
    }
    /**
     * Add a THREDDS 'catalog.xml' catalog to the database.
     * @param threddsCatalogURL
     * @throws SQLException
     * @throws IOException
     * @throws DecodeException
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public void addTHREDDSCatalog(String description, URL threddsCatalogURL) throws SQLException, IOException, DecodeException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {
        prepStmtScanSource.setInt(1, ++sourceSequence);
        prepStmtScanSource.setString(2, description);
        prepStmtScanSource.setString(3, THREDDS);
        prepStmtScanSource.setString(4, threddsCatalogURL.toString());
        prepStmtScanSource.setString(5, new Date().toString());
        addScanResults(sourceSequence, wctDirScanner.scanTHREDDSDirectory(threddsCatalogURL));
        // only execute if no errors in scanning
        prepStmtScanSource.execute();  
        connection.commit();
    }
    /**
     * Add a remote directory via SSH to the database.
     * @param host
     * @param user
     * @param pass
     * @param dir
     * @throws Exception
     */
    public void addSSHDirectory(String host, String user, String pass, String dir) throws Exception {
        throw new Exception("NOT YET IMPLEMENTED");
    }

    
    
    
    private void addScanResults(int sourceId, ScanResults[] scanResultsArray) throws SQLException {

        
        
        for (int n=0; n<scanResultsArray.length; n++) {

            ScanResults scanResult = scanResultsArray[n];

            try {
                prepStmtScanResults.setInt(1, sourceId);
                prepStmtScanResults.setString(2, scanResult.getUrl().toString());
                prepStmtScanResults.setString(3, scanResult.getDataType().toString());
                prepStmtScanResults.setString(4, scanResult.getDisplayName());
                prepStmtScanResults.setString(5, scanResult.getProductID());
                prepStmtScanResults.setString(6, scanResult.getTimestamp());
    
                prepStmtScanResults.addBatch();
            } catch (Exception e) {
                System.out.println("["+n+"] ERROR LOADING: "+scanResult);
            }
        }
        
        prepStmtScanResults.executeBatch();
        connection.commit();
    }
    
    
    /**
     * Shuts down the database and writes data to persistant disk, if
     * a file-based database is used.
     * @throws SQLException
     */
    public void shutdown() throws SQLException {
    
        String shutdownSQL = "SHUTDOWN COMPACT";
        stmt.execute(shutdownSQL);
        
    }
    
    /**
     * Executes custom User-defined SQL 'select' Query.
     * @param sql
     * @throws SQLException
     * @return
     */
    public ResultSet executeQuery(String sqlQuery) throws SQLException {
        return stmt.executeQuery(sqlQuery);
    }
    
    /**
     * Dumps a custom User-defined SQL 'select' Query.
     * @param sql
     * @throws SQLException
     * @return
     */
    public String dumpQuery(String sqlQuery) throws SQLException {
        ResultSet rs = executeQuery(sqlQuery);
        StringBuffer sb = new StringBuffer();
        ResultSetMetaData rsMeta = rs.getMetaData();
        for (int n=0; n<rsMeta.getColumnCount()-1; n++) {
            sb.append(rsMeta.getColumnName(n+1)+"\t");
        }
        sb.append(rsMeta.getColumnName(rsMeta.getColumnCount())+"\n");
        
        while (rs.next()) {
            for (int n=0; n<rsMeta.getColumnCount()-1; n++) {
                sb.append(rs.getString(n+1)+"\t");
            }
            sb.append(rs.getString(rsMeta.getColumnCount())+"\n");
        }
        return sb.toString();
    }
    
    /**
     * Delete an entry in both tables based on the SOURCE_ID
     * @param id
     * @throws SQLException
     */
    public void deleteID(int id) throws SQLException {
        stmt.execute("DELETE FROM SCAN_RESULTS WHERE SOURCE_ID="+id);
        stmt.execute("DELETE FROM SCAN_SOURCE WHERE SOURCE_ID="+id);
        connection.commit();
    }
    
    public void clearDatabase() throws SQLException {
        stmt.execute("DROP TABLE SCAN_RESULTS");
        stmt.execute("DROP TABLE SCAN_SOURCE");
        
        createTables();
    }
    
    /**
     * Dumps a summary of the table columns and types.
     * @return
     */
    public String dumpSchemas() {
        String tableScript = 
            "TABLE SCAN_RESULTS( \n"+
            " SOURCE_ID INT, \n"+
            " URL VARCHAR(255), \n"+
            " DATA_TYPE VARCHAR(255), \n"+
            " DISPLAY_NAME VARCHAR(255), \n"+
            " PRODUCT_ID VARCHAR(255), \n"+
            " TIMESTAMP_STRING VARCHAR(255) \n"+
            "); \n\n";

        tableScript += 
            "TABLE SCAN_SOURCE( \n"+
            " SOURCE_ID INT PRIMARY KEY, \n"+
            " SOURCE_DESC VARCHAR(255), \n"+
            " SOURCE_TYPE VARCHAR(255), \n"+
            " SOURCE VARCHAR(255), \n"+
            " DATETIME_ADDED VARCHAR(255) \n"+
            "); \n\n";
        
        tableScript += "TO JOIN: SCAN_RESULTS.SOURCE_ID = SCAN_SOURCE.SOURCE_ID";
        return tableScript;
    }
    
    /**
     * Dumps each entry along with the source information (directory/url/has-job).
     * @return
     */
    public String dumpEntries() {
        try {

            StringBuffer sb = new StringBuffer();
            String selectQuery = "SELECT sr.SOURCE_ID, ss.SOURCE_DESC, sr.DISPLAY_NAME, sr.PRODUCT_ID, sr.DATA_TYPE, ss.SOURCE_TYPE, ss.SOURCE, ss.DATETIME_ADDED "+
                "FROM SCAN_RESULTS sr, SCAN_SOURCE ss "+
                "WHERE sr.SOURCE_ID = ss.SOURCE_ID";
            ResultSet rs = stmt.executeQuery(selectQuery);
            
            sb.append("SOURCE_ID\t");
            sb.append("SOURCE_DESC\t");
            sb.append("DISPLAY_NAME\t");
            sb.append("PRODUCT_ID\t");
            sb.append("DATA_TYPE\t");
            sb.append("SOURCE_TYPE\t");
            sb.append("SOURCE\t");
            sb.append("DATETIME_ADDED\n");
            
            //if(rs.next()) {
            while(rs.next()) {
             
                sb.append(rs.getString("SOURCE_ID")+"\t");
                sb.append(rs.getString("SOURCE_DESC")+"\t");
                sb.append(rs.getString("DISPLAY_NAME")+"\t");
                sb.append(rs.getString("PRODUCT_ID")+"\t");
                sb.append(rs.getString("DATA_TYPE")+"\t");
                sb.append(rs.getString("SOURCE_TYPE")+"\t");
                sb.append(rs.getString("SOURCE")+"\t");
                sb.append(rs.getString("DATETIME_ADDED")+"\n");
                
            }
            return sb.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
    
    /**
     * Prints a summarization (count) of the number of scan entries along with
     * the source directory/url/has-job information.
     */
    public String toString() {
        try {

            StringBuffer sb = new StringBuffer();
            String selectQuery = "SELECT sr.SOURCE_ID, sr.SOURCE_DESC, count(sr.DISPLAY_NAME) as ENTRY_COUNT, ss.SOURCE_TYPE, ss.SOURCE, ss.DATETIME_ADDED "+
                "FROM SCAN_RESULTS sr, SCAN_SOURCE ss "+
                "WHERE sr.SOURCE_ID = ss.SOURCE_ID "+
                "GROUP BY sr.SOURCE_ID, ss.SOURCE_TYPE, ss.SOURCE, ss.DATETIME_ADDED";
            ResultSet rs = stmt.executeQuery(selectQuery);
            
            sb.append("ID\t");
            sb.append("SOURCE_DESC\t");
            sb.append("COUNT\t");
            sb.append("SOURCE_TYPE\t");
            sb.append("SOURCE\t");
            sb.append("DATETIME_ADDED\n");
            
            //if(rs.next()) {
            while(rs.next()) {
             
                sb.append(rs.getString("SOURCE_ID")+"\t");
                sb.append(rs.getString("SOURCE_DESC")+"\t");
                sb.append(rs.getString("ENTRY_COUNT")+"\t");
                sb.append(rs.getString("SOURCE_TYPE")+"\t");
                sb.append(rs.getString("SOURCE")+"\t");
                sb.append(rs.getString("DATETIME_ADDED")+"\n");
                
            }
            return sb.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
    
}
