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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.openide.util.NbPreferences;

/**
 * based on org.netbeans.modules.java.hints.introduce.IntroduceMethodPanel
 * @author Jan Lahoda
 * @author Vladimir Voskresensky
 */
public class IntroduceMethodPanel extends javax.swing.JPanel {
    
    public static final int INIT_METHOD = 1;
    public static final int INIT_FIELD = 2;
    public static final int INIT_CONSTRUCTORS = 4;
    
    private static final int ACCESS_PUBLIC = 1;
    private static final int ACCESS_PROTECTED = 2;
    private static final int ACCESS_PRIVATE = 4;
    
    private JButton btnOk;
    
    public IntroduceMethodPanel(String name) {
        initComponents();
        
        this.name.setText(name);
        if ( name != null && name.trim().length() > 0 ) {
            this.name.setCaretPosition(name.length());
            this.name.setSelectionStart(0);
            this.name.setSelectionEnd(name.length());
        }
        
        Preferences pref = getPreferences();
        
        int accessModifier = pref.getInt( "accessModifier", ACCESS_PRIVATE ); //NOI18N
        switch( accessModifier ) {
        case ACCESS_PUBLIC:
            accessPublic.setSelected( true );
            break;
        case ACCESS_PROTECTED:
            accessProtected.setSelected( true );
            break;
        case ACCESS_PRIVATE:
            accessPrivate.setSelected( true );
            break;
        }
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( IntroduceFieldPanel.class ).node( "introduceField" ); //NOI18N
    }
    
    public void setOkButton( JButton btn ) {
        this.btnOk = btn;
        btnOk.setEnabled(((ErrorLabel)errorLabel).isInputTextValid());
    }
    
    private JLabel createErrorLabel() {
        ErrorLabel.Validator validator = new ErrorLabel.Validator() {

            @Override
            public String validate(String text) {
                if( null == text 
                    || text.length() == 0 ) return "";
                if (!CndLexerUtilities.isCppIdentifier(text))
                    return getDefaultErrorMessage( text );
                return null;
            }
        };
        
        final ErrorLabel eLabel = new ErrorLabel( name.getDocument(), validator );
        eLabel.addPropertyChangeListener(  ErrorLabel.PROP_IS_VALID, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                btnOk.setEnabled(eLabel.isInputTextValid());
            }
        });
        return eLabel;
    }
    
    String getDefaultErrorMessage( String inputText ) {
        return "'" + inputText +"' is not a valid identifier"; // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        initilizeIn = new javax.swing.ButtonGroup();
        accessGroup = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        lblAccess = new javax.swing.JLabel();
        accessPublic = new javax.swing.JRadioButton();
        accessProtected = new javax.swing.JRadioButton();
        accessPrivate = new javax.swing.JRadioButton();
        errorLabel = createErrorLabel();

        lblName.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceMethodPanel.class).getString("LBL_Name")); // NOI18N

        name.setColumns(20);

        lblAccess.setLabelFor(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(lblAccess, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_Access")); // NOI18N

        accessGroup.add(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(accessPublic, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_public")); // NOI18N
        accessPublic.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPublic.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessProtected);
        org.openide.awt.Mnemonics.setLocalizedText(accessProtected, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_protected")); // NOI18N
        accessProtected.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessProtected.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessPrivate);
        accessPrivate.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(accessPrivate, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_private")); // NOI18N
        accessPrivate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPrivate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAccess)
                            .addComponent(lblName))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(accessPublic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessProtected)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessPrivate))))
                    .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccess)
                    .addComponent(accessPublic)
                    .addComponent(accessProtected)
                    .addComponent(accessPrivate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 137, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addContainerGap())
        );

        name.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AN_IntrMethod_Name")); // NOI18N
        name.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Name")); // NOI18N
        accessPublic.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Public")); // NOI18N
        accessProtected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Protected")); // NOI18N
        accessPrivate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Private")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JRadioButton accessPrivate;
    private javax.swing.JRadioButton accessProtected;
    private javax.swing.JRadioButton accessPublic;
    private javax.swing.JLabel errorLabel;
    private javax.swing.ButtonGroup initilizeIn;
    private javax.swing.JLabel lblAccess;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField name;
    // End of variables declaration//GEN-END:variables
    
    public String getMethodName() {
        if (methodNameTest != null) return methodNameTest;
        return this.name.getText();
    }
    
    public CsmVisibility getAccess() {
        if (accessTest != null) return accessTest;
        CsmVisibility out;
        int val;
        if( accessPublic.isSelected() ) {
            val = ACCESS_PUBLIC;
            out = CsmVisibility.PUBLIC;
        } else if( accessProtected.isSelected() ) {
            val = ACCESS_PROTECTED;
            out = CsmVisibility.PROTECTED;
        } else {
            val = ACCESS_PRIVATE;
            out = CsmVisibility.PRIVATE;
        }
        getPreferences().putInt( "accessModifier", val ); //NOI18N
        return out;
    }
    
    //For tests:
    private String methodNameTest;
    private CsmVisibility accessTest;
    
    void setAccess(CsmVisibility access) {
        this.accessTest = access;
    }

    void setMethodName(String methodName) {
        this.methodNameTest = methodName;
    }

}