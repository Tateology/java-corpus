/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

public class NoCast implements Cast
{
  public final static NoCast instance = new NoCast();

  private NoCast()
  {}

  @Override
  public Object cast(Object o)
  {
    return o;
  }
}
