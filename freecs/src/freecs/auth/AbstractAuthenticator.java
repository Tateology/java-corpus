package freecs.auth;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import freecs.Server;

/**
 * base class for all Authenticators, provides methods for reading config files
 */
public abstract class AbstractAuthenticator implements IAuthenticator {
    String additionalPrefix = null;
    Properties props = null;
    String toStringValue = null;

    public void init (Properties allProps, String additionalPrefix) {
        this.additionalPrefix = additionalPrefix;
        props = filterProperties(allProps);
    }

	/***************************************************************************
	 * helpers
	 **************************************************************************/
	
    /**
     *  parses a comma-separated uselist (e.g. friendslist)
     * @param string like this: "friend1,friend2,friend3"
     * @return array of valid usernames, empty array if no users are valid
     */
    public List<String> parseUserList(String friendslist) {
        List<String> users = new ArrayList<String>();
        if (friendslist != null && friendslist.length() > 0) {
        	friendslist = friendslist.toLowerCase();
            String friendsArray[] = friendslist.split (",");
            for (int i = 0; i < friendsArray.length; i++) {
                String friend = friendsArray[i].trim();
                if (friend.length() < 1)
                    continue;
                if (!Server.srv.auth.isValidName(friend))
                    continue;
                users.add(friend);
            }
        }
        return users;
	}
	
	public boolean parseBoolean(Object value) {
		if (value == null) {
			return false;
		} else if (value instanceof Boolean) {
			return ((Boolean)value).booleanValue();
		} else if (value instanceof String) {
			String stringVal = (String)value;
			if ("true".equalsIgnoreCase(stringVal) || "1".equalsIgnoreCase(stringVal) || "yes".equalsIgnoreCase(stringVal)) {
				return true;
			} else {
				return false;
			}
		} else if (value instanceof Number) {
			int i = ((Number)value).intValue();
			return (i==1) ? true : false;
		}
		return false;
	}
	

	/**
	 * parses properties so that only keys that start with the
	 * name of this class remain
	 */
	public Properties filterProperties(Properties props) {
		Class<? extends AbstractAuthenticator> clazz = getClass();
		String packageName = clazz.getPackage().getName(); 
		String simpleClassname = clazz.getName().substring(packageName.length() + 1);
        String filter = simpleClassname + ".";
        if (additionalPrefix != null)
            filter += additionalPrefix + ".";
		return filterProperties(props, filter);
	}
		
	/**
	 * parses properties so that only keys that start with the
	 * given prefix remain
	 */
	public Properties filterProperties(Properties props, String prefix) {
		Properties newProps = new Properties();
		for (Enumeration<Object> e = props.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (key.startsWith(prefix)) {
				String propName = key.substring(prefix.length());
				String propVal  = props.getProperty(key);
				newProps.put(propName, propVal);
			}
		}
		
		return newProps;
	}
	
    public String toString() {
        if (toStringValue != null)
            return toStringValue;
        StringBuffer sb = new StringBuffer();
        Class<? extends AbstractAuthenticator> clazz = getClass();
        String packageName = clazz.getPackage().getName();
        sb.append ("[");
        sb.append (clazz.getName().substring(packageName.length() + 1));
        if (additionalPrefix!=null)
            sb.append (" ").append(additionalPrefix);
        sb.append ("]");
        toStringValue = sb.toString();
        return toStringValue;
    }
}
