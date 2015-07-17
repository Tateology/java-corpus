package org.netbeans.modules.php.project.connections.sftp;
/** Localizable strings for {@link org.netbeans.modules.php.project.connections.sftp}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Private Key</i>
     * @see SftpConfigurationValidator
     */
    static String SftpConfigurationValidator_identityFile() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "SftpConfigurationValidator.identityFile");
    }
    /**
     * @return <i>Known Hosts</i>
     * @see SftpConfigurationValidator
     */
    static String SftpConfigurationValidator_knownHosts() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "SftpConfigurationValidator.knownHosts");
    }
    /**
     * @return <i>&lt;html>&lt;b>Error in SFTP library detected:&lt;/b>&lt;br>&lt;br>Your Known Hosts file is too big and will not be used.</i>
     * @see SftpClient
     */
    static String SftpConfiguration_bug_knownHosts() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "SftpConfiguration.bug.knownHosts");
    }
    private void Bundle() {}
}
