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
 * 
 * FreeCS Server
 * This is the main-class which starts the server
 */
package freecs;

import freecs.commands.CommandSet;
import freecs.content.*;
import freecs.layout.*;
import freecs.auth.*;
import freecs.core.*;
import freecs.external.xmlrpc.XmlRpcManager;
import freecs.interfaces.*;
import freecs.util.*;
import freecs.util.logger.LogCleaner;
import freecs.util.logger.LogWriter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.text.SimpleDateFormat;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
/**
 * 
 * @author Manfred Andres
 *
 */
public class Server implements IReloadable {
	// the versionstring for this release
	private static final String VERSION = "1.3.20111224";

	// different request-types may be inserted here (for further use)
	public  static final int    REQUEST_TYPE_HTTP   = 1;
	
	// the home directory will be stored here
	public static String        BASE_PATH;
    
    public static final long startupTime=System.currentTimeMillis();
	
	// these fields are used for the configuration
	// default-values may be changed in checkForConfigVals()
    public volatile String SMILEY_SERVER; 
    public volatile boolean USE_SMILEY; 
    public volatile int SMILEY_PER_LINE; 

    public String[] ADMIN_HTTP_USERNAME, ADMIN_HTTP_PASSWORD, ADMIN_HTTP_SECLEVEL;
    public String   DEFAULT_CHARSET="iso-8859-1", TIMEZONE, ADMIN_HTTP_ALLOWED,
				    ADMIN_XMLRPC_ALLOWED, DEFAULT_TEMPLATESET, DEFAULT_MEMBERSHIP, MOBILE_BROWSER_REGEX;
	public long   	TOUCH_USER_DELAY, READER_MAX_IDLETIME, FILE_CHECK_INTERVAL, 
                    FLOOD_PROTECT_MILLIS, USER_TIMEOUT, USER_AWAY_TIMEOUT, 
                    USER_REMOVE_SCHEDULE_TIME, HOST_BAN_DURATION, VIP_TIMEOUT, VIP_AWAY_TIMEOUT,
                    READER_TIMEOUT, LOGIN_TIMEOUT, PUNISH_DURATION;
	public boolean 	ALLOW_EXTERNAL, DEBUG_TEMPLATESET, USE_HTTP11, 
                    USE_IP_BAN, THREAD_PER_READ, USE_TOKENSTORE, MD5_PASSWORDS,
                    USE_MESSAGE_RENDER_CACHE, USE_TRAFFIC_MONITOR, USE_CENTRAL_REQUESTQUEUE, ALLOW_CHANGE_USERAGENT,
                    STRICT_HOST_BINDING,CAN_DEL_LOGS, USE_FADECOLOR, USE_BGCOLOR, BLOCKED_NICK_AUTOHARDKICK, USE_PLUGINS, 
                    REDIRECT_MOBILE_BROWSER;
	public int    	READER_MAX_QUEUE, READER_MAX_QUEUE_USAGE, MAX_READERS, MAX_BAN_DURATION, MAX_FLOCK_DURATION, 
	                MAX_SU_BAN_DURATION, DEFAULT_BAN_DURATION, FLOOD_PROTECT_TOLERANC, FLOOD_BAN_DURATION, 
                    READBUFFER_SIZE, MAX_USERS, COLOR_CHANGE_INTERVAL, MESSAGE_FLOOD_INTERVAL, MAX_USERNAME_LENGTH,  
                    MAX_DIE_NUMBER, MAX_DIE_EYES, TCP_RECEIVE_BUFFER_WINDOW,
                    LOG_QUEUE_SIZE, MAX_REQUESTS_PER_PROXY_IP, MAX_REQUESTS_PER_IP,
                    MAX_SUUSERS_PER_STARTGROUP, INITIAL_RESPONSE_QUEUE, MAX_RESPONSE_QUEUE,
                    MAX_GROUPNAME_LENGTH, MAX_GROUPTHEME_LENGTH, ADMIN_XMLRPC_PORT, TOOL_PROTECT_COUNTER, 
                    TOOL_PROTECT_TOLERANC, TOOL_BAN_DURATION, TOOL_PROTECT_MINMILLS, TOOL_PROTECT_MINCOUNTER,
                    JOIN_PUNISHED_COUNTER, LOGFILE_DELDAYS, LOGFILE_DELHOUR, MESSAGE_FLOOD_LENGHT, MAX_MCALL_KEY,
                    MAX_PMSTORE;

	private Vector<InetAddress> 				adminHosts;
	private Vector<String>                      tempAdmins;
	public  Vector<InetAddress> allowedLoginHosts;   
	public  Vector<String>      SERVER_NAME;
	public  StringBuffer        COOKIE_DOMAIN;
	public 	TemplateManager 	templatemanager = null;
	public 	AuthManager 		auth;
	public 	static Server 		srv = null;
	private Hashtable<String, BanObject> 			banList;
	private Hashtable<ActionstoreObject, String>           storeList;
	public 	InetAddress  		lh = null;
	public  Properties			props;
	public static Calendar		cal = Calendar.getInstance();
	private volatile boolean 	isRunning = true;
    public Charset              defaultCs=Charset.forName(DEFAULT_CHARSET);
    public CharsetEncoder       defaultCsEnc=defaultCs.newEncoder();
    
    public static boolean TRACE_CREATE_AND_FINALIZE = false;
	
	private Hashtable<String, String>	tokenStore = new Hashtable<String, String>();

    public long KEEP_ALIVE_TIMEOUT;

	public String 	UNAME_PREFIX_GOD,
	                UNAME_PREFIX_GUEST,
					UNAME_PREFIX_MODERATOR,
					UNAME_PREFIX_PUNISHED,
					UNAME_PREFIX_SU,
					UNAME_PREFIX_VIP,
					UNAME_SUFFIX_GOD,
	                UNAME_SUFFIX_GUEST,
					UNAME_SUFFIX_MODERATOR,
					UNAME_SUFFIX_PUNISHED,
					UNAME_SUFFIX_SU,
					UNAME_SUFFIX_VIP;

    public short FN_DEFAULT_MODE_FALSE = 0;
    public short FN_DEFAULT_MODE_TRUE = 2;
    public short COLOR_LOCK_MODE = 0;// brightly =1, darkly=2
    public short COLOR_LOCK_LEVEL = 1, FADECOLOR_LOCK_LEVEL = -1;
    public String  MIN_BBC_FONT_RIGHT_ENTRACE, MIN_BBC_FONT_RIGHT_SEPA, MIN_BBC_B_RIGHT_ENTRACE, MIN_BBC_B_RIGHT_SEPA,
                   MIN_BBC_U_RIGHT_ENTRACE, MIN_BBC_U_RIGHT_SEPA, MIN_BBC_I_RIGHT_ENTRACE, MIN_BBC_I_RIGHT_SEPA ;
    public boolean USE_BBC, BBC_CONVERT_GROUPNAME, BBC_CONVERT_GROUPTHEME;
    public int     MAX_BBCTAGS;
    
    public IServerPlugin [] serverPlugin = null; 
    public IThreadManagerPlugin [] threadManager = null;

    public HashMap<String, Object> pluginStore = new HashMap<String, Object>();
    public HashMap<String, Object> allCommands = new HashMap<String, Object>();
    public HashMap<String, Object> xmlRpcHandler = new HashMap<String, Object>();

	public Server () {
		// this sets the logging to very verbose (just for startup)
		LOG_MASK[0]=new Short (LVL_MINOR);
		LOG_MASK[1]=new Short (LVL_MINOR);
		LOG_MASK[2]=new Short (LVL_MINOR);
		LOG_MASK[3]=new Short (LVL_MINOR);
		LOG_MASK[4]=new Short (LVL_MINOR);
		LOG_MASK[5]=new Short (LVL_MINOR);
		LOG_MASK[6]=new Short (LVL_MINOR);
	
		try {
		   lh = InetAddress.getLocalHost ();
		} catch (UnknownHostException uhe) {
		   System.out.println ("Server: No networkinterface found: " + uhe.getCause());
		   uhe.printStackTrace();
		   System.exit (1);
		}
		banList = new Hashtable<String, BanObject> ();
		storeList = new Hashtable<ActionstoreObject, String> ();
		props = new Properties ();
		tempAdmins = new Vector<String>();
		adminHosts = new Vector<InetAddress> ();
		allowedLoginHosts = new Vector<InetAddress> ();
	}

	/**
	 * the main-method of server is responsible for:
	 * .) reading in the configuration
	 * .) initializing the server
	 * .) starting the neccessary threads
	 * 
	 * If this task's are completed this main-thread will
	 * be used to remove bans which are not valid anymore
	 * @param args the base-path may be given @commandline. Usage: freecs.Server -b=[path to mainfolder]
	 */
    public static void main (String args[]) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith ("-b=")) {
                BASE_PATH=args[i].substring(3);
            } else if (args[i].equals("printcharsets")) {
                System.out.println("Available Charsets:");
                Set<String> ks = Charset.availableCharsets().keySet();
                for (Iterator<String> it = ks.iterator(); it.hasNext(); ) {
                    System.out.print(":> ");
                    System.out.println((String) it.next());
                }
                System.exit(0);
            }
        }
        if (BASE_PATH==null)
            BASE_PATH="./";
		srv=new Server ();
		srv.readConfig ();
		srv.initServer ();
		srv.loadThreadManager(new Vector<String>());
		srv.startThreads ();
        if (srv.USE_CENTRAL_REQUESTQUEUE)
            Server.log ("Server", "starting up with CENTRAL-requestqueue", Server.MSG_STATE, Server.LVL_MAJOR);
        else
            Server.log ("Server", "starting up with per RequestReader-requestqueue", Server.MSG_STATE, Server.LVL_MAJOR);
        long lastMessage=0;
		while (srv.isRunning ()) {
/*            if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                Server.log ("Server", "loopstart", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                lastMessage = System.currentTimeMillis();
            } */
			try {
				long now = System.currentTimeMillis ();
				long rws[][] = RequestReader.getWorkingSince();
				boolean b[] = RequestReader.getAliveState();
				StringBuffer sb = new StringBuffer ("ThreadsWorkingTime:");
				for (int i = 0; i < rws.length; i++) {
					if (rws[i][0] == 0) {
						sb.append (" 0, ");
					} else {
                        sb.append (" ");
						sb.append (now - rws[i][0]);
						sb.append (", ");
					}
					if (b[i]) sb.append ("alive@");
					else      sb.append ("dead@");
					switch ((short) rws[i][1]) {
						case RequestReader.WAITING:
							sb.append("waiting");
							continue;
						case RequestReader.EVAL_GET_MESSAGES_APND2WRITE:
							sb.append("appending message to writequeue");
							continue;
						case RequestReader.EVAL_GET_MESSAGES_SND_MSGS:
							sb.append("sending scheduled message");
							continue;
						case RequestReader.EVAL_GET_MESSAGES:
							sb.append("sending messages-frame");
							continue;
						case RequestReader.EVAL_GET_STATE:
							sb.append("retrieving /state");
							continue;
						case RequestReader.EVAL_GET:
							sb.append("evaluating getrequest");
							continue;
						case RequestReader.EVAL_POST:
							sb.append("evaluating postrequest");
							continue;
						case RequestReader.EVAL_POST_LOGIN:
							sb.append("loging in");
							continue;
						case RequestReader.EVAL_PREP4SEND:
							sb.append("perparing for sending");
							continue;
						case RequestReader.EVAL_SEND:
							sb.append("evaluating a /SEND request");
							continue;
						case RequestReader.EVAL_SENDFINAL:
							sb.append("sending content");
							continue;
						case RequestReader.EVALUATE_COMMAND:
							sb.append("evaluating a command");
                            String cmd = RequestReader.getCurrCommant(i);
                            if (cmd != null)
                                sb.append (" (").append (cmd).append (")");
							continue;
						case RequestReader.EVALUATING:
							sb.append("evaluating");
							continue;
						case RequestReader.PARSE_MSG:
							sb.append("parsing message");
							continue;
						case RequestReader.READING:
							sb.append("reading");
							continue;
						case RequestReader.EVAL_POST_LOGIN_RESULT:
							sb.append("evaluating login-result");
							continue;
						case RequestReader.TRYLOGIN:
							sb.append("trylogin");
							continue;
						case RequestReader.TRYLOGIN_AUTHENTICATE:
							sb.append("trylogin authenticate");
							continue;
						case RequestReader.TRYLOGIN_CHECK_FRIENDS:
							sb.append("trylogin check friends");
							continue;
						case RequestReader.TRYLOGIN_CHECK4PRESENCE:
							sb.append("trylogin check for presence");
							continue;
						case RequestReader.TRYLOGIN_CORRECT_PERMISSION:
							sb.append("trylogin correct permission");
							continue;
						case RequestReader.TRYLOGIN_SCHEDULE_FRIENDMSGS:
							sb.append("trylogin schedule online-friends-messages");
							continue;
						case RequestReader.TRYLOGIN_SCHEDULE_VIPMSG:
							sb.append("trylogin schedule vip-message");
							continue;
						case RequestReader.TRYLOGIN_SEND_LOGINMSG:
							sb.append("trylogin send loginmessages");
							continue;
						case RequestReader.TRYLOGIN_SET_GROUP:
							sb.append("trylogin set group");
							continue;
						case RequestReader.TRYLOGIN_SET_PERMISSION:
							sb.append("trylogin set permission");
							continue;
					}
				}
				Server.log ("static Server", sb.toString(), MSG_STATE, LVL_VERBOSE);
                Runtime r = Runtime.getRuntime ();
                long free = r.freeMemory ();
                long total = r.totalMemory ();
                long max = r.maxMemory ();
                long used = r.totalMemory () - r.freeMemory ();
                sb = new StringBuffer();
                sb.append ("Memory-Report (VM-MaxSize/VM-CurrSize/free/used): ");
                sb.append (max).append ("/");
                sb.append (total).append ("/");
                sb.append (free).append ("/");
                sb.append (used);
                Server.log (null, sb.toString(), MSG_STATE, LVL_MINOR);

                // Cleaning up the banlist is triggered here
                long lVal = now + 30001;
				for (Enumeration<String> e = srv.banList.keys (); e.hasMoreElements () ; ) {
					Object key = e.nextElement ();
					BanObject bObj = (BanObject) srv.banList.get(key);
					if (bObj == null)
						continue;
					if (bObj.bannedBy.equals("Config( parmaBannedIp )"))
						continue;
					if (bObj.time < now) {
					if (checkLogLvl (Server.MSG_STATE, Server.LVL_MINOR)) {
						sb = new StringBuffer ("Server: removing ban for ").append (key);
						Server.log ("static Server", sb.toString (), MSG_STATE, LVL_MINOR);
					}
						srv.banList.remove (key);
					} else if (bObj.time < lVal) 
						lVal = bObj.time;
				}
				 // Cleaning up the storelist is triggered here
				lVal = now + 30001;
				for (Enumeration<ActionstoreObject> e = srv.storeList.keys(); e
						.hasMoreElements();) {
					Object key = e.nextElement();

					ActionstoreObject sObj = (ActionstoreObject) key;
					if (sObj == null)
						continue;
					if (sObj.time < now) {
						if (checkLogLvl(Server.MSG_STATE, Server.LVL_MINOR)) {
							sb = new StringBuffer("Server: removing store for ");
							sb.append(sObj.usr);
							sb.append("(");
							sb.append(sObj.action);
							sb.append(")");
							Server.log("static Server", sb.toString(),
									MSG_STATE, LVL_MINOR);
						}
						srv.storeList.remove(key);
						if (sObj.equalsActionState(IActionStates.SUBAN)) {
							Group g = GroupManager.mgr.getGroup(sObj.room);
							User u = UserManager.mgr.getUserByName(sObj.usr);
							if (g != null) {
								if (u != null) {
									MessageParser mp = new MessageParser();
									mp
											.setMessageTemplate("message.uban.server");
									mp.setTargetGroup(g);
									u.sendMessage(mp);
									g.setBanForUser(u.getName(), false);
								} else
									g.setBanForUser(sObj.usr, false);
							}
						}
						if (sObj.equalsActionState(IActionStates.FLOCKCOL)) {
						    User u =UserManager.mgr.getUserByName(sObj.usr);
						    if (u!=null)
						        u.setCollock(false);
						}
                        if (sObj.equalsActionState(IActionStates.FLOCKAWAY)) {
                            User u =UserManager.mgr.getUserByName(sObj.usr);
                            if (u!=null)
                                u.setAwaylock(false);
                        }
                        if (sObj.equalsActionState(IActionStates.FLOCKME)) {
                            User u =UserManager.mgr.getUserByName(sObj.usr);
                            if (u!=null)
                                u.setActlock(false);
                        }
                        if (sObj.equalsActionState(IActionStates.IGNOREUSER)) {
                            User u =UserManager.mgr.getUserByName(sObj.usr);
                            if (u!=null)
                                u.respectUser(sObj.cu);
                        }
                        sObj.clearObject();
					} else if (sObj.time < lVal)
						lVal = sObj.time;
				}
				long slpTime = lVal - now;
				if (slpTime < 30)
					slpTime = 30;
				Thread.sleep(slpTime);
			} catch (Exception ie) {
			}
		}
	}

	private void startThreads() {
		try {
			UserManager.startUserManager();
			RequestReader.startRequestReader(true);
			RequestReader.startRequestReader(true);
			CentralSelector.startCentralSelector();
			Responder.startResponder();
			Listener.startListener();
			LogCleaner.startLogCleaner();
		} catch (Exception e) {
			Server.debug(this, "Exception during starting threads:", e, MSG_ERROR, LVL_HALT);
		}
		
 	    try {
            if (Server.srv.threadManager != null) {
                Server.log(this, "---- loading ThreadManager ----", Server.MSG_CONFIG, Server.LVL_MAJOR);
                IThreadManagerPlugin[] thm = Server.srv.threadManager;
                if (thm != null) {
                    for (int s = 0; s < thm.length; s++) {                       
                        try {
                            thm[s].startManager();
                            
                        } catch (Exception e) {
                            Server.debug(thm[s],"catched exception from ThreadManager Plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);
                        }                        
                    }
                }
           }      

       } catch (Exception e) {
           Server.debug(this, "Exception during starting Threads: ", e, MSG_ERROR, LVL_MAJOR);
       }

	    try {
        	TrafficMonitor.startTrafficMonitor();

 		}catch (Exception e) {
            Server.debug(this, "Exception during starting TrafficMonitor: ", e, MSG_ERROR, LVL_MAJOR);
        }
        try {
            XmlRpcManager.startManager();
        } catch (Exception e) {
            Server.debug(this, "Exception during starting XmlRpcManager (Server will not be reachable via XML-RPC):", e, MSG_ERROR, LVL_MAJOR);
        }
	}

	public void readConfig() {
		Server.log(this, "FreeCS Startup", MSG_CONFIG, LVL_MINOR);
		StringBuffer sb = new StringBuffer(BASE_PATH).append("/config");
		File cFile = new File(sb.toString());
		if (!cFile.exists()) {
			Server.log(this, "config directory missing\r\n" + BASE_PATH + "/config", MSG_ERROR, LVL_HALT);
		}
		sb = new StringBuffer(BASE_PATH).append("/config/config.cfg");
		cFile = new File(sb.toString());
		if (!cFile.exists()) {
			Server.log(this, "config file missing\r\n" + sb.toString(), MSG_ERROR, LVL_HALT);
		}
	
		try {
			FileInputStream in = new FileInputStream(cFile);
			props.load(in);
			in.close();
		} catch (FileNotFoundException fnfe) {
			// never
		} catch (IOException ioe) {
			Server.log(this, "unable to read config-files", MSG_ERROR, LVL_HALT);
		}
	
		// check for port-specification
		if (props.getProperty("port") == null)
			Server.log(this, "No port specified in config: port=[portnumber]", MSG_ERROR, LVL_HALT);
	
		checkForConfigValues();
		configFile = cFile;
		lastModified = cFile.lastModified();
		FileMonitor.getFileMonitor().addReloadable(srv);
	}

	/**
	 * checks for properties in the config-file to overrule the default-values
	 * this has to be "reloadable" meaning that values may be changed on the fly
	 * @see freecs.util.FileMonitor
	 * @see freecs.interfaces.IReloadable
	 */
	private synchronized void checkForConfigValues() {
        String sgroups = props.getProperty("startgroups");
        if (sgroups == null)
            Server.log(this, "No starting-rooms are deffined: startrooms=[room1/TITLE1 [, room2/TITLE2, ...", MSG_ERROR, LVL_HALT);

        GroupManager.mgr.updateStartingGroups(sgroups.split(","));

		Server.log (this, "updating log-destinations", MSG_CONFIG, LVL_MINOR);
		LOGFILE[MSG_CONFIG]       = checkProperty("logfileCfg",       "console");
		LOGFILE[MSG_AUTH]         = checkProperty("logfileAuth",      "console");
		LOGFILE[MSG_STATE]        = checkProperty("logfileState",     "console");
		LOGFILE[MSG_TRAFFIC]      = checkProperty("logfileTraffic",   "console");
		LOGFILE[MSG_ERROR]        = checkProperty("logfileError",     "console");
		LOGFILE[MSG_MESSAGE]      = checkProperty("logfileMessage",   "console");
		if (LOGFILE[MSG_MESSAGE]!= null && !LOGFILE[MSG_MESSAGE].equals("console")){
		    File f = new File(LOGFILE[MSG_MESSAGE]);
		    if (!f.exists()){
			    f.mkdirs();
		    }
		}
		LOG_QUEUE_SIZE            = checkProperty("logQueueSize",     500);

		Server.log(this, "Reading config...", MSG_CONFIG, LVL_MINOR);
		READBUFFER_SIZE 			= checkProperty("readbuffer", 			640);
		READER_MAX_IDLETIME 		= checkProperty("threadMaxIdletime", 	30000);
		READER_MAX_QUEUE 			= checkProperty("ioQueueSize", 			5);
		FLOOD_PROTECT_TOLERANC 		= checkProperty("floodProtectTolerance",3);
		FLOOD_PROTECT_MILLIS 		= checkProperty("floodProtectMillis", 	500);
		FLOOD_BAN_DURATION 			= checkProperty("floodBanDuration", 	30000);
		TOOL_PROTECT_TOLERANC 		= checkProperty("toolProtectTolerance", 250);
		TOOL_PROTECT_COUNTER        = checkProperty("toolProtectCounter", 	10);
		TOOL_BAN_DURATION 			= checkProperty("toolBanDuration", 	    600) * 60000;
		TOOL_PROTECT_MINMILLS 		= checkProperty("toolProtectMinmills", 12000);
		TOOL_PROTECT_MINCOUNTER     = checkProperty("toolProtectMincounter",10);
		JOIN_PUNISHED_COUNTER       = checkProperty("joinpunishedcounter",2);
		COLOR_CHANGE_INTERVAL 		= checkProperty("colorChangeInterval", 	15000);
	    MESSAGE_FLOOD_INTERVAL      = checkProperty("messageFloodInterval",  5000);
		USER_TIMEOUT 				= checkProperty("userTimeout", 			15) * 60000;
		if (USER_TIMEOUT < 0) {
            USER_TIMEOUT = 15 * 60000;
            Server.log(this, "WARNING Usrtimeout < 0 setting Standarttimeout",
                    Server.MSG_CONFIG, Server.LVL_MAJOR);
        }
		USER_AWAY_TIMEOUT 			= checkProperty("userAwayTimeout", 		30) * 60000;
		if (USER_AWAY_TIMEOUT < 0){
            Server.log(this, "WARNING UsrAwayTimeout < 0 deactivated",
                    Server.MSG_CONFIG, Server.LVL_MAJOR);

		}
		USER_REMOVE_SCHEDULE_TIME 	= checkProperty("userRemoveDelay", 		2000);
		TCP_RECEIVE_BUFFER_WINDOW 	= checkProperty("tcpReceiveBuffer", 	4096);
		FILE_CHECK_INTERVAL         = checkProperty("fileCheckInterval",    10000);
        if (FILE_CHECK_INTERVAL < 1000)
            FILE_CHECK_INTERVAL = 1000;
        CAN_DEL_LOGS                = checkProperty("canDelLogs",    false);
        LOGFILE_DELHOUR             = checkProperty("logfileDelhour",    1);
        LOGFILE_DELDAYS             = checkProperty("logfileDeldays",    2);
        if (LOGFILE_DELDAYS <2)
        	LOGFILE_DELDAYS =2;
		
        String ndcs                 = checkProperty("charset",              "iso-8859-1");
        if (!ndcs.equals(DEFAULT_CHARSET)) {
            defaultCs = Charset.forName(ndcs);
            defaultCsEnc = defaultCs.newEncoder();
            DEFAULT_CHARSET=ndcs;
        }
        
        int cookied = 0;
        String cookiedomain = props.getProperty("cookieDomain");
        if (cookiedomain != null) {
            StringBuffer cookie = new StringBuffer();
            String cd[] = cookiedomain.split(",");
            for (int i = 0; i < cd.length; i++) {
            	cookied ++;
                cookie.append(cd[i].trim().toLowerCase()).append(",");
                Server.log ("[Server]", "CookieDomain Configured: "+ cd[i].trim().toLowerCase(), Server.MSG_CONFIG, Server.LVL_MAJOR);
            }
            COOKIE_DOMAIN = new StringBuffer(cookie);
        }
        String servername = props.getProperty("server");
        if (servername != null) {
        	int servern = 0;
            Vector<String> server = new Vector<String>();
            String sv[] = servername.split(",");
            for (int i = 0; i < sv.length; i++) {
            	servern ++;
                server.addElement(sv[i].trim().toLowerCase());
                Server.log ("[Server]", "Server Configured: "+ sv[i].trim().toLowerCase(), Server.MSG_CONFIG, Server.LVL_MAJOR);
            }
            SERVER_NAME = new Vector<String>(server);
            if (servern >1)
                if (cookied < servern || cookied > servern)
                    Server.log (this, "Server Halt:CookieDomain(" +cookied+")<>Servername("+servern+")", Server.MSG_ERROR ,Server.LVL_HALT);
        }
	    DEFAULT_MEMBERSHIP           = checkProperty("defaultMembership","standart");
		USE_SMILEY                  = checkProperty("useSmiley",           false); 
		SMILEY_PER_LINE             = checkProperty("SmileyPerLine",           5); 
		SMILEY_SERVER               = checkProperty("SmileysDir", 	   "/static"); 
	    BLOCKED_NICK_AUTOHARDKICK   = checkProperty("blockedNickAutohardkick", false); 
	    USE_PLUGINS                 = checkProperty("usePlugins",          false); 		
	    USE_BBC                     = checkProperty("useBBC",              false); 
		BBC_CONVERT_GROUPNAME       = checkProperty("bbcConvertGroupname", false);
		BBC_CONVERT_GROUPTHEME      = checkProperty("bbcConvertGrouptheme",false);
		MAX_BBCTAGS                 = checkProperty("maxBBCTags", 			   1);
		if (MAX_BBCTAGS<1 || MAX_BBCTAGS>5)
			MAX_BBCTAGS =1;
		MIN_BBC_FONT_RIGHT_ENTRACE   = checkProperty("minBbcFontRightEntrace","user");
		MIN_BBC_FONT_RIGHT_SEPA      = checkProperty("minBbcFontRightSepa",   "user");
		MIN_BBC_B_RIGHT_ENTRACE      = checkProperty("minBbcBRightEntrace",   "user");
		MIN_BBC_B_RIGHT_SEPA         = checkProperty("minBbcBRightSepa",      "user");
		MIN_BBC_I_RIGHT_ENTRACE      = checkProperty("minBbcIRightEntrace",   "user");
		MIN_BBC_I_RIGHT_SEPA         = checkProperty("minBbcIRightSepa",      "user");
		MIN_BBC_U_RIGHT_ENTRACE      = checkProperty("minBbcURightEntrace",   "user");
		MIN_BBC_U_RIGHT_SEPA         = checkProperty("minBbcURightSepa",      "user");

		ALLOW_EXTERNAL 				= checkProperty("allowExternalLogin", 	true);
        USE_CENTRAL_REQUESTQUEUE    = checkProperty("useCentralRequestqueue",false);
		DEBUG_TEMPLATESET 			= checkProperty("debugTemplateset", 	false);
        STRICT_HOST_BINDING         = checkProperty("useStrictHostBinding", true);
		MAX_READERS 				= checkProperty("maxThreads", 			100);
		USE_HTTP11 					= checkProperty("useHTTP1.1", 			true);
		MAX_USERS 					= checkProperty("maxUsers", 			2000);
		TOUCH_USER_DELAY 			= checkProperty("touchUserDelay", 		20000);
		MAX_BAN_DURATION 			= checkProperty("maxBanDuration", 		120);
		USE_IP_BAN 					= checkProperty("useIpBan", 			true);
		DEFAULT_BAN_DURATION 		= checkProperty("defaultBanDuration", 	10);
		INITIAL_RESPONSE_QUEUE 		= checkProperty("responseQueueSize", 	100);
        MAX_RESPONSE_QUEUE          = checkProperty("maxResponseQueueSize", 1000);
		MAX_REQUESTS_PER_PROXY_IP 	= checkProperty("maxRequestsPerProxy", 	20000);
		MAX_REQUESTS_PER_IP 		= checkProperty("maxRequestsPerIp", 	90);
		HOST_BAN_DURATION 			= checkProperty("floodHostBanDuration", 3600000);
		USE_TRAFFIC_MONITOR 		= checkProperty("useTrafficMonitor", 	false);
		MAX_DIE_NUMBER 				= checkProperty("maximumDieNumber", 	10);
		MAX_DIE_EYES 				= checkProperty("maximumDieEyes", 		20);
		MAX_SUUSERS_PER_STARTGROUP  = checkProperty("maxSuPerStartgroup", 	5);
		USE_TOKENSTORE				= checkProperty("useTokenedLogin",		false);
        MD5_PASSWORDS               = checkProperty("MD5EncodePasswords",   false);
        MAX_USERNAME_LENGTH         = checkProperty("maxUserNameLength",    30);
        MAX_GROUPNAME_LENGTH        = checkProperty("maxGroupNameLength",   -1);
        MAX_GROUPTHEME_LENGTH       = checkProperty("maxGroupThemeLength",  -1);
        MESSAGE_FLOOD_LENGHT        = checkProperty("messageFloodLength",   -1);
        USE_MESSAGE_RENDER_CACHE    = checkProperty("useMessageRenderCache",false);
        VIP_TIMEOUT                 = checkProperty("vipTimeout",           0) * 60000;
        VIP_AWAY_TIMEOUT            = checkProperty("vipAwayTimeout",       0) * 60000;
	    MAX_MCALL_KEY               = checkProperty("maxMcallKey",   30);
        MAX_PMSTORE                 = checkProperty("maxPmstore",           0);

        String httpUname            = checkProperty("admin.http.username",  null);
        if (httpUname!=null)
            ADMIN_HTTP_USERNAME         = httpUname.split(",");      
        String httpPassword         = checkProperty("admin.http.password",  null);
        if (httpPassword!=null)
            ADMIN_HTTP_PASSWORD         = httpPassword.split(",");
        String sLevel               = checkProperty("admin.http.securitylevel",  null);
        if (sLevel!=null)
            ADMIN_HTTP_SECLEVEL         = sLevel.split(",");
        ADMIN_HTTP_ALLOWED			= checkProperty("admin.http.allowedClients", "");
        ADMIN_XMLRPC_PORT           = checkProperty("admin.xmlrpc.port",    0);
        ADMIN_XMLRPC_ALLOWED        = checkProperty("admin.xmlrpc.allowedClients", "");
        
		UNAME_PREFIX_GOD 				= checkProperty("prefix.admin",		"<b>");
		UNAME_SUFFIX_GOD 				= checkProperty("suffix.admin",		"(A)</b>");
		UNAME_PREFIX_GUEST 				= checkProperty("prefix.guest",		"");
		UNAME_SUFFIX_GUEST 				= checkProperty("suffix.guest",		"(G)");
		UNAME_PREFIX_MODERATOR 			= checkProperty("prefix.moderator",	"");
		UNAME_SUFFIX_MODERATOR 			= checkProperty("suffix.moderator",	"(M)");
		UNAME_PREFIX_PUNISHED 			= checkProperty("prefix.punished",	"<s>");
		UNAME_SUFFIX_PUNISHED 			= checkProperty("suffix.punished",	"</s>");
		UNAME_PREFIX_SU 				= checkProperty("prefix.su", 		"<i>");
		UNAME_SUFFIX_SU 				= checkProperty("suffix.su", 		"</i>");
		UNAME_PREFIX_VIP 				= checkProperty("prefix.vip", 		"<b>");
		UNAME_SUFFIX_VIP 				= checkProperty("suffix.vip", 		"</b>");
        
        READER_TIMEOUT                  = checkProperty("readerTimeout",     5000);
        LOGIN_TIMEOUT                   = checkProperty("loginTimeout",     20000);
        
        FN_DEFAULT_MODE_FALSE           = (short) 
                            checkProperty("friendNotificationMode.false",   0);
        FN_DEFAULT_MODE_TRUE            = (short) 
                            checkProperty("friendNotificationMode.true",    2);
        COLOR_LOCK_MODE                 = (short) 
                            checkProperty("colorLockMode",                  0);
        COLOR_LOCK_LEVEL                 = (short) 
                            checkProperty("colorLockLevel",                   1);
        if (COLOR_LOCK_LEVEL <1 || COLOR_LOCK_LEVEL>10)
            COLOR_LOCK_LEVEL = 1;
        
        FADECOLOR_LOCK_LEVEL                 = (short) 
                             checkProperty("fadecolorLockLevel",                   -1);
        if (FADECOLOR_LOCK_LEVEL == -1)
            FADECOLOR_LOCK_LEVEL = COLOR_LOCK_LEVEL;
        if (FADECOLOR_LOCK_LEVEL <1 || FADECOLOR_LOCK_LEVEL>10)
            FADECOLOR_LOCK_LEVEL = 1;
        
        KEEP_ALIVE_TIMEOUT              = checkProperty("keepAliveTimeout", 30) * 1000;
		PUNISH_DURATION 			    = checkProperty("punishBanDuration", -1);
	    ALLOW_CHANGE_USERAGENT          = checkProperty("allowChangeUseragent",true);       
		MAX_FLOCK_DURATION              = checkProperty("maxFlockDuration",   -1);
		MAX_SU_BAN_DURATION             = checkProperty("maxSuBanDuration",   -1);
	    DEFAULT_TEMPLATESET             = checkProperty("defaultTemplateset",null);
        MOBILE_BROWSER_REGEX            = checkProperty("mobileBrwoserRegex", "android|avantgo|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino");
	    REDIRECT_MOBILE_BROWSER         = checkProperty("redirectMobileBrowser",false);

        TRACE_CREATE_AND_FINALIZE   = checkProperty("traceCreateAndFinalize",false);
        
        //Load Plugins
        if (USE_PLUGINS){
            String plugins = props.getProperty("plugins");
            if (plugins != null) {
                String values[] = plugins.split(",");
                Vector<String> pluginUrl = new Vector<String>();
                for (int i = 0; i < values.length; i++)
                    pluginUrl.add(values[i].trim());
                loadPlugin(pluginUrl);
            } else {
                resetPluginStore();
            }
        } else {
            resetPluginStore();
        }
        //Load Commands
        String url = props.getProperty("commandsUrl");
        if (url != null) {
            String values[] = url.split(",");
            Vector<String> jarUrl = new Vector<String>();
            for (int i = 0; i < values.length; i++)
                jarUrl.add(values[i].trim());
                      
            loadCommands(jarUrl);
            CommandSet.getCommandSet().checkCommendSet();
        } else {
            Vector<String> jarUrl = new Vector<String>();
            jarUrl.add("lib/freecs.jar");
            loadCommands(jarUrl);
        }
        //Load xmlrpc Handler
        String xmlRpcHandlerUrl = props.getProperty("xmlRpcHandlerUrl");
        if (xmlRpcHandlerUrl != null) {
            String values[] = xmlRpcHandlerUrl.split(",");
            Vector<String> jarUrl = new Vector<String>();
            for (int i = 0; i < values.length; i++)
                jarUrl.add(values[i].trim());
            loadXmlRpcHandler(jarUrl);    
        }

		String val = props.getProperty("moderatedgroups");
		if (val != null) {
			String values[] = val.split(",");
			Vector<String> names = new Vector<String>();
			for (int i = 0; i < values.length; i++)
				names.add(values[i].trim().toLowerCase());
			GroupManager.mgr.updateModeratedGroups(names);
		} else {
			GroupManager.mgr.updateModeratedGroups(new Vector<String>());
		}

		val = props.getProperty("vips");
		if (val != null) {
			Vector<String> tvl = new Vector<String>();
			String values[] = val.split(",");
			for (int i = 0; i < values.length; i++) {
				tvl.addElement(values[i].trim().toLowerCase());
			}
			UserManager.mgr.updateVips(tvl);
		} else
			UserManager.mgr.updateVips(new Vector<String>());

        val = props.getProperty("admins");
        if (val != null) {
            Vector<String> tvl = new Vector<String>();
            String values[] = val.split(",");
            for (int i = 0; i < values.length; i++) {
                tvl.addElement(values[i].trim().toLowerCase());
            }
            UserManager.mgr.updateAdmins(tvl);
        } else
            UserManager.mgr.updateAdmins(new Vector<String>());

		val = props.getProperty("moderators");
		if (val != null) {
			Vector<String> tvl = new Vector<String>();
			String values[] = val.split(",");
			for (int i = 0; i < values.length; i++) {
				tvl.addElement(values[i].trim().toLowerCase());
			}
			UserManager.mgr.updateModerators(tvl);
		} else
			UserManager.mgr.updateModerators(new Vector<String>());
	
		val = props.getProperty("guests");
				if (val != null) {
					Vector<String> tvl = new Vector<String>();
					String values[] = val.split(",");
					for (int i = 0; i < values.length; i++) {
						tvl.addElement(values[i].trim().toLowerCase());
					}
					UserManager.mgr.updateGuests(tvl);
				} else
					UserManager.mgr.updateGuests(new Vector<String>());
	
		val = srv.ADMIN_HTTP_ALLOWED;
		if (val != null) {
			String hsts[] = val.split(",");
			Vector<InetAddress> newHsts = new Vector<InetAddress>();
			for (int i = 0; i < hsts.length; i++) {
				try {
					InetAddress ia = InetAddress.getByName(hsts[i].trim());
					if (!newHsts.contains(ia))
						newHsts.addElement(ia);
				} catch (Exception e) {
					StringBuffer tsb = new StringBuffer("Server.checkForConfigValues: unable to add adminHost ");
					tsb.append(hsts[i]);
					Server.debug(this, tsb.toString(), e, Server.MSG_ERROR, Server.LVL_MAJOR);
				}
			}
			Vector<InetAddress> remove = (Vector) adminHosts.clone();
			remove.removeAll(newHsts);
			newHsts.removeAll(adminHosts);
			adminHosts.removeAll(remove);
			adminHosts.addAll(newHsts);
			for (Enumeration<String> tp = tempAdmins.elements(); tp.hasMoreElements(); ) {
	            String u = (String) tp.nextElement();
	            User nu = UserManager.mgr.getUserByName(u);
	            if (nu != null)
	                addTempAdminhost(nu);	            
	        }
		}
		val = props.getProperty("permaBannedIp");
		if (val != null) {
			String hsts[] = val.split(",");
			Vector<InetAddress> newHsts = new Vector<InetAddress>();
			for (int i = 0; i < hsts.length; i++) {
				try {
					InetAddress ia = InetAddress.getByName(hsts[i].trim());
					if (!newHsts.contains(ia)){
						newHsts.addElement(ia);
					    permaBanHost (ia, "perma banned IP");

					}
				} catch (Exception e) {
					StringBuffer tsb = new StringBuffer("Server.checkForConfigValues: unable to add perma banned IP ");
					tsb.append(hsts[i]);
					Server.debug(this, tsb.toString(), e, Server.MSG_ERROR, Server.LVL_MAJOR);
				}
			}		
		}
		
		String oldTimeZone = TIMEZONE;
		TIMEZONE = checkProperty("timezone", null);
		TimeZone tz = null;
		if (TIMEZONE == null || TIMEZONE.length() < 1) {
			StringBuffer sb = new StringBuffer("checkForConfigValues: setting TimeZone to default-TimeZone (");
			tz = TimeZone.getDefault();
			sb.append(tz.getID());
			sb.append(")");
			Server.log(this, sb.toString(), Server.MSG_STATE, Server.LVL_MINOR);
			if (tz.equals(cal.getTimeZone()))
				tz = null;
		} else if (oldTimeZone == null || !oldTimeZone.equals(TIMEZONE)) {
			try {
				tz = TimeZone.getTimeZone(TIMEZONE);
				if (tz.equals(cal.getTimeZone())) {
					Server.log(this, "checkForConfigVals: TimeZone has not changed", Server.MSG_ERROR, Server.LVL_MINOR);
					tz = null;
				} else if (!tz.getID().equals(TIMEZONE)) {
					StringBuffer sb = new StringBuffer("checkForConfigVals: TimeZone is set to ");
					sb.append(tz.getID());
					sb.append(" now. The following TimeZones are available:\r\n");
					String[] ids = TimeZone.getAvailableIDs();
					for (int i = 0; i < ids.length; i++) {
						sb.append(ids[i]);
						if (i < ids.length)
							sb.append(", ");
					}
					Server.log(this, sb.toString(), Server.MSG_STATE, Server.LVL_MINOR);
				}
				StringBuffer sb = new StringBuffer("checkForConfigValues: setting TimeZone to ");
				sb.append(tz.getID());
				Server.log(this, sb.toString(), Server.MSG_STATE, Server.LVL_MINOR);
			} catch (Exception e) {
				Server.debug(this, "checkForConfigValues: unable to set TimeZone!", e, Server.MSG_ERROR, Server.LVL_MAJOR);
			}
		}
		if (tz != null) {
			cal.setTimeZone(tz);
		}
	
        Listener.updateSscRecieveBuffer(this.TCP_RECEIVE_BUFFER_WINDOW);

        val = checkProperty("allowedLoginHosts", null);
        if (val != null) {
            String hsts[] = val.split(",");
            Vector<InetAddress> newHsts = new Vector<InetAddress>();
            for (int i = 0; i < hsts.length; i++) {
                try {
                    InetAddress ia = InetAddress.getByName(hsts[i].trim());
                    if (!newHsts.contains(ia))
                    	newHsts.addElement(ia);
                } catch (Exception e) {
                    StringBuffer tsb = new StringBuffer("Server.checkForConfigValues: unable to add adminHost ");
                    tsb.append(hsts[i]);
                    Server.debug(this, tsb.toString(), e, Server.MSG_ERROR, Server.LVL_MAJOR);
                }
            }
            Vector<InetAddress> remove = (Vector) allowedLoginHosts.clone();
            remove.removeAll(newHsts);
            newHsts.removeAll(allowedLoginHosts);
            allowedLoginHosts.removeAll(remove);
            allowedLoginHosts.addAll(newHsts);
        }

		// set logmask and debug-switch
		String logVal = props.getProperty("debug");
		if (logVal != null && logVal.equalsIgnoreCase("false"))
			DEBUG = false;
		else if (logVal != null && logVal.equalsIgnoreCase("true"))
			DEBUG = true;
	
		logVal = props.getProperty("log_config");
		if (logVal != null)
			LOG_MASK[MSG_CONFIG] = new Short(Short.parseShort(logVal, 10));
		logVal = props.getProperty("log_auth");
		if (logVal != null)
			LOG_MASK[MSG_AUTH] = new Short(Short.parseShort(logVal, 10));
		logVal = props.getProperty("log_state");
		if (logVal != null)
			LOG_MASK[MSG_STATE] = new Short(Short.parseShort(logVal, 10));
		logVal = props.getProperty("log_traffic");
		if (logVal != null)
			LOG_MASK[MSG_TRAFFIC] = new Short(Short.parseShort(logVal, 10));
		logVal = props.getProperty("log_error");
		if (logVal != null)
			LOG_MASK[MSG_ERROR] = new Short(Short.parseShort(logVal, 10));
		logVal = props.getProperty("log_message");
		if (logVal != null)
			LOG_MASK[MSG_MESSAGE] = new Short(Short.parseShort(logVal, 10));
		logVal = props.getProperty("log_sepamessage");
		if (logVal != null)
			LOG_MASK[MSG_SEPAMESSAGE] = new Short(Short.parseShort(logVal, 10));
	}
	
	/**********************************************************************************************
	 * PLUGIN-METHODS (used for Commands,XmlRpc Handler and ServerPlugin)
	 **********************************************************************************************/
	private void loadCommands(Vector<String> jarUrl){
	    allCommands = new HashMap<String, Object>();
	    Vector<String> commandUrl = new Vector<String>();
	    HashMap<String, Object> commandsStore = new HashMap<String, Object>();
	  
        Enumeration<JarEntry> entries = null;
        for (Iterator<String> iterator = jarUrl.iterator(); iterator.hasNext();) {
            String jUrl = (String) iterator.next();
            try {
                entries = new JarFile(jUrl).entries();
            } catch (IOException e) {
                Server.log(this, "Jar File:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
            }  
            String packagePattern = "freecs/commands/[^/]+\\.class";  
            if (entries == null)
                Server.log(this, "illegal jar File", Server.MSG_ERROR, Server.LVL_HALT);
            
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().matches(packagePattern)){ //list only the classes in the com.google.inject Package 
                    StringBuilder url = new StringBuilder(jarEntry.getName());
                    int i = url.toString().indexOf(".");
                    url = new StringBuilder(url.substring(0, i).toString().replaceAll("/", "."));
                   // url = new StringBuilder(url.toString().replaceAll("/", "."));
                    if (url.toString().equals("freecs.commands.AbstractCommand")
                            || url.toString().equals("freecs.commands.CommandSet"))
                        continue;
                    commandUrl.add(url.toString());
                   
                    Class<?> piClass = null;
                    try {
                        piClass = Class.forName(url.toString());
                    } catch (ClassNotFoundException e) {
                        Server.log(this, "Class.forName:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                    }   
                    if (piClass == null)
                        continue;

                    Method getInstance = null;
                    try {
                        getInstance = piClass.getMethod("getInstance");
                    } catch (SecurityException e) {
                        Server.log(this, "get Instance:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                    } catch (NoSuchMethodException e) {
                        Server.log(this, "get Instance:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                    }
               
                    if (getInstance==null){
                        Server.log(this, "Specified command-object doesn't implement static getInstance", Server.MSG_ERROR, Server.LVL_MAJOR);
                        continue;
                    }
                } else {
                    Server.log(this, "ignore Command Url "+jarEntry.getName(), Server.MSG_CONFIG, Server.LVL_VERY_VERBOSE);           
                }
            }
        }
                
        for (Iterator<String> iterator = commandUrl.iterator(); iterator.hasNext();) {
            StringBuilder url = new StringBuilder((String) iterator.next());
            if (url.toString().length()<1)
                continue;
            Object o = null;;
            synchronized (commandsStore) {
                o = commandsStore.get(url.toString());
                if (o == null) {
                    try {                 
                        Class<?> piClass = null;
                        try {
                            piClass = Class.forName(url.toString());
                        } catch (ClassNotFoundException e) {
                            Server.log(this, "Class.forName:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                        }   
                        
                        Method getInstance = piClass.getMethod("getInstance");
                       
                        if (getInstance==null){
                            Server.log(this, "Specified Command-object doesn't implement static getMasterInstance", Server.MSG_ERROR, Server.LVL_MAJOR);                        
                            continue;
                        }
                      
                        Object arg0 = null;
                        o = getInstance.invoke(arg0);
                        if (!(o instanceof ICommand)){
                            Server.log(this, "Specified Command-object ("+url.toString()+") doesn't implement interface ICommand", Server.MSG_ERROR, Server.LVL_MAJOR);
                            continue;
                        }
                       
                        commandsStore.put(url.toString(), o);
 
                    } catch (Exception e) {
                        Server.log (this, "invalid url for Command: ("+e+") Url:" + url.toString(), Server.MSG_ERROR, Server.LVL_MINOR);
                        continue;
                    }
                }
            }
            String cmd = ((ICommand) o).getCmd();
            String version = ((ICommand) o).getVersion();
            
            if (!version.startsWith("1.")){
                Server.log(this, "invalid commandversion "+cmd+" ("+url.toString()+")", Server.MSG_ERROR, Server.LVL_MAJOR);           
                continue;
            }
            
            if (!allCommands.containsKey(cmd)){
                allCommands.put(((ICommand) o).getCmd(), ((ICommand) o).instanceForSystem());
                if (Server.DEBUG)
                    Server.log(this, "added Command "+cmd+"[ "+version+" ] ("+url.toString(), Server.MSG_CONFIG, Server.LVL_MAJOR);           
            } else {
                Server.log(this, "Command "+cmd+" exists!", Server.MSG_CONFIG, Server.LVL_MAJOR);
                if (Server.DEBUG)
                    Server.log(this, "ignore Command "+cmd+"[ "+version+" ] ("+url, Server.MSG_CONFIG, Server.LVL_MAJOR);
            }
        }
        commandUrl =null;
        commandsStore = null;
        entries = null;
	}
	
    private void loadXmlRpcHandler(Vector<String> jarUrl){
        xmlRpcHandler = new HashMap<String, Object>();
        Vector<String> xmlRpcUrl = new Vector<String>();
        HashMap<String, Object> handlerStore = new HashMap<String, Object>();     
        Enumeration<JarEntry> entries = null;
        
        for (Iterator<String> iterator = jarUrl.iterator(); iterator.hasNext();) {
            String jUrl = (String) iterator.next();
            try {
                entries = new JarFile(jUrl).entries();
            } catch (IOException e) {
                Server.log(this, "Jar File:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
            }  
            String packagePattern = "freecs/external/xmlrpc/[^/]+\\.class";  
            if (entries == null)
                Server.log(this, "illegal jar File", Server.MSG_ERROR, Server.LVL_HALT);
            
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().matches(packagePattern)){ //list only the classes in the freecs.external.xmlrpc Package 
                    StringBuilder url = new StringBuilder(jarEntry.getName());
                    int i = url.toString().indexOf(".");
                    url = new StringBuilder(url.substring(0, i).toString().replaceAll("/", "."));
                    if (url.toString().equals("freecs.external.xmlrpc.XmlRpcSendUser")
                            || url.toString().equals("freecs.external.xmlrpc.XmlRpcManager")
                            || url.toString().equals("freecs.external.xmlrpc.XmlRpcSendUser$UserState"))
                        continue;                   
                    xmlRpcUrl.add(url.toString());                    
                    Class<?> piClass = null;
                    try {
                        piClass = Class.forName(url.toString());
                    } catch (ClassNotFoundException e) {
                        Server.log(this, "Class.forName:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                    }   
                    
                    if (piClass == null)
                        continue;
                    Method getInstance = null;
                    try {
                        getInstance = piClass.getMethod("getInstance");
                    } catch (SecurityException e) {
                        Server.log(this, "get Instance:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                    } catch (NoSuchMethodException e) {
                        Server.log(this, "get Instance:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                    }
               
                    if (getInstance==null){
                        Server.log(this, "Specified xml rpc handler-object ("+url.toString()+") doesn't implement static getInstance", Server.MSG_ERROR, Server.LVL_MAJOR);
                        continue;
                    }
                } else {
                    Server.log(this, "ignore XmlRpcHandler Url "+jarEntry.getName(), Server.MSG_CONFIG, Server.LVL_VERY_VERBOSE);           

                }
            }
        }
                
        for (Iterator<String> iterator = xmlRpcUrl.iterator(); iterator.hasNext();) {
            StringBuilder url = new StringBuilder((String) iterator.next());

            if (url.toString().length()<1)
                continue;
            Object o = null;;
            synchronized (handlerStore) {
                o = handlerStore.get(url.toString());
                if (o == null) {
                    try {
                        Class<?> piClass = Class.forName(url.toString());
                        Method getInstance = piClass.getMethod("getInstance");
                       
                        if (getInstance==null){
                            Server.log(this, "Specified Handler-object doesn't implement static getMasterInstance", Server.MSG_ERROR, Server.LVL_MAJOR);                        
                            continue;
                        }
                      
                        Object arg0 = null;
                        o = getInstance.invoke(arg0);
                        if (!(o instanceof IXmlRpcHandler)){
                            Server.log(this, "Specified Handler-object doesn't implement interface IXmlRpcHandler", Server.MSG_ERROR, Server.LVL_MAJOR);
                            continue;
                        }
                       
                        handlerStore.put(url.toString(), o);
 
                    } catch (Exception e) {
                        Server.log (this, "invalid url for Handler: ("+e+") Url:" + url, Server.MSG_ERROR, Server.LVL_MINOR);
                        continue;
                    }
                }
            }
            String handler = ((IXmlRpcHandler) o).getHandlername();
            String version = ((IXmlRpcHandler) o).getVersion();
            
            if (!version.startsWith("1.")){
                Server.log(this, "invalid xmlrpchandlerversion "+handler+" ("+url+")", Server.MSG_ERROR, Server.LVL_MAJOR);           
                continue;
            }
            
            if (!xmlRpcHandler.containsKey(handler)){
                xmlRpcHandler.put(((IXmlRpcHandler) o).getHandlername(), ((IXmlRpcHandler) o).instanceForSystem());
                if (Server.DEBUG)
                    Server.log(this, "add XmlRpcHandler "+handler+"[ "+version+" ] ("+url+")", Server.MSG_CONFIG, Server.LVL_MAJOR);           
            } else {
                Server.log(this, "XmlRpcHandler "+handler+" exists!", Server.MSG_CONFIG, Server.LVL_MAJOR);
                if (Server.DEBUG)
                    Server.log(this, "ignore XmlRpcHandler "+handler+"[ "+version+" ] ("+url, Server.MSG_CONFIG, Server.LVL_MAJOR);
            }
        }
        xmlRpcUrl =null;
        handlerStore = null;
        entries = null;
    }

	private void resetPluginStore() {
	    pluginStore = new HashMap<String, Object>();
	    serverPlugin = null;
    }

    private void loadPlugin(Vector<String> pluginUrl) {
        Vector<IServerPlugin> plugins = new Vector<IServerPlugin>();

        for (Iterator<String> iterator = pluginUrl.iterator(); iterator.hasNext();) {
            StringBuilder url = new StringBuilder((String) iterator.next());
            if (url.toString().length()<1)
                continue;
            Object o;
            synchronized (pluginStore) {
                o = pluginStore.get(url.toString());
                if (o == null) {
                    try {
                        Class<?> piClass = null;
                        try {
                            piClass = Class.forName(url.toString());
                        } catch (ClassNotFoundException e) {
                            Server.log(this, "Class.forName:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                        }   
                        if (piClass == null)
                            continue;
                             
                        Method getInstance = piClass.getMethod("getMasterInstance");
                       
                        if (getInstance==null){
                            Server.log(this, "Specified plugin-object doesn't implement static getMasterInstance", Server.MSG_ERROR, Server.LVL_MAJOR);
                            continue;
                        }
                        Object arg0 = null;
                        o = getInstance.invoke(arg0);
                        if (!(o instanceof IServerPlugin)){
                            Server.log(this, "Specified plugin-object doesn't implement interface IServerPlugin", Server.MSG_ERROR, Server.LVL_MAJOR);
                            continue;
                        }
                        pluginStore.put(url.toString(), o);
                    } catch (Exception e) {
                        Server.log (this, "invalid url for Plugin: ("+e+") Url:" + url.toString(), Server.MSG_ERROR, Server.LVL_MINOR);
                        continue;
                    }
                }
            }
            try {
                plugins.add(((IServerPlugin) o).instanceForSystem());
            } catch (Exception e) {
                Server.debug(this, "catched exception while getting ServerPlugin-instance", e, Server.MSG_STATE, Server.LVL_MAJOR);
            }
            serverPlugin = plugins.toArray(new IServerPlugin[0]);
        }       
        plugins = null;
    }
    
    /**
     * 
     * @param jarUrl
     */
    
    private void loadThreadManager(Vector<String> jarUrl){
        Vector<IThreadManagerPlugin> plugins = new Vector<IThreadManagerPlugin>();
        StringBuffer path = new StringBuffer(BASE_PATH);
        path.append("lib/threadmanager.jar");
        jarUrl.add(path.toString());
        
        Vector<String> threadManagerUrl = new Vector<String>();
        HashMap<String, Object> threadManagerStore = new HashMap<String, Object>();     
        Enumeration<JarEntry> entries = null;
        boolean foundEntry =false;
        for (Iterator<String> iterator = jarUrl.iterator(); iterator.hasNext();) {
            String jUrl = (String) iterator.next();
            File f = new File(jUrl);
            if (!f.exists()){
                if (DEBUG)
                    Server.log(this, "File "+jUrl+" not found", Server.MSG_CONFIG, Server.LVL_MAJOR);
                continue;
            }

            try {
                entries = new JarFile(jUrl).entries();
            } catch (IOException e) {
                Server.log(this, "Jar File:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
            }  
            String packagePattern = "freecs/threadmanager/[^/]+\\.class";  
            if (entries == null)
                Server.log(this, "illegal jar File", Server.MSG_ERROR, Server.LVL_HALT);
            
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().matches(packagePattern)){ //list only the classes in the freecs.external.threadmanager Package 
                    foundEntry=true;
                    StringBuilder url = new StringBuilder(jarEntry.getName());
                    int i = url.toString().indexOf(".");
                    url = new StringBuilder(url.substring(0, i).toString().replaceAll("/", "."));
                   
                    if (url.toString().indexOf("$") >0)
                        continue;     
                                 
                    Class<?> piClass = null;
                    try {
                        piClass = Class.forName(url.toString());
                    } catch (ClassNotFoundException e) {
                        Server.log(this, "Class.forName:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                    }   
                    
                    if (piClass == null)
                        continue;
                    threadManagerUrl.add(url.toString());                    
                } else {
                    Server.log(this, "ignore thread manager Url "+jarEntry.getName(), Server.MSG_CONFIG, Server.LVL_VERY_VERBOSE);           

                }
            } 
        }
        if (foundEntry) {
            for (Iterator<String> iterator = threadManagerUrl.iterator(); iterator
                    .hasNext();) {
                StringBuilder url = new StringBuilder((String) iterator.next());

                if (url.toString().length() < 1)
                    continue;
                Object o = null;
                synchronized (threadManagerStore) {
                    o = threadManagerStore.get(url.toString());
                    if (o == null) {
                        try {
                            Class<?> piClass = Class.forName(url.toString());
                            Method getInstance = piClass
                                    .getMethod("getMasterInstance");

                            if (getInstance == null) {
                                Server.log(
                                        this,
                                        "Specified Handler-object doesn't implement static getMasterInstance",
                                        Server.MSG_ERROR, Server.LVL_MAJOR);
                                continue;
                            }

                            Object arg0 = null;
                            o = getInstance.invoke(arg0);
                            if (o == null) {
                                Server.log(this, "Specified object is NULL",
                                        Server.MSG_ERROR, Server.LVL_MAJOR);
                                continue;

                            }
                            if (!(o instanceof IThreadManagerPlugin)) {
                                Server.log(
                                        this,
                                        "Specified Handler-object doesn't implement interface IThreadManagerPlugin",
                                        Server.MSG_ERROR, Server.LVL_MAJOR);
                                continue;
                            }

                            threadManagerStore.put(url.toString(), o);
                            Server.log(this, "load Manager " + url,
                                    Server.MSG_CONFIG, Server.LVL_MAJOR);
                        } catch (Exception e) {
                            Server.log(this, "invalid url for Plugin: (" + e
                                    + ") Url:" + url, Server.MSG_ERROR,
                                    Server.LVL_MINOR);
                            continue;
                        }
                    }
                }
                try {
                    plugins.add(((IThreadManagerPlugin) o).instanceForSystem());
                } catch (Exception e) {
                    Server.debug(
                            this,
                            "catched exception while getting IThreadManagerPlugin-instance",
                            e, Server.MSG_STATE, Server.LVL_MAJOR);
                }
                threadManager = plugins.toArray(new IThreadManagerPlugin[0]);

            }
        } else {
        	if (DEBUG)
                Server.log(this, "path freecs/threadmanager/ exists?", Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        threadManagerUrl =null; threadManagerStore = null;entries = null;
    }

	/**
	 * util-method for checking boolean config-values
	 * @param p
	 * @param def
	 * @return
	 */
	private boolean checkProperty (String p, boolean def) {
		String pval = props.getProperty (p);
		if (pval == null) return def;
		return pval.equalsIgnoreCase("true");
	}
	
	/**
	 * util-method for checking long config-values
	 * @param p
	 * @param def
	 * @return
	 */
	private long checkProperty (String p, long def) {
		String pval = props.getProperty (p);
		if (pval == null) return def;
        try {
            return Long.parseLong (pval, 10);
        } catch (Exception e) {
            Server.debug("Server", "invalid value specified for configuration-parameter " + pval, e , Server.MSG_ERROR, Server.LVL_MAJOR);
            return def;
        }
	}
	
	/**
	 * util-method for checking int config-values
	 * @param p
	 * @param def
	 * @return
	 */
	private int checkProperty (String p, int def) {
		String pval = props.getProperty (p);
		if (pval == null) return def;
        try {
            return Integer.parseInt (pval.trim(), 10);
        } catch (Exception e) {
            Server.debug("Server", "invalid value specified for configuration-parameter " + pval, e , Server.MSG_ERROR, Server.LVL_MAJOR);
            return def;
        }
	}
	
	/**
	 * util-method for checking String config-values
	 * @param p
	 * @param def
	 * @return
	 */
	private String checkProperty (String p, String def) {
		String pval = props.getProperty (p);
		if (pval == null) return def;
		return pval;
	}

	public void initServer() {
		try {
			if (templatemanager == null)
				templatemanager = new TemplateManager();
		} catch (IOException ioe) {
			Server.debug(this, "unable to load TemplateSet: ", ioe, MSG_ERROR, LVL_HALT);
		}
		auth = AuthManager.instance;
		auth.init();
	
		Runtime rt = Runtime.getRuntime();
		// register the CleanupClass as shutdown-hook
		rt.addShutdownHook(new CleanupClass());
	}

	public void removeToken (String cookie) {
		if (cookie == null) 
			return;
		tokenStore.remove(cookie);
	}

	public boolean isTokenValid (String token, String cookie) {
		if (!USE_TOKENSTORE)
			return true;
		if (token == null || cookie == null)
			return false;
		String t = (String) tokenStore.get(cookie);
		if (t==null || !t.equals(token))
			return false;
		return true;
	}

	public void addToken (String token, String cookie) {
		if (!USE_TOKENSTORE || token==null || cookie==null)
			return;
		tokenStore.put(cookie, token);
	}


/**********************************************************************************************
 * QUERY-METHODS (used for retriefing values...)
 **********************************************************************************************/
	public void startShutdown() {
		isRunning = false;
	}
	public boolean isRunning() {
		return isRunning;
	}
	
    public boolean isAdminHost(InetAddress ia) {
		return adminHosts.contains(ia);
	}
    
    public void addTempAdminhost(User nu){    	
        InetAddress ia = nu.conn.clientAddress;		
    	if (ia != null && mayTempAdminhost(nu.conn.clientIp)){
    		if (!adminHosts.contains(ia)){
       	   		nu.setAsTempadminhost();
    			StringBuffer sb = new StringBuffer("[").append(nu.getName()).append("] ");
        		sb.append(nu.conn.clientIp).append(" add tempAdminhost");
    			Server.log(this,sb.toString() , MSG_STATE, LVL_MAJOR);
    			adminHosts.add(ia);
    			if (!tempAdmins.contains(nu.getName().toLowerCase()))
    		        tempAdmins.add(nu.getName().toLowerCase());
    		}
    	}
    }
    
    public void removeTempAdminhost(User nu){
        if (!tempAdmins.contains(nu.getName().toLowerCase()))
            return;
                
    	InetAddress ia = nu.conn.clientAddress;	
    	if (ia != null){
    		StringBuffer sb = new StringBuffer("[").append(nu.getName()).append("] ");
    		sb.append(nu.conn.clientIp).append(" remove tempAdminhost");
     		Server.log(this,sb.toString() , MSG_STATE, LVL_MAJOR);
    		adminHosts.remove(ia);
    		tempAdmins.remove(nu.getName().toLowerCase());
    	}
    }
    
    private boolean mayTempAdminhost(String ia) {
    	if (ia.startsWith("192.168.")
    			|| ia.startsWith("10.")
    			|| ia.startsWith("172.16")
    			|| ia.equals("127.0.0.1"))
    		return false;
    	return true;
    }
    
	public InetAddress getLocalHost() {
		return lh;
	}
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * returns http-url to this server
	 * @return the http-url to this server
	 */
	public String getUrl() {
		StringBuffer sb = new StringBuffer();
        sb.append(SERVER_NAME == null ? lh.getCanonicalHostName() : SERVER_NAME.firstElement());
		String port = props.getProperty("mappedPort");
        if (port==null)
            port = props.getProperty("port");
        if (!"80".equals(port)) {
    		sb.append(":");
    		sb.append(port);
        }
		return (sb.toString());
	}
	
	/**
	 * returns the version of this server
	 * @return version of server
	 */
	public static String getVersion() {
		return VERSION;
	}
/**********************************************************************************************
 * BAN-METHODS (for checking and banning users...
 **********************************************************************************************/
   /**
    * Used to ban a user from the server. Name and cookie are baned
    * If the ban-duration is reached, this ban will be removed by the main-thread
    * @param u the user object
    * @param msgTemplate the message template to use for this ban (eg. user.flood)
    * @param millis how long this user will be banned
    */
   public void banUser (User u, String msgTemplate, String message, long millis, String bannedBy) {
   	  if (u==null)
   	  	 return;
      MessageParser mp = new MessageParser ();
      mp.setSender (u);
      mp.setUsercontext(u);
      Group g = u.getGroup ();
      if (g != null) {
         mp.setMessageTemplate (msgTemplate);
         g.removeUser (u);
         g.sendMessage(mp);
      }
      banUser (u, message, millis, bannedBy);
   }

    /**
     * ban all users contained in given vector
     * @param v vector containing users
     * @param message reason for the kick
     * @param millis milliseconds this user will be banned
     * @param bannedBy name of the banner (may be Server in case of floodprotection
     */
    public void banUser (Vector<User> v, String message, long millis, String bannedBy) {
        for (Enumeration<User> e = v.elements(); e.hasMoreElements(); ) {
            User u = (User) e.nextElement();
            this.banUser (u, message, millis, bannedBy);
        }
    }
   
    public void banUser (User u, String message, long millis, String bannedBy) {
        if (u==null)
            return;
        StringBuffer sb = new StringBuffer ("banUser: User=");
        sb.append (u.getName ());
        sb.append (" BannedBy=");
        sb.append (bannedBy);
        sb.append (" Cookie=");
        sb.append (u.getCookie());
        if (u.conn == null) {
            sb.append (" Connection-Object was null");
        } else if (u.conn.clientIp != null) {
            sb.append(" +IP=");
            sb.append(u.conn.clientIp);
        } else {
            sb.append(" Came over Proxy (Proxy: ");
            sb.append(u.conn.peerIp);
            sb.append(", ForwardChain: ");
            sb.append(u.conn.fwChain);
        }
        sb.append (" Duration=");
        sb.append (millis/1000);
        sb.append ("secs Message=");
        sb.append (message);
        Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
        BanObject bo = new BanObject (message, bannedBy, System.currentTimeMillis () + millis);
        bo.cookie = u.getCookie();
        bo.usr = u.getName().toLowerCase().trim();
        bo.email = (String) u.getProperty("email");
        if (bo.email != null) {
            bo.email = bo.email.trim().toLowerCase();
            banList.put (bo.email, bo);
        }
        banList.put (bo.usr, bo);
        banList.put (bo.cookie, bo);
        if (USE_IP_BAN && u.conn != null && u.conn.isBanable()) {
            bo.con=u.conn;
            banList.put (bo.con.getBanKey(), bo);
        }
        u.sendQuitMessage(true, null);
    }

    public void banConn(Connection conn, String user, String cookie, String message, long millis){
         if (conn != null && conn.isBanable()) {              
            BanObject bo = new BanObject (message, "SYS", System.currentTimeMillis () + millis);
            bo.cookie = cookie;
            bo.usr = user;
            bo.con=conn;
            banList.put (bo.usr, bo);
            banList.put (bo.cookie, bo);
            banList.put (bo.con.getBanKey(), bo);
            StringBuffer sb = new StringBuffer ("banUser: User=");
            sb.append (user);           
            if (conn.clientIp != null) {
                sb.append(" +IP=");
                sb.append(conn.clientIp);
            } else {
                sb.append(" Came over Proxy (Proxy: ");
                sb.append(conn.peerIp);
                sb.append(", ForwardChain: ");
                sb.append(conn.fwChain);
            }
            sb.append (" Duration=");
            sb.append (millis/1000);
            sb.append ("secs Message=");
            sb.append (message);
            Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);           
        }

    }
    
    public boolean removeBan (String key) {
        BanObject bo = (BanObject) banList.get(key);
        if (bo == null)
            return false;
        if (bo.hostban!=null) {
            banList.remove(bo.hostban);
        } else {
            if (bo.usr!=null)
                banList.remove(bo.usr);
            if (bo.cookie!=null)
                banList.remove(bo.cookie);
            if (bo.con!=null)
                banList.remove(bo.con.getBanKey());
            if (bo.email!=null)
                banList.remove(bo.email);
        }
        return true;
    }
    
    public BanObject[] getBanList() {
        if (banList.size() < 0) {
            return new BanObject[0];
        }
        Vector<Object> v = new Vector<Object>();
        for (Enumeration<BanObject> e = banList.elements(); e.hasMoreElements(); ) {
            Object o = e.nextElement();
            //if (v.contains(o))
            //    continue;
            v.add(o);
        }
        return (BanObject[]) v.toArray(new BanObject[0]);
    }

	public void banHost (InetAddress ia, long millis, String msg) {
        BanObject bo = new BanObject (msg, "Server", millis);
        bo.hostban = ia.getHostAddress();
		banList.put (bo.hostban, bo);
	}
	
	public void permaBanHost (InetAddress ia, String msg) {
		BanObject bo = new BanObject (msg, "Config( parmaBannedIp )", 0);
        bo.hostban = ia.getHostAddress();
		banList.put (bo.hostban, bo);
		StringBuffer sb = new StringBuffer ("banHost: Host=");
        sb.append (bo.hostban);
        if (ia.getHostName() != null){
        	sb.append(" (");
        	sb.append(ia.getHostName());
        	sb.append(")");
        }
        
        sb.append (" Message=");
        sb.append (msg);
        Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_MAJOR);

	}
	
	/**
    * checks if this Object is associated to a ban
    * @param o the Object to check
    * @return boolean true if this Object is associated with a ban
    */
	public boolean isBanned (Object o) {
        // null is not ban-able
		if (o == null)
            return false;
        
        // get the ban-object if there is one
        BanObject b;
		if (o instanceof String) {
			String s = ((String) o).toLowerCase();
			b = (BanObject) banList.get (s);
		} else if (o instanceof Connection) {
			Connection conn = (Connection) o;
            if (!conn.isBanable())
                return false;
			b = (BanObject) banList.get(conn.getBanKey());
		} else if (o instanceof InetAddress) {
            InetAddress ia = (InetAddress) o;
            b = (BanObject) banList.get(ia.getHostAddress());
        } else {
            b = (BanObject) banList.get (o);
        }
        
        // not-banned if there is no ban-object
		if (b == null) 
			return false;
        
        // check if the ban is still fresh
		if (b.time < System.currentTimeMillis ()) {
			banList.remove (o);
			return false;
		}
		return true;
	}
	
	public BanObject getBanObject (Object o) {
        // null is not ban-able
        if (o == null)
            return null;
        
        // get the ban-object if there is one
        BanObject b;
        if (o instanceof String) {
            String s = ((String) o).toLowerCase();
            b = banList.get (s);
        } else if (o instanceof Connection) {
            Connection conn = (Connection) o;
            if (!conn.isBanable())
                return null;
            b = banList.get(conn.getBanKey());
        } else if (o instanceof InetAddress) {
            InetAddress ia = (InetAddress) o;
            b = banList.get(ia.getHostAddress());
        } else {
            b = banList.get (o);
        }
        return b;
    }
    
	public boolean isTrafficBanned (Object o) {
        // null is not ban-able
		if (o == null)
            return false;
        
        // get the ban-object if there is one
        BanObject b = null;
        if (o instanceof Connection)
            return false;
        InetAddress ia = null;
        try {
            ia = (InetAddress) o;
        } catch (ClassCastException ce){
            Server.log(this, ce.toString(), Server.MSG_ERROR, Server.LVL_MAJOR);
            return false;
        }
       
        if (props.getProperty("permaIpBlock") != null){
            StringBuffer ipBlock = new StringBuffer(props.getProperty("permaIpBlock"));        
            String values[] = ipBlock.toString().split(",");
            for (int i = 0; i < values.length; i++){
                int pos = values[i].trim().indexOf("*");
                String ip = values[i].substring(0, pos);
                if (ia.getHostAddress().startsWith(ip)){
                     return true;
                }
                ip=null;
            }
        } 

		if (o instanceof InetAddress) {
            b = banList.get(ia.getHostAddress());
        }  
        
        // not-banned if there is no ban-object
		if (b == null || b.hostban == null) 
			return false;
		if (b.hostban != null && !b.hostban.equals(ia.getHostAddress()))
			return false;
		if (b.bannedBy.equals("Config( parmaBannedIp )"))
			return true;
		
        // check if the ban is still fresh
		if (b.time < System.currentTimeMillis ()) {
			banList.remove (o);
			return false;
		}
		return true;
	}
	
/***********************************************************************************************
*  ACTIONSTORE -METHODS (for checking and storeded users...
************************************************************************************************/
    public void storeUser (Vector<Object> v, int action, String message, long millis, String storedBy) {
        for (Enumeration<?> e = v.elements(); e.hasMoreElements(); ) {
		    User u = (User) e.nextElement();
		    this.storeUser (action, u, message, millis, storedBy);
		}
    }

    public void storeUser (int action, User u, String message, long millis, String storedBy) {
        if (u==null)
		    return;
		if (action != IActionStates.PUNISH && 
				action != IActionStates.FLOCKCOL && 
				action != IActionStates.FLOCKAWAY && 
				action != IActionStates.FLOCKME && 
				action != IActionStates.SUBAN &&
				action != IActionStates.ISPUNISHABLE)
		    return;
	    if (action == IActionStates.PUNISH &&  srv.PUNISH_DURATION == -1)
	        return;
	    if (action == IActionStates.ISPUNISHABLE &&  (srv.PUNISH_DURATION == -1 || MESSAGE_FLOOD_LENGHT == -1))
	        return;
	    if ((action == IActionStates.FLOCKCOL  
	            || action == IActionStates.FLOCKAWAY
	            || action == IActionStates.FLOCKME) 
	            && srv.MAX_FLOCK_DURATION == -1){
            return;
	    }
	    String room = "";
	    if (UserManager.mgr.getUserByName(storedBy) != null)
		    room = UserManager.mgr.getUserByName(storedBy).getGroup().getName(); 
	   
	    String actionString = null;
        if (action== IActionStates.FLOCKCOL)
            actionString ="FLOCKCOL";
        if (action== IActionStates.FLOCKAWAY)
            actionString ="FLOCKAWAY";
	    if (action== IActionStates.FLOCKME)
	        actionString ="FLOCKME";
	    if (action== IActionStates.ISPUNISHABLE)
	        actionString ="ISPUNISHABLE";
	    if (action== IActionStates.PUNISH)
	        actionString ="PUNISH";
	    if (action== IActionStates.SUBAN)
	        actionString ="SUBAN"; 

		StringBuffer sb = new StringBuffer ("StorUser: User=");
		sb.append (u.getName ());
		sb.append (" StoredBy=");
		sb.append (storedBy);
		sb.append (" Action=");
		sb.append (actionString);
		sb.append (" Cookie=");
		sb.append (u.getCookie());
		if (u.conn == null) {
	        sb.append (" Connection-Object was null");
		} else if (u.conn.clientIp != null) {
				   sb.append(" +IP=");
				   sb.append(u.conn.clientIp);
	    } else {
				   sb.append(" Came over Proxy (Proxy: ");
				   sb.append(u.conn.peerIp);
				   sb.append(", ForwardChain: ");
				   sb.append(u.conn.fwChain);
	    }
        sb.append (" Duration=");
        sb.append (millis/1000);
        sb.append ("secs Message=");
        sb.append (message);
        sb.append (" room=");
        sb.append (room);
        Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_MAJOR);
        ActionstoreObject po = new ActionstoreObject (action, message, storedBy, room, System.currentTimeMillis () + millis);
        if (USE_IP_BAN && u.conn != null && u.conn.isBanable()&& po.equalsActionState(IActionStates.ISPUNISHABLE)) {
            po.con=u.conn;
        }
        po.u = u;
        po.cookie = u.getCookie();
        po.usr = u.getName().toLowerCase().trim();       
        storeList.put (po, po.usr);
    }
    
    public void storeUser (int action, User u, User cu, String message, long millis, String storedBy) {
        if (u==null || cu == null)
            return;
        if (action != IActionStates.IGNOREUSER) 
             return;
  
        String room = "";
        if (UserManager.mgr.getUserByName(storedBy) != null)
            room = UserManager.mgr.getUserByName(storedBy).getGroup().getName(); 
       
        String actionString = null;
        if (action== IActionStates.FLOCKCOL)
            actionString ="FLOCKCOL";
        if (action== IActionStates.FLOCKAWAY)
            actionString ="FLOCKAWAY";
        if (action== IActionStates.FLOCKME)
            actionString ="FLOCKME";
        if (action== IActionStates.ISPUNISHABLE)
            actionString ="ISPUNISHABLE";
        if (action== IActionStates.PUNISH)
            actionString ="PUNISH";
        if (action== IActionStates.SUBAN)
            actionString ="SUBAN"; 

        StringBuffer sb = new StringBuffer ("StorUser: User=");
        sb.append (u.getName ());
        sb.append (" StoredBy=");
        sb.append (storedBy);
        sb.append (" Action=");
        sb.append (actionString);
        sb.append (" Cookie=");
        sb.append (u.getCookie());
        if (u.conn == null) {
            sb.append (" Connection-Object was null");
        } else if (u.conn.clientIp != null) {
                   sb.append(" +IP=");
                   sb.append(u.conn.clientIp);
        } else {
                   sb.append(" Came over Proxy (Proxy: ");
                   sb.append(u.conn.peerIp);
                   sb.append(", ForwardChain: ");
                   sb.append(u.conn.fwChain);
        }
        sb.append (" Duration=");
        sb.append (millis/1000);
        sb.append ("secs Message=");
        sb.append (message);
        sb.append (" room=");
        sb.append (room);
        Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_MAJOR);
        ActionstoreObject po = new ActionstoreObject (action, message, storedBy, room, System.currentTimeMillis () + millis);
        if (USE_IP_BAN && u.conn != null && u.conn.isBanable()&& po.equalsActionState(IActionStates.ISPUNISHABLE)) {
            po.con=u.conn;
        }
        po.u = u;
        po.cu = cu.getName().toLowerCase().trim();
        po.cookie = u.getCookie();
        po.usr = u.getName().toLowerCase().trim();       
        storeList.put (po, po.usr);
    }
    
    public ActionstoreObject[] getStoreList() {
        if (storeList.size() < 0) {
        	return new ActionstoreObject[0];
	    }
	    Vector<Object> v = new Vector<Object>();
	    for (Enumeration<ActionstoreObject> e = storeList.keys(); e.hasMoreElements(); ) {
		     Object o = e.nextElement();
		     if (v.contains(o))
				 continue;
		     v.add(o);
	    }
		return (ActionstoreObject[]) v.toArray(new ActionstoreObject[0]);
	}
			
    public boolean removeStore (Object val, int action) {
	    for (Enumeration<?> e =  srv.storeList.keys(); e.hasMoreElements () ; ) {				
		    Object key = e.nextElement ();
			ActionstoreObject so =  (ActionstoreObject) key;
			
			if (so.usr.equals(val) && so.action == action) {	
                if (so.equalsActionState(IActionStates.FLOCKCOL)) {
                    User u =UserManager.mgr.getUserByName(so.usr);
                    if (u!=null)
                        u.setCollock(false);
                }
                if (so.equalsActionState(IActionStates.FLOCKAWAY)) {
                    User u =UserManager.mgr.getUserByName(so.usr);
                    if (u!=null)
                        u.setAwaylock(false);
                }
                if (so.equalsActionState(IActionStates.FLOCKME)) {
                    User u =UserManager.mgr.getUserByName(so.usr);
                    if (u!=null)
                        u.setActlock(false);
                }

				srv.storeList.remove(key);	
				so.clearObject();
				return true;
			}
		}
		return false;
	}
				  
	public boolean isPunished (Object o) {
        // null is not punish-able
		if (o == null || srv.storeList.size() == 0)
		    return false;
				        
		// get the punish-object if there is one
	    Hashtable<?, ?>   st	= (Hashtable<?, ?>) srv.storeList.clone();
		
		for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {				
			 Object key = e.nextElement ();
			 ActionstoreObject p =  (ActionstoreObject) key;
			 if (p.usr.equals(o) && p.equalsActionState(IActionStates.PUNISH)) {	
				 if (p.time < System.currentTimeMillis ()) {
					 storeList.remove (key);
					 p.clearObject();
					 return false;
				 }
				 return true;	
			 }

			 //	check if the punish is still fresh
			 if (p.time < System.currentTimeMillis ()) {
				 storeList.remove (key);
				 p.clearObject();
			 }
		}
				        
		// not-punished if there is no punish-object
		return false;
    }
	
	public boolean isEntranceBanned (Object o) {
        // null is not bann-able
		if (o == null || srv.storeList.size() == 0)
		    return false;
				        
		// get the punish-object if there is one
	    Hashtable<?, ?>   st	= (Hashtable<?, ?>) srv.storeList.clone();
		
		for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {				
			 Object key = e.nextElement ();
			 ActionstoreObject p =  (ActionstoreObject) key;
			 if (p.usr.equals(o) && p.equalsActionState(IActionStates.SUBAN)) {	
				 if (p.time < System.currentTimeMillis ()) {
					 storeList.remove (key);
					 p.clearObject();
					 return false;
				 }
				 return true;	
			 }

			 //	check if the punish is still fresh
			 if (p.time < System.currentTimeMillis ()) {
				 storeList.remove (key);
				 p.clearObject();
			 }
		}
				        
		// not-ban if there is no ban-object
		return false;
    }
		
	public boolean isCollocked (Object o) {
	    // null is not locked-able
	    if (o == null || srv.storeList.size() == 0)
	        return false;
			    
	    // get the lock-object if there is one
	    Hashtable<?, ?>   st	= (Hashtable<?, ?>) srv.storeList.clone();
		for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {				
			Object key = e.nextElement ();
			ActionstoreObject l =  (ActionstoreObject) key;
			if (l.usr.equals(o) && l.equalsActionState(IActionStates.FLOCKCOL)) {		
				if (l.time < System.currentTimeMillis ()) {
					storeList.remove (key);
					l.clearObject();
					return false;
				}
				return true;
			}

			// check if the lock is still fresh
			if (l.time < System.currentTimeMillis ()) {
			    storeList.remove (key);
			    l.clearObject();
			}
		}
				        
		// get the lock-object if there is one
	    return false;
    }

    public boolean isActlocked (Object o) {
        // null is not locked-able
        if (o == null || srv.storeList.size() == 0)
            return false;
                
        // get the lock-object if there is one
        Hashtable<?, ?>   st    = (Hashtable<?, ?>) srv.storeList.clone();
        for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {              
            Object key = e.nextElement ();
            ActionstoreObject l =  (ActionstoreObject) key;
            if (l.usr.equals(o) && l.equalsActionState(IActionStates.FLOCKME)) {       
                if (l.time < System.currentTimeMillis ()) {
                    storeList.remove (key);
                    l.clearObject();
                    return false;
                }
                return true;
            }

            // check if the lock is still fresh
            if (l.time < System.currentTimeMillis ()) {
                storeList.remove (key);
                l.clearObject();
            }
        }
                        
        // get the lock-object if there is one
        return false;
    }

    public boolean isAwaylocked (Object o) {
        // null is not locked-able
        if (o == null || srv.storeList.size() == 0)
            return false;
                
        // get the lock-object if there is one
        Hashtable<?, ?>   st    = (Hashtable<?, ?>) srv.storeList.clone();
        for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {              
            Object key = e.nextElement ();
            ActionstoreObject l =  (ActionstoreObject) key;
            if (l.usr.equals(o) && l.equalsActionState(IActionStates.FLOCKAWAY)) {       
                if (l.time < System.currentTimeMillis ()) {
                    storeList.remove (key);
                    l.clearObject();
                    return false;
                }
                return true;
            }

            // check if the lock is still fresh
            if (l.time < System.currentTimeMillis ()) {
                storeList.remove (key);
                l.clearObject();
            }
        }
                        
        // get the lock-object if there is one
        return false;
    }
    
    public boolean isPermaIgnoredUser (Object o, String i) {
        // null is not locked-able
        if (o == null || srv.storeList.size() == 0)
            return false;
                
        // get the lock-object if there is one
        Hashtable<?, ?>   st    = (Hashtable<?, ?>) srv.storeList.clone();
        for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {              
            Object key = e.nextElement ();
            ActionstoreObject l =  (ActionstoreObject) key;
            if (l.usr.equals(o) && l.equalsActionState(IActionStates.IGNOREUSER)) {       
                if (l.time < System.currentTimeMillis ()) {
                    storeList.remove (key);
                    l.clearObject();
                    return false;
                }
                if (l.cu.equals(i))
                    return true;
            }

            // check if the lock is still fresh
            if (l.time < System.currentTimeMillis ()) {
                storeList.remove (key);
                l.clearObject();
            }
        }
                        
        // get the lock-object if there is one
        return false;
    }
    
    public void checkPermaIgnorlistForUser (User u) {
        // null is not locked-able
        if (srv.storeList.size() == 0)
            return;
                
        // get the lock-object if there is one
        Hashtable<?, ?>   st    = (Hashtable<?, ?>) srv.storeList.clone();
        for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {              
            Object key = e.nextElement ();
            ActionstoreObject l =  (ActionstoreObject) key;
            if (l.equalsActionState(IActionStates.IGNOREUSER)) {       
                if (l.time < System.currentTimeMillis ()) {
                    storeList.remove (key);
                    l.clearObject();
                    return;
                }
                if (!u.userIsIgnored(l.cu)){
                    u.ignoreUser(l.cu);
                    Server.log(this, "add ingnored User "+l.cu+" for User ["+u.getName()+"]", Server.MSG_AUTH, Server.LVL_MAJOR);
                }
            }

            // check if the lock is still fresh
            if (l.time < System.currentTimeMillis ()) {
                storeList.remove (key);
                l.clearObject();
            }
        }
                        
        // get the lock-object if there is one
        return;
    }
    
	public boolean isPunishable (Object o) {
	    // null is not punish-able
	    if (o == null || srv.storeList.size() == 0)
	        return false;

	    // get the punish-object if there is one
	    Hashtable<?, ?>   st	= (Hashtable<?, ?>) srv.storeList.clone();
		for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {
			Object key = e.nextElement ();
			ActionstoreObject l =  (ActionstoreObject) key;
			if (l.usr.equals(o) && l.equalsActionState(IActionStates.ISPUNISHABLE)) {
				if (l.time < System.currentTimeMillis ()) {
					storeList.remove (key);
					return false;
				}
				return true;
			}

			// check if the punish is still fresh
			if (l.time < System.currentTimeMillis ()) {
			    storeList.remove (key);
			}
		}

		// get the punishable-object if there is one
	    return false;
    }

	public User getPunishableKey (Object o) {
	    // null is not punish-able
	    if (o == null || srv.storeList.size() == 0)
	        return null;

	    // get the punish-object if there is one
	    Hashtable<?, ?>   st	= (Hashtable<?, ?>) srv.storeList.clone();
		for (Enumeration<?> e =  st.keys(); e.hasMoreElements () ; ) {
			Object key = e.nextElement ();
			ActionstoreObject l =  (ActionstoreObject) key;
			if (l.usr.equals(o) && l.equalsActionState(IActionStates.ISPUNISHABLE)) {
				if (l.time < System.currentTimeMillis ()) {
					storeList.remove (key);
					return null;
				}
				return l.u;
			}

			// check if the punish is still fresh
			if (l.time < System.currentTimeMillis ()) {
			    storeList.remove (key);
			}
		}

		// get the punishable-object if there is one
	    return null;
    }
/**********************************************************************************************
 * LOGGING (will be moved to an extra object...
 **********************************************************************************************/
	public static String[] LOGFILE = {"console", "console", "console", 
         "console", "console", "console", "console"};

    public static final short MSG_CONFIG        =  0;
    public static final short MSG_AUTH          =  1;
    public static final short MSG_STATE         =  2;
    public static final short MSG_TRAFFIC       =  3;
    public static final short MSG_ERROR         =  4;
    public static final short MSG_MESSAGE       =  5;
    public static final short MSG_SEPAMESSAGE   =  6;

    public static final short LVL_HALT          = 0;
    public static final short LVL_MAJOR         = 1; 
    public static final short LVL_MINOR         = 2;
    public static final short LVL_VERBOSE       = 3;
    public static final short LVL_VERY_VERBOSE  = 4;

    public static boolean   DEBUG = false;
    public static Short     LOG_MASK[] = new Short[7];
   /**
    * allows to check for logability of a message in advance of
    * performance critical messages
    */
   public static boolean checkLogLvl (short type, short lvl) {
      return (LOG_MASK[type].intValue () >= lvl || DEBUG);
   }

   /**
    * Logging method configured by the loglevel mechanism
    * @param msg the message to log
    * @param type the type of message (MSG_ prefixed konstants are used here)
    * @param lvl the level of attention to use here (LVL_ prefixed konstants are used here)
    */
    public static void log (Object o, String msg, short type, short lvl) {
        StringBuffer sb = new StringBuffer ();
        try {
            if (LOG_MASK[type].intValue () < lvl && !DEBUG) return;
            sb.append ("[");
            sb.append (Server.formatDefaultTimeStamp (System.currentTimeMillis ()));
            switch (lvl) {
                case LVL_MAJOR:
                    sb.append ("] MAJOR-| ");
                    break;
                case LVL_HALT:
                    sb.append ("] HALT -| ");
                    break;
                default:
                    sb.append ("]      -| ");
            }
            if (o != null) {
                sb.append (o.toString());
                sb.append (": ");
            }
            sb.append (msg);
            sb.append ("\r\n");
            if ((type == MSG_CONFIG && LOGFILE[MSG_CONFIG].equals("console")) 
                    || (type == MSG_AUTH && LOGFILE[MSG_AUTH].equals("console")) 
                    || (type == MSG_STATE && LOGFILE[MSG_STATE].equals("console"))
                    || (type == MSG_TRAFFIC && LOGFILE[MSG_TRAFFIC].equals("console"))
                    || (type == MSG_ERROR && LOGFILE[MSG_ERROR].equals("console"))) {
                System.out.print (sb.toString ());
            } else {
                LogWriter.instance.addLogMessage (type, sb.toString());
            } 
            if (lvl == LVL_HALT) 
                System.exit (1);
        } catch (Exception e) {
            System.err.println("Server.log caused Exception for Message:");
            System.err.print(sb.toString());
            e.printStackTrace();
        }
    }

    public static void logMessage (MessageState messageState, Object o, String msg, short type, short lvl) {
        StringBuffer sb = new StringBuffer ();
        try {
            if (LOG_MASK[type].intValue () < lvl && !DEBUG) return;
            sb.append ("[");
            sb.append (Server.formatDefaultTimeStamp (System.currentTimeMillis ()));
            sb.append ("]");
            if (o != null) {
                sb.append (o.toString());
                sb.append (": ");
            }
            sb.append (msg);
            sb.append ("\r\n");
            if ((type == MSG_MESSAGE &&  LOGFILE[MSG_MESSAGE].equals("console"))
                    || (type == MSG_SEPAMESSAGE) &&  LOGFILE[MSG_MESSAGE].equals("console")) {
                System.out.print (sb.toString ());
            } else {
                LogWriter.instance.addMessageLogMessage (messageState, type, sb.toString());
            }  
            if (lvl == LVL_HALT) 
                System.exit (1);
        } catch (Exception e) {
            System.err.println("Server.log caused Exception for Message:");
            System.err.print(sb.toString());
            e.printStackTrace();
        }
    }
    
   /**
    * Debuging method configured by the loglevel mechanism
    * @param prefix to write before other stuff
    * @param t is the throwable to print the stacktrace from
    * @param type the type of message, konstants with MSG_ prefix are used here
    * @param lvl the level of atention to use here, konstants with LVL_ prefix are used here
    */
    public static void debug (Object o, String prefix, Throwable t, short type, short lvl) {
        if (LOG_MASK[type].intValue () < lvl && !DEBUG) return;
        StringBuffer sb = new StringBuffer ();
        sb.append (prefix);
        sb.append ("\r\n");
        sb.append (t.toString ());
        StackTraceElement ste[] = t.getStackTrace ();
        for (int i = 0; i < ste.length; i++) {
            sb.append ("\r\n    at ");
            sb.append (ste[i].getClassName ());
            sb.append ("(");
            sb.append (ste[i].getFileName ());
            sb.append (":");
            sb.append (ste[i].getLineNumber ());
            sb.append (")");
        }
        log (o, sb.toString (), type, lvl);
    }


 /**********************************************************************************************
 * INTERFACE RELOADABLE
 **********************************************************************************************/
   private long lastModified;
   private File configFile;

   public long lastModified () {
      return lastModified;
   }

   public void changed () {
      try {
         FileInputStream fis = new FileInputStream (configFile);
         Properties tprop = new Properties ();
         tprop.load (fis);
         fis.close ();
         props = tprop;
         checkForConfigValues ();
         lastModified = configFile.lastModified ();
         Server.log (this, "reload: reloaded configfile", Server.MSG_STATE, Server.LVL_MINOR);
      } catch (Exception e) {
         Server.debug (this, "reload: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
      }
   }

   public void removed () {
      Server.log (this, "CRITICAL-WARNING: Config file has been removed!\r\nThe Serverconfiguration of the last configuration-file present will stay in charge, but the server won't start if no config is present!", Server.MSG_ERROR, Server.LVL_MAJOR);
   }
   public File getFile () {
	  return configFile;
   }
	public boolean filePresent() {
		return true; // server-config must always be present
	}
	public void created() {
		changed();
	}
	public String getFormatedTime (String pattern) {
		return formatTimeStamp (System.currentTimeMillis(), pattern); 
	}
    
    public static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    public static SimpleDateFormat hourSDF   = new SimpleDateFormat ("HH");
    public static SimpleDateFormat minuteSDF = new SimpleDateFormat ("mm");

    public static String formatDefaultTimeStamp (long ts) {
        cal.setTimeInMillis(ts);
        return defaultDateFormat.format (cal.getTime());
    }
	public String formatTimeStamp (long ts, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat (pattern);
		cal.setTimeInMillis (ts);
		return sdf.format (cal.getTime ());
	}
    
    public static String formatTimeStamp (long ts, SimpleDateFormat sdf) {
        cal.setTimeInMillis (ts);
        return sdf.format (cal.getTime ());
    }

	/**
	 *  returns the directory of the main server config. other classes can
	 * fetch their configs from there (e.g. the Authenticator-classes. 
	 */
	public File getConfigDir() {
		return (configFile != null) ? configFile.getParentFile() : null;
	}
	
    public String toString () {
        return ("[Server]");
    }
}

