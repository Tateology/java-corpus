/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;


public class BooleanCast implements Cast
{
  public final static BooleanCast instance = new BooleanCast();

  private BooleanCast()
  {}

  @Override
  public  Object cast(Object o)
  {
    return (Boolean) o;
  }
}
