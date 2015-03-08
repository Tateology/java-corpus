package freecs.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import freecs.Server;
import freecs.content.ContentContainer;
import freecs.interfaces.IRequest;

public class StaticRequestHandler extends AbstractRequestHandler {
    private static final String handler= "/static";
    private final String version = "1.0";
    
    private static final IRequestHandler selve= new StaticRequestHandler(handler);

    
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

	private HashMap<String, FileProperties> fileCache = new HashMap<String, FileProperties>();
    
	public StaticRequestHandler(String handlerName) {
		super(handlerName);
	}

	/**
	 * class rendering information on server internals
	 */

	public void handle(IRequest req, ContentContainer c) {
		String file = req.getAction();
		if (!file.toLowerCase().equals ("/robots.txt") && !file.toLowerCase().equals ("/favicon.ico")) {
	        file = file.substring(file.substring(1).indexOf("/") + 2);
		} else {
		    if (file.toLowerCase().equals ("/robots.txt"))
		        file = "robots.txt";
		    if (file.toLowerCase().equals ("/favicon.ico"))
		        file = "favicon.ico";
		}
        Object cached = fileCache.get(file);
        if (cached != null) {
            FileProperties fp = (FileProperties) cached;
            if (!fp.f.exists()) {
                    c.setTemplate("not_found");
                    return;
            } else if (fp.f.lastModified() == fp.lastModified) {
                try {
                    c.setContentType(fp.contentType);
                    c.setContent(fp.content);
                } catch (Exception e) {
                    throw new AccessForbiddenException(true);
                }
                c.setContentType(fp.contentType);
                return;
            }
        }
        if (file.indexOf("/") > -1 && !file.endsWith(".class"))
            throw new AccessForbiddenException (true);
        StringBuilder dirpath = new StringBuilder(Server.BASE_PATH).append("/static/");
        File sFile = new File (dirpath.toString());
        File fList[] = sFile.listFiles ();
   
        StringBuilder filepath = new StringBuilder(Server.BASE_PATH);
        filepath.append(File.separator);
        filepath.append("static");
        filepath.append(File.separator);
        filepath.append(file);
        File f = new File (filepath.toString());
        if (!f.exists()) {     
            if (fList == null || fList.length == 0){
                c.setTemplate("not_found");
                return;
            }
            for (int i = 0; i < fList.length; i++) {
                if (!fList[i].isDirectory () ) 
                   continue;
              //  filepath = new StringBuilder(Server.BASE_PATH);  
                filepath = new StringBuilder();
                filepath.append(fList[i]);
                filepath.append(File.separator);
                filepath.append(file);

                f = new File (filepath.toString());
                if (f.exists())
                    break;
            }
        	
        	if (!f.exists()) {
                c.setTemplate("not_found");
                return;
        	}
        }
        
        String contentType;
        if (file.toLowerCase().endsWith (".gif"))
            contentType="image/gif";
        else if (file.toLowerCase().endsWith (".jpg")
                || file.toLowerCase().endsWith (".jpeg"))
            contentType="image/jpeg";
        else if (file.toLowerCase().endsWith (".class"))
            contentType="application/x-java-applet";
        else if (file.toLowerCase().endsWith (".png"))
            contentType="image/png";
        else if (file.toLowerCase().endsWith (".css"))
            contentType="text/css";
        else if (file.toLowerCase().endsWith (".js"))
            contentType="application/x-javascript";
        else if (file.toLowerCase().endsWith (".wav"))
            contentType="audio/wav";
        else if (file.toLowerCase().endsWith (".mp3"))
            contentType="audio/x-mp3";
        else if (file.toLowerCase().endsWith (".txt"))
            contentType="text/plain";
        else if (file.toLowerCase().endsWith (".ico"))
            contentType="image/x-icon";
        else
            throw new AccessForbiddenException (true);
        try {
            InputStream is = new FileInputStream (f);
            byte[] cntnt = new byte[(int) f.length()];
            for (int i = 0; i<cntnt.length; i++)
                cntnt[i] = (byte) is.read();
             fileCache.put(file, new FileProperties(f, cntnt, contentType));
            c.setContentType(contentType);
            c.setContent(cntnt);
        } catch (Exception e) {
            Server.debug ("StaticRequestHandler", "exception during reading file " + file, e, Server.MSG_ERROR, Server.LVL_MINOR);
            c.setTemplate("techerror");
            return;
        }
        c.setContentType(contentType);
    }
    
    class FileProperties {
        long lastModified = -1;
        File f;
        byte[] content;
        String contentType;
        
        FileProperties (File f, byte[] content, String contentType) {
            this.f = f;
            this.content = content;
            this.contentType = contentType;
            this.lastModified = f.lastModified();
        }
    }
}
