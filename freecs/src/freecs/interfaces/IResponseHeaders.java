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
package freecs.interfaces;

public interface IResponseHeaders {
	public final short OK_CODE 			= 200;
	public final short NOCONTENT_CODE 	= 205;
	public final short REDIRECT_CODE		= 303;
	public final short AUTHENTICATE_CODE    = 401;
	public final short NOTFOUND_CODE		= 404;

   public final String OK_HDR       = " 200 OK\r\nServer: Pure Java HTTP-Chat\r\n";
   public final String NOCONTENT_HDR= " 205 NO CONTENT\r\n\r\n";
   public final String NOT_MODIFIED = " 304 NOT MODIFIED\r\n\r\n";
   public final String REDIRECT_HDR = " 303 Redirect\r\nServer: Pure Java HTTP-Chat\r\nContent-Type: text/html; charset=";
   public final String AUTHENTICATE_HDR = " 401 Unauthorized\r\nWWW-authenticate: basic realm=\"freecs\"\r\n";
   public final String NOTFOUND_HDR = " 404 Not found\r\nServer: Pure Java HTTP-Chat\r\n";
}