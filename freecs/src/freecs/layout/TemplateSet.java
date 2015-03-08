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
package freecs.layout;

import freecs.Server;
import freecs.util.FileMonitor;
import freecs.interfaces.IReloadable;

import java.io.*;
import java.util.*;

public class TemplateSet implements IReloadable {
	private Hashtable<String, Template> tpl;
	private Properties props;
	public String name;
    private int hashCode=Integer.MIN_VALUE;
	private boolean isValide;

	private File msgSet;
	private long lastModified;
	private boolean msgSetPresent=false;
	private TemplateManager tm;

	public TemplateSet(File dir, TemplateManager tm) throws IOException {
		this.tm = tm;
		this.isValide = false;
		this.name = dir.getName ();
        this.tpl = createTemplates(dir);
		if (!msgSetPresent) {
			if (name.equals("default")) {
				Server.log (this, "Default-templateset doesn't have a message.set-file", Server.MSG_ERROR, Server.LVL_HALT);
			} else {
				TemplateSet foreignTS = tm.getTemplateSet("default");
				if (foreignTS == null) {
					Server.log (this, "No default-templateset present", Server.MSG_ERROR, Server.LVL_MAJOR);
				} else {
					props = foreignTS.getMessageTemplateSet();
                    msgSetPresent=true;
				}
			}
		}
        if (!"admin".equals(name)) { // admin-templates are different
            if (!checkTemplateCompleteness(templatesNeeded))
                return;
            if (!msgSetPresent) {
                Server.log (this, "TemplateSet.construct: Templateset has no message.set! Ignoring.", Server.MSG_STATE, Server.LVL_MAJOR);
                this.isValide= false;
                return;
            }
        } else {
            if (!checkTemplateCompleteness(adminTemplates)) {
                Server.log (this, "TemplateSet.construct: Admin-Templateset doesn't have a header and wount work without it", Server.MSG_STATE, Server.LVL_MAJOR);
                return;
            }
        }
		this.isValide=true;
	}

    private Hashtable<String, Template> createTemplates(File dir) {
        Hashtable<String, Template> tempTable = new Hashtable<String, Template>();
        File tFiles[] = dir.listFiles ();
        for (int i = 0; i < tFiles.length; i++) {
            if (!tFiles[i].isFile ()) 
                continue;
            if (tFiles[i].getName ().equalsIgnoreCase ("message.set")) try {
                readMessageSet (tFiles[i]);
                continue;
            } catch (FileNotFoundException fnfe) {
                // doesn't happen
            } catch (IOException ioe) {
                Server.debug(this, "message.set of " + name + " caused exception", ioe, Server.MSG_ERROR, Server.LVL_MAJOR);
            }
            try {
                Template t = new Template(tFiles[i], this);
                if (t.isValide ()) 
                    tempTable.put (t.getName(), t);
            } catch (IOException ioe) {
                Server.debug(this, "constructing Template caused exception for file " + tFiles[i].getName(), ioe, Server.MSG_ERROR, Server.LVL_MAJOR);
            }
        }
        if (!msgSetPresent) {
            if (name.equals("default")) {
                Server.log (this, "Default-templateset doesn't have a message.set-file", Server.MSG_ERROR, Server.LVL_HALT);
            } else {
                TemplateSet foreignTS = tm.getTemplateSet("default");
                if (foreignTS == null) {
                    Server.log (this, "No default-templateset present", Server.MSG_ERROR, Server.LVL_MAJOR);
                } else {
                    props = foreignTS.getMessageTemplateSet();
                    msgSetPresent=true;
                }
            }
            msgSet=null;
            FileMonitor.getFileMonitor().removeMonitor(this);
        } else {
            FileMonitor.getFileMonitor ().addReloadable (this);
        }
        return tempTable;
    }

    public void reload(File dir) {
        createTemplates(dir);
    }
    
   public String getName () {
      return name;
   }

   public Template getTemplate (String tName) {
      return ((Template) tpl.get (tName));
   }

	public void removeTemplate (Template t) {
		tpl.remove(t.getName());
	}
	public void addTemplate (Template t) {
		tpl.put(t.getName(), t);
	}

   public String getMessageTemplate (String msgTplName) {
      return props.getProperty (msgTplName);
   }
   public Properties getMessageTemplateSet () {
   		return props;
   }

   public boolean isValide () {
      return isValide;
   }

	public void readMessageSet (File f) throws FileNotFoundException, IOException {
		// we have found the messageset
		msgSetPresent=true;
		msgSet = f;
		lastModified = msgSet.lastModified ();
		FileInputStream fis = new FileInputStream (f);
		if (props == null)
			props = new Properties ();
		props.load (fis);
		fis.close ();
		Properties tProps = (Properties) props.clone();
		
		
		for (int i = 0; i < neededMessageTemplates.length; i++) {
			if (props.get(neededMessageTemplates[i]) == null) {
				Server.log (this, "readMessageSet [" + name + "]: Message-template '" + neededMessageTemplates[i] + "' is not present", Server.MSG_STATE, Server.LVL_VERBOSE);
			}
			tProps.remove(neededMessageTemplates[i]);
		}
		for (Enumeration<Object> ig = tProps.keys(); ig.hasMoreElements(); ) {
		    String param = ig.nextElement().toString ();
		    int pos = param.indexOf(".");
		 
		    if (pos > 0) {
		        String p = param.substring(0,pos);
		        if (Ignore(p))
		    	    tProps.remove(param);
		    }
		    
		}
		for (Enumeration<Object> e = tProps.keys(); e.hasMoreElements(); ) {
			Server.log (this, "readMessageSet [" + name + "]: Unknown message-template found: " + e.nextElement().toString (), Server.MSG_STATE, Server.LVL_VERBOSE);
		}
	}

   /* INTERFACE RELOADABLE */
   public long lastModified () {
      return lastModified;
   }

   public void changed () {
      try {
         FileInputStream fis = new FileInputStream (msgSet);
         Properties tprop = new Properties ();
         tprop.load (fis);
         fis.close ();
         props = tprop;
         lastModified = msgSet.lastModified ();
         Server.log (this, "reload: reloaded messagetemplates", Server.MSG_STATE, Server.LVL_MINOR);
      } catch (Exception e) {
         Server.debug (this, "reload: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
      }
   }

	public void removed () {
   		if (name.equals("default")) {
			Server.log (this, "WARNING: message.set has been removed for DEFAULT-layout! Default-Layout must have one!", Server.MSG_ERROR, Server.LVL_MAJOR);
   		} else {
			Server.log (this, "WARNING: message.set has been removed for layout " + name, Server.MSG_ERROR, Server.LVL_MINOR);
   		}
		TemplateSet foreignTS = tm.getTemplateSet("default");
		if (foreignTS == null) {
			Server.log (this, "No default-templateset present making it impossible to fall back to the default-layout's message.set. Keeping the old messag.set", Server.MSG_ERROR, Server.LVL_MAJOR);
		} else {
			props = foreignTS.getMessageTemplateSet();
		}
		msgSetPresent = false;
	}

	public boolean filePresent() {
		return msgSetPresent;
	}

	public void created () {
		changed();
		msgSetPresent = true;
	}

   public File getFile () {
      return msgSet;
   }


    private boolean checkTemplateCompleteness(String[] templates) {
        for (int i=0; i < templates.length; i++) {
            if (tpl.containsKey (templates[i])) 
                continue;
            this.isValide = false;
            return false;
        }
        return true;
    }
    
    private boolean Ignore(String param) {
    	if (param.equals("constant") || param.equals("funcommand")
    	        || param.equals("messageextern") || param.equals("errorextern"))
    		return true;
    	return false;
    }
    
    private volatile String strgVal;
    public String toString() {
        if (strgVal==null) {
            StringBuffer sb = new StringBuffer("[TemplateSet: ");
            sb.append (this.name);
            sb.append ("]");
            strgVal = sb.toString();
        }
        return strgVal;
    }
    
    public boolean equals (Object obj) {
        if (obj==null)
            return false;
        if (!(obj instanceof TemplateSet))
            return false;
        if (!this.name.equals(((TemplateSet) obj).name))
            return false;
        return true;
    }
    
    public int hashCode () {
        if (hashCode != Integer.MIN_VALUE)
           return hashCode;
        hashCode = ("TemplateSet" + name).hashCode();
        return (hashCode);
     }


    private static final String templatesNeeded[] = {
            "start",
            "welcome",
            "login_missing",
            "login_failed",
            "not_found" };
    
    private static final String adminTemplates[] = {
            "admin_header"
    };
    
    public static final String neededMessageTemplates[] =
      { "error.away.confirm",
    	"error.bgcol.notAllowedCode",
    	"error.group.notExisting",
        "error.user.notOnServer.singular",
        "error.user.notOnServer.plural",
        "error.a.noLongerValid",
        "error.me.noArg",
        "error.m.cantHearYou",
        "error.ban.noArg",
        "error.ban.noRight",
        "error.ban.alreadyBaned",
        "error.uban.noArg",
        "error.uban.noRight",
        "error.col.startingGroup",
        "error.col.noRight",
        "error.col.wrongCode",
        "error.col.lockedColor",
        "error.col.tooOften",
        "error.td.noArg",
        "error.td.noRight",
        "error.td.wrong",
        "error.td.toomany",
        "error.ig.noArg",
        "error.i.noRight",
        "error.i.noArg",
        "error.i.alreadyInvited",
        "error.ia.all.noRight",
        "error.ia.noRight",
        "error.ia.inviteStartGroup",
        "error.j.noArg",
        "error.j.alreadyHere",
        "error.j.banned",
        "error.j.noRightToOpen",
        "error.j.closed",
        "error.j.joinunreg",
        "error.j.membershiplocked",
        "error.j.reserved",
        "error.j.noRight",
        "error.jclosed.noRight",
        "error.jclosed.groupNotExisting",
        "error.ju.alreadyHere",
        "error.ju.noArg",
        "error.k.noRight",
        "error.k.noGroupRight",
        "error.kc.noroom",
        "error.kh.noRight",
        "error.fl.nofriends",
        "error.f.noFriendOnline",
        "error.wban.noRight",
        "error.wban.nobodyBanned",
        "error.vip.noVipOnline",
        "error.l.noRight",
        "error.ul.noRight",
        "error.m.noArg",
        "error.m.noMessage",
        "error.m.vip.noRight",
        "error.moderated.timelock",
		"error.gag.noRight",
        "error.gag.noArg",
        "error.gag.protected",
        "error.aq.noArg",
        "error.rig.noArg",
        "error.rig.noIgnoredUser",
        "error.rsu.noArg",
        "error.rsu.noRight",
        "error.su.noArg",
        "error.su.noRight",
        "error.su.tooManyForStartgroup",
        "error.sepa.noArg",
        "error.sepa.noRight",
        "error.sepa.l.noRight",
        "error.sepa.alreadyExists",
        "error.sepa.confirm",
        "error.j.confirm",
        "error.ju.confirm",
        "error.jclosed.confirm",
        "error.q.confirm",
        "error.t.noRight",
        "error.s.noArg",
        "error.ip.noArg", 
        "error.ip.noProxyUser",
        "error.w.nobodyHere",
        "error.th.noArg",
        "error.sys.noArg",
        "error.user.punished",
		"error.fun.noArg",
		"error.fun.commandnotfound",
		"message.bgcol.listAllowedCode",
        "message.send",
        "message.send.moderated",
        "message.send.moderated.personal",
        "message.a",
        "message.a.personal",
        "message.away.on",
        "message.away.off",
        "message.away.whisper.singular",
        "message.away.whisper.plural",
        "message.away.friendnotification.singular",
        "message.away.friendnotification.plural",
        "message.col",
        "message.c",
        "message.me",
        "message.ban.personal",
        "message.ban.plural",
        "message.ban.singular",
        "message.ban.confirm.plural",
        "message.ban.confirm.singular",
        "message.uban.personal",
        "message.uban.plural",
        "message.uban.singular",
        "message.uban.confirm.plural",
        "message.uban.confirm.singular",
        "error.uban.notOnList.singular",
        "error.uban.notOnList.plural",
        "message.td",
        "message.ig.plural",
        "message.ig.singular",
        "message.i.personal",
        "message.i.plural",
        "message.i.singular",
        "message.ia.all",
        "message.ia.group",
        "message.j.created",
        "message.j",
        "message.jclosed",
        "message.k.destination.singular",
        "message.k.confirm.singular",
        "message.k.singular",
        "message.k.destination.plural",
        "message.k.confirm.plural",
        "message.k.plural",
        "message.k.personal",
        "message.kh.singular",
        "message.kh.confirm.singular",
        "message.kh.plural",
        "message.kh.confirm.plural",
        "message.kh.personal",
		"message.kh.godinfo",
        "message.kc.personal",
        "message.kc.destination.singular",
        "message.kc.confirm.singular",
        "message.kc.singular",
        "message.kc.destination.plural",
        "message.kc.confirm.plural",
        "message.kc.plural",
		"message.kc.godinfo",
        "message.f.headline",
        "message.f.isOnline",
        "message.f.joined",
        "message.f",
        "message.f.count",
        "message.fonly.true",
        "message.fonly.false",
        "message.wban",
        "message.wc.headline",
        "message.m.vip",
        "message.m.vip.confirm",
        "message.mcall.history",
        "message.mcall",
        "message.vip.singular",
        "message.vip.plural",
        "message.l",
        "message.ul",
        "message.mycol",
        "message.m.notification",
        "message.m",
        "message.m.away",
        "message.m.confirm",
        "message.gag.singular",
        "message.gag.plural",
        "message.gag.confirm.singular",
        "message.gag.confirm.plural",
        "message.aq",
        "message.q",
        "message.rig.singular",
        "message.rig.plural",
        "message.rsu.personal",
        "message.rsu.singular",
        "message.rsu.plural",
        "message.rsu.confirm.singular",
        "message.rsu.confirm.plural",
        "message.su.personal",
        "message.su.singular",
        "message.su.plural",
        "message.su.confirm.singular",
        "message.su.confirm.plural",
        "message.sepa",
        "message.sepa.confirm",
        "message.s",
        "message.ip",
        "message.ip.anoproxy",
        "message.time",
        "message.user.detail",
        "message.t",
        "message.t.removed",
        "message.th",
        "message.sys",
        "message.fl.headline",
        "message.fl.entry.offline",
        "message.fl.entry.online",
        "message.fl.count",
        "message.user.short",
        "message.user.short.seperator",
        "message.user.overview",
        "message.user.flooded",
        "message.user.tooled",
        "message.user.leaving.group",
        "message.user.join.group",
        "message.user.join.closedGroup",
        "message.user.join.server",
        "message.user.join.server.personal",
        "message.user.vip",
        "message.user.loginpresent",
        "message.server.shutdown",
        "message.wc.underline",
        "message.softClose",
        "list.users",
        "constant.userListItem",
        "status.showtime",
        "status.showtime.timeformat",
        "constant.defaultColor",
		"constant.lockedGroup",
		"constant.openGroup",
        "constant.linkedName",
        "error.ack.groupNotModerated",
        "error.ack.noArg",
        "error.ack.noMessage",
        "error.ack.userNotInGroup",
        "error.i.alreadyHere",
        "error.rgag.noArg",
        "error.rgag.noRight",
        "error.sepa.alreadyHere",
        "message.ack",
        "message.gag.personal",
        "message.rgag.personal",
        "message.rgag.confirm.plural",
        "message.rgag.confirm.singular",
        "message.rgag.plural",
        "message.rgag.singular",
        "message.user.leaving.server",
        "message.user.leaving.server.kicked",
        "error.raq.noRight",
        "message.raq",
        "error.flock.noRight",
        "error.flock.noArg",
        "error.flock.noUser",
        "message.flock.singular",
        "message.flock.plural",
        "message.flock.confirm.singular",
        "message.flock.confirm.plural",
        "message.flock.personal",
        "error.noRight.isAdmin",
        "error.noRight.isVip",
        "error.noRight.noSuVipAdmin",
        "error.noRight.noVipAdmin",
        "error.noRight.noAdmin",
        "error.noRight.noModAdmin",
        "error.noRight.noMod",
        "error.noRight.deactivated",
		"error.noRight.isSuForbiddenMembership",
        "error.mass.noRight.noVipAdmin",
		"error.rc.noArg",
		"error.rc.userNotFound",
		"error.rc.rightNotFound",
		"error.rc.notRight",
		"rc.rightAssigned",
		"rc.rightRevoked",
		"rc.newAssignedRight",
		"rc.newRevokedRight",
		"rc.rightsReset",
		"rc.newResetRight",
		"message.woff.activated",
		"message.woff.deactivated"};
}