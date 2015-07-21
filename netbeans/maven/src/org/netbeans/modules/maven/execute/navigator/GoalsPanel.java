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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute.navigator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import static org.netbeans.modules.maven.execute.navigator.Bundle.*;
import org.netbeans.modules.maven.spi.IconResources;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * @author  mkleint
 */
public class GoalsPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable {
    private static final @StaticResource String LIFECYCLE_ICON = "org/netbeans/modules/maven/execute/navigator/thread_running_16.png";
    private static final @StaticResource String HELP_ICON = "org/netbeans/modules/maven/execute/navigator/help.png";
    private static final String PROP_SHOW_LIFECYCLE_GOALS = "showLifecycleGoals";
    private static final String PROP_SHOW_HELP_GOALS = "showHelpGoals";
    
    private static final Logger LOG = Logger.getLogger(GoalsPanel.class.getName());

    private final transient ExplorerManager explorerManager = new ExplorerManager();
    
    private final BeanTreeView treeView;
    private NbMavenProject current;
    private Project currentP;
    private final TapPanel filtersPanel;
    private final Preferences preferences;

    private final PropertyChangeListener pchadapter = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                showWaitNode();
                RequestProcessor.getDefault().post(GoalsPanel.this);
            }
        }
    };

    /** Creates new form POMInheritancePanel */
    @Messages("HINT_Panel_hide=Click or press {0} to hide/show when the Navigator is active")
    public GoalsPanel() {
        initComponents();
        treeView = (BeanTreeView)jScrollPane1;
        preferences = NbPreferences.forModule(GoalsPanel.class).node("goalNavigator");
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(HINT_Panel_hide(keyText)); //NOI18N

        JComponent buttons = createFilterButtons();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) {
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background"));//NOI18N
        } 

        add(filtersPanel, BorderLayout.SOUTH);
        
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    void navigate(DataObject d) {
        if (current != null) {
            current.removePropertyChangeListener(pchadapter);
        }
        NbMavenProject n = null;

        FileObject f = d.getPrimaryFile();
        if (!f.isFolder()) {
            f = f.getParent();
        }
        Project p = null;
        try {
            p = ProjectManager.getDefault().findProject(f);
            if (p != null) {
                n = p.getLookup().lookup(NbMavenProject.class);
            }
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            //Exceptions.printStackTrace(ex);
        }

        if (n == null) {
            release();
            return;
        }
         
        current = n;
        currentP = p;
        current.addPropertyChangeListener(pchadapter);
        showWaitNode();
        RequestProcessor.getDefault().post(this);
    }
    
    @Override
    public void run() {
        //#164852 somehow a folder dataobject slipped in, test mimetype to avoid that.
        // the root cause of the problem is unknown though
        if (currentP != null ) { //NOI18N
         
            NbMavenProject mpp = currentP.getLookup().lookup(NbMavenProject.class);
            if (mpp != null) {
                final Children ch = Children.create(new PluginChildren(currentP), true);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        treeView.setRootVisible(false);
                        explorerManager.setRootContext(new AbstractNode(ch));
                        treeView.expandAll();
                    }
                });
                return;
            }


        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                treeView.setRootVisible(false);
                explorerManager.setRootContext(createEmptyNode());
            }
        });
    }

    /**
     * 
     */
    void release() {
        if (current != null) {
            current.removePropertyChangeListener(pchadapter);
        }
        current = null;
        currentP = null;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               treeView.setRootVisible(false);
               explorerManager.setRootContext(createEmptyNode());
            } 
        });
    }

    /**
     * 
     */
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               treeView.setRootVisible(true);
               explorerManager.setRootContext(createWaitNode());
            } 
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
    @Messages("LBL_Wait=Please Wait...")
    private static Node createWaitNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setIconBaseWithExtension(WAIT_GIF);
        an.setDisplayName(Bundle.LBL_Wait());
        return an;
    }
    private static final @StaticResource String WAIT_GIF = "org/netbeans/modules/maven/resources/wait.gif";

    private static Node createEmptyNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        return an;
    }

    @Messages({"LBL_Show_Help=Show help goals",
               "LBL_Show_Lifecycle=Show lifecycle bound goals"
    })
    private JComponent createFilterButtons() {
        Box box = new Box(BoxLayout.X_AXIS);
        box.setBorder(new EmptyBorder(1, 2, 3, 5));

            // configure toolbar
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL) {
            @Override
            protected void paintComponent(Graphics g) {
            }
        };
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            final JToggleButton tg1 = new JToggleButton();
            tg1.setIcon(ImageUtilities.loadImageIcon(HELP_ICON, true));
            tg1.setToolTipText(LBL_Show_Help());
            tg1.setSelected(preferences.getBoolean(PROP_SHOW_HELP_GOALS, false));
            tg1.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    preferences.putBoolean(PROP_SHOW_HELP_GOALS, tg1.isSelected());
                    
                }
            });
            toolbar.add(tg1);
            final JToggleButton tg2 = new JToggleButton();
            tg2.setIcon(ImageUtilities.loadImageIcon(LIFECYCLE_ICON, true));
            tg2.setToolTipText(LBL_Show_Lifecycle());
            tg2.setSelected(preferences.getBoolean(PROP_SHOW_LIFECYCLE_GOALS, false));
            tg2.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    preferences.putBoolean(PROP_SHOW_LIFECYCLE_GOALS, tg2.isSelected());
                }
            });
            toolbar.add(tg2);
            Dimension space = new Dimension(3, 0);
            toolbar.addSeparator(space);

            box.add(toolbar);
            return box;

    }

    private class PluginChildren extends ChildFactory<Mojo> implements PreferenceChangeListener {
        private final Project prj;

        PluginChildren(Project prj) {
            this.prj = prj;
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, preferences));
        }

        protected @Override
        boolean createKeys(List<Mojo> toPopulate) {
            Set<Mojo> goals = new TreeSet<Mojo>();
            MavenProject mp = prj.getLookup().lookup(NbMavenProject.class).getMavenProject();
            for (Artifact p : mp.getPluginArtifacts()) {
                try {

                    EmbedderFactory.getOnlineEmbedder().resolve(p, mp.getPluginArtifactRepositories(), EmbedderFactory.getOnlineEmbedder().getLocalRepository());
                    Document d = loadPluginXml(p.getFile());
                    if (d != null) {
                        Element root = d.getDocumentElement();
                        Element mojos = XMLUtil.findElement(root, "mojos", null);
                        if (mojos == null) {
                            LOG.log(Level.WARNING, "no mojos in {0}", p.getFile());
                            continue;
                        }
                        Element goalPrefix = XMLUtil.findElement(root, "goalPrefix", null);
                        if (goalPrefix == null) {
                            LOG.log(Level.WARNING, "no goalPrefix in {0}", p.getFile());
                            continue;
                        }

                        for (Element mojo : XMLUtil.findSubElements(mojos)) {
                            if (!mojo.getTagName().equals("mojo")) {
                                continue;
                            }
                            Element goal = XMLUtil.findElement(mojo, "goal", null);
                            if (goal == null) {
                                LOG.log(Level.WARNING, "mojo missing goal in {0}", p.getFile());
                                continue;
                            }
                            String goalString = XMLUtil.findText(goal).trim();
                            if ("help".equals(goalString) && !preferences.getBoolean(PROP_SHOW_HELP_GOALS, false)) {
                                continue;
                            }
                            List<Param> params = new ArrayList<Param>();
                            Element parameters = XMLUtil.findElement(mojo, "parameters", null);
                            if (parameters != null) {
                                for (Element param : XMLUtil.findSubElements(parameters)) {
                                    if (!param.getTagName().equals("parameter")) {
                                        continue;
                                    }
                                    Element nameEl = XMLUtil.findElement(param, "name", null);
                                    Element editableEl = XMLUtil.findElement(param, "editable", null);
                                    Element requiredEl = XMLUtil.findElement(param, "required", null);
                                    if (nameEl != null && requiredEl != null && editableEl != null && "true".equals(XMLUtil.findText(editableEl))) {
                                        String r = XMLUtil.findText(requiredEl);
                                        Param par = new Param(XMLUtil.findText(nameEl), "true".equals(r));
                                        params.add(par);
                                    }
                                }
                            }
                            Element config = XMLUtil.findElement(mojo, "configuration", null);
                            if (config != null) {
                                for (Param par : params) {
                                    Element pconfEl = XMLUtil.findElement(config, par.parameterName, null);
                                    if (pconfEl != null) {
                                        Attr attr = pconfEl.getAttributeNode("default-value");
                                        if (attr != null) {
                                            par.defValue = attr.getValue();
                                        }
                                        String val = XMLUtil.findText(pconfEl);
                                        if (val != null && val.startsWith("${") && val.endsWith("}")) {
                                            par.property = val.substring(2, val.length() - 1);
                                        }
                                    }
                                    Plugin pl = mp.getPlugin(Plugin.constructKey(p.getGroupId(), p.getArtifactId()));
                                    if (pl != null) {
                                        Xpp3Dom c = (Xpp3Dom) pl.getConfiguration();
                                        if (c != null) {
                                            par.parameterInModel = c.getChild(par.parameterName) != null;
                                        }
                                    }
                                    if (par.property != null) {
                                        par.propertyInModel = mp.getProperties().getProperty(par.property) != null;
                                    }
                                }
                            }
                            Plugin plg = mp.getPlugin(Plugin.constructKey(p.getGroupId(), p.getArtifactId()));
                            boolean lifecycleBound = false;
                            List<PluginExecution> execs = plg.getExecutions();
                            if (execs != null) {
                                for (PluginExecution exec : execs) {
                                    //TODO there are also executions defined in the pom, that don't have the default id and don't have phase defined.
                                    //however phase is defined also in the plugin.xml .. do we want to include these?
//                                    String execgoal = exec.getGoals() != null && exec.getGoals().size() == 1 ? exec.getGoals().get(0) : null;
                                    if (exec.getGoals().contains(goalString) /*&& exec.getPhase() != null && ("default-" + execgoal).equals(exec.getId())*/) {
                                        //a bit of a heuristics applied..
                                        lifecycleBound = true;
                                        break;
                                    }
                                }
                                if (lifecycleBound && !preferences.getBoolean(PROP_SHOW_LIFECYCLE_GOALS, false)) {
                                    continue; //skip lifecycle goals
                                }
                            }
                            goals.add(new Mojo(XMLUtil.findText(goalPrefix).trim(), goalString, p, params, lifecycleBound));
                        }

                    }
                } catch (ArtifactResolutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ArtifactNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            toPopulate.addAll(goals);
            return true;
        }

        protected @Override
        Node createNodeForKey(Mojo mdl) {
            return new MojoNode(mdl, prj);
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            refresh(false);
        }
    }

    private static class Mojo implements Comparable<Mojo> {

        final Artifact a;
        final String goal;
        final String prefix;
        final List<Param> parameters;
        final boolean lifecycleBound;

        public Mojo(String prefix, String goal, Artifact a, List<Param> parameters, boolean lifecycleBound) {
            this.a = a;
            this.goal = goal;
            this.prefix = prefix;
            this.parameters = parameters;
            this.lifecycleBound = lifecycleBound;
        }
        
        List<Param> getNotSetParams() {
            List<Param> toRet = new ArrayList<Param>();
            for (Param p : parameters) {
                if (p.required && !p.parameterInModel && (p.property == null || !p.propertyInModel)) {
                    toRet.add(p);
                }
            }
            return toRet;
        }

        @Override
        public int compareTo(Mojo o) {
            int res = prefix.compareTo(o.prefix);
            if (res == 0) {
                res = goal.compareTo(o.goal);
            }
            return res;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + (this.goal != null ? this.goal.hashCode() : 0);
            hash = 17 * hash + (this.prefix != null ? this.prefix.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Mojo other = (Mojo) obj;
            if ((this.goal == null) ? (other.goal != null) : !this.goal.equals(other.goal)) {
                return false;
            }
            if ((this.prefix == null) ? (other.prefix != null) : !this.prefix.equals(other.prefix)) {
                return false;
            }
            return true;
        }
        
    }
    
    private static final class Param {
        final String parameterName;
        final boolean required;
        String property;
        String defValue;
        boolean parameterInModel = false; //calculated from presence of parameterName in <configuration> and property in <properties>
        boolean propertyInModel = false;

        public Param(String parameterName, boolean required) {
            this.parameterName = parameterName;
            this.required = required;
        }
        
    }
    
    @Messages({"ACT_Execute_mod=Execute Goal With Modifiers...", "ACT_Execute_help=Show Documentation..."})
     private static class MojoNode extends AbstractNode {
        
 
        private final Mojo mojo;
        private final Project project;
        private MojoNode(@NonNull Mojo mojo, Project p) {
            super(Children.LEAF);
            setDisplayName(mojo.goal);
            setShortDescription("<html>Plugin:" + mojo.a.getId() + "<br/>Goal:" + mojo.goal + "<br/>Prefix:" + mojo.prefix + (mojo.lifecycleBound ? "<br/>Bound to lifecycle in current POM." : ""));
            this.mojo = mojo;
            this.project = p;
        }

        @Override
        public Action[] getActions(boolean context) {
            NetbeansActionMapping mapp = new NetbeansActionMapping();
            for (Param p : mojo.getNotSetParams()) {
                if (p.property != null) {
                    mapp.addProperty(p.property, "");
                }
            }
            
            //
            // Execute with modifiers 
            //
            mapp.setGoals(Collections.singletonList(mojo.a.getGroupId() + ":" + mojo.a.getArtifactId() + ":" + mojo.a.getVersion() + ":" + mojo.goal));
            Action runGoalWithModsAction = ActionProviderImpl.createCustomMavenAction(mojo.prefix + ":" + mojo.goal, mapp, true, Lookup.EMPTY, project);
            runGoalWithModsAction.putValue(Action.NAME, ACT_Execute_mod());
            
            //
            // Show Documentation
            //
            // f.e.: help:describe -Dcmd=org.codehaus.mojo:gwt-maven-plugin:1.2:debug -Ddetail
            NetbeansActionMapping mappForHelpDesc = new NetbeansActionMapping();

            mappForHelpDesc.setGoals(Collections.singletonList("help:describe"));
            
            HashMap<String, String> m = new HashMap<>();
            m.put("cmd", String.format("%s:%s:%s:%s", mojo.a.getGroupId(), mojo.a.getArtifactId(), mojo.a.getVersion(), mojo.goal));
            m.put("detail", "true");
            mappForHelpDesc.setProperties(m);
            Action runHelpDescAction = ActionProviderImpl.createCustomMavenAction(String.format("help:describe for %s:%s", mojo.prefix, mojo.goal), mappForHelpDesc, false, Lookup.EMPTY, project);
            runHelpDescAction.putValue(Action.NAME, ACT_Execute_help());
            
            return new Action[] {
                new RunGoalAction(mojo, project),
                runGoalWithModsAction,
                null,
                runHelpDescAction
            };
        }

        @Override
        public String getHtmlDisplayName() {
            return "<html>" /*<font color='!controlShadow'>"*/ + mojo.prefix /*+"</font>"*/ + " <b>" +  mojo.goal + "</b></html>";
        }

        @Override
        public Action getPreferredAction() {
            return new RunGoalAction(mojo, project);
        }

        @Override
        public Image getIcon(int type) {
            if ("help".equals(mojo.goal)) {
                return ImageUtilities.loadImage(HELP_ICON);
            }
            if (mojo.lifecycleBound) {
                return ImageUtilities.loadImage(LIFECYCLE_ICON);
            } else {
                return ImageUtilities.loadImage(IconResources.MOJO_ICON);
            }
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }
     
    private static @CheckForNull Document loadPluginXml(File jar) {
        if (!jar.isFile() || !jar.getName().endsWith(".jar")) {
            return null;
        }
        LOG.log(Level.FINER, "parsing plugin.xml from {0}", jar);
            try {
            return XMLUtil.parse(new InputSource("jar:" + Utilities.toURI(jar) + "!/META-INF/maven/plugin.xml"), false, false, XMLUtil.defaultErrorHandler(), null);
        } catch (Exception x) {
            LOG.log(Level.FINE, "could not parse " + jar, x.toString());
            return null;
        }
    }

    @Messages("ACT_Execute=Execute Goal")
    private static class RunGoalAction extends AbstractAction {
        private final Mojo mojo;
        private final Project project;

        public RunGoalAction(Mojo mojo, Project prj) {
            this.mojo = mojo;
            this.project = prj;
            putValue(Action.NAME, ACT_Execute());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RunConfig config = RunUtils.createRunConfig(FileUtil.toFile(project.getProjectDirectory()), project, mojo.prefix + ":" + mojo.goal, 
                    Collections.singletonList(mojo.a.getGroupId() + ":" + mojo.a.getArtifactId() + ":" + mojo.a.getVersion() + ":" + mojo.goal));
            //TODO run in RP
            M2ConfigProvider prof = project.getLookup().lookup(M2ConfigProvider.class);
            M2Configuration m2c = prof.getActiveConfiguration(); //TODO in mutex
            if (m2c != null) {
                config.addProperties(m2c.getProperties());
                config.setActivatedProfiles(m2c.getActivatedProfiles());
            }
            RunUtils.run(config);
        }

        
    }
}