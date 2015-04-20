/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

import java.util.*;

@SuppressWarnings("rawtypes")
public class ConflictException extends Exception
{
  private Set conflictualProductions;

  public ConflictException(Set conflictualProductions, String message)
  {
    super(message);
    this.conflictualProductions = conflictualProductions;
  }

  public Set getConflictualProductions()
  {
    return conflictualProductions;
  }
}
