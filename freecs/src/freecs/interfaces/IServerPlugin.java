/**
 * Copyright (C) 2009 Rene M
 * Created: 27.2.2009
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

import java.util.Vector;

import freecs.content.MessageState;
import freecs.core.Group;
import freecs.core.User;




/**
 * @author Rene M
 * 
 * 
 */
public interface IServerPlugin {
    public abstract IServerPlugin instanceForSystem() throws Exception;
    
    public abstract String convertAwaymessage (String message, Vector <String> blockedServerPlugin);
    
    public abstract String convertGroupname (String key, Vector <String> blockedServerPlugin);
    
    public abstract String convertGroutheme (String grp,String theme, Vector <String> blockedServerPlugin);
   
    public abstract String convertAwaymessage (MessageState msgState, String convert, User cu, Vector <String> blockedServerPlugin);
    
    public abstract String convertGroupname (MessageState msgState, String convert, Vector <String> blockedServerPlugin);
    
    public abstract String convertGrouptheme (MessageState msgState, String convert, Vector <String> blockedServerPlugin);
    
    public abstract String convertMessage (MessageState msgState, String convert, boolean toupper, Vector <String> blockedServerPlugin);
   
    public abstract String convertReason (MessageState msgState, String convert, Vector <String> blockedServerPlugin);
   
    public abstract String convertParam (MessageState msgState, String convert, Vector <String> blockedServerPlugin);
   
    public abstract boolean canConvert(MessageState msgState);
    
    public abstract boolean canConvert(Group g);
    
    public abstract boolean canConvert(User u);
    
    public abstract String getName();
}
