package org.netbeans.modules.php.project.runconfigs.validation;
/** Localizable strings for {@link org.netbeans.modules.php.project.runconfigs.validation}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Index File</i>
     * @see BaseRunConfigValidator
     */
    static String BaseRunConfigValidator_error_index_label() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "BaseRunConfigValidator.error.index.label");
    }
    /**
     * @param source_of_error source of error
     * @return {@code source_of_error}<i> must be a valid relative URL.</i>
     * @see BaseRunConfigValidator
     */
    static String BaseRunConfigValidator_error_relativeFile_invalid(Object source_of_error) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "BaseRunConfigValidator.error.relativeFile.invalid", source_of_error);
    }
    /**
     * @param source_of_error source of error
     * @return {@code source_of_error}<i> must be specified in order to run or debug project in command line.</i>
     * @see BaseRunConfigValidator
     */
    static String BaseRunConfigValidator_error_relativeFile_missing(Object source_of_error) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "BaseRunConfigValidator.error.relativeFile.missing", source_of_error);
    }
    /**
     * @return <i>Router</i>
     * @see RunConfigInternalValidator
     */
    static String RunConfigInternalValidator_router_label() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunConfigInternalValidator.router.label");
    }
    /**
     * @param error error
     * @return <i>Remote Connection: </i>{@code error}
     * @see RunConfigRemoteValidator
     */
    static String RunConfigRemoteValidator_error_remoteConnection(Object error) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunConfigRemoteValidator.error.remoteConnection", error);
    }
    /**
     * @return <i>Upload files type must be selected.</i>
     * @see RunConfigRemoteValidator
     */
    static String RunConfigRemoteValidator_error_uploadFilesType_none() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunConfigRemoteValidator.error.uploadFilesType.none");
    }
    /**
     * @return <i>Working directory</i>
     * @see RunConfigScriptValidator
     */
    static String RunConfigScriptValidator_workDir_label() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunConfigScriptValidator.workDir.label");
    }
    /**
     * @return <i>Project URL is not valid.</i>
     * @see RunConfigWebValidator
     */
    static String RunConfigWebValidator_error_url_invalid() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunConfigWebValidator.error.url.invalid");
    }
    private void Bundle() {}
}
