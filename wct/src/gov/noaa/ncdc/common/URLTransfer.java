////////////////////////////////////////////////////////////////////////
/*
     FILE: URLTransfer.java
  PURPOSE: To perform data transfers from a URL to a local file.
   AUTHOR: Peter Hollemans
     DATE: 2002/02/16
  CHANGES: 2003/10/26, PFH
           - added to CoastWatch package
           - modified constructor
           2003/11/22, PFH, fixed Javadoc comments

  CoastWatch Software Library and Utilities
  Copyright 1998-2003, USDOC/NOAA/NESDIS CoastWatch

 */
////////////////////////////////////////////////////////////////////////

// Package
// -------
package gov.noaa.ncdc.common;
//package noaa.coastwatch.net;

// Imports
// -------
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

/**
 * The <code>URLTransfer</code> class initiates a connection to an
 * Internet host specified by a URL and downloads the content provided
 * by the URL.  The URL may contain username and password information,
 * in which case the URL is opened using a
 * <code>PasswordAuthentication</code> object as the default
 * authenticator for the connection.
 */
public class URLTransfer
extends DataTransfer {

    ////////////////////////////////////////////////////////////

    private URLConnection conn; 

    
    /** 
     * Creates a new URL transfer with the specified parameters.
     *
     * @param inputUrl the URL to access for content.
     * @param output the output stream for the URL contents.
     *
     * @throws IOException if an error occurred accessing the URL stream.
     */   
    public URLTransfer (
            URL inputUrl,
            OutputStream output
    ) throws IOException {

        // Check for authentication 
        // ------------------------
        String userInfo = inputUrl.getUserInfo();
        if (userInfo != null) {
            String[] array = userInfo.split (":");
            final String user = array[0];
            final String pass = array[1];
            Authenticator.setDefault (new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication () {
                    return (new PasswordAuthentication (user, pass.toCharArray()));
                } // getPasswordAuthentication
            });
        } // if

        // Set input and output streams
        // ----------------------------
        this.conn = inputUrl.openConnection();
        InputStream input = conn.getInputStream();
//        InputStream input = inputUrl.openStream();
        setStreams (input, output, 8192);

    } // URLTransfer constructor



    /**
     * Gets the open URLConnection object.  This could be closed if .close() has been called.
     * @return
     */
    public URLConnection getURLConnection() {
        return conn;
    }

    ////////////////////////////////////////////////////////////

} // URLTransfer class

////////////////////////////////////////////////////////////////////////
