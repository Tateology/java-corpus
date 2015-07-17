package org.netbeans.modules.php.project.problems;
/** Localizable strings for {@link org.netbeans.modules.php.project.problems}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Choose</i>
     * @see DirectoryProblemResolver
     */
    static String DirectoryProblemResolver_dialog_choose() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DirectoryProblemResolver.dialog.choose");
    }
    /**
     * @return <i>Some directories on project's Include Path are invalid.</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidIncludePath_description() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidIncludePath.description");
    }
    /**
     * @return <i>Invalid Include Path</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidIncludePath_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidIncludePath.title");
    }
    /**
     * @param selenium_dir_path selenium dir path
     * @return <i>The directory "</i>{@code selenium_dir_path}<i>" does not exist and cannot be used for Selenium Test Files.</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidSeleniumDir_description(Object selenium_dir_path) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidSeleniumDir.description", selenium_dir_path);
    }
    /**
     * @param project_name project name
     * @return <i>Select Selenium Test Files for </i>{@code project_name}
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidSeleniumDir_dialog_title(Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidSeleniumDir.dialog.title", project_name);
    }
    /**
     * @return <i>Invalid Selenium Test Files</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidSeleniumDir_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidSeleniumDir.title");
    }
    /**
     * @param src_dir_path src dir path
     * @return <i>The directory "</i>{@code src_dir_path}<i>" does not exist and cannot be used for Source Files.</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidSrcDir_description(Object src_dir_path) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidSrcDir.description", src_dir_path);
    }
    /**
     * @param project_name project name
     * @return <i>Select Source Files for </i>{@code project_name}
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidSrcDir_dialog_title(Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidSrcDir.dialog.title", project_name);
    }
    /**
     * @return <i>Invalid Source Files</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidSrcDir_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidSrcDir.title");
    }
    /**
     * @param test_dir_path test dir path
     * @return <i>The directory "</i>{@code test_dir_path}<i>" cannot be used for Test Files.</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidTestDir_description(Object test_dir_path) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidTestDir.description", test_dir_path);
    }
    /**
     * @return <i>Invalid Test Files</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidTestDir_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidTestDir.title");
    }
    /**
     * @param web_root_path web root path
     * @return <i>The directory "</i>{@code web_root_path}<i>" does not exist and cannot be used for Web Root.</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidWebRoot_description(Object web_root_path) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidWebRoot.description", web_root_path);
    }
    /**
     * @return <i>Invalid Web Root</i>
     * @see ProjectPropertiesProblemProvider
     */
    static String ProjectPropertiesProblemProvider_invalidWebRoot_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "ProjectPropertiesProblemProvider.invalidWebRoot.title");
    }
    private void Bundle() {}
}
