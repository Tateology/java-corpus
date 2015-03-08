/**
 * Copyright (C) 2005 manfred andres
 * Created: 07.03.2005 (16:50:46)
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
package freecs.util.logger;


/**
 * @author manfred andres
 *
 */
public class LogWriterBenchmark implements Runnable {
    public static int threads=10;
    public static int msgsPerSecond=10;
    
    private final String[] logPaths;
    private final int id;
    
    public LogWriterBenchmark(String[] filePaths, int id) {
        this.logPaths=filePaths;
        this.id = id;
    }

    public static void main(String args[]) {
        for (int i = 0; i < args.length; i++) {
            String curr = args[i];
            if (curr.startsWith("-threads="))
                threads = Integer.parseInt(curr.substring(9));
            else if (curr.startsWith("-messages="))
                msgsPerSecond = Integer.parseInt(curr.substring(10));
        }
        String[] filePaths = new String[5];
        filePaths[0]="d:/var/log/freecs/freecs1.log";
        filePaths[1]="d:/var/log/freecs/freecs2.log";
        filePaths[2]="d:/var/log/freecs/freecs3.log";
        filePaths[3]="d:/var/log/freecs/freecs4.log";
        filePaths[4]="d:/var/log/freecs/freecs5.log";

        ThreadGroup tg = new ThreadGroup("LogWriterBenchmarkers");
        tg.setDaemon(true);
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(tg, new LogWriterBenchmark(filePaths, i));
            t.start();
        }
        try {
            Thread.sleep(60000*60); // run for one hour
        } catch (InterruptedException ie) { }
        LogWriter.instance.stopLogging();
    }

    public void run() {
        long counter = 0;
        long defaultSleepTime = Math.round(1000/msgsPerSecond);
        long sleepTime = 0;
        while (true) {
            long start = System.currentTimeMillis();
            String path = logPaths[(int) Math.round(Math.random()*(logPaths.length-1))];
            StringBuffer sb = new StringBuffer();
            sb.append (counter);
            sb.append (". message of the LogWriter started with id ");
            sb.append (id);
            sb.append (" having a current sleepTime of ");
            sb.append (sleepTime);
            sb.append (" millis");
            LogWriter.instance.addLogMessage(path, sb.toString());
            counter++;
            sleepTime = (defaultSleepTime - (System.currentTimeMillis() - start));
            if (sleepTime > 0) try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) { }
        }
    }
}
