////////////////////////////////////////////////////////////////////////
/*
     FILE: DataTransferListener.java
  PURPOSE: To handle data transfer events.
   AUTHOR: Peter Hollemans
     DATE: 2002/02/16
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
import java.util.EventListener;

/**
 * A data transfer listener receives data transfer events and performs
 * some appripriate action in response.  Data transfer events are used
 * to signal the details of a data transfer, such as starting,
 * transfer progress, and ending.
 */
public interface DataTransferListener
  extends EventListener {

  ////////////////////////////////////////////////////////////

  /** Responds to a data transfer starting. */
  public void transferStarted (
    DataTransferEvent event
  );

  ////////////////////////////////////////////////////////////

  /** Responds to a data transfer in progress. */
  public void transferProgress (
    DataTransferEvent event
  );

  ////////////////////////////////////////////////////////////

  /** Responds to a data transfer ending. */
  public void transferEnded (
    DataTransferEvent event
  );

  ////////////////////////////////////////////////////////////

  /** Responds to a data transfer error. */
  public void transferError (
    DataTransferEvent event
  );

  ////////////////////////////////////////////////////////////

} // DataTransferListener class

////////////////////////////////////////////////////////////////////////
