package org.netbeans.modules.php.project.internalserver;
/** Localizable strings for {@link org.netbeans.modules.php.project.internalserver}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @param project_name project name
     * @return <i>Cannot cancel running internal web server for project </i>{@code project_name}<i>.</i>
     * @see InternalWebServer
     */
    static String InternalWebServer_error_cancelProcess(Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InternalWebServer.error.cancelProcess", project_name);
    }
    /**
     * @return <i>Timeout occured while stopping PHP built-in web server.</i>
     * @see InternalWebServer
     */
    static String InternalWebServer_error_stop() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InternalWebServer.error.stop");
    }
    /**
     * @param project_name project name
     * @return <i>Internal WebServer [</i>{@code project_name}<i>]</i>
     * @see InternalWebServer
     */
    static String InternalWebServer_output_title(Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InternalWebServer.output.title", project_name);
    }
    /**
     * @param project_name project name
     * @return <i>Stopping PHP built-in web server for project </i>{@code project_name}<i>...</i>
     * @see InternalWebServer
     */
    static String InternalWebServer_stopping(Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "InternalWebServer.stopping", project_name);
    }
    private void Bundle() {}
}
