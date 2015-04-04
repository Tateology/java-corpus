package steve.test;


import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.nc2.units.DateUnit;

public class UdUnitsTest {

	public static void main(String[] args) {
		
		try {
			DateUnit du = new DateUnit("hours since 1978-01-01 00:00:00");
			String isoDate = du.makeStandardDateString(273888);
			System.out.println(isoDate);
			
			// 4.3 NCJ upgrade broke this
			
//			double hours = du.makeValue(
//					CalendarDateFormatter.isoStringToCalendarDate(Calendar.getDefault(), "2008-08-01T00:00:00Z"));
////			double hours = du.makeValue(df.isoDateTimeFormat(isoDate));
//			System.out.println(hours);
////			System.out.println("date index="+dateIndex/6);
//			
//			hours = du.makeValue(df.getISODate("2010-05-01T00:00:00Z"));
////			System.out.println("date index="+dateIndex/6);
//			System.out.println(hours);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
