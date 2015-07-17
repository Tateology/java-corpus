package org.netbeans.modules.php.project;
/** Localizable strings for {@link org.netbeans.modules.php.project}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Copy Support is still running - do you really want to reload the page?</i>
     * @see PhpProject
     */
    static String ClientSideDevelopmentSupport_reload_copySupportRunning() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ClientSideDevelopmentSupport.reload.copySupportRunning");
    }
    /**
     * @return <i>Customize...</i>
     * @see PhpProject
     */
    static String ClientSideDevelopmentSupport_reload_customize() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ClientSideDevelopmentSupport.reload.customize");
    }
    /**
     * @return <i>Review and correct important project settings detected by the IDE.</i>
     * @see PhpProject
     */
    static String PhpOpenedHook_notification_autoconfigured_details() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PhpOpenedHook.notification.autoconfigured.details");
    }
    /**
     * @param project_name project name
     * @return <i>Project </i>{@code project_name}<i> automatically configured</i>
     * @see PhpProject
     */
    static String PhpOpenedHook_notification_autoconfigured_title(Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PhpOpenedHook.notification.autoconfigured.title", project_name);
    }
    /**
     * @return <i>Selenium Test Files</i>
     * @see PhpProject
     */
    static String PhpProject_sourceRoots_selenium() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PhpProject.sourceRoots.selenium");
    }
    /**
     * @return <i>Source Files</i>
     * @see PhpProject
     */
    static String PhpProject_sourceRoots_sources() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PhpProject.sourceRoots.sources");
    }
    /**
     * @return <i>Test Files</i>
     * @see PhpProject
     */
    static String PhpProject_sourceRoots_tests() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "PhpProject.sourceRoots.tests");
    }
    /**
     * @return <i>Select a directory with project selenium test files.</i>
     * @see ProjectPropertiesSupport
     */
    static String ProjectPropertiesSupport_browse_selenium_test() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesSupport.browse.selenium.test");
    }
    /**
     * @return <i>Select a directory with project test files.</i>
     * @see ProjectPropertiesSupport
     */
    static String ProjectPropertiesSupport_browse_tests() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesSupport.browse.tests");
    }
    /**
     * @return <i>More directories can be added in Project Properties.</i>
     * @see ProjectPropertiesSupport
     */
    static String ProjectPropertiesSupport_browse_tests_info() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesSupport.browse.tests.info");
    }
    /**
     * @return <i>Saving project metadata...</i>
     * @see ProjectPropertiesSupport
     */
    static String ProjectPropertiesSupport_project_metadata_saving() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesSupport.project.metadata.saving");
    }
    /**
     * @param display_name_of_the_source_root display name of the source root
     * @param directory_of_the_source_root directory of the source root
     * @return {@code display_name_of_the_source_root}<i> (</i>{@code directory_of_the_source_root}<i>)</i>
     * @see SourceRoots
     */
    static String SourceRoots_displayName(Object display_name_of_the_source_root, Object directory_of_the_source_root) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "SourceRoots.displayName", display_name_of_the_source_root, directory_of_the_source_root);
    }
    private void Bundle() {}
}
