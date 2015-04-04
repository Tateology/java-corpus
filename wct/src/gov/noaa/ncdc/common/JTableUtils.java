package gov.noaa.ncdc.common;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class JTableUtils {

    
    public static String getText(JTable table) {
        
        TableModel model = table.getModel();
        
        StringBuffer sb = new StringBuffer();
        
        for (int i=0; i<model.getColumnCount(); i++) {
        	sb.append(model.getColumnName(i)+"\t");
        }        
        sb.append("\n");
        
        
        for (int j=0; j<model.getRowCount(); j++) {
            for (int i=0; i<model.getColumnCount(); i++) {
                sb.append(model.getValueAt(j, i)+"\t");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

    
    public static String getHtml(JTable table) {
        
        TableModel model = table.getModel();
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("<html><head></head><body>");
        sb.append("<table>");
        
        sb.append("<tr>");
        for (int i=0; i<model.getColumnCount(); i++) {
        	sb.append("<th>"+model.getColumnName(i)+"</th>");
        }        
        sb.append("</tr>");
        
        
        for (int j=0; j<model.getRowCount(); j++) {
            sb.append("<tr>");
            for (int i=0; i<model.getColumnCount(); i++) {
                sb.append("<td>"+model.getValueAt(j, i)+"</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table></body></html>");
        
        return sb.toString();
    }

}
