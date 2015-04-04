package gov.noaa.ncdc.wct.io;



import java.io.File;

import org.junit.Test;

public class TestWCTDatabase {

    
    
    
    public static void main(String[] args) {
        
        TestWCTDatabase test = new TestWCTDatabase();
        test.testDataOrganizer();
        
    }
    
    
    @Test
    public void testDataOrganizer() {
        
//        try {
//            
//            final DataOrganizer dataOrg = new DataOrganizer(null);
//            
//            //dataOrg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            
//            dataOrg.addWindowListener(new WindowListener() {
//
//                public void windowActivated(WindowEvent arg0) {
//                }
//                public void windowClosed(WindowEvent arg0) {
//                }
//                public void windowClosing(WindowEvent arg0) {
//                    try {
//                        System.out.println("CLOSING DATABASE");
//                        dataOrg.closeDb();
//                        System.exit(1);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                public void windowDeactivated(WindowEvent arg0) {
//                }
//                public void windowDeiconified(WindowEvent arg0) {
//                }
//                public void windowIconified(WindowEvent arg0) {
//                }
//                public void windowOpened(WindowEvent arg0) {
//                }
//                
//            });
//            
//            dataOrg.setSize(750, 350);
//            dataOrg.setVisible(true);
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    
    @Test
    public void testDB() {
        try {
        
            String tempdir = System.getProperty("java.io.tmpdir");

            if ( !(tempdir.endsWith(File.separator)) ) {
               tempdir = tempdir + File.separator;
            }
            
            tempdir += "jnx"+File.separator+"datadb";
            
            System.out.println("CREATING/OPENING DATABASE AT: "+tempdir);
            
            WCTDataSourceDB wrtDataSourceDB = WCTDataSourceDB.getSharedInstance(new File(tempdir).toURL().toString());
            //WRTDataSourceDB wrtDataSourceDB = new WRTDataSourceDB("file:/Documents and Settings/steve.ansari/Local Settings/Temp/jnx");
            
            
            //wrtDataSourceDB.clearDatabase();
            
            System.out.println("ADDING LOCAL DIR");
            wrtDataSourceDB.addLocalDirectory("TEST 1", new File("H:\\ViewerData\\HAS999900001"));
            wrtDataSourceDB.addLocalDirectory("TEST 2", new File("H:\\ViewerData\\HAS999900002"));
            wrtDataSourceDB.addLocalDirectory("TEST 3", new File("H:\\ViewerData\\HAS999900003"));
            //wrtDataSourceDB.deleteID(7);
//            System.out.println("ADDING THREDDS CATALOG");
//            wrtDataSourceDB.addTHREDDSCatalog(
//                    new URL("http://motherlode.ucar.edu:8080/thredds/catalog/nexrad/level3/N1R/GSP/20070609/catalog.xml"));
//            System.out.println("ADDING HAS JOB");
//            wrtDataSourceDB.addHASJob("001005501");
            
            System.out.println("SUMMARY QUERY START (.toString())");
            System.out.println(wrtDataSourceDB);
            
            System.out.println(wrtDataSourceDB.dumpSchemas());
            
//            System.out.println(wrtDataSourceDB.dumpQuery("SELECT * FROM SCAN_RESULTS"));
            
            wrtDataSourceDB.shutdown();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

}
