package org.netbeans.modules.php.project.connections.ftp;
/** Localizable strings for {@link org.netbeans.modules.php.project.connections.ftp}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Only passive mode is supported with HTTP proxy.</i>
     * @see FtpConfigurationValidator
     */
    static String FtpConfigurationValidator_error_proxyAndNotPassive() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FtpConfigurationValidator.error.proxyAndNotPassive");
    }
    /**
     * @return <i>Detecting HTTP proxy...</i>
     * @see FtpConfigurationValidator
     */
    static String FtpConfigurationValidator_proxy_detecting() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FtpConfigurationValidator.proxy.detecting");
    }
    /**
     * @return <i>Configured HTTP proxy will be used only for Pure FTP. To avoid problems, do not use any SOCKS proxy.</i>
     * @see FtpConfigurationValidator
     */
    static String FtpConfigurationValidator_warning_proxy() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "FtpConfigurationValidator.warning.proxy");
    }
    /**
     * @return <i>Pure FTP</i>
     * @see FtpConfiguration
     */
    static String LBL_EncryptionNone() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "LBL_EncryptionNone");
    }
    /**
     * @return <i>Explicit FTP using TLS</i>
     * @see FtpConfiguration
     */
    static String LBL_EncryptionTlsExplicit() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "LBL_EncryptionTlsExplicit");
    }
    /**
     * @return <i>Implicit FTP using TLS</i>
     * @see FtpConfiguration
     */
    static String LBL_EncryptionTlsImplicit() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "LBL_EncryptionTlsImplicit");
    }
    private void Bundle() {}
}
