package org.netbeans.modules.php.project.ui.wizards;
/** Localizable strings for {@link org.netbeans.modules.php.project.ui.wizards}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Sources cannot be your home directory.</i>
     * @see ConfigureProjectPanel
     */
    static String ConfigureProjectPanel_error_sources_homeDir() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ConfigureProjectPanel.error.sources.homeDir");
    }
    /**
     * @return <i>Namespace is not valid.</i>
     * @see NewFileNamespacePanelVisual
     */
    static String NewFileNamespacePanelVisual_error_namespace_invalid() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "NewFileNamespacePanelVisual.error.namespace.invalid");
    }
    /**
     * @return <i>Please wait...</i>
     * @see NewFileNamespacePanelVisual
     */
    static String NewFileNamespacePanelVisual_message_pleaseWait() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "NewFileNamespacePanelVisual.message.pleaseWait");
    }
    /**
     * @return <i>Namespace must be provided</i>
     * @see NewFileNamespacePanel
     */
    static String NewFileNamespacePanel_error_namespace() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "NewFileNamespacePanel.error.namespace");
    }
    /**
     * @return <i>Project was not created because it already exists (maybe only in memory).</i>
     * @see NewPhpProjectWizardIterator
     */
    static String NewPhpProjectWizardIterator_project_alreadyExists() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "NewPhpProjectWizardIterator.project.alreadyExists");
    }
    /**
     * @param extender_name extender name
     * @return {@code extender_name}<i> does not provide any configuration UI.</i>
     * @see PhpExtenderPanel
     */
    static String PhpExtenderPanel_noUi(Object extender_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PhpExtenderPanel.noUi", extender_name);
    }
    private void Bundle() {}
}
