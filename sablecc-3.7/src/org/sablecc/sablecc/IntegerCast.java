/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;


public class IntegerCast implements Cast
{
  public final static IntegerCast instance = new IntegerCast();

  private IntegerCast()
  {}

  @Override
  public  Object cast(Object o)
  {
    return (Integer) o;
  }
}
