/**
 * Copyright (C) 2008 Rene M.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
 * 
 * Created on 17.10.2008
 */

package freecs.content;

import freecs.core.Membership;
import freecs.core.User;

public class CallMembershipObject {
    public volatile Membership cms;
    public volatile String param;
    public volatile String message;
    public volatile User cu;
    
    public CallMembershipObject(Membership cms, String param, String message, User cu){
        this.cms = cms;
        this.param = param;
        this.message = message;
        this.cu = cu;
    }
    
}