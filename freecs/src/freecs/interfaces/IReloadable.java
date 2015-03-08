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

import java.io.File;

public interface IReloadable {
	/**
	 * used to retrieve the File-object, which will be checked for the last-modified stamp
	 * @return The File to watch for reloadability
	 */
	public abstract File getFile ();
	/**
	 * returns true if the file was present within the last check
	 * @return true if the file was present within the last check
	 */
	public abstract boolean filePresent();
	/**
	 * returns the lastModified-time of the last check
	 * @return the lastModified-time of the last check
	 */
	public abstract long lastModified ();
	/**
	 * This method will be called if the FileMonitor comes to the conclusion
	 * that the file was changed
	 */
	public abstract void changed ();
	/**
	 * This method will be called if the FileMonitor comes to the conclusion
	 * that the file was remove
	 */
	public abstract void removed ();
	/**
	 * This method will be called if the FileMonitor comes to the conclusion
	 * that the file was created
	 */
	public abstract void created ();
}