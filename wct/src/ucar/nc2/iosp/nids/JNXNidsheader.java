// $Id: Nidsheader.java,v 1.11 2005/04/27 16:22:55 caron Exp $
/*
 * Copyright 1997-2004 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, strlenwrite to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ucar.nc2.iosp.nids;

import java.io.IOException;

//  ** STEVE ANSARI ADDED 'PUBLIC' ACCESS TO METHOD
public class JNXNidsheader extends Nidsheader {
  
  
  
  // STEVE ANSARI ADDITION
  public byte[] readZLibNIDS(ucar.unidata.io.RandomAccessFile raf) throws IOException {

    int      hedsiz;                  /* NEXRAD header size            */
    int      rc;                      /* function return status        */
    int      hoff = 0;
    int      type;
    int      zlibed = 0;
    boolean  isZ = false;
    int      encrypt = 0;
    long     actualSize = 0;
    int      readLen = 0;

    actualSize = raf.length();
    int pos = 0;
    raf.seek(pos);

    // Read in the whole contents of the NEXRAD Level III product since
    // some product require to go through the whole file to build the  struct of file.

    readLen = (int)actualSize;

    byte[] b = new byte[readLen];
    rc = raf.read(b);
    if ( rc != readLen )
    {
        System.out.println(" error reading nids product header");
    }

    //Get product message header into a string for processing
    String pib = new String(b, 0, 100);
    type = 0;
    pos = pib.indexOf ( "\r\r\n" );
    while ( pos != -1 ) {
        hoff = (int) pos + 3;
        type++;
        pos = pib.indexOf ( "\r\r\n" , pos+1);
    }
    raf.seek(hoff);

    // Test the next two bytes to see if the image portion looks like
    // it is zlib-compressed.
    byte[] b2 = new byte[2];
    byte[] b4 = new byte[4];
    System.arraycopy(b, hoff, b2, 0, 2);

    zlibed = isZlibHed( b2 );
    if ( zlibed == 0)
      encrypt = IsEncrypt( b2 );

   // process product description for station ID
    byte[] b3 = new byte[3];
    //byte[] uncompdata = null;

    switch ( type ) {
      case 0:
        System.out.println( "ReadNexrInfo:: Unable to seek to ID ");
        break;
      case 1:
      case 2:
      case 3:
      case 4:
        System.arraycopy(b, hoff - 6, b3, 0, 3);
        stationId  = new String(b3);
        break;

      default:
        break;
    }

    
    byte[] uncompdata;
    if ( zlibed == 1 ) {
          isZ = true;
          uncompdata = GetZlibedNexr( b, readLen,  hoff );
          //uncompdata = Nidsiosp.readCompData(hoff, 160) ;
          if ( uncompdata == null ) {
            System.out.println( "ReadNexrInfo:: error uncompressing image" );
          }
    }
    else {
         uncompdata = new byte[b.length-hoff];
         System.arraycopy(b, hoff, uncompdata, 0, b.length- hoff);
    }
    
    
    
    return uncompdata;
  }  
  // END STEVE ANSARI ADDITION ----------------------------------------------------
  
  
  

  /*
  ** Name:       IsZlibed
  **
  ** Purpose:    Check a two-byte sequence to see if it indicates the start of
  **             a zlib-compressed buffer
  **
  */

   public int isZlibHed( byte[] buf ){
    short b0 = convertunsignedByte2Short(buf[0]);
    short b1 = convertunsignedByte2Short(buf[1]);

    if ( (b0 & 0xf) == Z_DEFLATED ) {
      if ( (b0 >> 4) + 8 <= DEF_WBITS ) {
        if ( (((b0 << 8) + b1) % 31)==0 ) {
          return 1;
        }
      }
    }

    return 0;

  }


}
