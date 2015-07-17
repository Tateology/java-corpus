package org.netbeans.modules.php.project.ui.customizer;
/** Localizable strings for {@link org.netbeans.modules.php.project.ui.customizer}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Browser</i>
     * @see CompositePanelProviderImpl
     */
    static String CompositePanelProviderImpl_category_browser_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CompositePanelProviderImpl.category.browser.title");
    }
    /**
     * @return <i>License Headers</i>
     * @see CompositePanelProviderImpl
     */
    static String CompositePanelProviderImpl_category_licenceHeaders_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CompositePanelProviderImpl.category.licenceHeaders.title");
    }
    /**
     * @return <i>Selenium Testing</i>
     * @see CompositePanelProviderImpl
     */
    static String CompositePanelProviderImpl_category_selenium_testing_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CompositePanelProviderImpl.category.selenium.testing.title");
    }
    /**
     * @return <i>Testing</i>
     * @see CompositePanelProviderImpl
     */
    static String CompositePanelProviderImpl_category_testing_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CompositePanelProviderImpl.category.testing.title");
    }
    /**
     * @param include_path_type_shared_private_ include path type (shared/private)
     * @param error_message error message
     * @return {@code include_path_type_shared_private_}<i>: </i>{@code error_message}
     * @see CustomizerIncludePath
     */
    static String CustomizerIncludePath_error(Object include_path_type_shared_private_, Object error_message) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerIncludePath.error", include_path_type_shared_private_, error_message);
    }
    /**
     * @return <i>Private</i>
     * @see CustomizerIncludePath
     */
    static String CustomizerIncludePath_tab_private_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerIncludePath.tab.private.title");
    }
    /**
     * @return <i>Shared</i>
     * @see CustomizerIncludePath
     */
    static String CustomizerIncludePath_tab_public_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerIncludePath.tab.public.title");
    }
    /**
     * @param include_path_type_shared_private_ include path type (shared/private)
     * @param warning_message warning message
     * @return {@code include_path_type_shared_private_}<i>: </i>{@code warning_message}
     * @see CustomizerIncludePath
     */
    static String CustomizerIncludePath_warning(Object include_path_type_shared_private_, Object warning_message) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerIncludePath.warning", include_path_type_shared_private_, warning_message);
    }
    /**
     * @param file_path file path
     * @param project_name project name
     * @return <i>Path </i>{@code file_path}<i> belongs to project </i>{@code project_name}<i>. Remove it and add Source Files of that project?</i>
     * @see CustomizerIncludePath
     */
    static String CustomizerPhpIncludePath_error_anotherProjectSubFile(Object file_path, Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerPhpIncludePath.error.anotherProjectSubFile", file_path, project_name);
    }
    /**
     * @param project_name project name
     * @return <i>Project </i>{@code project_name}<i> is broken, open and repair it manually.</i>
     * @see CustomizerIncludePath
     */
    static String CustomizerPhpIncludePath_error_brokenProject(Object project_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerPhpIncludePath.error.brokenProject", project_name);
    }
    /**
     * @return <i>Select Source Files</i>
     * @see CustomizerSources
     */
    static String CustomizerSources_src_browse_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerSources.src.browse.title");
    }
    /**
     * @return <i>For running tests, at least one testing provider must be selected.</i>
     * @see CustomizerTesting
     */
    static String CustomizerTesting_error_none() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerTesting.error.none");
    }
    /**
     * @return <i>No PHP testing provider found, install one via Plugins (e.g. PHPUnit).</i>
     * @see CustomizerTesting
     */
    static String CustomizerTesting_testingProviders_noneInstalled() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CustomizerTesting.testingProviders.noneInstalled");
    }
    /**
     * @return <i>Web Root directory does not exist (see Sources).</i>
     * @see RunAsLocalWeb
     */
    static String RunAsLocalWeb_webRoot_notFound() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunAsLocalWeb.webRoot.notFound");
    }
    /**
     * @return <i>Web Root directory does not exist (see Sources).</i>
     * @see RunAsRemoteWeb
     */
    static String RunAsRemoteWeb_webRoot_notFound() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunAsRemoteWeb.webRoot.notFound");
    }
    /**
     * @return <i>Select PHP Interpreter</i>
     * @see RunAsScript
     */
    static String RunAsScript_interpreter_browse_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunAsScript.interpreter.browse.title");
    }
    /**
     * @return <i>PHP Built-in Web Server (running on built-in web server)</i>
     * @see PhpProjectProperties
     */
    static String RunAsType_internal_label() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunAsType.internal.label");
    }
    /**
     * @return <i>Local Web Site (running on local web server)</i>
     * @see PhpProjectProperties
     */
    static String RunAsType_local_label() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunAsType.local.label");
    }
    /**
     * @return <i>Remote Web Site (FTP, SFTP)</i>
     * @see PhpProjectProperties
     */
    static String RunAsType_remote_label() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunAsType.remote.label");
    }
    /**
     * @return <i>Script (run in command line)</i>
     * @see PhpProjectProperties
     */
    static String RunAsType_script_label() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RunAsType.script.label");
    }
    /**
     * @param file_path file path
     * @param error_message error message
     * @return {@code file_path}<i>: </i>{@code error_message}
     * @see SeleniumTestDirectoriesPathSupport
     */
    static String SeleniumTestDirectoriesPathSupport_Validator_error(Object file_path, Object error_message) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "SeleniumTestDirectoriesPathSupport.Validator.error", file_path, error_message);
    }
    /**
     * @param file_path file path
     * @param error_message error message
     * @return {@code file_path}<i>: </i>{@code error_message}
     * @see TestDirectoriesPathSupport
     */
    static String TestDirectoriesPathSupport_Validator_error(Object file_path, Object error_message) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TestDirectoriesPathSupport.Validator.error", file_path, error_message);
    }
    private void Bundle() {}
}
