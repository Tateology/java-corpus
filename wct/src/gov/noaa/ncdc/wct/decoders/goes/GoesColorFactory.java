package gov.noaa.ncdc.wct.decoders.goes;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster.Band;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

public class GoesColorFactory {

    private ColorsAndValues colorsAndValues;
    private static GoesColorFactory singleton;
    
    private HashMap<Integer, Double> brightnessTempMap;

    private  GoesColorFactory() {
    }

    public static GoesColorFactory getInstance() {
        if (singleton == null) {
            singleton = new GoesColorFactory();
        }
        return singleton;
    }




    public ColorsAndValues getColorsAndValues(GoesRemappedRaster raster) throws WCTException, IOException {
        GoesColorFactory gcf = GoesColorFactory.getInstance();
        
        String colorTableName = raster.getColorTableAlias();
        
        if (colorTableName.equalsIgnoreCase("Default")) {
            gcf.calculateEqualColorsAndValues(raster);
            
            return gcf.getEqualColorsAndValues();
        }
        else if (colorTableName.equalsIgnoreCase("Auto Grayscale Flat")) {
            Color[] c = null;
            if (raster.getLastBandDecoded() == Band.BAND1) {
                c = new Color[] { Color.WHITE, Color.BLACK };
//                c = new Color[] { new Color(255, 255, 255, 0), Color.BLACK };
            }
            else {
                c = new Color[] { Color.BLACK, Color.WHITE };
//                c = new Color[] { new Color(0, 0, 0, 0), Color.WHITE };
            }
            Double[] vals = new Double[] { raster.getMaxValue(), raster.getMinValue() };
//            System.out.println(Arrays.deepToString(vals));
            if (raster.getMaxValue() == GoesRemappedRaster.STARTING_MAX_VALUE) {
                vals = new Double[] { -999.0, -999.1 }; 
            }
            return ColorsAndValues.calculateEqualColorsAndValues(new ColorsAndValues(c, vals));
        }
        else if (colorTableName.equalsIgnoreCase("Auto Grayscale - Black/Trans to White/Opaque")) {
            Color[] c = null;
            if (raster.getLastBandDecoded() == Band.BAND1) {
                c = new Color[] { new Color(255, 255, 255, 240), new Color(0, 0, 0, 0) };
            }
            else {
                c = new Color[] { new Color(0, 0, 0, 0), new Color(255, 255, 255, 240) };
            }
            Double[] vals = new Double[] { raster.getMaxValue(), raster.getMinValue() };
//            System.out.println(Arrays.deepToString(vals));
            if (raster.getMaxValue() == GoesRemappedRaster.STARTING_MAX_VALUE) {
                vals = new Double[] { -999.0, -999.1 }; 
            }
            return ColorsAndValues.calculateEqualColorsAndValues(new ColorsAndValues(c, vals));
        }
        else if (colorTableName.equalsIgnoreCase("Auto Grayscale - Gray/Trans to White/Opaque")) {
            Color[] c = null;
            if (raster.getLastBandDecoded() == Band.BAND1) {
                c = new Color[] { new Color(255, 255, 255, 240), new Color(128, 128, 128, 0) };
            }
            else {
                c = new Color[] { new Color(128, 128, 128, 0), new Color(255, 255, 255, 240) };
            }
            Double[] vals = new Double[] { raster.getMaxValue(), raster.getMinValue() };
//            System.out.println(Arrays.deepToString(vals));
            if (raster.getMaxValue() == GoesRemappedRaster.STARTING_MAX_VALUE) {
                vals = new Double[] { -999.0, -999.1 }; 
            }
            return ColorsAndValues.calculateEqualColorsAndValues(new ColorsAndValues(c, vals));
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_TSTORM1.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TSTORM1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("Water Vapor 1 (McIDAS_VAPOR1.ET)")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-VAPOR1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("Water Vapor 2 (McIDAS_WVRBT3.ET)")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-WVRBT3.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_TSTORM2.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TSTORM2.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_TEMPS1.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TEMPS1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_SSTS.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-SSTS.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_CA.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-ca.et", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_BB.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-bb.et", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_BD.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-bd.et", null))
               );
        }
//        else if (colorTableName.equalsIgnoreCase("")) {
//            
//        }
        else {
            gcf.calculateEqualColorsAndValues(raster);
            return gcf.getEqualColorsAndValues();
        }

    }




//
//
//
//
//    public static Category[] getCategories(GoesRemappedRaster raster) throws IOException {
//        Category[] c;
//
//
//        URL url = null;
//        try {
//
//
//            // read color map
//            url = NexradIAViewer.class.getResource("/config/colormaps/goes-IR4.cmap");
//            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
//            ArrayList<Category> catList = new ArrayList<Category>();
//            String str;
//            while ((str=br.readLine()) != null) {
//
//                if (str.trim().length() == 0) {
//                    continue;
//                }
//
//                // 0         1         2         3         4
//                // 012345678901234567890123456789012345678901234567890
//                //       255     255     255     255   163.0
//                int r = Integer.parseInt(str.substring(0, 12).trim());
//                int g = Integer.parseInt(str.substring(13, 20).trim());
//                int b = Integer.parseInt(str.substring(21, 28).trim());
//                int a = Integer.parseInt(str.substring(29, 34).trim());
//                double val = Double.parseDouble(str.substring(35, str.length()).trim());
//
//                Color color = new Color(r, g, b, a);
////              NumberRange numRange = new NumberRange()
////              Category cat = new Category(String.valueOf(val), color, numRange);
//                Category cat = new Category(String.valueOf(val), color, val);
//                catList.add(cat);
//
//            }
//            br.close();
//
//            c = (Category[]) catList.toArray(new Category[catList.size()]);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new IOException("Error reading color map: "+url);
//        }
//
//
////      c = new Color[] {
////      new Color(255, 255, 255, 240), 
////      new Color(0, 0, 0, 0) 
////      };
//
//        return c;
//    }
//
//










    public ColorsAndValues getEqualColorsAndValues() {
        return colorsAndValues;
    }


    public void calculateEqualColorsAndValues(GoesRemappedRaster raster) throws WCTException, IOException {

        GoesColorFactory factory = new GoesColorFactory();
        ColorsAndValues colorsAndVals = factory.readCmapColorsAndValues(raster);

        this.colorsAndValues = ColorsAndValues.calculateEqualColorsAndValues(colorsAndVals);
    }







    
    public ColorsAndValues readETColorsAndValues(File etFile) throws IOException {
        return readETColorsAndValues(etFile.toURI().toURL());
    }

    public ColorsAndValues readETColorsAndValues(URL etURL) throws IOException {
        
        DataInputStream dis = new DataInputStream(new BufferedInputStream(etURL.openStream()));
        
        initBrightnessToTempMap();
        
        // skip header
        dis.readInt();
        
        Color[] colors = new Color[256];
        Double[] values = new Double[256];
//        for (int n=0; n<256; n++) {
//            int r = dis.readInt();
//            int g = dis.readInt();
//            int b = dis.readInt();
//            colors[n] = new Color(r, g, b);
//            values[n] = brightnessTempMap.get(n);
//        }

        
        int[] r = new int[256];
        int[] g = new int[256];
        int[] b = new int[256];
        for (int n=0; n<256; n++) {
            r[n] = dis.readInt();
        }
        for (int n=0; n<256; n++) {
            g[n] = dis.readInt();
        }
        for (int n=0; n<256; n++) {
            b[n] = dis.readInt();
        }
        for (int n=0; n<256; n++) {
            colors[n] = new Color(r[n], g[n], b[n]);
            values[n] = brightnessTempMap.get(n);
        }

        return new ColorsAndValues(colors, values);
    }


    public ColorsAndValues readCmapColorsAndValues(GoesRemappedRaster raster) throws IOException {
        
        
//        if (true) {
//            return readETColorsAndValues(new File("E:\\work\\satellite\\TSTORM1.ET"));
//        }
        
        
        
        Color[] c;
        Double[] vals;

        URL url = null;
        try {

//          System.out.println("start cmap read");

            // read color map
            if (raster.getLastBandDecoded() == Band.BAND1) {

                c = new Color[] { Color.WHITE, Color.BLACK };
                vals = new Double[] { raster.getMaxValue(), raster.getMinValue() };
                if (raster.getMaxValue() == GoesRemappedRaster.STARTING_MAX_VALUE) {
                    vals = new Double[] { -999.0, -999.1 }; 
                }
                return new ColorsAndValues(c, vals);

            }
            else if (raster.getLastBandDecoded() == Band.BAND2) {

                c = new Color[] { Color.BLACK, Color.WHITE };
                vals = new Double[] { raster.getMaxValue(), raster.getMinValue() };
                if (raster.getMaxValue() == GoesRemappedRaster.STARTING_MAX_VALUE) {
                    vals = new Double[] { -999.0, -999.1 }; 
                }
                return new ColorsAndValues(c, vals);

            }
            else if (raster.getLastBandDecoded() == Band.BAND3) {
                url = ResourceUtils.getInstance().getJarResource(
                        new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                        "/config/colormaps/goes-IR3.cmap", null);
            }
            else if (raster.getLastBandDecoded() == Band.BAND4) {
                url = ResourceUtils.getInstance().getJarResource(
                        new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                        "/config/colormaps/goes-IR4.cmap", null);
            }
            else if (raster.getLastBandDecoded() == Band.BAND6) {
                url = ResourceUtils.getInstance().getJarResource(
                        new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                        "/config/colormaps/goes-IR4.cmap", null);
            }
            else {
                url = ResourceUtils.getInstance().getJarResource(
                        new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                        "/config/colormaps/goes-IR4.cmap", null);
            }
//            System.out.println(url);

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            ColorsAndValues cav = ColorLutReaders.parseCmapFormat(br);
            br.close();

            return cav;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error reading color map: "+url);
        }


//      c = new Color[] {
//      new Color(255, 255, 255, 240), 
//      new Color(0, 0, 0, 0) 
//      };
    }






    public static Double[] getValues(GoesRemappedRaster raster) throws IOException {
        Double[] vals;

//        System.out.println("start cmap read");

        URL url = null;
        try {


            // read color map
            url = ResourceUtils.getInstance().getJarResource(
                    new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                    "/config/colormaps/goes-IR4.cmap", null);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            vals = ColorLutReaders.parseCmapFormat(br).getValues();
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error reading color map: "+url);
        }

//        System.out.println("end cmap read");

//      vals = new Double[] {
//      new Double(163), 
//      new Double(330) 
//      };

        return vals;
    }


    
    private HashMap<Integer, Double> initBrightnessToTempMap() {
        if (brightnessTempMap == null) {
            brightnessTempMap = new HashMap<Integer, Double>();
        }
        
        brightnessTempMap.put(0,   330.000);
        brightnessTempMap.put(1,   329.500);
        brightnessTempMap.put(2,   329.000);
        brightnessTempMap.put(3,   328.500);
        brightnessTempMap.put(4,   328.000);
        brightnessTempMap.put(5,   327.500);
        brightnessTempMap.put(6,   327.000);
        brightnessTempMap.put(7,   326.500);
        brightnessTempMap.put(8,   326.000);
        brightnessTempMap.put(9,   325.500);
        brightnessTempMap.put(10,  325.000);
        brightnessTempMap.put(11,  324.500);
        brightnessTempMap.put(12,  324.000);
        brightnessTempMap.put(13,  323.500);
        brightnessTempMap.put(14,  323.000);
        brightnessTempMap.put(15,  322.500);
        brightnessTempMap.put(16,  322.000);
        brightnessTempMap.put(17,  321.500);
        brightnessTempMap.put(18,  321.000);
        brightnessTempMap.put(19,  320.500);
        brightnessTempMap.put(20,  320.000);
        brightnessTempMap.put(21,  319.500);
        brightnessTempMap.put(22,  319.000);
        brightnessTempMap.put(23,  318.500);
        brightnessTempMap.put(24,  318.000);
        brightnessTempMap.put(25,  317.500);
        brightnessTempMap.put(26,  317.000);
        brightnessTempMap.put(27,  316.500);
        brightnessTempMap.put(28,  316.000);
        brightnessTempMap.put(29,  315.500);
        brightnessTempMap.put(30,  315.000);
        brightnessTempMap.put(31,  314.500);
        brightnessTempMap.put(32,  314.000);
        brightnessTempMap.put(33,  313.500);
        brightnessTempMap.put(34,  313.000);
        brightnessTempMap.put(35,  312.500);
        brightnessTempMap.put(36,  312.000);
        brightnessTempMap.put(37,  311.500);
        brightnessTempMap.put(38,  311.000);
        brightnessTempMap.put(39,  310.500);
        brightnessTempMap.put(40,  310.000);
        brightnessTempMap.put(41,  309.500);
        brightnessTempMap.put(42,  309.000);
        brightnessTempMap.put(43,  308.500);
        brightnessTempMap.put(44,  308.000);
        brightnessTempMap.put(45,  307.500);
        brightnessTempMap.put(46,  307.000);
        brightnessTempMap.put(47,  306.500);
        brightnessTempMap.put(48,  306.000);
        brightnessTempMap.put(49,  305.500);
        brightnessTempMap.put(50,  305.000);
        brightnessTempMap.put(51,  304.500);
        brightnessTempMap.put(52,  304.000);
        brightnessTempMap.put(53,  303.500);
        brightnessTempMap.put(54,  303.000);
        brightnessTempMap.put(55,  302.500);
        brightnessTempMap.put(56,  302.000);
        brightnessTempMap.put(57,  301.500);
        brightnessTempMap.put(58,  301.000);
        brightnessTempMap.put(59,  300.500);
        brightnessTempMap.put(60,  300.000);
        brightnessTempMap.put(61,  299.500);
        brightnessTempMap.put(62,  299.000);
        brightnessTempMap.put(63,  298.500);
        brightnessTempMap.put(64,  298.000);
        brightnessTempMap.put(65,  297.500);
        brightnessTempMap.put(66,  297.000);
        brightnessTempMap.put(67,  296.500);
        brightnessTempMap.put(68,  296.000);
        brightnessTempMap.put(69,  295.500);
        brightnessTempMap.put(70,  295.000);
        brightnessTempMap.put(71,  294.500);
        brightnessTempMap.put(72,  294.000);
        brightnessTempMap.put(73,  293.500);
        brightnessTempMap.put(74,  293.000);
        brightnessTempMap.put(75,  292.500);
        brightnessTempMap.put(76,  292.000);
        brightnessTempMap.put(77,  291.500);
        brightnessTempMap.put(78,  291.000);
        brightnessTempMap.put(79,  290.500);
        brightnessTempMap.put(80,  290.000);
        brightnessTempMap.put(81,  289.500);
        brightnessTempMap.put(82,  289.000);
        brightnessTempMap.put(83,  288.500);
        brightnessTempMap.put(84,  288.000);
        brightnessTempMap.put(85,  287.500);
        brightnessTempMap.put(86,  287.000);
        brightnessTempMap.put(87,  286.500);
        brightnessTempMap.put(88,  286.000);
        brightnessTempMap.put(89,  285.500);
        brightnessTempMap.put(90,  285.000);
        brightnessTempMap.put(91,  284.500);
        brightnessTempMap.put(92,  284.000);
        brightnessTempMap.put(93,  283.500);
        brightnessTempMap.put(94,  283.000);
        brightnessTempMap.put(95,  282.500);
        brightnessTempMap.put(96,  282.000);
        brightnessTempMap.put(97,  281.500);
        brightnessTempMap.put(98,  281.000);
        brightnessTempMap.put(99,  280.500);
        brightnessTempMap.put(100, 280.000);
        brightnessTempMap.put(101, 279.500);
        brightnessTempMap.put(102, 279.000);
        brightnessTempMap.put(103, 278.500);
        brightnessTempMap.put(104, 278.000);
        brightnessTempMap.put(105, 277.500);
        brightnessTempMap.put(106, 277.000);
        brightnessTempMap.put(107, 276.500);
        brightnessTempMap.put(108, 276.000);
        brightnessTempMap.put(109, 275.500);
        brightnessTempMap.put(110, 275.000);
        brightnessTempMap.put(111, 274.500);
        brightnessTempMap.put(112, 274.000);
        brightnessTempMap.put(113, 273.500);
        brightnessTempMap.put(114, 273.000);
        brightnessTempMap.put(115, 272.500);
        brightnessTempMap.put(116, 272.000);
        brightnessTempMap.put(117, 271.500);
        brightnessTempMap.put(118, 271.000);
        brightnessTempMap.put(119, 270.500);
        brightnessTempMap.put(120, 270.000);
        brightnessTempMap.put(121, 269.500);
        brightnessTempMap.put(122, 269.000);
        brightnessTempMap.put(123, 268.500);
        brightnessTempMap.put(124, 268.000);
        brightnessTempMap.put(125, 267.500);
        brightnessTempMap.put(126, 267.000);
        brightnessTempMap.put(127, 266.500);
        brightnessTempMap.put(128, 266.000);
        brightnessTempMap.put(129, 265.500);
        brightnessTempMap.put(130, 265.000);
        brightnessTempMap.put(131, 264.500);
        brightnessTempMap.put(132, 264.000);
        brightnessTempMap.put(133, 263.500);
        brightnessTempMap.put(134, 263.000);
        brightnessTempMap.put(135, 262.500);
        brightnessTempMap.put(136, 262.000);
        brightnessTempMap.put(137, 261.500);
        brightnessTempMap.put(138, 261.000);
        brightnessTempMap.put(139, 260.500);
        brightnessTempMap.put(140, 260.000);
        brightnessTempMap.put(141, 259.500);
        brightnessTempMap.put(142, 259.000);
        brightnessTempMap.put(143, 258.500);
        brightnessTempMap.put(144, 258.000);
        brightnessTempMap.put(145, 257.500);
        brightnessTempMap.put(146, 257.000);
        brightnessTempMap.put(147, 256.500);
        brightnessTempMap.put(148, 256.000);
        brightnessTempMap.put(149, 255.500);
        brightnessTempMap.put(150, 255.000);
        brightnessTempMap.put(151, 254.500);
        brightnessTempMap.put(152, 254.000);
        brightnessTempMap.put(153, 253.500);
        brightnessTempMap.put(154, 253.000);
        brightnessTempMap.put(155, 252.500);
        brightnessTempMap.put(156, 252.000);
        brightnessTempMap.put(157, 251.500);
        brightnessTempMap.put(158, 251.000);
        brightnessTempMap.put(159, 250.500);
        brightnessTempMap.put(160, 250.000);
        brightnessTempMap.put(161, 249.500);
        brightnessTempMap.put(162, 249.000);
        brightnessTempMap.put(163, 248.500);
        brightnessTempMap.put(164, 248.000);
        brightnessTempMap.put(165, 247.500);
        brightnessTempMap.put(166, 247.000);
        brightnessTempMap.put(167, 246.500);
        brightnessTempMap.put(168, 246.000);
        brightnessTempMap.put(169, 245.500);
        brightnessTempMap.put(170, 245.000);
        brightnessTempMap.put(171, 244.500);
        brightnessTempMap.put(172, 244.000);
        brightnessTempMap.put(173, 243.500);
        brightnessTempMap.put(174, 243.000);
        brightnessTempMap.put(175, 242.500);
        brightnessTempMap.put(176, 242.000);
        brightnessTempMap.put(177, 241.000);
        brightnessTempMap.put(178, 240.000);
        brightnessTempMap.put(179, 239.000);
        brightnessTempMap.put(180, 238.000);
        brightnessTempMap.put(181, 237.000);
        brightnessTempMap.put(182, 236.000);
        brightnessTempMap.put(183, 235.000);
        brightnessTempMap.put(184, 234.000);
        brightnessTempMap.put(185, 233.000);
        brightnessTempMap.put(186, 232.000);
        brightnessTempMap.put(187, 231.000);
        brightnessTempMap.put(188, 230.000);
        brightnessTempMap.put(189, 229.000);
        brightnessTempMap.put(190, 228.000);
        brightnessTempMap.put(191, 227.000);
        brightnessTempMap.put(192, 226.000);
        brightnessTempMap.put(193, 225.000);
        brightnessTempMap.put(194, 224.000);
        brightnessTempMap.put(195, 223.000);
        brightnessTempMap.put(196, 222.000);
        brightnessTempMap.put(197, 221.000);
        brightnessTempMap.put(198, 220.000);
        brightnessTempMap.put(199, 219.000);
        brightnessTempMap.put(200, 218.000);
        brightnessTempMap.put(201, 217.000);
        brightnessTempMap.put(202, 216.000);
        brightnessTempMap.put(203, 215.000);
        brightnessTempMap.put(204, 214.000);
        brightnessTempMap.put(205, 213.000);
        brightnessTempMap.put(206, 212.000);
        brightnessTempMap.put(207, 211.000);
        brightnessTempMap.put(208, 210.000);
        brightnessTempMap.put(209, 209.000);
        brightnessTempMap.put(210, 208.000);
        brightnessTempMap.put(211, 207.000);
        brightnessTempMap.put(212, 206.000);
        brightnessTempMap.put(213, 205.000);
        brightnessTempMap.put(214, 204.000);
        brightnessTempMap.put(215, 203.000);
        brightnessTempMap.put(216, 202.000);
        brightnessTempMap.put(217, 201.000);
        brightnessTempMap.put(218, 200.000);
        brightnessTempMap.put(219, 199.000);
        brightnessTempMap.put(220, 198.000);
        brightnessTempMap.put(221, 197.000);
        brightnessTempMap.put(222, 196.000);
        brightnessTempMap.put(223, 195.000);
        brightnessTempMap.put(224, 194.000);
        brightnessTempMap.put(225, 193.000);
        brightnessTempMap.put(226, 192.000);
        brightnessTempMap.put(227, 191.000);
        brightnessTempMap.put(228, 190.000);
        brightnessTempMap.put(229, 189.000);
        brightnessTempMap.put(230, 188.000);
        brightnessTempMap.put(231, 187.000);
        brightnessTempMap.put(232, 186.000);
        brightnessTempMap.put(233, 185.000);
        brightnessTempMap.put(234, 184.000);
        brightnessTempMap.put(235, 183.000);
        brightnessTempMap.put(236, 182.000);
        brightnessTempMap.put(237, 181.000);
        brightnessTempMap.put(238, 180.000);
        brightnessTempMap.put(239, 179.000);
        brightnessTempMap.put(240, 178.000);
        brightnessTempMap.put(241, 177.000);
        brightnessTempMap.put(242, 176.000);
        brightnessTempMap.put(243, 175.000);
        brightnessTempMap.put(244, 174.000);
        brightnessTempMap.put(245, 173.000);
        brightnessTempMap.put(246, 172.000);
        brightnessTempMap.put(247, 171.000);
        brightnessTempMap.put(248, 170.000);
        brightnessTempMap.put(249, 169.000);
        brightnessTempMap.put(250, 168.000);
        brightnessTempMap.put(251, 167.000);
        brightnessTempMap.put(252, 166.000);
        brightnessTempMap.put(253, 165.000);
        brightnessTempMap.put(254, 164.000);
        brightnessTempMap.put(255, 163.000);
        
        return brightnessTempMap;
    }






    
}
