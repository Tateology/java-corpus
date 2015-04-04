/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */


package gov.noaa.ncdc.conversions;

public class Conversions {    
    
    /**
     * Converts a julian date (number of days from Jan 1, 1970)
     * @return String representation of YYYYMMDD
     */
    public static String convertJulianDate(short jdate) {

    int days[]={31,28,31,30,31,30,31,31,30,31,30,31};
    int year,month=0,ndays,day=0,total,len,i;
    boolean leap_year;

      total=0;
      for(year=1970;year<2098;year++)
      {
         leap_year=false;
         if (year%4 == 0)
            leap_year=true;

         for(month=0;month<12;month++)
         {
            total = total+days[month];
            if (month==1 && leap_year)
               total++;
            if (total >= jdate)
            {
               ndays=days[month];
               if (month==1 && leap_year)
                  ndays++;
               day=ndays - (total-jdate);
               month=month+1;
               return (""+(year*10000 + month*100 + day));
            }
         }
      }  
    return null;
    } // END convertJulianDate
    
}
