package org.netbeans.modules.php.project.classpath;
/** Localizable strings for {@link org.netbeans.modules.php.project.classpath}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @param file_path file path
     * @param project_name project name
     * @return <i>Path </i>{@code file_path}<i> belongs to project </i>{@code project_name}<i>. Remove it and add Source Files of that project.</i>
     * @see IncludePathSupport
     */
    static String IncludePathSupport_Validator_error_anotherProjectSubFile(Object file_path, Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IncludePathSupport.Validator.error.anotherProjectSubFile", file_path, project_name);
    }
    /**
     * @param file_path file path
     * @return <i>Path </i>{@code file_path}<i> does not exist.</i>
     * @see IncludePathSupport
     */
    static String IncludePathSupport_Validator_error_notFound(Object file_path) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IncludePathSupport.Validator.error.notFound", file_path);
    }
    /**
     * @param file_path file path
     * @return <i>Path </i>{@code file_path}<i> is already part of project.</i>
     * @see IncludePathSupport
     */
    static String IncludePathSupport_Validator_error_projectFile(Object file_path) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "IncludePathSupport.Validator.error.projectFile", file_path);
    }
    private void Bundle() {}
}
