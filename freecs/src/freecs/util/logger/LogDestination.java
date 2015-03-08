/**
 * Copyright (C) 2005 KMFDM
 * Created: 13.01.2005 (19:23:39)
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
package freecs.util.logger;

import java.nio.channels.WritableByteChannel;


/**
 * @author KMFDM
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
interface LogDestination {
    abstract WritableByteChannel getChannel() throws Exception;
	abstract WritableByteChannel getCloseChannel(boolean only) throws Exception;
}
