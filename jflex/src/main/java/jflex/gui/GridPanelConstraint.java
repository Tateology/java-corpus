/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * JFlex 1.6                                                               *
 * Copyright (C) 1998-2014  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package jflex.gui;

import java.awt.Component;

/**
 * Constraints for layout elements of GridLayout
 *
 * @author Gerwin Klein
 * @version JFlex 1.6, $Revision: 815 $, $Date: 2014-05-19 03:07:52 +1000 (Mon, 19 May 2014) $
 */
public class GridPanelConstraint {

  int x, y, width, height, handle;
  Component component;

  public GridPanelConstraint(int x, int y, int width, int height, 
                             int handle, Component component) {
    this.x         = x;
    this.y         = y;
    this.width     = width;
    this.height    = height;
    this.handle    = handle;
    this.component = component;
  }
}
