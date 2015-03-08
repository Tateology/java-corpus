/**
 * Copyright (C) 2004 Manfred Andres
 * Created: 18.07.2004 (10:20:04)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package freecs.util;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import freecs.Server;


/**
 * @author Manfred Andres
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HttpDateParser {
    public static final String DP_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String DP_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
    public static final String DP_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

    public static final SimpleDateFormat[] formats = {
            HttpDateParser.constructSdf(DP_RFC1123),
            HttpDateParser.constructSdf(DP_RFC1036),
            HttpDateParser.constructSdf(DP_ASCTIME) };

    /**
     * @param pattern for parsing the date
     * @return
     */
    private static SimpleDateFormat constructSdf(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat (pattern, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf;
    }

    
    public static long parseDate(String dateStrg) {
        if (dateStrg == null)
            return -1;
        for (int i = 0; i<formats.length; i++) {
            try {
                long val = formats[i].parse(dateStrg).getTime();
                Server.log ("HTTP-Date-Parser", "time parsed: " + val, Server.MSG_STATE, Server.LVL_MAJOR);
                return val;
            } catch (Exception pe) {
                continue;
            }
        }
        Server.log("HTTP-Date-Parser", "unable to parse date: " + dateStrg, Server.MSG_STATE, Server.LVL_MAJOR);
        return -1;
    }

    private HttpDateParser() { }
}
