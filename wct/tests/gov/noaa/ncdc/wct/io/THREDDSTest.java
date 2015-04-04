package gov.noaa.ncdc.wct.io;
//package gov.noaa.ncdc.ndit.io;
//
//
//import java.util.*;
//import thredds.catalog.*;
//
//import thredds.catalog.ui.*;
//import javax.swing.*;
//
//public class THREDDSTest {
//
//
//   public static void main(String[] args) {
//      
//      testAPI();
//      //testUI();
//      //testTree();
//   
//   }
//   
//   
//   public static void testTree() {
//      
//      final CatalogTreeView tree = new CatalogTreeView();
//      tree.addPropertyChangeListener(  new java.beans.PropertyChangeListener() {
//         public void propertyChange( java.beans.PropertyChangeEvent e) {
//            if (e.getPropertyName().equals("Selection")) {
//               System.out.println("Selection");
//           //    ...
//            } else if (e.getPropertyName().equals("Dataset")) {
//               System.out.println("Dataset");
//               java.util.List access = tree.getSelectedDataset().getAccess();
//               InvAccess invAccess = ((InvAccess)access.get(0));
//               System.out.println("getStandardUrlName[0]  "+invAccess.getStandardUrlName());
//               
//           //    ...
//            } else if (e.getPropertyName().equals("Catalog")) {
//               System.out.println("Catalog");
//           //    ...
//            }
//         }
//      });      
//      
//      
//      String dirString = "http://motherlode.ucar.edu:8080/thredds/idd/nexrad/level3/catalog.xml"; 
//      //String dirString = "http://motherlode.ucar.edu:8080/thredds/idd/nexrad/level3/N1R/TLX/20060531/catalog.xml"; 
//      InvCatalogFactory factory = new InvCatalogFactory("default", true);
//      InvCatalogImpl catalog = (InvCatalogImpl) factory.readXML(dirString);
//      tree.setCatalog(catalog);
//      
//      
//      
//      JFrame frame = new JFrame();
//      frame.getContentPane().add(tree);
//      frame.pack();
//      frame.setVisible(true);
//      
//      
//   }
//   
//   public static void testUI() {
//      
//      //ThreddsDatasetChooser datasetChooser = new ThreddsDatasetChooser( null, null);
//      //JDialog datasetChooserDialog = datasetChooser.makeDialog(null, "Open THREDDS dataset", true);
//      //datasetChooserDialog.show();      
//   }
//   
//   public static void testAPI() {
//
//            //String dirString = "http://motherlode.ucar.edu:8080/thredds/idd/nexrad/level3/N1R/TLX/20060531/catalog.xml"; 
//            String dirString = "http://motherlode.ucar.edu:8080/thredds/catalog/nexrad/level3/N1R/OHX/20060804/catalog.xml";
//
//
//            boolean validate = true;
//            
//            InvCatalogFactory factory = new InvCatalogFactory("default", validate);
//            InvCatalogImpl catalog = (InvCatalogImpl) factory.readXML(dirString);
//            StringBuffer buff = new StringBuffer();
//            if (!catalog.check( buff)) {
//               javax.swing.JOptionPane.showMessageDialog(null, "Invalid catalog <"+ dirString+">\n"+
//               buff.toString());
//               return;
//            }
//
//            //System.out.println(catalog.dump());
//            
//            java.util.List datasets = catalog.getDatasets();
//            for (int n=0; n<datasets.size(); n++) {
//               InvDataset invDataset = ((InvDataset)datasets.get(n));
//               System.out.println("DATASET getCatalogUrl: ["+n+"]  "+invDataset.getCatalogUrl());
//               System.out.println("DATASET getName: ["+n+"]  "+invDataset.getName());
//               java.util.List datasets2 = invDataset.getDatasets();
//               for (int i=0; i<datasets2.size(); i++) {
//                  InvDataset invDataset2 = ((InvDataset)datasets2.get(i));
//                  System.out.println("DATASET2 getCatalogUrl: ["+i+"]  "+invDataset2.getCatalogUrl());
//                  System.out.println("DATASET2 getName: ["+i+"]  "+invDataset2.getName());
//
//                  java.util.List access2 = invDataset2.getAccess();
//                  for (int j=0; j<access2.size(); j++) {
//                     InvAccess invAccess2 = ((InvAccess)access2.get(j));
//                     System.out.println("ACCESS2 getUrlPath: ["+j+"]  "+invAccess2.getUrlPath());
//                     System.out.println("ACCESS2 getStandardUrlName: ["+j+"]  "+invAccess2.getStandardUrlName());
//                  }
//               }
//            }
//            
//   }            
//}
