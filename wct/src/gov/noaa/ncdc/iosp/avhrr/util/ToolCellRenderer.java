package gov.noaa.ncdc.iosp.avhrr.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ToolCellRenderer extends DefaultTableCellRenderer {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
            boolean focused, int row, int column) {
    	
              setEnabled(table == null || table.isEnabled());

              setForeground(Color.black);
              setBackground(Color.white);
      
              boolean valid = (Boolean)table.getValueAt(row, 4);
              String converted = (String)table.getValueAt(row,8);
              
              if(valid){
            	  if("YES".equals(converted)){
            		  setBackground(Color.green);
            	  }else if("FAILED".equals(converted)){
            		  setBackground(Color.red);
            	  }else{
            		  setBackground(Color.white);
            	  }
              }else{
            	  setBackground(Color.red);
              }
                          
              super.getTableCellRendererComponent(table, value, selected, focused, row, column);

              return this;
      }
  }

