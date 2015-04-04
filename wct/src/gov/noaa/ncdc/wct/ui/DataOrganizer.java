//package gov.noaa.ncdc.wct.ui;
//
//import gov.noaa.ncdc.wct.ui.table.DbFileTableModel;
//import gov.noaa.ncdc.wct.ui.table.DbSourceTableModel;
//import gov.noaa.ncdc.wct.ui.table.DbSummaryTableModel;
//
//import java.awt.BorderLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.net.URL;
//import java.sql.SQLException;
//
//import javax.swing.JButton;
//import javax.swing.JDialog;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTabbedPane;
//import javax.swing.JTable;
//import javax.swing.JTextField;
//
//public class DataOrganizer extends JDialog {
//
//
//    private DbSourceTableModel dbSourceTableModel;
//    private DbFileTableModel dbFileTableModel;
//    private JTable sourceTable, fileTable;
//    private DataSourcePanel dataSourcePanel = 
//    	new DataSourcePanel(new SubmitButtonListener(), new SortByListener(), new FilterKeyListener());
//    private final JDialog dialog = new JDialog(this, "Add Data", true);
//    
//    private WCTViewer nexview;
//    
//    public DataOrganizer(WCTViewer parent) {
//        super(parent, "Data Organizer", false);
//        this.nexview = parent;
//        createGUI();
//    }
//
//
//    private void createGUI() {    	
//        
////      1. Set up Source tab
//        
//        JPanel sourcePanel = new JPanel();
//        sourcePanel.setLayout(new BorderLayout());
//        
//        try {
//
//            dbSourceTableModel = new DbSourceTableModel();
//            sourceTable = new JTable(dbSourceTableModel);
//            sourceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//            
//            JButton addButton = new JButton("Add Data");
//            addButton.addActionListener(new AddButtonListener());
//            JButton removeButton = new JButton("Remove");
//            removeButton.addActionListener(new RemoveButtonListener());
//            JButton removeAllButton = new JButton("Remove All");
//            removeAllButton.addActionListener(new RemoveAllButtonListener());
//            JPanel buttonPanel = new JPanel();
//            buttonPanel.add(addButton);
//            buttonPanel.add(removeButton);
//            buttonPanel.add(removeAllButton);
//            
//            sourcePanel.add(buttonPanel, "South");
//            sourcePanel.add(sourceTable.getTableHeader(), "North");
//            sourcePanel.add(new JScrollPane(sourceTable), "Center");
//
//            // setup the add data dialog
//            dialog.add(dataSourcePanel);
//
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            sourcePanel.add(new JScrollPane(new JLabel("<html>ERROR ADDING SOURCE TABLE<br><pre>"+e+"</pre></html>")), "Center");
//        }
//
//        
//        
//        // 2. Set up data tab
//        
//        JPanel filePanel = new JPanel();
//        filePanel.setLayout(new BorderLayout());
//        
//        try {
//
//            dbFileTableModel = new DbFileTableModel();
//            fileTable = new JTable(dbFileTableModel);
//            fileTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//            
//            JButton viewButton = new JButton("View Data");
//            viewButton.addActionListener(new LoadButtonListener());
//            JButton exportButton = new JButton("Export Data");
////            exportButton.addActionListener(new ExportButtonListener());
//            JPanel buttonPanel = new JPanel();
//            buttonPanel.add(viewButton);
//            buttonPanel.add(exportButton);
//            
//            filePanel.add(buttonPanel, "South");
//            filePanel.add(fileTable.getTableHeader(), "North");
//            filePanel.add(new JScrollPane(fileTable), "Center");
//
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            filePanel.add(new JScrollPane(new JLabel("<html>ERROR ADDING FILE TABLE<br><pre>"+e+"</pre></html>")), "Center");
//        }
//
//        JPanel summaryPanel = new JPanel();
//
//        DbSummaryTableModel dbSummaryTableModel = new DbSummaryTableModel();
//        JTable summaryTable = new JTable();
//        summaryTable.setModel(dbSummaryTableModel);
//        summaryPanel.add(summaryTable);
//
//        JTabbedPane tabPane = new JTabbedPane();
//        tabPane.add(sourcePanel, "Source");
//        tabPane.add(filePanel, "Data");
//        tabPane.add(summaryPanel, "Summary");
//
//        this.getContentPane().add(tabPane);
//    }
//
//
//    public void closeDb() throws SQLException {
//        if (dbSourceTableModel != null) {
//            dbSourceTableModel.closeDb();
//        }
//    }
//
//
//
//    
//    class AddButtonListener implements ActionListener {
//        public void actionPerformed(ActionEvent evt) {
//            dialog.pack();
//            dialog.setVisible(true);
//            dialog.setLocation(dialog.getParent().getLocation().x+25, dialog.getParent().getLocation().y+25);
//        }
//    }
//    class RemoveButtonListener implements ActionListener {
//        public void actionPerformed(ActionEvent evt) {
//            try {
//                dbSourceTableModel.removeEntry(sourceTable.getSelectedRows());
//                dbSourceTableModel.refresh();
//                dbFileTableModel.refresh();
//                
//                sourceTable.updateUI();
//                sourceTable.clearSelection();
//                
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(dialog, "REMOVE DATA ERROR:\n"+e, "REMOVE DATA ERROR", JOptionPane.ERROR_MESSAGE);         
//            }
//        }
//    }
//    class RemoveAllButtonListener implements ActionListener {
//        public void actionPerformed(ActionEvent evt) {
//        }
//    }
//    class SubmitButtonListener implements ActionListener {
//        public void actionPerformed(ActionEvent evt) {
//            try {
//                System.out.println(dataSourcePanel.getDataType() +" ::: "+dataSourcePanel.getDataLocation());
//                dbSourceTableModel.addEntry(dataSourcePanel.getDataType(), dataSourcePanel.getDataDescription(), dataSourcePanel.getDataLocation());
//                dbSourceTableModel.refresh();
//                dbFileTableModel.refresh();
//                
//                sourceTable.updateUI();
//                fileTable.updateUI();
//                
//                
//                dialog.setVisible(false);
//                
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(dialog, "ADD DATA ERROR:\n"+e, "ADD DATA ERROR", JOptionPane.ERROR_MESSAGE);         
//            }
//            
//        }
//    }
//
//    /**
//     * Does absolutely nothing
//     * @author steve.ansari
//     *
//     */
//    class SortByListener implements ActionListener {
////        @Override
//        public void actionPerformed(ActionEvent e) {
//        }
//    }
//
//    
//    
//    class LoadButtonListener implements ActionListener {
//        public void actionPerformed(ActionEvent evt) {
//            try {
//            	int[] selectedIndices = fileTable.getSelectedRows();
//                
//            	if (selectedIndices.length == 0) {
//            		return;
//            	}
//            	URL url = new URL(
//            			dbFileTableModel.getValueAt(selectedIndices[0], dbFileTableModel.getColumnCount()-1).toString());
//            	System.out.println("LOADING: "+url);
//            	nexview.loadFile(url);
//            	
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(dialog, "LOAD DATA ERROR:\n"+e, "LOAD DATA ERROR", JOptionPane.ERROR_MESSAGE);         
//            }
//            
//        }
//    }
//    
//    
//    
//    
//    
//    
//    final class FilterKeyListener implements KeyListener {      
//        
//		@Override
//		public void keyPressed(KeyEvent e) {
//		}
//		@Override
//		public void keyReleased(KeyEvent e) {
////			resultsList.
//			System.out.println("I'm sorting on "+((JTextField) e.getSource()).getText());
//			
//		}
//		@Override
//		public void keyTyped(KeyEvent e) {			
//		}    	
//    }
//
//}
