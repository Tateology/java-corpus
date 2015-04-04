package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrBufferDecodingStream;

import ucar.unidata.io.RandomAccessFile;
import uk.ac.starlink.util.Compression;


public class DecodeRSL {
    

    private DecodeL3Header header;
    private ArrayList textCompList = new ArrayList();
    
    public DecodeRSL(DecodeL3Header header) throws DecodeException {
        this.header = header;
        decode();
    }
    
    public void decode() throws DecodeException {
        
        try {
        
            // Initiate binary buffered read
            RandomAccessFile f = header.getRandomAccessFile();
        
            // rewind 
            f.seek(0);
            // ADVANCE PAST WMO HEADER
            while (f.readShort() != -1) {
               ;
            }
            
//            System.out.println("FILE POS: "+f.getFilePointer());
//            double dlat = f.readInt() / 1000.0;
//            double dlon = f.readInt() / 1000.0;
//            System.out.println("lat lon: "+dlat+" , "+dlon);
//
            // Skip over product description block (pg. 3.26 in 2620001J.pdf)
            // This has already been decoded by DecodeL3Header
            //f.skipBytes(92);
            f.skipBytes(100);
            
            
            byte[] magic = new byte[3];
            f.read(magic);
            Compression compression = Compression.getCompression(magic);
            System.out.println(compression.toString());
            f.skipBytes(-3);
            
            long compressedFileSize = f.length()-f.getFilePointer();
            byte[] buf = new byte[(int)compressedFileSize];
            f.read(buf);
            InputStream decompStream = compression.decompress(new ByteArrayInputStream(buf));
            DataInputStream dis = new DataInputStream(decompStream);

            System.out.println("TOTAL FILE SIZE: "+f.length());
            System.out.println("COMPRESSED SIZE: "+compressedFileSize);
            
            
            
            short blockDivider = dis.readShort();
            short blockID = dis.readShort();
            int blockLen = dis.readInt();
            short numLayers = dis.readShort();
            
            System.out.println("blockDivider="+blockDivider);
            System.out.println("blockID="+blockID);
            System.out.println("blockLen="+blockLen);
            System.out.println("numLayers="+numLayers);
            
            // advance past next divider
            while (dis.readShort() != -1);
            int layerLen = dis.readInt();
            short packetCode = dis.readShort();
            
            if (packetCode != 28) {
                throw new DecodeException("Generic Product Format code=28 NOT FOUND.  Found packet code of: "+packetCode, header.getDataURL());
            }
            
            dis.readShort(); // full word aligment (pg 3-120 of 2620001J.pdf)
            int dataLen = dis.readInt();
            
            System.out.println("layerLen="+layerLen);
            System.out.println("packetCode="+packetCode);
            System.out.println("dataLen="+dataLen);

            byte[] dataBuf = new byte[layerLen];
            dis.read(dataBuf);
            
//            System.out.println(new String(dataBuf));
//            if (true) return;
            
            
            XdrBufferDecodingStream xdrBuf = new XdrBufferDecodingStream(dataBuf);
            xdrBuf.setCharacterEncoding("UTF8"); 
            xdrBuf.beginDecoding();
            
            // KGSP_SDUS42_RSLGSP_200704140756
            String name = xdrBuf.xdrDecodeString();
            String desc = xdrBuf.xdrDecodeString();
            int code = xdrBuf.xdrDecodeInt();
            int type = xdrBuf.xdrDecodeInt();
            long gentime = xdrBuf.getUnsignedInt(xdrBuf.xdrDecodeInt());
            String radarID = xdrBuf.xdrDecodeString();
            double lat = xdrBuf.xdrDecodeFloat();
            double lon = xdrBuf.xdrDecodeFloat();
            double alt = xdrBuf.xdrDecodeFloat();
            long volStartTime = xdrBuf.getUnsignedInt(xdrBuf.xdrDecodeInt());
            long elevStartTime = xdrBuf.getUnsignedInt(xdrBuf.xdrDecodeInt());
            double elevAngle = xdrBuf.xdrDecodeFloat();
            int volScanNum = xdrBuf.xdrDecodeInt();
            int opMode = xdrBuf.xdrDecodeShort();
            int vcp = xdrBuf.xdrDecodeShort();
            int elevNum = xdrBuf.xdrDecodeShort();
            // spare
            xdrBuf.xdrDecodeShort(); 
            // spare
            xdrBuf.xdrDecodeInt();
            
            
            // 1. Decode parameters
            int numParams = xdrBuf.xdrDecodeInt();
//            System.out.println("NUM PARAMS: "+numParams);
            
            for (int n=0; n<numParams; n++) {
                String paramID = xdrBuf.xdrDecodeString();
                String paramAtts = xdrBuf.xdrDecodeString();
//                System.out.println(paramID+" = "+paramAtts);
            }
            
            // 2. Decode components
            int numComponents = xdrBuf.xdrDecodeInt();
//            System.out.println("NUM COMPONENTS: "+numComponents);
            
            textCompList.clear();
            textCompList.ensureCapacity(numComponents);
            
            //System.out.println(xdrBuf.xdrDecodeInt());
            xdrBuf.xdrDecodeInt(); // read unknown int
            for (int n=0; n<numComponents; n++) {
                //System.out.println("RECORD ["+n+"]");
                xdrBuf.xdrDecodeInt();  // read unknown int
                int componentType = xdrBuf.xdrDecodeInt();
                //System.out.println("COMP. TYPE="+componentType);
                if (componentType == 4) {
                    TextComponent textComp = parseTextComponent(xdrBuf);
//                    System.out.println(textComp.toString());
//                    System.out.println(n+",  "+textComp.getParamAttributeHashMap().get("Value") + " ::: "+textComp.getText());
                    
                    textCompList.add(n, textComp);
                    
                }
                else {
//                    System.out.println("LOC="+xdrBuf.getBufferIndex()+" MAX="+xdrBuf.getBufferHighmark());
                    //System.out.println(new String(dataBuf));
//                    System.out.println("n["+n+"] UNSUPPORTED COMPONENT TYPE FOUND: "+componentType);
                    return;
                }
            }
            
            xdrBuf.endDecoding();
            
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new DecodeException("ERROR DECODING FILE: ", header.getDataURL());
        }
        
    }
    
    
    private TextComponent parseTextComponent(XdrBufferDecodingStream xdrBuf) throws IOException, OncRpcException {
        
        int unknownInt1 = xdrBuf.xdrDecodeInt();
        int unknownInt2 = xdrBuf.xdrDecodeInt();
        String paramID = xdrBuf.xdrDecodeString();
        String paramAttributeString = xdrBuf.xdrDecodeString();
        String textString = xdrBuf.xdrDecodeString();
        
        // put attributes into a hashmap for easy access
        String[] atts = paramAttributeString.split(";");
        HashMap paramAttributeHashMap = new HashMap();
        for (int n=0; n<atts.length; n++) {
            if (atts[n].length() > 0) {
                String[] att = atts[n].split("=");
                paramAttributeHashMap.put(att[0].trim(), att[1].trim());
//                System.out.println("paramAttributeHashMap.put("+att[0]+", "+att[1]+");");
            }
        }
        
        // remove DOS return and \n from text string
        textString = textString.substring(0, textString.length()-2);
        
        return new TextComponent(unknownInt1, unknownInt2, paramID, paramAttributeHashMap, textString);
        
    }
    
    
    public void setHeader(DecodeL3Header header) throws DecodeException {
        this.header = header;
        decode();
    }
    
    public DecodeL3Header getHeader() {
        return header;
    }
    
    public ArrayList getTextCompList() {
        return textCompList;
    }
    
    
    public String getRSLDisplayData() {

        StringBuffer sb = new StringBuffer();
        for (int n=0; n<textCompList.size(); n++) {
            TextComponent textComp = (TextComponent)textCompList.get(n);
            sb.append("["+(n+1)+"],  "+textComp.getParamAttributeHashMap().get("Value") + " ::: "+textComp.getText());
            sb.append("\n");
            
            //System.out.println(n+",  "+textComp.getParamAttributeHashMap().get("Value") + " ::: "+textComp.getText());
            
        }
        
        return sb.toString();
    }
    
    
    
    public class TextComponent {
        
        private int unknownInt1;
        private int unknownInt2;
        private String paramID;
        private HashMap paramAttributesHashMap;
        private String text;
        
        private TextComponent(int unknownInt1, int unknownInt2, String paramID, HashMap paramAttributesHashMap, String text) {            
            this.unknownInt1 = unknownInt1;
            this.unknownInt2 = unknownInt2;
            this.paramID = paramID;
            this.paramAttributesHashMap = paramAttributesHashMap;
            this.text = text;
        }

        public HashMap getParamAttributeHashMap() {
            return paramAttributesHashMap;
        }

        public String getParamID() {
            return paramID;
        }

        public String getText() {
            return text;
        }

        public int getUnknownInt1() {
            return unknownInt1;
        }

        public int getUnknownInt2() {
            return unknownInt2;
        }
        
        public String toString() {
            return unknownInt1 + "  // int - ? \n" +
                unknownInt2 + "  // int - ? \n" +
                paramID + "  // String - paramID \n" +
                paramAttributesHashMap.toString() + "  // HashMap - paramAttributes \n" +
                text + "  // String - text \n";
        }
        
    }
    
    
    
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        try {
            
            
            DecodeL3Header header = new DecodeL3Header();
//            URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\GenericProductFormat\\KGSP_SDUS42_RSLGSP_200704140756").toURL();         
            URL url = new java.io.File("C:\\work\\GenericProductFormat\\KGSP_SDUS42_RSLGSP_200704140756").toURL();         
            System.out.println("PROCESSING: "+url);
            header.decodeHeader(url);
            
            DecodeRSL decoder = new DecodeRSL(header);
            
            System.out.println(header.getProductType()+" : "+header.getProductCode());
            
            
//            System.out.println(" ICAO: "+header.getICAO());
//            System.out.println("  LAT: "+header.getLat());
//            System.out.println("  LON: "+header.getLon());
//            System.out.println("  ALT: "+header.getAlt());
//            System.out.println("PCODE: "+header.getProductCode());
//
//
//            
//            
//            String[] categories = header.getDataThresholdStringArray();
//            for (int n=0; n<categories.length; n++) {
//               System.out.println("categories["+n+"] = "+categories[n]);  
//            }
//            
//            short[] prodSpec = header.getProductSpecificValueArray();
//            for (int n=0; n<prodSpec.length; n++) {
//               System.out.println("prodSpec["+n+"] = "+prodSpec[n]);  
//            }

            
            
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DecodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
