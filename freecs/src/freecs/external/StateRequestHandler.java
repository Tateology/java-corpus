package freecs.external;

import freecs.Server;
import freecs.content.ContentContainer;
import freecs.core.GroupManager;
import freecs.core.RequestReader;
import freecs.core.UserManager;
import freecs.interfaces.IRequest;
import freecs.layout.Template;
import freecs.layout.TemplateSet;
import freecs.util.logger.LogCleaner;
import freecs.util.logger.LogWriter;

public class StateRequestHandler extends AbstractRequestHandler {
    private static final String handler= "/state";
    private final String version = "1.0";
    
    private static final IRequestHandler selve= new StateRequestHandler(handler);

    
    public static IRequestHandler getHandlerInstance () {
        return selve;
    }
       
    public Object instanceForSystem() {
        return this;
    }
    
    public String getHandler() {
        return handler;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

	public StateRequestHandler(String handlerName) {
		super(handlerName);
	}

	/**
	 * class rendering information on server internals
	 */

	public void handle(IRequest req, ContentContainer c) {
		checkAccessIp(req,c);

        if (req.getValue("gc")!=null) {
            System.gc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { 
            }
        }
		
		StringBuffer sb = new StringBuffer ();
		renderTemplate(req, "admin_state_header", sb);
		sb.trimToSize();
	    sb.append (Server.getVersion ());
	    sb.append (")</td></tr><tr><td width=\"10\"></td><td witdh=\"200\" align=\"right\"><strong>Onlinetime:</strong></td><td width=10></td><td width=\"400\">");
	    long sec=0, min=0, hour=0, day=0;
	    sec = (System.currentTimeMillis()-Server.startupTime)/1000;
	    min = sec/60;
	    if (min>0)
	    	sec = sec-min*60;
	    if (min >=60){
	    	hour = min/60;
	    	min = min -(hour*60);
	    }
	    if (hour >=24){
	    	day = hour/24;
	    	hour = hour -(day*24);
	    }   
	    sb.append (day);
	    sb.append (" day ");
	    sb.append (hour);
	    sb.append (" h ");
	    sb.append (min);
	    sb.append (" min ");
	    sb.append (sec);
	    sb.append (" sec");
	    sb.append ("</td></tr><tr><td colspan=\"4\" bgcolor=\"#000000\" height=\"1\"></td></tr><tr><td width=\"10\"></td><td witdh=\"200\" align=\"right\"><strong>Users:</strong></td><td width=\"10\"></td><td width=\"400\">");
	    UserManager umgr = UserManager.mgr;
	    sb.append (umgr.getActiveUserCount ());
	    sb.append ("(");
	    sb.append (umgr.getHighWaterMark());
	    sb.append (" max)");
	    sb.append ("</td></tr><tr><td colspan=\"4\" bgcolor=\"#000000\" height=\"1\"></td></tr><tr><td width=\"10\"></td><td witdh=\"200\" align=\"right\"><strong>Rooms:</strong></td><td width=\"10\"></td><td width=\"400\">");
	    GroupManager gmgr = GroupManager.mgr;
	    sb.append (gmgr.openGroupsCount());
	    sb.append ("(");
	    sb.append (gmgr.getHighWaterMark());
	    sb.append (" max)");
	    sb.append ("</td></tr><tr><td colspan=\"4\" bgcolor=\"#000000\" height=\"1\"></td></tr><tr><td width=\"10\"></td><td witdh=\"200\" align=\"right\"><strong>Reader-Threads:</strong></td><td width=\"10\"></td><td width=\"400\">");
	    sb.append (RequestReader.activeReaders ());
	    if (!Server.srv.THREAD_PER_READ) {
	    	sb.append ("</td></tr><tr><td colspan=\"4\" bgcolor=\"#000000\" height=\"1\"></td></tr><tr><td width=\"10\"></td><td witdh=\"200\" align=\"right\"><strong>Reader-Queue-Usage:</strong></td><td width=10></td><td width=400>");
	    	double d[] = RequestReader.getOveralUsage();
	    	for (int i = 0; i < d.length; i++) {
	    		sb.append (d[i]);
	    		sb.append ("%");
	    		if (i < d.length) 
	    			sb.append (", ");
	    	}
	    }
	    sb.append ("</td></tr><tr><td colspan=\"4\" bgcolor=\"#000000\" height=\"1\"></td></tr><tr><td width=\"10\"></td><td witdh=\"200\" align=\"right\"><strong>Memory:</strong></td><td width=\"10\"></td><td width=\"400\">");
	    Runtime r = Runtime.getRuntime ();
	    long free = r.freeMemory ();
	    long total = r.totalMemory ();
	    long max = r.maxMemory ();
	    long used = total - free;
	    sb.append ("free: ");
	    sb.append (free);
	    sb.append ("<br />used: ");
	    sb.append (used);
	    sb.append ("<br />total/maxTotal: ");
	    sb.append (total);
	    sb.append ("/");
	    sb.append (max);
	    sb.append ("<br /><strong>Usagepercentage:</strong> ");
	    sb.append (100 - (free / (total / 100)));
	    sb.append ("</td></tr></table>");
	    sb.append ("<a href=/STATE?gc=now>run garbagecollector</a>");
	    sb.append ("<br />LogQueue: ").append (LogWriter.instance.logQueue.size());
        sb.append (" of ").append(LogWriter.instance.logQueue.capacity());
        sb.append ("<br />LogWriter priority: ").append(LogWriter.instance.getPriority());
	    sb.append ("<br />LogDestinations:").append  (LogCleaner.instance.getLogDestinations().size());
	    sb.append ("<br />LogCleaner priority: ").append(LogCleaner.instance.getPriority());
	    sb.append ("</body></html>");
	    sb.trimToSize();
	    c.wrap (sb.toString(), req.getCookieDomain());;
	}
	private void renderTemplate(IRequest req, String name, StringBuffer sb) {
        sb.trimToSize();
		sb.append(renderTemplate(req, name));
    }

    private String renderTemplate(IRequest req, String name) {
        TemplateSet ts = Server.srv.templatemanager.getTemplateSet("admin");
        Template tpl = ts.getTemplate(name);
        if (tpl == null){
            Server.log(this, "File "+name+" not loaded", Server.MSG_ERROR, Server.LVL_MAJOR);
            return "<html><head><title>state query</title><meta http-equiv=\"refresh\" content=\"4\"></head><body bgcolor=\"#ffff99\" text=\"000000\"><table border=0><tr><td colspan=\"4\" align=\"center\">FreeCS (" ;  
        }
        return tpl.render(req);
    }
}
