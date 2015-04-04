package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.export.WCTExport;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.junit.Test;

import ucar.ma2.InvalidRangeException;

public class TestTDWR {

//    @Test
    public void testDecode() throws DecodeException, IOException {
        

        Logger.getLogger("gov.noaa.ncdc.wct.decoders").setLevel(Level.ALL);
        
        DecodeL3Header header = new DecodeL3Header();
//        header.decodeHeader(new File("E:\\work\\TDWR\\TEWR20090115.tar\\KOKX_SDUS21_TR1EWR_200901150005").toURI().toURL());
        header.decodeHeader(new File("E:\\devel\\wct\\testdata\\KBIS_SDUS53_DHRMBX_200806191830").toURI().toURL());
//        header.decodeHeader(new File("E:\\work\\TDWR\\TEWR20090115.tar\\KOKX_SDUS51_TZLEWR_200901150005").toURI().toURL());
//        header.decodeHeader(new File("E:\\work\\TDWR\\TEWR20090115.tar\\KOKX_SDUS71_TV1EWR_200901151344").toURI().toURL());
        
        
        System.out.println("ICAO = "+header.getICAO());
        System.out.println("PRODUCT CODE = "+header.getProductCode());
        
        System.out.println("HASHINFO: "+RadarHashtables.getSharedInstance().getLocation(header.getICAO()));
        
        System.out.println(header.toString());
        System.out.println(Arrays.deepToString(header.getDataThresholdStringArray()));
        System.out.println(Arrays.toString(header.getProductSpecificValueArray()));
        
        // product specific [2] = elevation angle * 10
        // [7] = compression? ( 0 or 1 )
        
        DecodeL3Nexrad decoder = new DecodeL3Nexrad(header);
        
        StreamingProcess sp = new StreamingProcess() {
//            @Override
            public void addFeature(Feature feature) throws StreamingProcessException {
//                System.out.print(feature.getAttribute("value")+" ");
            }
//            @Override
            public void close() throws StreamingProcessException {
            }            
        };
        decoder.decodeData(new StreamingProcess[] { sp });
        
    }
    
    
    
    @Test
    public void testExport() throws WCTExportNoDataException, WCTExportException, DecodeException, 
        FeatureRasterizerException, IllegalAttributeException, InvalidRangeException, 
        DecodeHintNotSupportedException, URISyntaxException, ParseException, Exception {
        
        URL url = new File("E:\\devel\\wct\\testdata\\KBIS_SDUS53_DHRMBX_200806191830").toURI().toURL();
        WCTExport exporter = new WCTExport();
        exporter.setOutputFormat(ExportFormat.SHAPEFILE);
        exporter.exportData(url, new File("E:\\work\\export\\KBIS_SDUS53_DHRMBX_200806191830.shp"));
//        exporter.exportData(url, new File("E:\\work\\export\\"));
        
        
    }
    
}

