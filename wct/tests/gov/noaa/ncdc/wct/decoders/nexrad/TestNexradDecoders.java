package gov.noaa.ncdc.wct.decoders.nexrad;


import gov.noaa.ncdc.wct.decoders.DecodeException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class TestNexradDecoders {

    DecodeL3Header header = new DecodeL3Header();

    @Test
    public void testHail() throws MalformedURLException, DecodeException, IOException {

        // ------------- HAIL TESTS ----------------
        // URL url = new
        // java.io.File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf\\kmob-l3data\\KMOB_SDUS64_NHIMOB_200508290119").toURL();
        // URL url = new
        // java.io.File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf\\kmob-l3data\\KMOB_SDUS64_NHIMOB_200508290829").toURL();
//      URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NHICLE_200211102218").toURL();
        URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\Alphanumeric\\Tornado-1995\\7000KAMA_SDUS54_NHIAMA_199506090002").toURL();
        header.decodeHeader(url);
        StreamingRadialDecoder alpha = new DecodeHail(header);
        printInfo(header, alpha);

    }




    @Test
    public void testMeso() throws MalformedURLException, DecodeException, IOException {
        // ------------- MESO TESTS ----------------
//      URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf\\kmob-l3data\\KMOB_SDUS64_NMEMOB_200508290119").toURL();
//      URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf\\kmob-l3data\\KMOB_SDUS64_NMEMOB_200508290829").toURL();
//      URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NMECLE_200211102356").toURL();
        URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\Alphanumeric\\Tornado-1995\\7000KAMA_SDUS54_NMEAMA_199506090002").toURL();
        header.decodeHeader(url);
        StreamingRadialDecoder alpha = new DecodeMeso(header);
        printInfo(header, alpha);

    }

    @Test
    public void testTVS() throws MalformedURLException, DecodeException, IOException {
        // ------------- TVS TESTS ----------------
//      URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf\\kmob-l3data\\KMOB_SDUS64_NTVMOB_200508290119").toURL();
//      URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NTVCLE_200211102356").toURL();
//      URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NTVCLE_200211102218").toURL();
        URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\Alphanumeric\\Tornado-1995\\7000KAMA_SDUS54_NTVAMA_199506090012").toURL();
        header.decodeHeader(url);
        StreamingRadialDecoder alpha = new DecodeTVS(header);
        printInfo(header, alpha);

    }

    @Test
    public void testStructure() throws MalformedURLException, DecodeException, IOException {

        // ------------- STORM STRUCTURE TESTS ----------------
//      URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NSSCLE_200211102333").toURL();
//      URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NSSCLE_200211102218").toURL();
        URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\Alphanumeric\\Tornado-1995\\7000KAMA_SDUS54_NSSAMA_199506090002").toURL();
        header.decodeHeader(url);
        StreamingRadialDecoder alpha = new DecodeStormStructure(header);
        printInfo(header, alpha);

    }


    @Test
    public void testTracking() throws MalformedURLException, DecodeException, IOException {

        // ------------- STORM TRACKING TESTS ----------------
//      URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS61_NSTCLE_200211102218").toURL();
        URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\Alphanumeric\\Tornado-1995\\7000KAMA_SDUS54_NSTAMA_199506090002").toURL();
        header.decodeHeader(url);
        StreamingRadialDecoder alpha = new DecodeStormTracking(header);
        printInfo(header, alpha);

    }


    @Test
    public void testMDA() throws MalformedURLException, DecodeException, IOException {

        // ------------- MDA (DIGITAL MESOCYCLONE DETECTION ALGORITHM TESTS ----------------

        //URL url = new java.io.File("C:\\work\\GenericProductFormat\\KAKQ_SDUS31_NMDAKQ_200704280757").toURL();
//      URL url = new java.io.File("C:\\work\\SWDI\\Nexrad3Alpha\\KABR_SDUS33_NMDABR_200705230213").toURL();
        URL url = new java.io.File("C:\\work\\GenericProductFormat\\KTOP_SDUS33_NMDTWX_200704272106").toURL();
        header.decodeHeader(url);
        StreamingRadialDecoder alpha = new DecodeMDA(header);
        printInfo(header, alpha);


    }


    @Test
    public void testVAD() throws MalformedURLException, DecodeException, IOException {



        // ------------- VAD Wind Profile -------------------------------
        //URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS31_NVWCLE_200211102218").toURL();
        URL url = new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/kmob-level3/KMOB_SDUS34_NVWMOB_200508290917");
        header.decodeHeader(url);
        StreamingRadialDecoder decoder = new DecodeVADText(header);
        printInfo(header, decoder);

    }

    @Test
    public void testRadial() throws MalformedURLException, DecodeException, IOException {

        // ------------- Level-3 Radial  -------------------------------
        //URL url = new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/kmob-level3/KMOB_SDUS34_N1PMOB_200508292218");
        URL url = new File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS31_N1PCLE_200211102304").toURL();
        header.decodeHeader(url);
        
        StreamingRadialDecoder decoder = new DecodeL3Nexrad(header);
        decoder.decodeData();
        printInfo(header, decoder);

    }


    private void printInfo(NexradHeader header, StreamingRadialDecoder decoder) throws IOException {

        System.out.println("ICAO: " + header.getICAO());
        System.out.println(" LAT: " + header.getLat());
        System.out.println(" LON: " + header.getLon());
        System.out.println(decoder.getFeatures().size() + " FEATURES DECODED");
        String[] supData = decoder.getSupplementalDataArray();
        for (int n=0; n<supData.length; n++) {
            System.out.println("supData["+n+"] -------------------");
            System.out.println(supData[n]);
        }

    }

}
