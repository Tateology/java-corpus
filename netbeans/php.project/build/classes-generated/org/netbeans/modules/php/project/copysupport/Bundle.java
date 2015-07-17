package org.netbeans.modules.php.project.copysupport;
/** Localizable strings for {@link org.netbeans.modules.php.project.copysupport}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @param project_name project name
     * @return <i>&lt;html>Source Files of project "</i>{@code project_name}<i>" do not exist, file changes are not propagated to the server.&lt;br>&lt;br>Use "Resolve Project Problems..." action to repair the project.</i>
     * @see CopySupport
     */
    static String CopySupport_warn_invalidSources(Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CopySupport.warn.invalidSources", project_name);
    }
    /**
     * @return <i>Copy Support failed for:</i>
     * @see FailedFilesPanel
     */
    static String FailedFilesPanel_title_local() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FailedFilesPanel.title.local");
    }
    /**
     * @return <i>Upload Files On Save failed for:</i>
     * @see FailedFilesPanel
     */
    static String FailedFilesPanel_title_remote() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FailedFilesPanel.title.remote");
    }
    /**
     * @param project_name project name
     * @param reason reason
     * @return <i>Upload Files On Save cannot continue for project </i>{@code project_name}<i>: </i>{@code reason}<i><br>Do you want to open Project Properties dialog now?</i>
     * @see RemoteOperationFactory
     */
    static String RemoteOperationFactory_error(Object project_name, Object reason) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteOperationFactory.error", project_name, reason);
    }
    private void Bundle() {}
}
