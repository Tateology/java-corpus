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
 *
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
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

public final class AddInstanceVisualNamePasswordPanel extends JPanel {

    /**
     * Creates new form AddInstanceVisualNamePasswordPanel
     */
    public AddInstanceVisualNamePasswordPanel() {
        initComponents();
        DocumentListener l = new MyDocListener();
        adminName.getDocument().addDocumentListener(l);
        adminPassword.getDocument().addDocumentListener(l);
    }

    public String getName() {
        return NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, 
                "StepName_EnterAdminLoginInfo");                                // NOI18N
    }
    
    void setUName(String uname) {
        adminName.setText(uname);
    }
    
    String getUName() {
        return adminName.getText();
    }
    
    void setPWord(String pw) {
        adminPassword.setText(pw);
    }

    String getPWord() {
        return new String(adminPassword.getPassword());
    }
    
    // Event Handler
    //
    private Set/*<ChangeListener.*/ listenrs = new HashSet/*<Changelisteners.*/();

    void addChangeListener(ChangeListener l) {
        synchronized (listenrs) {
            listenrs.add(l);
        }
    }
    
    void removeChangeListener(ChangeListener l ) {
        synchronized (listenrs) {
            listenrs.remove(l);
        }
    }

    private void fireChangeEvent() {
        Iterator it;
        synchronized (listenrs) {
            it = new HashSet(listenrs).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged (ev);
        }
    }
    
    class MyDocListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            fireChangeEvent();
        }

        public void removeUpdate(DocumentEvent e) {
            fireChangeEvent();
        }

        public void changedUpdate(DocumentEvent e) {
            fireChangeEvent();
        }
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        description = new javax.swing.JLabel();
        adminNameLabel = new javax.swing.JLabel();
        adminName = new javax.swing.JTextField();
        adminPasswordLabel = new javax.swing.JLabel();
        adminPassword = new javax.swing.JPasswordField();
        warning = new javax.swing.JLabel();
        spacingHack = new javax.swing.JLabel();

        jLabel1.setText("jLabel1");

        setMaximumSize(new java.awt.Dimension(400, 300));
        setPreferredSize(new java.awt.Dimension(0, 0));
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle"); // NOI18N
        description.setText(bundle.getString("TXT_namePasswordDescription")); // NOI18N
        description.setFocusable(false);
        description.setMaximumSize(null);
        description.setMinimumSize(null);
        description.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(description, gridBagConstraints);

        adminNameLabel.setLabelFor(adminName);
        org.openide.awt.Mnemonics.setLocalizedText(adminNameLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, "LBL_adminNameLabel")); // NOI18N
        adminNameLabel.setMaximumSize(null);
        adminNameLabel.setMinimumSize(null);
        adminNameLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 6);
        add(adminNameLabel, gridBagConstraints);

        adminName.setText(org.openide.util.NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, "VAL_adminName_NOI18N")); // NOI18N
        adminName.setMaximumSize(null);
        adminName.setMinimumSize(null);
        adminName.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(adminName, gridBagConstraints);
        adminName.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_adminName")); // NOI18N

        adminPasswordLabel.setLabelFor(adminPassword);
        org.openide.awt.Mnemonics.setLocalizedText(adminPasswordLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, "LBL_adminPasswordLabel")); // NOI18N
        adminPasswordLabel.setMaximumSize(null);
        adminPasswordLabel.setMinimumSize(null);
        adminPasswordLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 6);
        add(adminPasswordLabel, gridBagConstraints);

        adminPassword.setColumns(10);
        adminPassword.setMaximumSize(null);
        adminPassword.setMinimumSize(null);
        adminPassword.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(adminPassword, gridBagConstraints);
        adminPassword.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_adminPassword")); // NOI18N

        warning.setText(bundle.getString("TXT_namePasswordWarning")); // NOI18N
        warning.setFocusable(false);
        warning.setMaximumSize(null);
        warning.setMinimumSize(null);
        warning.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(warning, gridBagConstraints);

        spacingHack.setEnabled(false);
        spacingHack.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weighty = 1.0;
        add(spacingHack, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField adminName;
    private javax.swing.JLabel adminNameLabel;
    private javax.swing.JPasswordField adminPassword;
    private javax.swing.JLabel adminPasswordLabel;
    private javax.swing.JLabel description;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel spacingHack;
    private javax.swing.JLabel warning;
    // End of variables declaration//GEN-END:variables

}
