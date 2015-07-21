/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * ContactListCellRenderer.java
 *
 * Created on Aug 3, 2009, 11:23:06 AM
 */
package org.netbeans.modules.kenai.collab.chat;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.team.commons.ColorManager;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Becicka
 */
public class ContactListCellRenderer extends javax.swing.JPanel implements ListCellRenderer {

    /** Creates new form ContactListCellRenderer */
    public ContactListCellRenderer() {
        initComponents();
        setOpaque(true);
        buddyLabel.setOpaque(true);
        messageLabel.setOpaque(true);
        kenaiName.setOpaque(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buddyLabel = HtmlRenderer.createLabel();
        messageLabel = new javax.swing.JLabel();
        kenaiName = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 5));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(buddyLabel, gridBagConstraints);

        messageLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(messageLabel, gridBagConstraints);

        kenaiName.setForeground(java.awt.Color.gray);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(kenaiName, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        buddyLabel.setText(value.toString());
        ContactListItem item = (ContactListItem) value;
        buddyLabel.setBorder(new EmptyBorder(0,item.getIcon()==null?22:3,0,0));
        buddyLabel.setIcon(item.getIcon());
        if (item.hasMessages()) {
            messageLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/newmessage.png", true)); // NOI18N
            messageLabel.setBorder(new EmptyBorder(0,3,0,3));
        } else {
            messageLabel.setIcon(null);
            messageLabel.setBorder(new EmptyBorder(0,3,16,19));
        }
        if (KenaiManager.getDefault().getKenais().size()>1) {
            kenaiName.setText(item.getKenaiName());
        } else {
            kenaiName.setText("");
        }

        if (isSelected) {
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
            buddyLabel.setBackground(list.getSelectionBackground());
            buddyLabel.setForeground(list.getSelectionForeground());
            messageLabel.setBackground(list.getSelectionBackground());
            messageLabel.setForeground(list.getSelectionForeground());
            kenaiName.setBackground(list.getSelectionBackground());
            kenaiName.setForeground(list.getSelectionForeground());
        } else {
            Color bg = ColorManager.getDefault().getDefaultBackground();
            this.setBackground(bg);
            this.setForeground(list.getForeground());
            buddyLabel.setBackground(bg);
            buddyLabel.setForeground(list.getForeground());
            messageLabel.setBackground(bg);
            messageLabel.setForeground(list.getForeground());
            kenaiName.setBackground(bg);
            kenaiName.setForeground(Color.gray);
        }
        this.setPreferredSize(new Dimension(10, getPreferredSize().height));
        return this;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel buddyLabel;
    private javax.swing.JLabel kenaiName;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables
}