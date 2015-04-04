package steve.test;

import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.goes.GoesColorFactory;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster;

public class CreateMapFileStyles {

    public static void main(String[] args) {
        
//        int n=0; 
//        for (Color c : NexradColorFactory.getColors(19)) {
//            System.out.println("    CLASS");
//            System.out.println("        EXPRESSION ([pixel] >= "+n+" AND [pixel] < "+(n+5)+")");
//            System.out.println("        STYLE");
//            System.out.println("            COLOR "+c.getRed()+" "+c.getGreen()+" "+c.getBlue());
//            System.out.println("        END");
//            System.out.println("    END");
//            
//            n+=5;
//        }
        
        
        try {
            
        
            GoesColorFactory gcf = GoesColorFactory.getInstance();
            GoesRemappedRaster raster = new GoesRemappedRaster();
            raster.setColorTableAlias("McIDAS - TSTORM1.ET");
            ColorsAndValues cav = gcf.getColorsAndValues(raster);
            
            for (int n=0; n<cav.getColors().length; n++) {
                System.out.println("    CLASS");
                System.out.println("        EXPRESSION ([pixel] >= "+cav.getValues()[n]+" AND [pixel] < "+cav.getValues()[n+1]+")");
                System.out.println("        STYLE");
                System.out.println("            COLOR "+cav.getColors()[n].getRed()+" "+cav.getColors()[n].getGreen()+" "+cav.getColors()[n].getBlue());
                System.out.println("        END");
                System.out.println("    END");
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
