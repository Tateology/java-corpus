package steve.test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundingTest {

	public static void main(String[] args) {
		
//		double y = -0.05;
//		double z = y*10;
		
//		System.out.println(y+" + "+z);
		
//		System.out.println(Math.floor(z*100+0.5)/100);
		
		
		// seems to work
//		BigDecimal k = BigDecimal.valueOf(270.00);
//		BigDecimal c = k.subtract(BigDecimal.valueOf(273.15));
//		System.out.println(c);
//		
//		BigDecimal x = c.multiply(BigDecimal.valueOf(10));
//		BigDecimal y = x.setScale(0, RoundingMode.HALF_UP);
//		
//		System.out.println(x+","+y+","+y.intValue());

		
		System.out.println(K_to_C_bsst("270"));
		
		// doesn't work
//		double a = 270;
//		double b = a - 273.15;
//		b = b*10;
//		DecimalFormat fmt = new DecimalFormat("#");
//		System.out.println(fmt.format(b));
		

				
	}
	
	
	public static String K_to_C_bsst (String Ktmp)
	{

		BigDecimal k = new BigDecimal(Ktmp);
		BigDecimal c = k.subtract(BigDecimal.valueOf(273.15));

		BigDecimal x = c.multiply(BigDecimal.valueOf(10));
		BigDecimal y = x.setScale(0, RoundingMode.HALF_UP);

		//			System.out.println(x+","+y+","+y.intValue());

		// Ctmp in tens of degrees C
		String Ctmp = y.toString();

		String stmp = Ctmp.trim();    // Check string length
		int l = stmp.length();

		if (l == 3) {Ctmp=" "+stmp;}    // Pad to 4 char where needed
		if (l == 2) {Ctmp="  "+stmp;}
		if (l == 1) {Ctmp="   "+stmp;}

		return Ctmp;
	}

	
}
