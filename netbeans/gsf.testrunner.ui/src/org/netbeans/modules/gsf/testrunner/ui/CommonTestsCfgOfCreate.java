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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.gsf.testrunner.ui;

import org.netbeans.modules.gsf.testrunner.api.SelfResizingPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.plugin.CommonSettingsProvider;
import org.netbeans.modules.gsf.testrunner.plugin.CommonTestUtilProvider;
import org.netbeans.modules.gsf.testrunner.plugin.GuiUtilsProvider;
import org.netbeans.modules.gsf.testrunner.plugin.RootsProvider;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfigurationProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/**
 *
 * @author  vstejskal
 * @author  Marian Petras
 */
@SuppressWarnings("serial")
public class CommonTestsCfgOfCreate extends SelfResizingPanel implements ChangeListener {
    
    /**
     * nodes selected when the Create Tests action was invoked
     */
    private  FileObject[] activatedFOs;
    /** whether the tests will be created for multiple classes */
    private  boolean multipleClasses;
    /** whether a single package/folder is selected */
    private boolean singlePackage;
    /** whether a single class is selected */
    private boolean singleClass;
    /** test class name specified in the form (or <code>null</code>) */
    private String testClassName;
    /** registered change listeners */
    private List<ChangeListener> changeListeners;
    /** */
    private String initialMessage;
    /** */
    private List<String> testingFrameworks;
//    public static final String JUNIT_TEST_FRAMEWORK = "JUnit";             //NOI18N
//    public static final String TESTNG_TEST_FRAMEWORK = "TestNG";             //NOI18N
    private String selectedTestingFramework = null;
    public static final String PROP_TESTING_FRAMEWORK = "testingFramework";  //NOI18N
    
    /**
     * is at least one target folder/source group available?
     *
     * @see  #isAcceptable()
     */
    private boolean hasTargetFolders = false;

    /**
     * is the entered class name non-empty and valid?
     *
     * @see  #isAcceptable()
     */
    private boolean classNameValid;
    
    /**
     * is the current form contents acceptable?
     *
     * @see  #isAcceptable()
     */
    private boolean isAcceptable;
    
    /**
     * is the current project a j2me project?
     * 
     */
    private boolean isJ2meProject;
    
    /** layer index for a message about an empty set of target folders */
    private static final int MSG_TYPE_NO_TARGET_FOLDERS = 0;
    /** layer index for a message about invalid class name */
    private static final int MSG_TYPE_CLASSNAME_INVALID = 1;
    /** layer index for a message about non-default class name */
    private static final int MSG_TYPE_CLASSNAME_NOT_DEFAULT = 2;
    /** layer index for a message about modified files */
    private static final int MSG_TYPE_MODIFIED_FILES = 3;
    /** layer index for a message about j2me project type */
    private static final int MSG_TYPE_J2ME_PROJECT = 4;
    /** layer index for a message about updating a single test class */
    private static final int MSG_TYPE_UPDATE_SINGLE_TEST_CLASS = 5;
    /** layer index for a message about updating all test classes */
    private static final int MSG_TYPE_UPDATE_ALL_TEST_CLASSES = 6;
    /** */
    private MessageStack msgStack = new MessageStack(7);

    private Collection<SourceGroup> createdSourceRoots = new ArrayList<SourceGroup>();
    private final JPanel jPanel = new JPanel();
    
    public CommonTestsCfgOfCreate(FileObject[] activatedFOs) {
        assert (activatedFOs != null) && (activatedFOs.length != 0);
        this.activatedFOs = activatedFOs;
    }

    /**
     * Creates a configuration panel.
     *
     * @param nodes  nodes selected when the Create Tests action was invoked
     * @param isShowMsgFilesWillBeSaved if {@code true} then a warning message
     *        like "Warning: All modified files will be saved." will be
     *        displayed on the panel, otherwise (i.e. if {@code false}) then
     *        the message won't be displayed.
     */
    @NbBundle.Messages({"MSG_J2ME_PROJECT_TYPE=Tests cannot be created for this project type. Please use the New File wizard to create a JMUnit test instead.",
    "MSG_MODIFIED_FILES=Warning: All modified files will be saved."
    })
    public void createCfgPanel(boolean isShowMsgFilesWillBeSaved, boolean isJ2meProject) {
//        assert (nodes != null) && (nodes.length != 0);
//        this.nodes = nodes;
        multipleClasses = checkMultipleClasses();
        this.isJ2meProject = isJ2meProject;
        
        initBundle();
        try {
            initComponents();
            if(isShowMsgFilesWillBeSaved) {
                String msg = Bundle.MSG_MODIFIED_FILES();
                setMessage(msg, MSG_TYPE_MODIFIED_FILES);
            }
            if(isJ2meProject) {
                setMessage(Bundle.MSG_J2ME_PROJECT_TYPE(), MSG_TYPE_J2ME_PROJECT);
            }
            setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 11));
            addAccessibleDescriptions();
            initializeCheckBoxStates();
            fillFormData();
            checkAcceptability();
            
            /*
             * checkAcceptability() must not be called
             *        before initializeCheckBoxStates() and fillFormData()
             * setupUserInteraction must not be called
             *        before initializeCheckBoxStates()
             */
            
        } finally {
            unlinkBundle();
        }
    }
    
    @NbBundle.Messages({
        "CommonTestsCfgOfCreate_AD=Create Tests",
        "CommonTestsCfgOfCreate_clsName_toolTip=Name of the test class",
        "CommonTestsCfgOfCreate_clsName_AN=Test Class Name",
        "CommonTestsCfgOfCreate_clsName_AD=Name of the test class to be created",
        "CommonTestsCfgOfCreate_location_toolTip=Target source root for the test class",
        "CommonTestsCfgOfCreate_location_AN=Test Class Location",
        "CommonTestsCfgOfCreate_location_AD=Target source root for the test class",
        "CommonTestsCfgOfCreate_framework_toolTip=Testing framework for the test class",
        "CommonTestsCfgOfCreate_framework_AN=Test Class Testing framework",
        "CommonTestsCfgOfCreate_framework_AD=Testing framework for the test class"
    })
    private void addAccessibleDescriptions() {
        
        // window
        this.getAccessibleContext().setAccessibleDescription(Bundle.CommonTestsCfgOfCreate_AD());
        
        // text-field and combo-box
        
        if (this.tfClassName != null) {
            this.tfClassName.setToolTipText(
                  Bundle.CommonTestsCfgOfCreate_clsName_toolTip());
            this.tfClassName.getAccessibleContext().setAccessibleName(
                  Bundle.CommonTestsCfgOfCreate_clsName_AN());
            this.tfClassName.getAccessibleContext().setAccessibleDescription(
                  Bundle.CommonTestsCfgOfCreate_clsName_AD());
        }
        
        this.cboxLocation.setToolTipText(
                Bundle.CommonTestsCfgOfCreate_location_toolTip());
        this.cboxLocation.getAccessibleContext().setAccessibleName(
                Bundle.CommonTestsCfgOfCreate_location_AN());
        this.cboxLocation.getAccessibleContext().setAccessibleDescription(
                Bundle.CommonTestsCfgOfCreate_location_AD());
        
        this.cboxLocation.setToolTipText(
                Bundle.CommonTestsCfgOfCreate_framework_toolTip());
        this.cboxLocation.getAccessibleContext().setAccessibleName(
                Bundle.CommonTestsCfgOfCreate_framework_AN());
        this.cboxLocation.getAccessibleContext().setAccessibleDescription(
                Bundle.CommonTestsCfgOfCreate_framework_AD());
        }
        
    /**
     * Checks whether multiple classes may be selected.
     * It also detects whether exactly one package/folder or exactly one class
     * is selected and sets values of variables {@link #singlePackage}
     * and {@link #singleClass} accordingly.
     *
     * @return  <code>false</code> if there is exactly one node selected
     *          and the node represents a single <code>DataObject</code>,
     *          not a folder or another <code>DataObject</code> container;
     *          <code>true</code> otherwise
     */
    private boolean checkMultipleClasses() {
        if (activatedFOs.length > 1) {
            return true;
        }
        
        singleClass = false;
        FileObject fo = activatedFOs[0];
        if (fo != null) {
            singlePackage = fo.isFolder();
            singleClass = fo.isData();
        }
        return !singleClass;
    }

    @NbBundle.Messages({"MSG_UPDATE_SINGLE_TEST_CLASS=The existing test class will be updated.",
    "MSG_UPDATE_ALL_TEST_CLASSES=Any existing test classes will be updated."})
    private void checkUpdatingExistingTestClass() {
        if(tfClassName == null) {
            setMessage(Bundle.MSG_UPDATE_ALL_TEST_CLASSES(), MSG_TYPE_UPDATE_ALL_TEST_CLASSES);
        } else {
            FileObject locationFO = getTargetFolder();
            if (locationFO != null) {
                String targetFolderPath = FileUtil.toFile(locationFO).getAbsolutePath();
                String className = tfClassName.getText();
                int index = className.lastIndexOf('.'); // index == -1 most probably means class is under <default package>
                String packageName = index == -1 ? "" : className.substring(0, index);
                String fileName = index == -1 ? className : className.substring(index + 1);
                FileObject testFolderFO = FileUtil.toFileObject(new File(targetFolderPath.concat(File.separator).concat(packageName.replace(".", "/"))));
                if(testFolderFO != null) {
                    for(FileObject testClassFO : testFolderFO.getChildren()) {
                        if(testClassFO.getName().equals(fileName)) {
                            setMessage(Bundle.MSG_UPDATE_SINGLE_TEST_CLASS(), MSG_TYPE_UPDATE_SINGLE_TEST_CLASS);
                            return;
                        }
                    }
                }
            }
            setMessage(null, MSG_TYPE_UPDATE_SINGLE_TEST_CLASS);
        }
    }
    
    /**
     * Displays a configuration dialog and updates JUnit options according
     * to the user's settings.
     *
     * @param  nodes  nodes selected when the Create Test action was invoked
     */
    @NbBundle.Messages({"CommonTestsCfgOfCreate_Title=Create Tests",
        "LBL_OK=OK",
        "AN_OK=Confirm options",
        "AD_OK=Confirm options"
    })
    public boolean configure() {
        
        String title = Bundle.CommonTestsCfgOfCreate_Title();
        String btnTxt = Bundle.LBL_OK();
        String btnAN = Bundle.AN_OK();
        String btnAD = Bundle.AD_OK();
        
        
        // create and display the dialog:
        ChangeListener changeListener;
        final JButton btnOK = new JButton(btnTxt);
        btnOK.getAccessibleContext().setAccessibleName(btnAN);//NbBundle.getMessage(GuiUtils.class, "AN_OK"));
        btnOK.getAccessibleContext().setAccessibleDescription(btnAD);//NbBundle.getMessage(GuiUtils.class, "AD_OK"));
        btnOK.setEnabled(isAcceptable());
        addChangeListener(changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                btnOK.setEnabled(isAcceptable());
            }
        });
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor (
                        this,
                        title,
                        true,                       //modal
                        new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                        btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx(CommonTestsCfgOfCreate.class),
                        (ActionListener) null
                ));
        removeChangeListener(changeListener);
        
        if (returned == btnOK) {
            rememberCheckBoxStates();
	    setLastSelectedTestingFramework();
            testClassName = (tfClassName != null) ? tfClassName.getText() 
                                                  : null;
            return true;
        }
        return false;
    }
    
    /**
     * Returns whether a test for a single class is to be created.
     *
     * @return  true if there is only one node selected and the node
     *          represents a class
     */
    public boolean isSingleClass() {
        return singleClass;
    }
    
    /**
     * Returns whether integration tests are to be created.
     *
     * @return  true if integration tests are to be created
     */
    public boolean isIntegrationTests() {
        if (chkIntegrationTests != null) { // java provider found
        return chkIntegrationTests.isSelected();
    }
        return false;
    }

    public boolean isSinglePackage() {
        return singlePackage;
    }
    
    /**
     * Returns the class name entered in the text-field.
     *
     * @return  class name entered in the form,
     *          or <code>null</code> if the form did not contain
     *          the field for entering class name
     */
    public String getTestClassName() {
        return testClassName;
    }
    
    /** resource bundle used during initialization of this panel */
    public ResourceBundle bundle;
    
    /**
     * Reads JUnit settings and initializes checkboxes accordingly.
     *
     * @see  #rememberCheckBoxStates
     */
    private void initializeCheckBoxStates() {
        if (chkIntegrationTests != null) {
        Collection<? extends CommonSettingsProvider> providers = Lookup.getDefault().lookupAll(CommonSettingsProvider.class);
        for (CommonSettingsProvider provider : providers) {
                chkIntegrationTests.setSelected(provider.isGenerateIntegrationTests());
            break;
        }
        }
    }
    
    /**
     * Stores settings given by checkbox states to JUnit settings.
     *
     * @see  #initializeCheckBoxStatesf
     */
    private void rememberCheckBoxStates() {
        Collection<? extends CommonSettingsProvider> providers = Lookup.getDefault().lookupAll(CommonSettingsProvider.class);
        for (CommonSettingsProvider provider : providers) {
            provider.setGenerateIntegrationTests(isIntegrationTests());
            break;
        }
    }

    private void setLastSelectedTestingFramework() {
	getPreferences().put(PROP_TESTING_FRAMEWORK, selectedTestingFramework);
    }

    private String getLastSelectedTestingFramework() {
	return getPreferences().get(PROP_TESTING_FRAMEWORK, "");
    }

    private static Preferences getPreferences() {
	return NbPreferences.forModule(CommonTestsCfgOfCreate.class);
    }

    /**
     * Loads a resource bundle so that it can be used during intialization
     * of this panel.
     *
     * @see  #unlinkBundle
     */
    private void initBundle() {
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            bundle = provider.getBundle();
            break;
        }
    }
    
    /**
     * Nulls the resource bundle so that it is not held in memory when it is
     * not used.
     *
     * @see  #initBundle
     */
    private void unlinkBundle() {
        bundle = null;
    }
    
    private static class CommonCfgOfCreateCallback implements TestCreatorConfigurationProvider.Callback {

        private final CommonTestsCfgOfCreate commonCfgPanel;
        
        public CommonCfgOfCreateCallback(CommonTestsCfgOfCreate commonCfgPanel) {
            this.commonCfgPanel = commonCfgPanel;
        }

        @Override
        public void checkAcceptability() {
            commonCfgPanel.checkAcceptability();
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        jPanel.setLayout(new BorderLayout(0, 12));
        
        jPanel.add(createNameAndLocationPanel(), BorderLayout.NORTH);
        jPanel.add(createMessagePanel(), BorderLayout.CENTER);
        jPanel.add(createCodeGenPanel(), BorderLayout.SOUTH);

	add(jPanel);
    }
    
    private String getTestingFrameworkSuffix() {
        Object tf = cboxFramework.getSelectedItem();
        if(tf == null) {
            return "";
        }
        String testngFramework = "";
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            testngFramework = provider.getTestngFramework();
            break;
        }
        return tf.toString().equals(testngFramework) ? "NG" : ""; //NOI18N
    }
    
    private void fireFrameworkChanged() {
        setSelectedTestingFramework();
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            if(selectedTestingFramework != null && selectedTestingFramework.equals(provider.getJunitFramework())) {
                chkIntegrationTests.setEnabled(true);
            } else {
                chkIntegrationTests.setEnabled(false);
            }
            break;
        }
        updateClassName();
        checkUpdatingExistingTestClass();
        
        Collection<? extends TestCreatorConfigurationProvider> panelProviders = Lookup.getDefault().lookupAll(TestCreatorConfigurationProvider.class);
        for (TestCreatorConfigurationProvider panelProvider : panelProviders) {
            if (selectedTestingFramework != null && panelProvider.canHandleProject(selectedTestingFramework)) {
                fillFormData();
                checkAcceptability();
                TestCreatorConfigurationProvider.Context context = new TestCreatorConfigurationProvider.Context(multipleClasses, new CommonCfgOfCreateCallback(this));
                Component bottomPanel = panelProvider.getConfigurationPanel(context);
                BorderLayout layout = (BorderLayout) jPanel.getLayout();
                jPanel.remove(layout.getLayoutComponent(BorderLayout.SOUTH));
                jPanel.add(bottomPanel, BorderLayout.SOUTH);
                jPanel.revalidate();
            }
        }
    }
    
    private void updateClassName() {
        if (tfClassName != null) {
            FileObject fileObj = activatedFOs[0];

            ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
            if (cp != null) {
            String className = cp.getResourceName(fileObj, '.', false);

                String suffix = (selectedTestingFramework != null && selectedTestingFramework.equals(TestCreatorProvider.FRAMEWORK_SELENIUM))
                        || (chkIntegrationTests != null && chkIntegrationTests.isEnabled() && chkIntegrationTests.isSelected()) ? TestCreatorProvider.INTEGRATION_TEST_CLASS_SUFFIX : TestCreatorProvider.TEST_CLASS_SUFFIX;
            String prefilledName = className + getTestingFrameworkSuffix() + suffix;
            tfClassName.setText(prefilledName);
            tfClassName.setDefaultText(prefilledName);
            tfClassName.setCaretPosition(prefilledName.length());
        }
    }
    }
    
    private void setSelectedTestingFramework() {
        Object tf = cboxFramework.getSelectedItem();
        if(tf != null) {
            selectedTestingFramework = tf.toString();
        }
    }
    
    public String getSelectedTestingFramework() {
        return selectedTestingFramework;
    }
    
    public void addTestingFrameworks(ArrayList<String> testingFrameworksToAdd) {
        testingFrameworks = new ArrayList<String>();
        for(String testingFramework : testingFrameworksToAdd) {
            testingFrameworks.add(testingFramework);
        }
        cboxFramework.setModel(new DefaultComboBoxModel(testingFrameworks.toArray()));
        cboxFramework.setSelectedItem(getLastSelectedTestingFramework());
        fireFrameworkChanged();
    }

    public void setPreselectedLocation(Object location) {
	if (location != null) {
	    cboxLocation.setSelectedItem(location);
	    cboxLocation.setEnabled(false);
	}
    }

    public void setPreselectedFramework(String testingFramework) {
	if (testingFramework != null) {
	    cboxFramework.setSelectedItem(testingFramework);
	    cboxFramework.setEnabled(false);
	    setSelectedTestingFramework();
	}
    }
    
    @NbBundle.Messages({"LBL_PackageToTest=Package:",
        "LBL_ClassToTest=&Class to Test:",
        "LBL_MultipleClassesSelected=More than one class is selected...",
        "LBL_ClassName=Class &Name:",
        "LBL_Location=&Location:",
        "LBL_Framework=&Framework:"
    })
    private Component createNameAndLocationPanel() {
        JPanel panel = new JPanel();
        
        final boolean askForClassName = singleClass;
        
        JLabel lblClassToTest = new JLabel();
        JLabel lblClassName = askForClassName ? new JLabel() : null;
        JLabel lblLocation = new JLabel();
        JLabel lblFramework = new JLabel();
        
        String classToTestKey = singlePackage
                                ? Bundle.LBL_PackageToTest()
                                : singleClass
                                  ? Bundle.LBL_ClassToTest()
                                  : Bundle.LBL_MultipleClassesSelected();
        String classToTest = classToTestKey;
        String classname = Bundle.LBL_ClassName();
        String location = Bundle.LBL_Location();
        String framework = Bundle.LBL_Framework();
        
        Mnemonics.setLocalizedText(
                lblClassToTest,
                classToTest);
        if (askForClassName) {
            Mnemonics.setLocalizedText(
                    lblClassName,
                    classname);
        }
        Mnemonics.setLocalizedText(
                lblLocation,
                location);
        Mnemonics.setLocalizedText(
                lblFramework,
                framework);
        
        if (singlePackage || singleClass) {
            lblClassToTestValue = new JLabel();
        }
        if (askForClassName) {
            tfClassName = new ClassNameTextField();
            tfClassName.setChangeListener(this);
        }
        cboxLocation = new JComboBox();
        cboxFramework = new JComboBox();
        cboxFramework.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireFrameworkChanged();
            }
        });
        
        if (askForClassName) {
            lblClassName.setLabelFor(tfClassName);
        }
        lblLocation.setLabelFor(cboxLocation);
        lblFramework.setLabelFor(cboxFramework);
        
        if (lblClassToTestValue != null) {
            Font labelFont = javax.swing.UIManager.getDefaults()
                             .getFont("TextField.font");                //NOI18N
            if (labelFont != null) {
                lblClassToTestValue.setFont(labelFont);
            }
        }
        
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        if (!providers.isEmpty()) {
        String[] chkBoxIDs = new String[1];
        JCheckBox[] chkBoxes;
        for (GuiUtilsProvider provider : providers) {
            chkBoxIDs = new String[]{provider.getCheckboxText("CHK_INTEGRATION_TESTS")};
            break;
        }
        chkBoxes = new JCheckBox[chkBoxIDs.length];
        for (GuiUtilsProvider provider : providers) {
            chkBoxes = provider.createCheckBoxes(chkBoxIDs);
            break;
        }
        chkIntegrationTests = chkBoxes[0];
        if (chkIntegrationTests != null) { // java provider found
            chkIntegrationTests.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateClassName();
                    checkUpdatingExistingTestClass();
                }
            });
        }
        }
        
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.anchor = GridBagConstraints.WEST;
        gbcLeft.insets.bottom = 12;
        gbcLeft.insets.right = 6;
        
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.anchor = GridBagConstraints.WEST;
        gbcRight.insets.bottom = 12;
        gbcRight.weightx = 1.0f;
        gbcRight.fill = GridBagConstraints.BOTH;
        gbcRight.gridwidth = GridBagConstraints.REMAINDER;
        
        if (lblClassToTestValue != null) {
            panel.add(lblClassToTest,      gbcLeft);
            panel.add(lblClassToTestValue, gbcRight);
        } else {
            panel.add(lblClassToTest,   gbcRight);
        }
        if (askForClassName) {
            panel.add(lblClassName,     gbcLeft);
            panel.add(tfClassName,      gbcRight);
        }
        panel.add(lblLocation,      gbcLeft);
        panel.add(cboxLocation,     gbcRight);
        gbcLeft.insets.bottom = 0;
        gbcRight.insets.bottom = 0;
        panel.add(lblFramework,      gbcLeft);
        panel.add(cboxFramework,     gbcRight);
        if (chkIntegrationTests != null) { // java provider found
            panel.add(chkIntegrationTests,     gbcRight);
        }
        
        return panel;
    }
    
    /**
     */
    private void checkClassNameValidity() {
        if (tfClassName == null) {
            classNameValid = true;
            return;
        }
        
        String key = null;
        final int state = tfClassName.getStatus();
        switch (state) {
            case ClassNameTextField.STATUS_EMPTY:
                key = "MSG_ClassnameMustNotBeEmpty";                    //NOI18N
                break;
            case ClassNameTextField.STATUS_INVALID:
                key = "MSG_InvalidClassName";                           //NOI18N
                break;
            case ClassNameTextField.STATUS_VALID_NOT_DEFAULT:
                key = "MSG_ClassNameNotDefault";                        //NOI18N
                break;
            case ClassNameTextField.STATUS_VALID_END_NOT_TEST:
                key = "MSG_ClassNameEndNotTest";                        //NOI18N
                break;
        }
        if (state != ClassNameTextField.STATUS_VALID_NOT_DEFAULT) {
            setMessage(null, MSG_TYPE_CLASSNAME_NOT_DEFAULT);
        }
        String message = "";
        if (key != null) {
            Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
            for (GuiUtilsProvider provider : providers) {
                message = provider.getMessageFor(key);
                break;
            }
        }
        setMessage((key != null)
                           ? message
                           : null,
                   MSG_TYPE_CLASSNAME_INVALID);
        
        classNameValid =
                (state == ClassNameTextField.STATUS_VALID)
                || (state == ClassNameTextField.STATUS_VALID_NOT_DEFAULT);
    }
    
    /**
     * This method gets called if status of contents of the Class Name
     * text field changes. See <code>STATUS_xxx</code> constants
     * in class <code>ClassNameTextField</code>.
     *
     * @param  e  event describing the state change event
     *            (unused in this method)
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        checkClassNameValidity();
        checkAcceptability();
    }
    
    /**
     */
    private void checkAcceptability() {
        final boolean wasAcceptable = isAcceptable;
        isAcceptable = hasTargetFolders && classNameValid && !isJ2meProject;
        if (isAcceptable != wasAcceptable) {
            fireStateChange();
        }
    }
    
    /**
     * Are the values filled in the form acceptable?
     *
     * @see  #addChangeListener
     */
    private boolean isAcceptable() {
        return isAcceptable;
    }
    
    /**
     * This method is called the first time this panel's children are painted.
     * By default, this method just calls {@link #adjustWindowSize()}.
     *
     * @param  g  <code>Graphics</code> used to paint this panel's children
     */
    @Override
    protected void paintedFirstTime(java.awt.Graphics g) {
        if (initialMessage != null) {
            displayMessage(initialMessage);
            initialMessage = null;
        }
    }
    
    /**
     * Displays a given message in the message panel and resizes the dialog
     * if necessary. If the message cannot be displayed immediately,
     * because of this panel not displayed (painted) yet, displaying the message
     * is deferred until this panel is painted.
     *
     * @param  message  message to be displayed, or <code>null</code> if
     *                  the currently displayed message (if any) should be
     *                  removed
     */
    private void setMessage(final String message, final int msgType) {
        String msgToDisplay = msgStack.setMessage(msgType, message);
        if (msgToDisplay == null) {
            return;                     //no change
        }

        /* display the message: */
        if (!isPainted()) {
            initialMessage = msgToDisplay;
        } else {
            displayMessage(msgToDisplay);
        }
    }
    
    /**
     * Displays a given message in the message panel and resizes the dialog
     * if necessary.
     *
     * @param  message  message to be displayed, or <code>null</code> if
     *                  the currently displayed message (if any) should be
     *                  removed
     * @see  #adjustWindowSize()
     */
    private void displayMessage(String message) {
        if (message == null) {
            message = "";                                               //NOI18N
        }
        
        txtAreaMessage.setText(message);
        adjustWindowSize();
    }
    
    /**
     * Creates a text component to be used as a multi-line, automatically
     * wrapping label.
     * <p>
     * <strong>Restriction:</strong><br>
     * The component may have its preferred size very wide.
     *
     * @return  created multi-line text component
     */
    private Component createMessagePanel() {
        if (txtAreaMessage == null) {
            JTextArea textArea = new JTextArea("");
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEnabled(false);
            textArea.setOpaque(false);
            textArea.setColumns(25);
        Color color = UIManager.getColor("nb.errorForeground");         //NOI18N
        if (color == null) {
            color = new Color(89, 79, 191);   //RGB suggested by Bruce in #28466
        }
            textArea.setDisabledTextColor(color);
            txtAreaMessage = textArea;
        }
        return txtAreaMessage;
    }
    
    /**
     * Creates a panel containing controls for settings code generation options.
     *
     * @return   created panel
     */
    private Component createCodeGenPanel() {
        Component bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(447, 344));
        Collection<? extends TestCreatorConfigurationProvider> panelProviders = Lookup.getDefault().lookupAll(TestCreatorConfigurationProvider.class);
        for (TestCreatorConfigurationProvider panelProvider : panelProviders) {
            if (selectedTestingFramework != null && panelProvider.canHandleProject(selectedTestingFramework)) {
                TestCreatorConfigurationProvider.Context context = new TestCreatorConfigurationProvider.Context(multipleClasses, new CommonCfgOfCreateCallback(this));//bundle);
                bottomPanel = panelProvider.getConfigurationPanel(context);
                return bottomPanel;
            }
            }
        return bottomPanel;
        }
        
    /**
     * Adds a border and a title around a given component.
     * If the component already has some border, it is overridden (not kept).
     *
     * @param  component  component the border and title should be added to
     * @param  insets  insets between the component and the titled border
     * @param  title  text of the title
     */
    private static void addTitledBorder(JComponent component,
                                        Insets insets,
                                        String title) {
        Border insideBorder = BorderFactory.createEmptyBorder(
                insets.top, insets.left, insets.bottom, insets.right);
        Border outsideBorder = new TitledBorder(
                BorderFactory.createEtchedBorder(), title);
        component.setBorder(new CompoundBorder(outsideBorder, insideBorder));
    }
    
    /**
     */
    public FileObject getTargetFolder() {
        Object selectedLocation = cboxLocation.getSelectedItem();
        
        if (selectedLocation == null) {
            return null;
        }
        
        if (selectedLocation instanceof SourceGroup) {
            return ((SourceGroup) selectedLocation).getRootFolder();
        }
        assert selectedLocation instanceof FileObject;      //root folder
        return (FileObject) selectedLocation;
    }
    
    /**
     * Initializes form in the Test Settings panel of the dialog.
     */
    @NbBundle.Messages("DefaultPackageName=<default package>")
    private void fillFormData() {
        final FileObject fileObj = activatedFOs[0];
        
        if (singleClass) {
            assert activatedFOs.length == 1;
            
            String className = "";
            String prefilledName = "";
            Collection<? extends TestCreatorConfigurationProvider> panelProviders = Lookup.getDefault().lookupAll(TestCreatorConfigurationProvider.class);
            for (TestCreatorConfigurationProvider panelProvider : panelProviders) {
                if (selectedTestingFramework != null && panelProvider.canHandleProject(selectedTestingFramework)) {
                    boolean isTestNG = !getTestingFrameworkSuffix().isEmpty();
                    boolean isSelenium = isIntegrationTests() || 
                            (selectedTestingFramework != null && selectedTestingFramework.equals(TestCreatorProvider.FRAMEWORK_SELENIUM));
                    String[] testClassNames = panelProvider.getSourceAndTestClassNames(fileObj, isTestNG, isSelenium);
                    className = testClassNames[0];
                    prefilledName = testClassNames[1];
                }
            }
            lblClassToTestValue.setText(className);
            
            if (tfClassName != null) {
                tfClassName.setText(prefilledName);
                tfClassName.setDefaultText(prefilledName);
                tfClassName.setCaretPosition(prefilledName.length());
            }
        } else if (singlePackage) {
            assert activatedFOs.length == 1;
            
            ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
            String packageName = (cp == null) ? Bundle.DefaultPackageName() : cp.getResourceName(fileObj, '.', true);
            lblClassToTestValue.setText(packageName);
        } else {
            //PENDING
        }
        
        setupLocationChooser(fileObj);
        
        checkClassNameValidity();
    }
    
    @NbBundle.Messages({
        "# {0} - folder", 
        "MSG_NoTestTarget_Fo=Unable to locate test package folders for folder {0}. The project must contain a test package folder to create tests. You can designate test package folders for your project in the Sources pane of the project Properties dialog.",
        "# {0} - file", 
        "MSG_NoTestTarget_Fi=Unable to locate test package folder for file {0}. The project must contain a test package folder to create tests. You can designate test package folders for your project in the Sources pane of the project Properties dialog."
    })
    private void setupLocationChooser(FileObject refFileObject) {
        Object[] targetFolders = null;
        Collection<? extends TestCreatorConfigurationProvider> panelProviders = Lookup.getDefault().lookupAll(TestCreatorConfigurationProvider.class);
        for (TestCreatorConfigurationProvider panelProvider : panelProviders) {
            if (selectedTestingFramework != null && panelProvider.canHandleProject(selectedTestingFramework)) {
                targetFolders = panelProvider.getTestSourceRoots(createdSourceRoots, refFileObject);
        }
                }

        if (targetFolders != null && targetFolders.length != 0) {
            hasTargetFolders = true;
            cboxLocation.setModel(new DefaultComboBoxModel(targetFolders));
            cboxLocation.setRenderer(new LocationChooserRenderer());
            setMessage(null, MSG_TYPE_NO_TARGET_FOLDERS);
        } else {
            hasTargetFolders = false;
            if(testingFrameworks != null) {
            //PENDING - message text:
                String msgNoTargetsFound = refFileObject.isFolder()? Bundle.MSG_NoTestTarget_Fo(refFileObject.getNameExt()) : Bundle.MSG_NoTestTarget_Fi(refFileObject.getNameExt());
            setMessage(msgNoTargetsFound, MSG_TYPE_NO_TARGET_FOLDERS);
            disableComponents();
        }
    }
    }

    public Collection<? extends SourceGroup> getCreatedSourceRoots() {
        return Collections.unmodifiableCollection(createdSourceRoots);
    }
    
    /**
     * Renderer which specially handles values of type
     * <code>SourceGroup</code> and <code>FileObject</code>.
     * It displays display names of these objects, instead of their default
     * string representation (<code>toString()</code>).
     *
     * @see  SourceGroup#getDisplayName()
     * @see  FileUtil#getFileDisplayName(FileObject)
     */
    private final class LocationChooserRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public LocationChooserRenderer () {
            setOpaque(true);
        }
        
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            String text = value instanceof SourceGroup
                        ? ((SourceGroup) value).getDisplayName()
                        : value instanceof FileObject
                              ?  FileUtil.getFileDisplayName((FileObject) value)
                              : value.toString();
            setText(text);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
            
    }

    /**
     * Registers a change listener.
     * Registered change listeners are notified when acceptability
     * of values in the form changes.
     *
     * @param  l  listener to be registered
     * @see  #isAcceptable
     * @see  #removeChangeListener
     */
    private void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList<ChangeListener>(3);
        }
        changeListeners.add(l);
    }
    
    /**
     * Unregisters the given change listener.
     * If the given listener has not been registered before, calling this
     * method does not have any effect.
     *
     * @param  l  change listener to be removed
     * @see  #addChangeListener
     */
    private void removeChangeListener(ChangeListener l) {
        if (changeListeners != null
                && changeListeners.remove(l)
                && changeListeners.isEmpty()) {
            changeListeners = null;
        }
    }
    
    /**
     * Notifies all registered change listeners about a change.
     *
     * @see  #addChangeListener
     */
    private void fireStateChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }
    
    /**
     * Disables all interactive visual components of this dialog
     * except the OK, Cancel and Help buttons.
     */
    private void disableComponents() {
        final Stack<Container> stack = new Stack<Container>();
        stack.push(this);
        
        while (!stack.empty()) {
            Container container = stack.pop();
            Component comps[] = container.getComponents();
            for (int i = 0; i < comps.length; i++) {
                final java.awt.Component comp = comps[i];
                
                if (comp == txtAreaMessage) {
                    continue;
                }
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    stack.push(panel);

                    final Border border = panel.getBorder();
                    if (border != null) {
                        disableBorderTitles(border);
                    }
                    continue;
                }
                comp.setEnabled(false);
                if (comp instanceof java.awt.Container) {
                    Container nestedCont = (Container) comp;
                    if (nestedCont.getComponentCount() != 0) {
                        stack.push(nestedCont);
                    }
                }
            }
        }
    }
    
    /**
     */
    private static void disableBorderTitles(Border border) {
        
        if (border instanceof TitledBorder) {
            disableBorderTitle((TitledBorder) border);
            return;
        }
        
        if (!(border instanceof CompoundBorder)) {
            return;
        }
        
        Stack<CompoundBorder> stack = new Stack<CompoundBorder>();
        stack.push((CompoundBorder) border);
        while (!stack.empty()) {
            CompoundBorder cb = stack.pop();
            
            Border b;
            b = cb.getOutsideBorder();
            if (b instanceof CompoundBorder) {
                stack.push((CompoundBorder) b);
            } else if (b instanceof TitledBorder) {
                disableBorderTitle((TitledBorder) b);
            }
            
            b = cb.getInsideBorder();
            if (b instanceof CompoundBorder) {
                stack.push((CompoundBorder) b);
            } else if (b instanceof TitledBorder) {
                disableBorderTitle((TitledBorder) b);
            }
        }
    }
    
    /**
     */
    private static void disableBorderTitle(TitledBorder border) {
        final Color color = UIManager.getColor(
                "Label.disabledForeground");                        //NOI18N
        if (color != null) {
            border.setTitleColor(color);
        }
    }

    private JLabel lblClassToTestValue;
    private ClassNameTextField tfClassName;
    private JTextComponent txtAreaMessage;
    private JComboBox cboxLocation;
    private JComboBox cboxFramework;
    private JCheckBox chkIntegrationTests;

}
