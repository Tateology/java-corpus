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

import freecs.content.Connection;
import freecs.core.ConnectionBuffer;

import java.nio.channels.SelectionKey;

/**
 * interface IRequest deffines the methods any requestobject must have
 * and constants same for any kinde of request
 */
public interface IRequest {
    public static final byte METHOD_GET = 0;
    public static final byte METHOD_POST = 1;
    
   /**
    * used to parse the content of this raw request.
    * must be called befoer first retrieval of any property.
    */
   public abstract void parse () throws Exception;

   /**
    * returns the property deffined by the given String
    */
   public abstract String getProperty (String key);
   public abstract String getValue (String key);
   public abstract byte getMethod ();
   public abstract String getAction ();
   public abstract String getProtokol ();
   public abstract String getCookie ();
   public abstract String getUrl ();
   public abstract String getCookieDomain ();
   public abstract String getUserAgent ();
   public abstract ConnectionBuffer getConnectionBuffer ();
   public abstract SelectionKey getKey ();
   public abstract Connection getConnectionObject();
   public abstract boolean foundReferer();
}