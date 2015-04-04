package gov.noaa.ncdc.wct.decoders.cdm.conv;

import java.io.IOException;
import java.io.Reader;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.util.CancelTask;

public class WCTNcmlReader extends NcMLReader {

    
    static boolean debugXML = false;
    static boolean showParsedXML = false;
    static boolean debugOpen = false;
    
    /**
     * Use NCML to modify the dataset, getting NcML from a URL
     *
     * @param ncDataset    modify this dataset
     * @param reader  Reader providing the NcML document
     * @param cancelTask   allow user to cancel task; may be null
     * @throws IOException on read error
     */
    static public void wrapNcML(NetcdfDataset ncDataset, Reader ncmlReader, CancelTask cancelTask) throws IOException {
      org.jdom2.Document doc;
      try {
        SAXBuilder builder = new SAXBuilder();
        doc = builder.build(ncmlReader);
      } catch (JDOMException e) {
        throw new IOException(e.getMessage());
      }
      
      
      if (debugXML) System.out.println(" SAXBuilder done");

      if (showParsedXML) {
        XMLOutputter xmlOut = new XMLOutputter();
        System.out.println("*** NetcdfDataset/showParsedXML = \n" + xmlOut.outputString(doc) + "\n*******");
      }

      Element netcdfElem = doc.getRootElement();

      NcMLReader reader = new NcMLReader();
//      reader.readNetcdf(ncmlLocation, ncDataset, ncDataset, netcdfElem, cancelTask);
//      NetcdfDataset ncd = readNcML(null, netcdfElem, cancelTask);
      reader.readNetcdf(ncDataset.getLocation(), ncDataset, ncDataset, netcdfElem, cancelTask);

      if (debugOpen) System.out.println("***NcMLReader.wrapNcML result= \n" + ncDataset);
      
//      return ncd;
    }
    
}
