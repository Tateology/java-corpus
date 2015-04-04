package steve.test;

import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;

public class GenerateL3Palettes {

	public static void main(String[] args) {
		
		try {
			
			Class cls = Class.forName("gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader");
			Field[] fields = cls.getFields();
			for (Field f : fields) {
				if (f.getName().startsWith("L3PC")) {
					System.out.println(f.getName()+"="+f.getInt(f));
					int productCode = f.getInt(f);

					String name = "nexrad_l3_p"+productCode+".wctpal";
					BufferedWriter bw = new BufferedWriter(new FileWriter(new File("E:\\work\\palettes\\"+name)));
					Color[] colors = NexradColorFactory.getColors(productCode);
					bw.write("# NEXRAD Level-3 palette for product code "+productCode);
					bw.newLine();
					bw.write("# "+f.getName());
					bw.newLine();
					bw.newLine();
					for (int n=colors.length-1; n>=0; n--) {
						if (colors[n].getRed() == 0 && colors[n].getGreen() == 0 && colors[n].getBlue() == 0) {
							continue;
						}

						//				Unique: 15 "(15) RF"              #77007D
						bw.write("Unique: "+n+" \"("+n+")\" "+colors[n].getRed()+" "+colors[n].getGreen()+" "+colors[n].getBlue());
						bw.newLine();				
					}
					bw.close();

				}
			}

			
			

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
