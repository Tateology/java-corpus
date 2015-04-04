/*
 * @(#)GridBagConstraints2.java  05/26/2000
 *
 * Copyright (c) 2000-2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Visit eTeks web site for up-to-date versions of this file and other
 * Java tools and tutorials : http://www.eteks.com/
 */
package com.eteks.tools.awt;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Same as <code>GridBagConstraints</code> with a simpler class constructor (this constructor exists only in Java2).
 *
 * @version   1.1
 * @author    Emmanuel Puybaret
 * @since     PJA1.1
 */
public class GridBagConstraints2 extends GridBagConstraints
{
  public GridBagConstraints2 (int gridx, int gridy,
                              int gridwidth, int gridheight,
                              double weightx, double weighty,
                              int anchor, int fill,
                              Insets insets, int ipadx, int ipady)
  {
    this.gridx = gridx;
    this.gridy = gridy;
    this.gridwidth = gridwidth;
    this.gridheight = gridheight;
    this.fill = fill;
    this.ipadx = ipadx;
    this.ipady = ipady;
    this.insets = insets;
    this.anchor  = anchor;
    this.weightx = weightx;
    this.weighty = weighty;
  }
};
