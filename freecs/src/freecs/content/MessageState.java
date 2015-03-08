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
package freecs.content;
import freecs.Server;
import freecs.core.*;

/**
 * Used to store values needed for a message and to
 * make it accessible to other methods (makes it possible
 * to move message-commands into seperate-classes)
 */
public class MessageState {
	public volatile boolean useRenderCache, moderated;
	public volatile Group sourceGroup, targetGroup;
	public volatile User sender, usercontext;
	public volatile String message, param, msg, reason;
	public volatile String msgTemplate;
	public volatile ConnectionBuffer cb;
    public MessageParser mp;
    public volatile Object[] usrList;

	public MessageState (MessageParser mp) {
        this.mp=mp;
		clear();
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
	}
	
	public void clear() {
		sender=null;
		usercontext=null;
		message=null;
		reason=null;
		message=null;
		param=null;
		msg=null;
		msgTemplate=null;
		useRenderCache=true;
		moderated = false;
        usrList = null;
	}
    
    public void inhale (MessageState mst) {
        this.cb = mst.cb;
        this.message = mst.message;
        this.moderated = mst.moderated;
        this.msg = mst.msg;
        this.msgTemplate = mst.msgTemplate;
        this.reason = mst.reason;
        this.sender = mst.sender;
        this.sourceGroup = mst.sourceGroup;
        this.targetGroup = mst.targetGroup;
        this.usercontext = mst.usercontext;
        this.useRenderCache = mst.useRenderCache;
        this.usrList = mst.usrList;
    }
    
    public Object clone () {
        MessageState mst = new MessageState(this.mp);
        mst.cb = this.cb;
        mst.message = this.message;
        mst.moderated = this.moderated;
        mst.msg = this.msg;
        mst.msgTemplate = this.msgTemplate;
        mst.reason = this.reason;
        mst.sender = this.sender;
        mst.sourceGroup = this.sourceGroup;
        mst.targetGroup = this.targetGroup;
        mst.usercontext = this.usercontext;
        mst.useRenderCache = this.useRenderCache;
        mst.usrList = this.usrList;
        return mst;
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}
