package gov.noaa.ncdc.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class URLDirectory {

    public static String[] getFileNames(URL urlDirectory) {
        return getFileNames(urlDirectory, 999999999L);
    }
    public static String[] getFileNames(URL urlDirectory, long numResults) {
    	URL[] urls = getURLs(urlDirectory, numResults);
    	String[] fileNames = new String[urls.length];
    	for (int n=0; n<urls.length; n++) {
    		String urlString = urls[n].toString();
    		fileNames[n] = urlString.substring(urlString.lastIndexOf("/")+1);
    	}
    	return fileNames;    	
    }

    
    
    public static URL[] getURLs(URL urlDirectory) {
    	return getURLs(urlDirectory, 999999999L);
    }
    
    public static URL[] getURLs(URL urlDirectory, long numResults) {
        ArrayList<URL> list = new ArrayList<URL>();

        try {

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(urlDirectory.openStream()));
            String str;
            int n, beg, end, count=0;
            while ((str = in.readLine()) != null && count < numResults) {

                // Make the tags lower case
                str = str.replaceAll("<A HREF=", "<a href=");
                str = str.replaceAll("</A>", "</a>");

                // str is one line of text; readLine() strips the newline character(s)
                if ((n = str.indexOf("<a ")) != -1) {
                    beg = str.indexOf("<a ");
                    end = str.indexOf("</a>");
                    if (beg >= 0 && end >= 0) {
                        String file = str.substring(beg, end); 
                        file = file.substring(file.indexOf("=")+2, file.indexOf(">")-1);

                        URL url = null;
                        if (file.startsWith("ftp://") || file.startsWith("http://")) {
                        	url = new URL(file);
                        }
                        else {
                        	String dir = urlDirectory.toString();
                        	dir = (! dir.endsWith("/") ? dir+"/" : dir);
                        	url = new URL(dir+file);
                        }
                        
                        list.add(url);
                        count++;
                    }
                }
            }
            in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        URL[] urls = new URL[list.size()];
        list.toArray(urls);
        return urls;
    }


}
