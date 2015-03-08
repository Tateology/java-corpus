/**
 * Copyright (C) 2004  Jochen Schurich
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

import java.security.MessageDigest;

import freecs.Server;

/**
 * @author jocsch
 *
 */
public class HashUtils {
	public static String encodeMD5(String source) throws Exception {
		if (source != null) {
        	MessageDigest md = MessageDigest.getInstance("MD5");
        	md.update(source.getBytes(Server.srv.DEFAULT_CHARSET));

        	byte[] hash = md.digest();
            // das byte[] in einen hexString, damit er mit der mySQL md5() Funktion 'kompatibel' wird
        	StringBuffer hexString = new StringBuffer();
        	for (int i = 0; i < hash.length; i++) {
        	    if ((0xff & hash[i]) < 0x10) {
        	        hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
        	    } else {
        	        hexString.append(Integer.toHexString(0xFF & hash[i]));
        	    }
        	}
        	return hexString.toString();
		}
		return null;
	}
}
