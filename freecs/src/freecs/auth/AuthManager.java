package freecs.auth;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.SelectionKey;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freecs.Server;
import freecs.commands.AbstractCommand;
import freecs.content.BanObject;
import freecs.content.Connection;
import freecs.content.ContentContainer;
import freecs.core.Group;
import freecs.core.GroupManager;
import freecs.core.Membership;
import freecs.core.MembershipManager;
import freecs.core.RequestReader;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.interfaces.IReloadable;
import freecs.interfaces.IRequest;
import freecs.layout.TemplateSet;
import freecs.util.FadeColor;
import freecs.util.FileMonitor;

public class AuthManager implements IReloadable {

    public static AuthManager instance = new AuthManager();
	// fields used for tracking config
	private File configFile;
	private long configLastModified;
    
    private boolean ALLOW_UNREGISTERED_USERS=false;
    public String USERNAME_REGEX = "^[a-z[A-Z[0-9[‰ˆ¸ƒ÷‹ﬂ]]]]+$";

    // field used for checking usernames
    public Pattern userNamePattern;

    private IAuthenticator[] list;

    /**
     * constructs a new AuthManagers
     */
    private AuthManager() {
        list = new IAuthenticator[0];
    }

	/**
	 * returns a reference to the authentication config. by default called auth.properties
	 * in server config directory
	 * @see freecs.Server#getConfigDir()
	 */
	public static File getDefaultConfigFile() {
		return new File(Server.srv.getConfigDir(), "auth.properties");
	}

	/**
	 * parses a config file into a Properties object
	 */
	public static Properties parseConfigFile(File configFile) {
		try {
			FileInputStream in = new FileInputStream (configFile);
			Properties props = new Properties ();
	        props.load (in);
	        in.close ();
	        return props;
		} catch (Exception e) {
			Server.log (null, "can't read authentication config from " + configFile + " (" + e.toString() + ")", Server.MSG_STATE, Server.LVL_MAJOR);
			return null;
		}
	}

	/**
	 * inits the class with the default configfile
	 */
	public void init() {
		init(getDefaultConfigFile());
	}
	
	/**
	 * inits the class with a given config file.
	 * @param configFile represents the Fileobject pointing to the config-file
	 */
	public void init(File configFile) {
		this.configFile = configFile;
		configLastModified = configFile.lastModified();
		FileMonitor.getFileMonitor().addReloadable(this);
		createAuthenticators();
	}

	/**
	 * creates authenticator objects according to <pre>authenticators</pre> property of config,
	 * then calls init() on each authenticator.
	 * if no Authenticator is configured property, NoAuthentication is used by default
	 */
	public void createAuthenticators() {
		Properties props = parseConfigFile(configFile);
        String allowUnregistered = props.getProperty("allowUnregisteredUsers", "false").toLowerCase();
        ALLOW_UNREGISTERED_USERS = ("true".equals(allowUnregistered)
                                    || "1".equals(allowUnregistered)
                                    || "yes".equals(allowUnregistered));
        USERNAME_REGEX = props.getProperty("usernameRegex", "^[a-z[A-Z[0-9[‰ˆ¸ƒ÷‹ﬂ]]]]+$");
        userNamePattern = Pattern.compile(AuthManager.instance.USERNAME_REGEX);
		String classNames = props.getProperty("authenticators", "NoAuthentication");
		Server.log(this, "creating new authenticators: " + classNames, Server.MSG_CONFIG, Server.LVL_MINOR);
		StringTokenizer tok = new StringTokenizer (classNames, ",");
		Vector<IAuthenticator> tmpList = new Vector<IAuthenticator>();
		while (tok.hasMoreTokens()) {
            String element = tok.nextToken().trim();
            String additionalConfigPrefix = null;
            if (element.indexOf(" ")!=-1) {
                additionalConfigPrefix = element.substring(element.indexOf(" ")+1).trim();
                element = element.substring(0, element.indexOf(" "));
            }
			String className = getClass().getPackage().getName() + "." + element;
			IAuthenticator authObj = null;
			try {
				authObj = (IAuthenticator) Class.forName(className).newInstance();
			} catch (Exception ex) {
				Server.log(this, "Can't load authentication class " + className + ": " + ex.toString(), Server.MSG_ERROR, Server.LVL_MAJOR);
				continue;
			}
			try {
				authObj.init(props, additionalConfigPrefix);
			} catch (Exception ex1)  {
				Server.log(this, "Error initializing authentication class " + className + ": " + ex1.toString(), Server.MSG_ERROR, Server.LVL_MAJOR);
				continue;
			}
			tmpList.add(authObj);
		}
		if (tmpList.size() == 0) {
            tmpList.add(new NoAuthentication());
			Server.log(this, "No authentication class, starting without authentication", Server.MSG_ERROR, Server.LVL_MAJOR);
		}
        list = (IAuthenticator[]) tmpList.toArray(new IAuthenticator[0]);
	}
	
	/**
	 * calls shutdown() on all registered Authenticators
	 * @see freecs.auth.IAuthenticator#shutdown()
	 */
	public void shutdown() throws Exception {
		for (int i = 0; i < list.length; i++) {
			list[i].shutdown();
		}
	}
	
	/**
	 * checks the credentials of a user against all registered Authenticators
	 * if an Authenticator returns null, the credentials are considered invalid
	 */
	public User loginUser(String username, String password, String cookie, IRequest req) throws Exception {
        if (username == null || username.trim().length() < 1)
            return null;
		User u = null;
		for (int i = 0; i < list.length; i++) {
			if (u == null) {
				u = list[i].loginUser(username, password, cookie, req);
			} else {
				u = list[i].loginUser(u, username, password, req);
			}
			if (u == null) {
				return null;
			} else if (u.getName() == null || "".equals(u.getName().trim())) {
                Server.log (this, list[i].toString() + " returned an invalid userobject having no name!", Server.MSG_AUTH, Server.LVL_MAJOR);
                return null;
            }
		}
        if (u.isUnregistered 
                && !ALLOW_UNREGISTERED_USERS)
            return null;

        if (u.getColCode() == null
                || AbstractCommand._isColorCodeValid(u.getColCode().trim().toLowerCase(), false) != 0) {
            TemplateSet ts = Server.srv.templatemanager.getTemplateSet ("default");
            String templateset = req.getValue ("templateset");
            if (templateset != null) {
               ts = Server.srv.templatemanager.getTemplateSet (templateset);
            }
            String col = ts.getMessageTemplate("constant.defaultColor");
            if (col == null)
                col = AuthManager.instance.generateColCode();
            u.setColCode(col);
            Server.log(Thread.currentThread(), this.toString() + "LOGIN set a newly generated colorcode for " + u.getName() + " to " + u.getColCode(), Server.MSG_AUTH, Server.LVL_VERBOSE);
        }
        
        if (u.getFadeColCode()!= null) {
        	if (AbstractCommand._isColorCodeValid(u.getFadeColCode().trim().toLowerCase(), true)!=0
                || u.getFadeColCode().length() == 0){
        	    u.setFadeColCode(null);
        	} else {
           	    u.setFadeColorUsername(FadeColor.getFadeColorUsername(u.getColCode(), u.getFadeColCode(), u.getName()));
        	}
        }
        
        // aply Memberships to this User
        String msList = (String) u.getProperty("memberships");
        if (u.isUnregistered)
           	msList = "undefined";
        
        if (msList != null){
            setMembership(u, msList);
        }
        
        //check if membership exists
        StringBuilder ml = new StringBuilder();
        if (msList != null) {
			String msListClone = msList;
			String[] msArr = msListClone.split(",");

			for (int i = 0; i < msArr.length; i++) {
				Membership cms = MembershipManager.instance
						.getMembership(msArr[i]);
				if (cms == null) {
					Server.log(this, "(" + u.getName()+ ") Membership for key " + msArr[i]+ " hasn't been found", Server.MSG_AUTH,Server.LVL_MAJOR);
					continue;
				}
				ml.append(msArr[i]);
				if (i < msArr.length)
					ml.append(" ,");
			}
		}
        if (ml != null && ml.length()>0){
            ml.trimToSize();
            msList = ml.toString();
        } else msList=null;

        if (!u.isUnregistered && isStandart(msList)){
            setMembership(u, Server.srv.DEFAULT_MEMBERSHIP);
        }
		return u;
	}

	private boolean isStandart(String mslist){
	    if (mslist == null)
	        return true;
	    if (mslist != null && mslist.length()>0)
	        return false;
	    return true;
	}
	
	private void setMembership(User u,String list){
	    if (list == null)
	        return;
	    String[] msArr = list.split(",");
        for (int i = 0; i < msArr.length; i++) {
             Membership cms = MembershipManager.instance.getMembership (msArr[i]);
             if (cms==null) {
                 Server.log (this, "Membership for key " + msArr[i] + " hasn't been found", Server.MSG_STATE, Server.LVL_VERBOSE);
                 continue;
             }
             cms.add(u);
        }
        return;
	}
	

	/**
	 * updates the username-patterns for all registered Authenticators
     * @param pattern represents the regular-expression, which will be used to check the validity of usernames
	 */
	public void updateUsernamePattern(String pattern) {
        userNamePattern = Pattern.compile(pattern);
    }

    /**
     * checks the given name for validity by using the predeffined pattern.
     * If this username doesn't match the pattern (or any other custom conditions
     * cause this username to be invalid), the login will be rejected. This get's
     * called by the tryLogin()-method of UserManager.java
     * @see freecs.core.UserManager
     * @param name username to check
     * @return true if the given username is a valid username, false if not
     */
    public boolean isValidName(String name) {
        Matcher m = userNamePattern.matcher(name);
        return m.matches();
    }

    public void logoutUser(User u) throws Exception {
		for (int i = 0; i < list.length; i++) {
			list[i].logoutUser(u);
		}
	}

    public void doLogin(IRequest cReq, SelectionKey key, String cookie, ContentContainer c, TemplateSet ts, User u, boolean isHTTP11, RequestReader req) {
        // LOGIN *********
        Connection conn = cReq.getConnectionObject();
      //  SocketChannel sc = (SocketChannel) key.channel ();
      //  InetAddress ia = sc.socket ().getInetAddress ();

        // check user and username for the user's name
        String user = cReq.getValue ("user");
        if (user == null) 
            user = cReq.getValue ("username");
        String pwd = cReq.getValue ("password");
        String grp = cReq.getValue ("group");

        // if no get params are present -> print login_missing template and return
        if (user == null || grp == null) {
            c.setTemplate("login_missing");
            return;
        }
        
        grp = grp.toLowerCase();
        StringBuffer sb = new StringBuffer ("logIN  - ");
        if (Server.srv.USE_TOKENSTORE && !Server.srv.isTokenValid(cReq.getValue("token"), cookie)) {
            sb.append ("invalid token: ");
            sb.append (user);
            sb.append ("@");
            sb.append (conn.toString());
            Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
            c.setTemplate ("login_missing");
        } else if (Server.srv.isBanned (cookie) || Server.srv.isBanned (user) || Server.srv.isBanned(conn)) {
            // USER IS BANNED
            sb.append ("user banned: ");
            sb.append (user);
            sb.append ("@");
            sb.append (conn.toString());
            Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
            if (Server.srv.isBanned(conn) && !Server.srv.isBanned (user) && Server.srv.BLOCKED_NICK_AUTOHARDKICK){
                if (!Server.srv.isTrafficBanned(conn)){
                    BanObject b = Server.srv.getBanObject(conn);
                    if (b != null){
                        Server.srv.banConn(conn, user, cookie, "logIN - user banned", b.time-System.currentTimeMillis ());
                    }
                }
            }
            c.setTemplate ("user_banned");
        } else {
            short result = UserManager.mgr.tryLogin (user, pwd, grp, ts, req, u, conn);
            req.currPosition=RequestReader.EVAL_POST_LOGIN_RESULT;
            if (result == UserManager.LOGIN_CANCELED) {
                sb.append ("login timed out ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate("techerror");
            } else if (result == UserManager.LOGIN_NOTALLOWED) {
                sb.append ("authentication failed: login not allowed ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate("login_notallowed");
            } else if (result == UserManager.LOGIN_GROUP_RESERVED) {
                sb.append ("authentication failed: login group reserved ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate("login_group_reserved");
            } else if (result == UserManager.LOGIN_UNREG_NOTALLOWED) {
                sb.append ("authentication failed: login unreg user not allowed ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate("login_unreguser_notallowed");
            } else if (result == UserManager.USEREMAIL_BANED) {
                sb.append ("user banned: (email-ban)");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate ("user_banned");
            } else if (result == UserManager.USERNAME_INVALID) {
                sb.append ("username not valid (");
                sb.append (user);
                sb.append (")");
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate("username_invalid");
            } else if (result == UserManager.TECHNICAL_ERROR) {
                sb.append ("technical errors! Consult error-log.");
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate ("techerror");
            } else if (result == UserManager.USERNAME_TOO_LONG) {
                sb.append ("authentication failed: username longer than max-username-length (");
                sb.append (Server.srv.MAX_USERNAME_LENGTH);
                sb.append (")@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
                c.setTemplate("username_too_long");
            } else if (result == UserManager.LOGIN_BLOCKED) {
                sb.append ("authentication failed: login-blocked Nick ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                if (conn.isBanable() 
                		&& Server.srv.USE_IP_BAN 
                		&& Server.srv.BLOCKED_NICK_AUTOHARDKICK){
                    long millis = Server.srv.MAX_BAN_DURATION * 60000;
                    Server.srv.banConn(conn, user, cookie, "authentication failed: login-blocked Nick", millis);
                }
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
                c.setTemplate ("user_banned");
            } else if (result == UserManager.USERNAME_NOT_ACTIVATED) {
                sb.append ("authentication failed: username not activated ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
                c.setTemplate ("username_not_activated");
            } else if (result == UserManager.LOGIN_GROUP_LOCK) {
                sb.append ("authentication failed: login-group locked ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
                c.setTemplate ("login_group_locked");
            } else if (result == UserManager.LOGIN_GROUP_BAN) {
                sb.append ("authentication failed: user is banned from startgroup '");
                sb.append (grp);
                sb.append ("' ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
                c.setTemplate ("login_group_banned");
            } else if (result == UserManager.LOGIN_OK) {
                sb.append ("authentication succeeded: ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
                c.setTemplate ("frameset");
            } else if (result == UserManager.LOGIN_MISSING) {
                sb.append ("login missing ");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_VERBOSE);
                c.setTemplate ("login_missing");
            } else if (result == UserManager.MAX_USERS) {
                sb.append ("max users reached: ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate ("too_many_users");
            } else if (result == UserManager.LOGIN_COOKIE_MISSING) {
                sb.append ("no cookie: ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString ());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
                c.setTemplate ("no_cookie");
            } else if (result == UserManager.LOGIN_GROUP_MISSING) {
                sb.append ("no group-name given for login: ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_VERBOSE);
                c.setTemplate ("login_failed");
            } else if (result == UserManager.LOGIN_GROUP_NOSTART) {
                sb.append ("invalid group '");
                sb.append (grp);
                sb.append ("': ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString ());
                Server.log (this, sb.toString (), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate ("login_failed");
            } else if (result == UserManager.LOGIN_COOKIE_DUPLICATE) {
                sb.append ("cookie is present for another user ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_VERBOSE);
                c.setTemplate ("login_failed");
            } else if (result == UserManager.LOGIN_PRESENT) {
                sb.append ("user is already logged in ");
                sb.append (user);
                sb.append (conn.toString());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                c.setTemplate ("user_present");
            } else if (result == UserManager.LOGIN_RELOAD && u != null) {
                sb.append ("user hit reload ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString());
                Group g = u.getGroup ();
                u.setHTTP11 (isHTTP11);
                if (g != null) {
                    g.addUser (u);
                    c.setTemplate ("frameset");
                    Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_VERBOSE);
                    if (ts != null) 
                        u.setTemplateSet (ts);
                } else {
                    sb.append (" but group was null");
                    g = GroupManager.mgr.getStartingGroup (grp);
                    if (g == null && !GroupManager.mgr.isStartingGroup(grp)) {
                        sb.append (" and ");
                        sb.append (grp);
                        sb.append (" is not a starting-group");
                        c.setTemplate ("login_failed");
                    } else if (g == null && GroupManager.mgr.isStartingGroup(grp)) {
                        sb.append (" and ");
                        sb.append (grp);
                        sb.append (" is a starting-group");
                        c.setTemplate ("login_failed");
                    } else {
                        sb.append (" - joins group ");
                        sb.append (g.getRawName());
                        g.addUser (u);
                        c.setTemplate ("frameset");
                        if (ts != null) 
                            u.setTemplateSet (ts);
                    }
                    Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
                }
            } else {
                sb.append ("authentication failed: ");
                sb.append (user);
                sb.append ("@");
                sb.append (conn.toString ());
                Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
                c.setTemplate ("login_failed");
            }
        }
    }

    /**
     * this can be used if a user doesn't have a color code and 
     * should be assigned a random one
     */
    private String generateColCode () {
        int r = (int) Math.round(Math.random() * 255);
        int g = (int) Math.round(Math.random() * 255);
        int b = (int) Math.round(Math.random() * 255);
        while ((r+g+b) > 432) {
            if (r>0) r--;
            if (g>0) g--;
            if (b>0) b--;
        }
        StringBuffer sb = new StringBuffer();
        if (r < 16)
            sb.append("0");
        sb.append (Integer.toHexString(r));
        if (g < 16)
            sb.append("0");
        sb.append (Integer.toHexString(g));
        if (b < 16)
            sb.append("0");
        sb.append (Integer.toHexString(b));
        return sb.toString();
    }

	/***************************************************************************
	 * interface IReloadable
	 **************************************************************************/
	
	public File getFile() {
		return configFile;
	}

	public boolean filePresent() {
		return configFile.exists();
	}

	public long lastModified() {
		return configLastModified;
	}

	public void changed() {
		configLastModified = configFile.lastModified();
		Server.log(this, "authconfig in " + configFile + " changed, reloading.", Server.MSG_CONFIG, Server.LVL_MINOR);
		synchronized(this) {
			try {
				// kill all existing authenticators
				shutdown();
			} catch (Exception ex) {
			}
			// create the new ones from scratch
			createAuthenticators();
		}
	}

	public void removed() {
	}

	public void created() {
		changed();
	}

	
}
