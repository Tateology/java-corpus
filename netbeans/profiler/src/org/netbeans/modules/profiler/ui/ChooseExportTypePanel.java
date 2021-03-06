/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.profiler.ui;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;

/**
 * Panel that allows the user to select which CPU data to export.
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "ChooseExportTypePanel_ChooseExportTypeDialogCaption=Select Data to Export",
    "ChooseExportTypePanel_CallChainsRadioText=Call Chains",
    "ChooseExportTypePanel_NamesInvocationsTimingsRadioText=Names, Invocations and Timings",
    "ChooseExportTypePanel_NamesInvocationsRadioText=Names and Invocations",
    "ChooseExportTypePanel_NamesOnlyRadioText=Names Only",
    "ChooseExportTypePanel_FlatProfileRadioText=Flat Profile"
})
public final class ChooseExportTypePanel extends javax.swing.JPanel implements ActionListener {
  public final static int CANCELLED = -1;
  public final static int FLAT = 0;
  public final static int CCT_1 = 1;
  public final static int CCT_2 = 2;
  public final static int CCT_3 = 3;
  private static final int PREFERRED_WIDTH = 400;

  private static ChooseExportTypePanel defaultInstance;

  public static int chooseExportType () {
    final ChooseExportTypePanel cetp = getDefault ();

    final DialogDescriptor dd = new DialogDescriptor(cetp, Bundle.ChooseExportTypePanel_ChooseExportTypeDialogCaption());
    final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
    d.setVisible(true);

    if (dd.getValue() == NotifyDescriptor.OK_OPTION) {
      if (cetp.callChainsRadio.isSelected()) {
        if (cetp.ccType1Radio.isSelected()) return CCT_1;
        else if (cetp.ccType2Radio.isSelected()) return CCT_2;
        else return CCT_3;

      } else return FLAT;
    }
    return CANCELLED;
  }
  
  private static ChooseExportTypePanel getDefault () {
    if (defaultInstance == null) defaultInstance = new ChooseExportTypePanel();
    return defaultInstance;
  }
  
  /** Creates new form ChooseExportTypePanel */
  private ChooseExportTypePanel () {
    initComponents ();
    callChainsRadio.setSelected (true);
    ccType1Radio.setSelected (true);
    setBorder (new EmptyBorder (12, 12, 12, 12));
    callChainsRadio.addActionListener(this);
    flatRadio.addActionListener(this);
  }

  public Dimension getPreferredSize() {
    final Dimension dim = super.getPreferredSize();
    return new Dimension (Math.max (PREFERRED_WIDTH, dim.width), dim.height);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        whichRadioGroup = new javax.swing.ButtonGroup();
        ccTypeRadioGroup = new javax.swing.ButtonGroup();
        callChainsRadio = new javax.swing.JRadioButton();
        ccType1Radio = new javax.swing.JRadioButton();
        ccType2Radio = new javax.swing.JRadioButton();
        ccType3Radio = new javax.swing.JRadioButton();
        flatRadio = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        whichRadioGroup.add(callChainsRadio);
        callChainsRadio.setText(Bundle.ChooseExportTypePanel_CallChainsRadioText());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(callChainsRadio, gridBagConstraints);

        ccTypeRadioGroup.add(ccType1Radio);
        ccType1Radio.setText(Bundle.ChooseExportTypePanel_NamesInvocationsTimingsRadioText());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 0, 0);
        add(ccType1Radio, gridBagConstraints);

        ccTypeRadioGroup.add(ccType2Radio);
        ccType2Radio.setText(Bundle.ChooseExportTypePanel_NamesInvocationsRadioText());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 0, 0);
        add(ccType2Radio, gridBagConstraints);

        ccTypeRadioGroup.add(ccType3Radio);
        ccType3Radio.setText(Bundle.ChooseExportTypePanel_NamesOnlyRadioText());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 0, 0);
        add(ccType3Radio, gridBagConstraints);

        whichRadioGroup.add(flatRadio);
        flatRadio.setText(Bundle.ChooseExportTypePanel_FlatProfileRadioText());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(flatRadio, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton callChainsRadio;
    private javax.swing.JRadioButton ccType1Radio;
    private javax.swing.JRadioButton ccType2Radio;
    private javax.swing.JRadioButton ccType3Radio;
    private javax.swing.ButtonGroup ccTypeRadioGroup;
    private javax.swing.JRadioButton flatRadio;
    private javax.swing.ButtonGroup whichRadioGroup;
    // End of variables declaration//GEN-END:variables

  /**
   * Invoked when an action occurs.
   */
  public  void actionPerformed(final ActionEvent e) {
    final boolean cctSelected = (e.getSource() == callChainsRadio);
    ccType1Radio.setEnabled (cctSelected);
    ccType2Radio.setEnabled (cctSelected);
    ccType3Radio.setEnabled (cctSelected);
  }
}
