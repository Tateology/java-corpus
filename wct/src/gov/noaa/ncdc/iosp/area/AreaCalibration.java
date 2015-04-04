package gov.noaa.ncdc.iosp.area;

import edu.wisc.ssec.mcidas.AreaDirectory;

/**
 *Calibrates images counts to brightness temperature for area files
 NOTES
 This routine was coded up from information freely available on the
 world wide web. References to web sites are provided in the code
 documentation.

 This code will need an update to work with satellites other than
 those listed below:
 GOES Imagers (GOES-8,9,10,11,12,13,14,15)
 
**TO DO**
 * Sounder calibration routines
 * Mode-A calibration
 * Multi-channel imagery (i.e., images=intarr(nchan,nx,ny)
 */
public class AreaCalibration {

        //;c1 & c2 from http://www.oso.noaa.gov/goes/goes-calibration/contents/page19.htm
        private static Double C1 = 1.191066e-5;             //mW/(m2-sr-cm-4)
        private static Double C2 = 1.438833;                    //K/cm-1
    
        
        //GOES sounder scaling coefficients
        //http://www.oso.noaa.gov/goes/goes-calibration/contents/page19.htm
        private static double[] M ={528.9773, 540.0049, 485.6243, 394.5752, 357.8019, 
             334.1747, 311.5226, 314.6032, 434.3518, 1126.224, 
             1899.565, 2874.342, 9642.747, 14105.38, 26221.34, 
             10720.60, 12136.11, 19358.13, -1.0};

        //GOES sounder scaling coefficients
        //http://www.oso.noaa.gov/goes/goes-calibration/contents/page19.htm
        private static double[] B = {1745.625, 1566.014, 1311.186, 887.7943, 787.1643, 
             417.7184, 249.2180, 251.6826, 716.6805, 900.9795, 
             1139.739, 2155.757, 626.7785, 916.8496, 1704.387, 
             428.8239, 497.5806, 348.4463}; 
        
        private static double[] m = { -1.0, 227.3889, 38.8383, 5.2285, 5.0273, 5.5297 };
        // ;Offset: UNITS= COUNTS
        private static double[] b = { -1.0, 68.2167, 29.1287, 15.6854, 15.3332, 16.5892 };
        
        private int chanNum;
        private int sensorId;
        private String sat;
        private String satType;
        private String g1, g2;
        private int datawidth;
        
        double[] row = new double[4];
            
        public AreaCalibration(int sensorId, int channel,int datawidth){
            this.sensorId = sensorId;
            chanNum = channel;
            this.datawidth=datawidth;
            init();
        }
        
        public AreaCalibration(AreaDirectory ad){
            int[] dir = ad.getDirectoryBlock();
            datawidth= dir[NCDCAreaFile.AD_DATAWIDTH];
            sensorId = dir[NCDCAreaFile.AD_SENSORID];
            chanNum = ad.getBands()[0];
            init();
        }
 
        public int getChanNum(AreaDirectory ad){
            int chanNum = ad.getBands()[0];
           
        return chanNum;
        }

        
        private void init( ){
//          int[] dir = ad.getDirectoryBlock();
            //determine which sensor we are calibrating
            sat = AreaFileUtil.findSensorSource(sensorId);      
            satType = sat.substring(0,4);
            g1 = sat.substring(sat.indexOf(' ')).trim();
            g2 = sat.substring(sat.indexOf('-')+1,sat.indexOf('-')+3);
        }
        
        
        public float[] calculateRadiance(int[] data){
            float[] radiance = new float[data.length];
            
            for (int i = 0; i < data.length; i++) {
                double rad = ((int) (data[i] / 32) - b[chanNum - 1]) / m[chanNum - 1];
                if (rad < 0) {
                    radiance[i] = 1.0e-10f;
                } else {
                    radiance[i] = (float)rad;
                }
            }           
            
            return radiance;
        }
        
        
        public float[] calibrate(int[] data, int detector) {    
            float[] calData = new float[data.length];
            double[] radiance = new double[data.length];
//              int[] dir = ad.getDirectoryBlock();   
            //determine sat type
            if(satType.equals("GOES")) {
                int goes_num = Integer.parseInt(g2.trim());
                if (goes_num > 8 && goes_num < 16){ 
                    if (datawidth != 2){ 
                        System.out.println("'This routine is not currently set up for 1-byte GOES-GVAR McIDAS files");
                        return null;
//                    	for (int i = 0; i < data.length; i++) {
//                    		data[i] = 163+data[i];
//                    	}
                    }   
                }

//               No Brightness temp for band_01 on Mcidas or Arhrr              
//              ;Calibrate GVAR files for GOES.
//              ;http://www.oso.noaa.gov/goes/goes-calibration/contents/page19.htm          
//              ;determine the channel number
                if (g1.equals("Imager")) {
                    if (chanNum < 2 || chanNum > 6) {
                        System.out.println("No Brightness Temp for Mcidas or AVHRR channel : " + chanNum);
                        return null;
                    }

                    // ;GOES-12/13 from
                    // http://www.oso.noaa.gov/goes/goes-calibration/gvar-conversion.htm
                    // ;Slope: UNITS= COUNTS/(mW/[m^2 ster sm^-1])
//                  double[] m = { -1.0, 227.3889, 38.8383, 5.2285, 5.0273, 5.5297 };
                    // ;Offset: UNITS= COUNTS
//                  double[] b = { -1.0, 68.2167, 29.1287, 15.6854, 15.3332, 16.5892 };
                    for (int i = 0; i < data.length; i++) {
                        double rad = ((int) (data[i] / 32) - b[chanNum - 1]) / m[chanNum - 1];
                        if (rad < 0) {
                            radiance[i] = 1.0e-10;
                        } else {
                            radiance[i] = rad;
                        }
                    }

                    if (goes_num == 8) { // then begin
                    // http://www.oso.noaa.gov/goes/goes-calibration/docs/table3_1.htm
                        if (detector == 5) { // then begin ;2/a
                            row[0] = 2556.71;
                            row[1] = -0.618007;
                            row[2] = 1.001825;
                            row[3] = -6.021442e-07;
                        } else if (detector == 6) { // then begin ;2/b
                            row[0] = 2558.62;
                            row[1] = -0.668648;
                            row[2] = 1.002221;
                            row[3] = -1.323758e-06;
                        } else if (detector == 7) { // then begin ;3
                            row[0] = 1481.91;
                            row[1] = -0.656443;
                            row[2] = 1.001914;
                            row[3] = -9.535221e-07;
                        } else if (detector == 1) { // then begin ;4/a
                            row[0] = 934.30;
                            row[1] = -0.519333;
                            row[2] = 1.002834;
                            row[3] = -3.005194e-06;
                        } else if (detector == 2) { // then begin ;4/b
                            row[0] = 935.38;
                            row[1] = -0.553431;
                            row[2] = 1.002894;
                            row[3] = -3.077855e-06;
                        } else if (detector == 3) { // then begin ;5/a
                            row[0] = 837.06;
                            row[1] = -0.383077;
                            row[2] = 1.000856;
                            row[3] = 6.026892e-07;
                        } else if (detector == 4) { // then begin ;5/b
                            row[0] = 837.00;
                            row[1] = -0.351510;
                            row[2] = 1.000340;
                            row[3] = 1.761416e-06;
                        }
                    } else if (goes_num == 9) {
                        // http://www.oso.noaa.gov/goes/goes-calibration/docs/table3_2.htm
                        if (detector == 5) { // then begin ;2/a
                            row[0] = 2555.18;
                            row[1] = -0.592268;
                            row[2] = 1.001040;
                            row[3] = -1.882973e-07;
                        } else if (detector == 6) { // then begin ;2/b
                            row[0] = 2555.18;
                            row[1] = -0.592268;
                            row[2] = 1.001040;
                            row[3] = -1.882973e-07;
                        } else if (detector == 7) { // then begin ;3
                            row[0] = 1481.82;
                            row[1] = -0.559306;
                            row[2] = 1.001602;
                            row[3] = -1.010812e-06;
                        } else if (detector == 1) { // then begin ;4/a
                            row[0] = 934.59;
                            row[1] = -0.525515;
                            row[2] = 1.002411;
                            row[3] = -2.148433e-06;
                        } else if (detector == 2) { // then begin ;4/b
                            row[0] = 934.28;
                            row[1] = -0.532929;
                            row[2] = 1.002616;
                            row[3] = -2.584012e-06;
                        } else if (detector == 3) { // then begin ;5/a
                            row[0] = 834.02;
                            row[1] = -0.317704;
                            row[2] = 1.001058;
                            row[3] = -2.245684e-07;
                        } else if (detector == 4) { // then begin ;5/b
                            row[0] = 834.09;
                            row[1] = -0.346344;
                            row[2] = 1.001261;
                            row[3] = -6.031501e-07;
                        }
                    } else if (goes_num == 10) { // then begin
                        // http://www.oso.noaa.gov/goes/goes-calibration/docs/table3_3.htm
                        if (detector == 5) { // then begin ;2/a
                            row[0] = 2552.9845;
                            row[1] = -0.63343694;
                            row[2] = 1.0013206;
                            row[3] = -4.2038547e-07;
                        } else if (detector == 6) { // then begin ;2/b
                            row[0] = 2552.9845;
                            row[1] = -0.63343694;
                            row[2] = 1.0013206;
                            row[3] = -4.2038547e-07;
                        } else if (detector == 7) { // then begin ;3
                            row[0] = 1486.2212;
                            row[1] = -0.66500842;
                            row[2] = 1.0017857;
                            row[3] = -7.3885254e-07;
                        } else if (detector == 1) { // then begin ;4/a
                            row[0] = 936.10260;
                            row[1] = -0.36939128;
                            row[2] = 1.0017466;
                            row[3] = -1.4981835e-06;
                        } else if (detector == 2) { // then begin ;4/b
                            row[0] = 935.98981;
                            row[1] = -0.41013697;
                            row[2] = 1.0020766;
                            row[3] = -2.1303556e-06;
                        } else if (detector == 3) { // then begin ;5/a
                            row[0] = 830.88473;
                            row[1] = -0.32763317;
                            row[2] = 1.0014057;
                            row[3] = -9.5563444e-07;
                        } else if (detector == 4) { // then begin ;5/b
                            row[0] = 830.89691;
                            row[1] = -0.32184480;
                            row[2] = 1.0013828;
                            row[3] = -9.3581045e-07;
                        }
                    } else if (goes_num == 11) {
                        // http://www.oso.noaa.gov/goes/goes-calibration/docs/table3_4.htm
                        if (detector == 5) { // then begin ;2/a
                            row[0] = 2562.07;
                            row[1] = -0.651377;
                            row[2] = 1.000828;
                            row[3] = -1.002675e-07;
                        } else if (detector == 6) { // then begin ;2/b
                            row[0] = 2562.07;
                            row[1] = -0.651377;
                            row[2] = 1.000828;
                            row[3] = -1.002675e-07;
                        } else if (detector == 7) { // then begin ;3
                            row[0] = 1481.53;
                            row[1] = -0.620175;
                            row[2] = 1.002104;
                            row[3] = -1.171163e-06;
                        } else if (detector == 1) { // then begin ;4/a
                            row[0] = 931.76;
                            row[1] = -0.546157;
                            row[2] = 1.003175;
                            row[3] = -3.656323e-06;
                        } else if (detector == 2) { // then begin ;4/b
                            row[0] = 931.76;
                            row[1] = -0.546157;
                            row[2] = 1.003175;
                            row[3] = -3.656323e-06;
                        } else if (detector == 3) { // then begin ;5/a
                            row[0] = 833.67;
                            row[1] = -0.329940;
                            row[2] = 1.000974;
                            row[3] = 5.000439e-08;
                        } else if (detector == 4) { // then begin ;5/b
                            row[0] = 833.04;
                            row[1] = -0.307032;
                            row[2] = 1.000903;
                            row[3] = 1.233306e-07;
                        }
                    } else if (goes_num == 12) { // then begin
                        // http://www.oso.noaa.gov/goes/goes-calibration/docs/table3_5.htm
                        if (detector == 1) { // then begin ;2/a
                            row[0] = 2562.45;
                            row[1] = -0.727744;
                            row[2] = 1.002131;
                            row[3] = -1.173898e-06;
                        } else if (detector == 2) { // then begin ;2/b
                            row[0] = 2562.45;
                            row[1] = -0.727744;
                            row[2] = 1.002131;
                            row[3] = -1.173898e-06;
                        } else if (detector == 3) { // then begin ;3/a
                            row[0] = 1536.43;
                            row[1] = -5.278975;
                            row[2] = 1.016476;
                            row[3] = -7.754348e-06;
                        } else if (detector == 4) { // then begin ;3/b
                            row[0] = 1536.95;
                            row[1] = -5.280110;
                            row[2] = 1.016383;
                            row[3] = -7.607900e-06;
                        } else if (detector == 5) { // then begin ;4/a
                            row[0] = 933.21;
                            row[1] = -0.534982;
                            row[2] = 1.002693;
                            row[3] = -2.667092e-06;
                        } else if (detector == 6) { // then begin ;4/b
                            row[0] = 933.21;
                            row[1] = -0.534982;
                            row[2] = 1.002693;
                            row[3] = -2.667092e-06;
                        } else if (detector == 7) { // then begin ;6
                            row[0] = 751.91;
                            row[1] = -0.177244;
                            row[2] = 1.000138;
                            row[3] = 1.163496e-06;
                        }
                    } else if (goes_num == 13) { // then begin
                        // http://www.oso.noaa.gov/goes/goes-calibration/docs/table3_6.htm
                        if (detector == 1) { // then begin ;2/a
                            row[0] = 2561.7421;
                            row[1] = -1.4755462;
                            row[2] = 1.0028656;
                            row[3] = -5.8203946e-07;
                        } else if (detector == 2) { // then begin ;2/b
                            row[0] = 2561.7421;
                            row[1] = -1.4755462;
                            row[2] = 1.0028656;
                            row[3] = -5.8203946e-07;
                        } else if (detector == 3) { // then begin ;3/a
                            row[0] = 1522.5182;
                            row[1] = -4.1556932;
                            row[2] = 1.0142082;
                            row[3] = -8.0255086e-06;
                        } else if (detector == 4) { // then begin ;3/b
                            row[0] = 1521.6645;
                            row[1] = -4.1411143;
                            row[2] = 1.0142255;
                            row[3] = -8.0755893e-06;
                        } else if (detector == 5) { // then begin ;4/a
                            row[0] = 937.23449;
                            row[1] = -0.52227011;
                            row[2] = 1.0023802;
                            row[3] = -2.0798856e-06;
                        } else if (detector == 6) { // then begin ;4/b
                            row[0] = 937.27498;
                            row[1] = -0.51783545;
                            row[2] = 1.0023789;
                            row[3] = -2.1027609e-06;
                        } else if (detector == 7) { // then begin ;6
                            //row[0] = 753.15307;
                            row[0] = 751.92589;
                            //row[1] = -0.16697924;
                            row[1] = -0.16048355;
                            //row[2] = 1.0003868;
                            row[2] = 1.0006854;
                            //row[3] = 4.2873045e-07;
                            row[3] = -3.9399190e-07;
                        }
                    }else  if (goes_num == 14) { // then begin
                        // ;http://www.oso.noaa.gov/goes/goes-calibration/docs/table3_7.htm
                        if (detector == 1) { // then begin ;2/a
                            row[0] = 2577.98;
                            row[1] = -1.624786;
                            row[2] = 1.002851;
                            row[3] = -4.220156e-07;
                        } else if (detector == 2) { // then begin ;2/b
                            row[0] = 2577.98;
                            row[1] = -1.624786;
                            row[2] = 1.002851;
                            row[3] = -4.220156e-07;
                        } else if (detector == 3) { // then begin ;3/a
                            row[0] = 1529.35;
                            row[1] = -4.068539;
                            row[2] = 1.013407;
                            row[3] = -7.391130e-06;
                        } else if (detector == 4) { // then begin ;3/b
                            row[0] = 1530.13;
                            row[1] = -4.071805;
                            row[2] = 1.013250;
                            row[3] = -7.198898e-06;
                        } else if (detector == 5) { // then begin ;4/a
                            row[0] = 936.20;
                            row[1] = -0.539978;
                            row[2] = 1.003263;
                            row[3] = -3.856398e-06;
                        } else if (detector == 6) { // then begin ;4/b
                            row[0] = 936.14;
                            row[1] = -0.543131;
                            row[2] = 1.003284;
                            row[3] = -3.884668e-06;
                        } else if (detector == 7) { // then begin ;6/a
                            row[0] = 753.30;
                            row[1] = -0.165606;
                            row[2] = 1.000381;
                            row[3] = 4.307351e-07;
                        }else if (detector == 8) { // then begin ;6/b
                            row[0] = 753.84;
                            row[1] =-0.170905;
                            row[2] = 1.000217;
                            row[3] = 8.971296e-07;
                        }
                    }else  if (goes_num == 15) { // then begin
                        // ;http://www.oso.noaa.gov/goes/goes-calibration/docs/table3_8.htm
                        if (detector == 1) { // then begin ;2/a
                            row[0] = 2560.75;
                            row[1] = -1.661727;
                            row[2] = 1.002864;
                            row[3] = -4.322291e-07;
                        } else if (detector == 2) { // then begin ;2/b
                            row[0] = 2560.75;
                            row[1] = -1.661727;
                            row[2] = 1.002864;
                            row[3] = -4.322291e-07;
                        } else if (detector == 3) { // then begin ;3/a
                            row[0] = 1538.62;
                            row[1] = -3.641359;
                            row[2] = 1.012076;
                            row[3] = -6.791749e-06;
                        } else if (detector == 4) { // then begin ;3/b
                            row[0] = 1538.66;
                            row[1] = -3.637214;
                            row[2] = 1.012033;
                            row[3] = -6.748332e-06;
                        } else if (detector == 5) { // then begin ;4/a
                            row[0] = 935.09;
                            row[1] = -0.518513;
                            row[2] = 1.002645;
                            row[3] = -2.674370e-06;
                        } else if (detector == 6) { // then begin ;4/b
                            row[0] = 934.89;
                            row[1] = -0.515443;
                            row[2] = 1.002754;
                            row[3] = -2.914254e-06;
                        } else if (detector == 7) { // then begin ;6/a
                            row[0] = 752.91;
                            row[1] = -0.164346;
                            row[2] = 1.000240;
                            row[3] = 7.850444e-07;
                        }else if (detector == 8) { // then begin ;6/b
                            row[0] = 752.76;
                            row[1] = -0.162255;
                            row[2] = 1.000288;
                            row[3] = 6.448548e-07;
                            }
                        }else {
                                System.out.println("Coefficients (n, a, b) for GOES-" + g2 +"not currently available.");
                                return null;
                        }

                    if (detector > 0) {
                        double wvn = row[0];
                        double a = row[1];
                        double b1 = row[2];
                        double g = row[3];
                        for (int i = 0; i < data.length; i++) {
                            double Teff = (C2 * wvn) / Math.log(1.0 + (C1 * wvn * wvn * wvn) / radiance[i]);
                            double bt = a + b1 * Teff + g * (Teff * Teff);
                            calData[i] = (float) bt;
                        }
                    } 

                    } else if (g1.equals("Sounder")){   
                        System.out.println("Sounder calibration not supported at this time.");
                        return null;
                    }
                }
              return calData;
        }
    
}