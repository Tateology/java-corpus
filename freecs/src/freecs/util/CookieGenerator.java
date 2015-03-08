/**
 * Copyright (C) 2003  Manfred Andres
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

import freecs.core.*;

public class CookieGenerator {
   private static int COOKIE_LENGTH = 32;

   private CookieGenerator () {
      // not instanciable
   }

	/**
	 * Ceckfunction to check the validity of a FreeCS-Cookie
	 * FIXME: has to get better (e.g. include ip-address)
	 * @param cookie
	 * @return boolean true if cookie is valid, false if not
	 */
	public static boolean checkValidity (String cookie) {
		if (cookie == null || cookie.length() != COOKIE_LENGTH)
			return false;
		return true;
	}

   public static String generateCookie () {
      String cval = "";
      for (boolean ok = false; !ok; ) {
         StringBuffer c = new StringBuffer ();
         while (c.length () < COOKIE_LENGTH) {
            char x = (char) Math.ceil(Math.random() * 34);
            if (x < 10) x = (char) (x + 48);
            else        x = (char) (x + 87);
            c.append(x);
         }
         cval = c.toString ();
         User u = UserManager.mgr.getUserByCookie (cval);
         if (u != null) continue;
         ok = true;
      }
      return cval;
   }

   public static void main (String args[]) {
      System.out.println ("generating cookie");
      long start = System.currentTimeMillis();
      for (int i = 0; i < 100000; i++) {
         String c = generateCookie ();
      }
      StringBuffer tsb = new StringBuffer ("spent ").append ((System.currentTimeMillis () - start)).append (" millis");
      System.out.println (tsb.toString ());
      System.out.println ("cookie generated");
   }
}