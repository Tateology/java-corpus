package steve.test.swath;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/********************************************************************************
 * Simplifies output of XML document
 * @author lou.vasquez Modified by Mike Urzen
 ********************************************************************************/
public class NppProfile
{
  private Vector<Integer> vFieldLocInt = new Vector<Integer>();

  public Vector<String[][]> vFields = new Vector<String[][]>();
  public Vector<String[][]> vGlobAtts = new Vector<String[][]>();
  private String[][] xmlArray = null;

  /*************************************************************************************
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   ***********************************************************************************/
  protected NppProfile(String xmlPath) throws ParserConfigurationException, SAXException, IOException
  {
    File file = new File(xmlPath);
    DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    Document doc = null;

    db = dbFact.newDocumentBuilder();
    doc = db.parse(file);
    
    if(doc == null)
      return;

    doc.getDocumentElement().normalize();
    Vector<String> vXml = new Vector<String>();
    
    listNodeItems(doc, 0, vXml);

    xmlArray = new String[vXml.size()][2];
    xmlArray[0][0] = "";
    
    int x = 0;
    for(int v = 0; v < vXml.size(); v++)
    {
      //System.out.println(vXml.elementAt(v) + "  " + vXml.elementAt(v + 1));

      if(vXml.elementAt(v).equalsIgnoreCase("Field"))  // Save loc of "File" node
        vFieldLocInt.add(x);
      
      if(vXml.elementAt(v).startsWith("value:") && xmlArray[x][1] == null)
      {
        if(! vXml.elementAt(v).substring(6, vXml.elementAt(v).length()).equalsIgnoreCase(" "))
        {
          xmlArray[x][1] = vXml.elementAt(v).substring(6, vXml.elementAt(v).length());  // Value
          x++;
          xmlArray[x][0] = ""; 
        }
      }
      else
        xmlArray[x][0] = vXml.elementAt(v);  // Build name chain
    }


    for(int g = 0; g < vFieldLocInt.elementAt(0); g++) // Get atts with a value before the "Field" tags start. 
      if(xmlArray[g][1] != null)
      {
        String[][] ss = new String[1][2];
        ss[0] = xmlArray[g];
        vGlobAtts.add(ss);  // Global atts
      }
    
    String header = "";
    Vector<String[][]> vf = null;
    for(int v = 0; v < vFieldLocInt.size() - 1; v++)
    {
      vf = new Vector<String[][]>();
      
      for(int f = vFieldLocInt.elementAt(v); f < vFieldLocInt.elementAt(v + 1); f++)
      {
        //System.out.println(v + "  " + f + "  " + xmlArray[f][0]);
        
        if(xmlArray[f][1] == null)
          header = xmlArray[f][0] + "_";
        else
        {
          String[][] ss = new String[1][2];
          ss[0] = xmlArray[f];
          ss[0][0] = header + ss[0][0];
          vf.add(ss);
        }
      }
      String[][] a = new String[vf.size()][2];
      
      for(int i = 0; i < vf.size(); i++)
      {
        a[i][0] = vf.elementAt(i)[0][0];
        a[i][1] = vf.elementAt(i)[0][1];
      }
      
      vFields.add(a);
    }
    
    // Rename Atts with the same name so all att names are unique.
    for(int v = 0; v < vFields.size(); v++)
      for(int a = 0; a < vFields.elementAt(v).length - 1; a++)
      {
        int iDup = 0;
        for(int aa = a + 1; aa < vFields.elementAt(v).length; aa++)
          if(vFields.elementAt(v)[a][0].equalsIgnoreCase(vFields.elementAt(v)[aa][0]))
            vFields.elementAt(v)[aa][0] = vFields.elementAt(v)[aa][0] + "_" + iDup++;
      }
  }
  /************************************************************************************************************/
  private void listNodeItems(Node nIn, int level, Vector<String> v)
  {
    NodeList nl = nIn.getChildNodes();
    Node n;

    for(int i = 0; i < nl.getLength(); i++ )
    {
      n = nl.item(i);
      if(n.getNodeType() == Node.TEXT_NODE)
      {
        //System.out.println(":" + n.getTextContent() + ":");
        String s = n.getTextContent().replace("\n", "");
        s = s.replace("\t", "");
        s = s.replace("  ", "");
        if(s.length() > 0)
          v.add("value:" + s);
      }
      else  //System.out.println(n.getNodeName());
        v.add(n.getNodeName());

      listNodeItems(n, level + 1, v);
    }
  }
}