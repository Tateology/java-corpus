package org.netbeans.modules.php.project.connections.common;
/** Localizable strings for {@link org.netbeans.modules.php.project.connections.common}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Remote Error</i>
     * @see RemoteUtils
     */
    static String LBL_RemoteError() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "LBL_RemoteError");
    }
    /**
     * @return <i>Keep-alive interval must be a number.</i>
     * @see RemoteValidator
     */
    static String MSG_KeepAliveNotNumeric() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "MSG_KeepAliveNotNumeric");
    }
    /**
     * @return <i>Keep-alive interval must be higher than or equal to 0.</i>
     * @see RemoteValidator
     */
    static String MSG_KeepAliveNotPositive() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "MSG_KeepAliveNotPositive");
    }
    /**
     * @param reason_of_the_failure reason of the failure
     * @return <i><br><br>Reason: </i>{@code reason_of_the_failure}
     * @see RemoteUtils
     */
    static String MSG_RemoteErrorReason(Object reason_of_the_failure) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "MSG_RemoteErrorReason", reason_of_the_failure);
    }
    /**
     * @return <i>Timeout must be a number.</i>
     * @see RemoteValidator
     */
    static String MSG_TimeoutNotNumeric() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "MSG_TimeoutNotNumeric");
    }
    /**
     * @return <i>Timeout must be higher than or equal to 0.</i>
     * @see RemoteValidator
     */
    static String MSG_TimeoutNotPositive() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "MSG_TimeoutNotPositive");
    }
    /**
     * @param invalid_path_separator invalid path separator
     * @return <i>Upload directory cannot contain "</i>{@code invalid_path_separator}<i>".</i>
     * @see RemoteValidator
     */
    static String RemoteValidator_error_uploadDirectory_content(Object invalid_path_separator) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteValidator.error.uploadDirectory.content", invalid_path_separator);
    }
    /**
     * @return <i>Upload directory must be specified.</i>
     * @see RemoteValidator
     */
    static String RemoteValidator_error_uploadDirectory_missing() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteValidator.error.uploadDirectory.missing");
    }
    /**
     * @param remote_path_separator remote path separator
     * @return <i>Upload directory must start with "</i>{@code remote_path_separator}<i>".</i>
     * @see RemoteValidator
     */
    static String RemoteValidator_error_uploadDirectory_start(Object remote_path_separator) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteValidator.error.uploadDirectory.start", remote_path_separator);
    }
    private void Bundle() {}
}
