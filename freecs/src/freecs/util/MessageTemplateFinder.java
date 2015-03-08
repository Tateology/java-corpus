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
 * Created on 11.05.2004
 */

package freecs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

/**
 * @author Manfred Andres
 *
 * freecs.util
 */
public class MessageTemplateFinder {
    static TreeSet v = new TreeSet();
    static Properties oldprops = new Properties();

    public static void main(String[] args) {
        if (args == null || args.length<1) {
            System.out.println ("please specify the path to the sourcefolder");
            System.exit(1);
        }
        String base = args[0];
        if (args.length > 1 && args[1]!=null && args[1].length()>0) {
            try {
                FileInputStream fis = new FileInputStream (args[1]);
                oldprops.load(fis);
                fis.close();
            } catch (Exception e) {
                System.out.println ("old properties-file wasn't found");
                e.printStackTrace();
            }
        }
        File basef = new File (base);
        if (!basef.exists() || !basef.isDirectory()) {
            System.out.println ("The path specified is not correct");
            System.exit (1);
        }
        System.out.println ("Searching...");
        search (basef);
        
        // add messagetemplates which are set via MessageParser.setMessageTemplate();
        v.add("message.user.join.server");
        v.add("message.q");
        v.add("message.kh.personal");
        v.add("message.user.leaving.server");
        v.add("message.user.leaving.server.kicked");

        System.out.println("Found templates:------------------------------------\r\n");
        for (Iterator e = v.iterator(); e.hasNext(); ) {
            String cs = (String) e.next();
            System.out.print(cs);
            String propval = oldprops.getProperty(cs); 
            if (propval!=null) {
                for (int i = 0; i < (36 - cs.length()); i++)
                    System.out.print (" ");
                System.out.print("= ");
                System.out.print(propval);
                oldprops.remove(cs);
            }
            System.out.print("\r\n");
        }
        System.out.println("----------------------- Values from old-properties-file, which have not been found in source\r\n");
        for (Enumeration e = oldprops.keys(); e.hasMoreElements(); ) {
            String k = (String) e.nextElement();
            System.out.print(k);
            for (int i = 0; i < (36 - k.length()); i++)
                System.out.print (" ");
            System.out.print ("= ");
            System.out.println (oldprops.getProperty(k));
        }
    }
    
    public static void search(File f) {
        System.out.print("searching ");
        if (f.isDirectory()) {
            System.out.print("DIRECTORY: ");
            System.out.println (f.getName());
            File[] farr = f.listFiles();
            for (int i = 0; i<farr.length; i++)
                search (farr[i]);
        } else if (f.isFile()) {
            System.out.print("file: ");
            System.out.println (f.getName());
            String cntnt = null;
            try {
                FileReader fr = new FileReader(f);
                char fcnt[] = new char[(int) f.length()];
                int read = fr.read(fcnt);
                cntnt=String.copyValueOf(fcnt);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int idx;
            while ((idx = cntnt.indexOf(".msgTemplate")) != -1) {
                cntnt = cntnt.substring(idx+12).trim();
                if (!cntnt.startsWith("="))
                    continue;
                cntnt = cntnt.substring(1).trim();
                if (!cntnt.startsWith("\""))
                    continue;
                cntnt = cntnt.substring(1);
                int idx2 = cntnt.indexOf("\"");
                if (idx2 == -1)
                    continue;
                String cs = cntnt.substring(0, idx2);
                if (!v.contains(cs))
                    v.add(cs);
            }
        }
    }
}
