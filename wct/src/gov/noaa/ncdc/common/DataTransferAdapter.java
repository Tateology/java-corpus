////////////////////////////////////////////////////////////////////////
/*
     FILE: DataTransferAdapter.java
  PURPOSE: To provide default implementations for a data transfer listener.
   AUTHOR: Peter Hollemans
     DATE: 2002/02/17
  CHANGES: 2003/10/26, PFH, added to the Coastwatch package

  CoastWatch Software Library and Utilities
  Copyright 1998-2003, USDOC/NOAA/NESDIS CoastWatch

*/
////////////////////////////////////////////////////////////////////////

// Package
// -------
//package noaa.coastwatch.io;
package gov.noaa.ncdc.common;

// Imports
// -------

/**
 * A data transfer adapter provides default implementations for a data
 * transfer listener.
 */
public class DataTransferAdapter
  implements DataTransferListener {

  ////////////////////////////////////////////////////////////

  public void transferStarted (DataTransferEvent event) { }

  ////////////////////////////////////////////////////////////

  public void transferProgress (DataTransferEvent event) { }

  ////////////////////////////////////////////////////////////

  public void transferEnded (DataTransferEvent event) { } 

  ////////////////////////////////////////////////////////////

  public void transferError (DataTransferEvent event) { }

  ////////////////////////////////////////////////////////////

} // DataTransferAdapter class

////////////////////////////////////////////////////////////////////////
