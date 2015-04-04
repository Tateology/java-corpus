package gov.noaa.ncdc.wct.ui;

/*
 * @(#)MyAppSplash.java  1.2  2003-06-01
 *
 * Copyright (c) 1999-2003 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland
 * All rights reserved.
 *
 * This material is provided "as is", with absolutely no warranty expressed
 * or implied. Any use is at your own risk.
 *
 * Permission to use or copy this software is hereby granted without fee,
 * provided this copyright notice is retained on all copies.
 */
import gov.noaa.ncdc.common.SplashWindow;
import gov.noaa.ncdc.wct.WCTConstants;

import java.awt.Frame;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
/**
 * Demonstrates how to displays a splash window during startup of an application.
 * Adapt this class to your liking but keep it small.
 *
 * @author Original Author: Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * @version Original Version: 1.2  2003-06-01 Revised.
 */
public abstract class GenericViewerSplash {

    
    public static void startApplication(String[] args, String classToLaunch, String splashImagePath) {
    	System.out.println("ViewerSplash: STARTING....");
    	
    	
    	JVMPropertyChecker propChecker = new JVMPropertyChecker();
    	propChecker.checkUserHome();
    	
    	
    	
    	
    	
    	
    	
    	
		if (WCTUiUtils.classExists("org.eclipse.swt.SWT")) {
			try {
				NativeInterface.initialize();
				NativeInterface.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


    	
    	
        try {
        	
//            Helpful for debugging classpath issues
//            ClassLoader cl = ClassLoader.getSystemClassLoader();            
//            URL[] urls = ((URLClassLoader)cl).getURLs();     
//            for(URL url: urls){
//            	System.out.println(url.getFile());
//            }
//            
//            Class klass = org.slf4j.Logger.class;
//            URL location = klass.getResource('/'+klass.getName().replace('.', '/')+".class");
//            System.out.println(klass + ": "+location);
            
            System.getProperties().list(System.out);

            System.out.println("ViewerSplash: STARTING....");
            System.out.println("ViewerSplash: ARGS["+args.length+"]="+Arrays.deepToString(args));



            // NOTE: The splash window should appear as early as possible.
            //       The code provided here uses Reflection to avoid time
            //       consuming class loading before the splash window is
            //       constructed.


            // Read the image data and open the splash screen
            // ----------------------------------------------

            // TO DO: Replace 'splash.gif' with the file name of your splash image.
            Frame splashFrame = null;
            URL imageURL = GenericViewerSplash.class.getResource(splashImagePath);
            if (imageURL != null) {
                splashFrame = SplashWindow.splash(
                        Toolkit.getDefaultToolkit().createImage(imageURL)
                );
            } else {
                System.err.println("WCTViewerSplash:  Splash image not found");
            }

            // NOTE: If you run this application using java -verbose
            //       you should not see any of your application classes
            //       being loaded by the JVM until this point (except
            //       for this class and the SplashWindow class).
            //System.out.println("Splash screen displayed");


            // Call the main method of the application using Reflection.
            // ---------------------------------------------------------
            try {


                Class.forName(classToLaunch)
                .getMethod("main", new Class[] {String[].class})
                .invoke(null, new Object[] {args});
            } catch (Throwable e) {
                e.printStackTrace();
                System.err.flush();
                //            System.exit(10);
                throw new Exception(e);
            }

            // Dispose the splash window by disposing its parent frame
            // -------------------------------------------------------
//            System.out.println("hiding and disposing splash frame");
            if (splashFrame != null) splashFrame.setVisible(false);
            if (splashFrame != null) splashFrame.dispose();


        } catch (Exception e) {
            e.printStackTrace();

            // Dump to a log file in cache
            String exceptionMessage = e.toString();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            try {
                new File(WCTConstants.getInstance().getCacheLocation()).mkdirs();


                
                BufferedWriter bw = new BufferedWriter(new FileWriter(
                        new File(WCTConstants.getInstance().getCacheLocation() + File.separator + "wct-crash-"+sdf.format(new Date())+".log")));
                
                StringWriter sw2 = new StringWriter();
                System.getProperties().list(new PrintWriter(sw2));
                String sysProps = sw2.toString();

                
                bw.write(new Date().toString());
                bw.newLine();
                bw.write(exceptionMessage);
                bw.newLine();
                bw.newLine();
                bw.write(stackTrace);
                bw.newLine();
                bw.newLine();
                bw.write(sysProps);
                
                bw.close();
            } catch (Exception ex) {
                System.err.println("ERROR WRITING CRASH LOG TO CACHE ("+WCTConstants.getInstance().getCacheLocation()+")");
            }

        }
        
        
		if ( WCTUiUtils.classExists("org.eclipse.swt.SWT")) {
			try {
	    		NativeInterface.runEventPump();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
		}
    }
}

