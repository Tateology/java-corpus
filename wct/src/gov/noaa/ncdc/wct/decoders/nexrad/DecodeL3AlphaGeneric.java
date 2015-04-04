package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.common.Hex;
import gov.noaa.ncdc.wct.decoders.DecodeException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import ucar.unidata.io.RandomAccessFile;
import uk.ac.starlink.util.Compression;


public class DecodeL3AlphaGeneric {
    
    private static final Logger logger = Logger.getLogger(DecodeL3AlphaGeneric.class.getName());

    private DecodeL3Header header;
    private DataInputStream dis;
    
    private boolean readBlock1 = true;
    private boolean readBlock2 = true;
    private boolean readBlock3 = true;
    
    private String block1Text;
    private String block2Text;
    private String block3Text;
    
    private long decodingTimeInMillis;
    
    public DecodeL3AlphaGeneric() throws DecodeException {
    }
    
    public void decode(DecodeL3Header header) throws DecodeException {
        
        this.header = header;
        
        long startTime = System.currentTimeMillis();
        
        try {
        
            // Initiate binary buffered read
            RandomAccessFile f = header.getRandomAccessFile();
        
            // rewind 
            f.seek(0);
            // ADVANCE PAST WMO HEADER
            while (f.readShort() != -1) {
               ;
            }
            
//            logger.info("FILE POS: "+f.getFilePointer());
//            double dlat = f.readInt() / 1000.0;
//            double dlon = f.readInt() / 1000.0;
//            logger.info("lat lon: "+dlat+" , "+dlon);
//
            // Skip over product description block (pg. 3.26 in 2620001J.pdf)
            // This has already been decoded by DecodeL3Header
            try {
                
                //f.skipBytes(92);
                f.skipBytes(100);
            } catch (Exception e) {
                block1Text = "";
                block2Text = "";
                block3Text = "";
                return;
            }
            
            
            byte[] magic = new byte[3];
            f.read(magic);
            Compression compression = Compression.getCompression(magic);
            logger.info(compression.toString());
            f.skipBytes(-3);
            
            // read remainder of small file into memory
            long compressedFileSize = f.length()-f.getFilePointer();
            byte[] buf = new byte[(int)compressedFileSize];
            f.read(buf);
            
            // close input file
            header.close();
            
            
            InputStream decompStream = compression.decompress(new ByteArrayInputStream(buf));
            dis = new DataInputStream(decompStream);
            short blockDivider = dis.readShort();
                logger.info("blockDivider=" + blockDivider);
            while (dis.available() > 0) {
                    logger.info("BYTES AVAILABLE: "+dis.available());
                
                // Certain products are 'Stand-Alone Tabular Alphanumeric Products
                // These products don't have 'blocks' but are just a standalone                 
                // Tabular Alphanumeric Block (BlockID=3)
                // This is defined in 3.3.2 (page 3-5) of 2620001J.pdf
                if (header.getProductCode() == 62 || // Storm Structure 
                        header.getProductCode() == 73 || // User Alert Message 
                        header.getProductCode() == 75 || // Free Text Message
                        header.getProductCode() == 77 || // PUP Text Message
                        header.getProductCode() == 82 // Supplemental Precip Message
                        ) {
                    
                        logger.info("DECODING 'STAND-ALONE TABULAR ALPHANUMERIC PRODUCT'");
                    
                    if (!readBlock3) {
                        dis.skip(dis.available());
                    }
                    else {
                        processBlock3(true);
                    }
                    // There's more stuff, but I'm not sure what and not currently interested.
                    break;
                    
                }
                else {
                
                    short blockID = dis.readShort();
                    int blockLen = dis.readInt();

                        logger.info("blockID=" + blockID);
                        logger.info("blockLen=" + blockLen);
                    if (blockID == 1) {
                        if (!readBlock1) {
                            dis.skip(blockLen - 6);
                        }
                        else {
                            processBlock1();
                        }
                    }
                    else if (blockID == 2) {
                        if (!readBlock2) {
                            dis.skip(blockLen - 6);
                        }
                        else {
                            processBlock2();
                        }
                    }
                    else if (blockID == 3) {
                        if (!readBlock3) {
                            dis.skip(blockLen - 6);
                        }
                        else {
                            processBlock3();
                        }
                    }
                }
            }
            
            decodingTimeInMillis = System.currentTimeMillis() - startTime;
            
        } catch (EOFException e) {
            decodingTimeInMillis = System.currentTimeMillis() - startTime;
            logger.info("EOF FOUND - NO DATA FOUND: "+ header.getDataURL());
            block1Text = "";
            block2Text = "";
            block3Text = "";
        } catch (Exception e) {
            decodingTimeInMillis = System.currentTimeMillis() - startTime;
            e.printStackTrace();
            throw new DecodeException("ERROR DECODING FILE: ", header.getDataURL());
        }
        
    }

    
    private void processBlock1() throws IOException, DecodeException {
        // BLOCK 1 - PRODUCT SYMBOLOGY BLOCK

            StringBuffer sb = new StringBuffer();

            // dis.skipBytes(blockLen-4);
            short numLayers = dis.readShort();

            // advance past next divider
            while (dis.readShort() != -1) {
                ;
            }
            int layerLen = dis.readInt();
            int layerBytesRead = 0;

            short packetCode = dis.readShort();
            String packetCodeHex = Hex.toHex(packetCode);
            logger.info("RADIAL: dataHeader[0] HEX: "+packetCodeHex);
            
            
            
            while (layerBytesRead < layerLen && packetCode != -1) {

                // read packet
                int packetBlockLen = readPacket(packetCode, sb);
                layerBytesRead += packetBlockLen + 2;

                    logger.info("layerBytesRead=" + layerBytesRead);

                // read next packet code - if it is -1 then we are
                // done!
                packetCode = dis.readShort();

                    logger.info("FOUND PACKET CODE: " + packetCode + " '"+Hex.toHex(packetCode)+"'");
            }

            block1Text = sb.toString();

    }

    private void processBlock2() throws IOException, DecodeException {
        
        
        // BLOCK 2 - GRAPHIC ALPHANUMERIC BLOCK
            StringBuffer sb = new StringBuffer();

            // dis.skipBytes(blockLen-4);
            short numPages = dis.readShort();

                logger.info("numPages=" + numPages);

            for (int pageNum = 0; pageNum < numPages; pageNum++) {

                short curPage = dis.readShort();
                short pageLen = dis.readShort();

                    logger.info("reading page " + pageNum);
                    logger.info("curPage " + curPage);
                    logger.info("pageLen " + pageLen);

                int pageBytesRead = 0;

                while (pageBytesRead < pageLen) {

                    short packetCode = dis.readShort();
                        logger.info("FOUND PACKET CODE: " + packetCode);
                    
                    // read packet
                    int packetBlockLen = readPacket(packetCode, sb);
                    pageBytesRead += packetBlockLen + 4;

                        logger.info("pageBytesRead=" + pageBytesRead);

                }
                
                sb.append("\n\n");
            }
            block2Text = sb.toString();
            
            short separator = dis.readShort();
            
            if (separator != -1) {
                logger.info("NOTE: SEPARATOR NOT FOUND IN CORRECT LOCATION");
            }

    }
    
    private void processBlock3() throws IOException, DecodeException {
        processBlock3(false);
    }

    private void processBlock3(boolean standAlone) throws IOException, DecodeException {
        // BLOCK 3 - TABULAR ALPHANUMERIC BLOCK


            StringBuffer sb = new StringBuffer();

            if (! standAlone) {
                // advance past message header block
                while (dis.readShort() != -1) {
                    ;
                }
                // advance past product description block
                while (dis.readShort() != -1) {
                    ;
                }

            }
            
            
            
            // dis.skipBytes(blockLen-4);
            short numPages = dis.readShort();

                logger.info("numPages=" + numPages);

            for (int pageNum = 0; pageNum < numPages; pageNum++) {

                    logger.info("reading page " + pageNum);

                short numChars = dis.readShort();
                    logger.info("numChars=" + numChars);
                // repeat for each
                while (numChars != -1) {

                    byte[] charData = new byte[numChars];
                    dis.read(charData);
                    sb.append(new String(charData) + "\n");
                        logger.info(new String(charData));
                    // read next line or -1 if we are done
                    numChars = dis.readShort();
                }
                sb.append("\n\n");
            }
            block3Text = sb.toString();

    }
    
    
    
    private int readPacket(short packetCode, StringBuffer textBuffer) throws IOException, DecodeException {
        
        short packetBlockLen = 0;
        String packetCodeHex = Hex.toHex(packetCode);
        
        if (packetCode == 8) {
            packetBlockLen = dis.readShort();
            short valueOfTextString = dis.readShort();
            short iStart = dis.readShort();
            short jStart = dis.readShort();
            byte[] text = new byte[packetBlockLen-6];
            dis.read(text);
            textBuffer.append(new String(text)+"\n");
                logger.info("packetCode="+packetCode+" packetLen="+packetBlockLen+" i,j="+iStart+","+jStart+" textValue="+valueOfTextString+" text="+new String(text));
        }
        // special graphic symbol
        else if (packetCode == 20) {
            packetBlockLen = dis.readShort();
            short iPos = dis.readShort();
            short jPos = dis.readShort();
            short pointFeatureType = dis.readShort();
            short pointFeatureAttribute = dis.readShort();
                logger.info("packetCode="+packetCode+" packetLen="+packetBlockLen+" i,j="+iPos+","+jPos+" pointFeatureType="+pointFeatureType+" pointFeatureAtt="+pointFeatureAttribute);
        }
        else if (packetCode == 2) {
            packetBlockLen = dis.readShort();
            short iStart = dis.readShort();
            short jStart = dis.readShort();
            byte[] text = new byte[packetBlockLen-4];
            dis.read(text);
                logger.info("packetCode="+packetCode+" packetBlockLen="+packetBlockLen+" i,j="+iStart+","+jStart+" text="+new String(text));
        }
        else if (packetCodeHex.equalsIgnoreCase("0802")) {
        	dis.readShort();
        	dis.readShort();
        }
        else if (packetCodeHex.equalsIgnoreCase("0E03")) {
        	dis.readShort();
            short iStart = dis.readShort();
            short jStart = dis.readShort();
            short lenOfVectors = dis.readShort();
            for (int n=0; n<lenOfVectors; n=n+4) {
            	short i1 = dis.readShort();
            	short j1 = dis.readShort();
            	short i2 = dis.readShort();
            	short j2 = dis.readShort();
            	logger.info("i1, j1, i2, j2: "+i1+","+j1+","+i2+","+j2);
            }
            
            logger.info("packetCode="+packetCode+" packetBlockLen="+packetBlockLen+" i,j="+iStart+","+jStart+" lenOfVectors="+lenOfVectors);
            
        }
//        else if (packetCode == 23 || packetCode == 24) {
//            packetBlockLen = dis.readShort();
//            
//            logger.info("pblen: "+packetBlockLen);
//            
//            int packetRead = 0;
//            short packetCode2 = dis.readShort();
//            while (packetRead < packetBlockLen && packetCode2 != -1) {
//                // read each display data packet
//                if (packetCode2 == 2) {
//                    short packetLen = dis.readShort();
//                    short iStart = dis.readShort();
//                    short jStart = dis.readShort();
//                    byte[] text = new byte[packetLen-4];
//                    dis.read(text);
//                    logger.info("packetCode="+packetCode2+" packetRead="+packetRead+" packetLen="+packetLen+" i,j="+iStart+","+jStart+" text="+new String(text));
//                    packetRead += packetLen;
//                }
//                packetCode2 = dis.readShort();
//            }
//        }
        else {
//            throw new NexradDecodeException("NMD Special Graphic Symbol code="+packetCode+" NOT FOUND.  Found packet code of: "+packetCode, header.getNexradURL());
                logger.info("NMD Special Graphic Symbol code="+packetCode+" NOT FOUND.  Found packet code of: "+packetCode+" ---- "+ header.getDataURL());
            packetBlockLen = dis.readShort();
            dis.skip(packetBlockLen);
        }
            
        return packetBlockLen;
        
        
    }
    
    
    
    
    
    
    
    public String getBlock1Text() {
        return block1Text;
    }

    public String getBlock2Text() {
        return block2Text;
    }

    public String getBlock3Text() {
        return block3Text;
    }
    
    public long getDecodingTimeInMillis() {
        return decodingTimeInMillis;
    }
    

    public boolean isReadBlock1() {
        return readBlock1;
    }

    public void setReadBlock1(boolean readBlock1) {
        this.readBlock1 = readBlock1;
    }

    public boolean isReadBlock2() {
        return readBlock2;
    }

    public void setReadBlock2(boolean readBlock2) {
        this.readBlock2 = readBlock2;
    }

    public boolean isReadBlock3() {
        return readBlock3;
    }

    public void setReadBlock3(boolean readBlock3) {
        this.readBlock3 = readBlock3;
    }


    
    
    
    
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        try {
            
            
            DecodeL3Header header = new DecodeL3Header();
//            URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\GenericProductFormat\\KAKQ_SDUS31_NMDAKQ_200704280757").toURL();         
//            URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NTVCLE_200211102356").toURL();
//            URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NTVCLE_200211102218").toURL();
//            URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NHICLE_200211102218").toURL();
//            URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NSSCLE_200211102333").toURL();
//            URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\Alphanumeric\\Tornado-1995\\7000KAMA_SDUS54_NHIAMA_199506090002").toURL();
//            URL url = new java.io.File("C:\\devel\\ndit\\testdata\\KAKQ_SDUS31_NMDAKQ_200704280757").toURL();
            URL url = new java.io.File("E:\\work\\level3-dualpole\\koax-dualpol-wmo\\koax-dualpol-wmo\\KOAX.166m0.20100428_2329").toURL();
            
            logger.info("PROCESSING: "+url);
            header.decodeHeader(url);
            DecodeL3AlphaGeneric decoder = new DecodeL3AlphaGeneric();
            decoder.setReadBlock1(true);
            decoder.setReadBlock2(true);
            decoder.setReadBlock3(true);
            decoder.decode(header);
            
            logger.info("----------- BLOCK 1----------- \n"+decoder.getBlock1Text());
            logger.info("----------- BLOCK 2----------- \n"+decoder.getBlock2Text());
            logger.info("----------- BLOCK 3----------- \n"+decoder.getBlock3Text());
            logger.info("DECODING TIME: "+decoder.getDecodingTimeInMillis());
            
            
//            logger.info(" ICAO: "+header.getICAO());
//            logger.info("  LAT: "+header.getLat());
//            logger.info("  LON: "+header.getLon());
//            logger.info("  ALT: "+header.getAlt());
//            logger.info("PCODE: "+header.getProductCode());
//
//
//            
//            
//            String[] categories = header.getDataThresholdStringArray();
//            for (int n=0; n<categories.length; n++) {
//               logger.info("categories["+n+"] = "+categories[n]);  
//            }
//            
//            short[] prodSpec = header.getProductSpecificValueArray();
//            for (int n=0; n<prodSpec.length; n++) {
//               logger.info("prodSpec["+n+"] = "+prodSpec[n]);  
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
