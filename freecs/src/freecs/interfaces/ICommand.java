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
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 * Abstract interface for a command
 * freecs.interfaces
 */
public interface ICommand {
	public abstract String getCmd();
	public abstract String getVersion();
	public abstract boolean execute (MessageState msgState, String parm);
    public abstract Object instanceForSystem();
}
