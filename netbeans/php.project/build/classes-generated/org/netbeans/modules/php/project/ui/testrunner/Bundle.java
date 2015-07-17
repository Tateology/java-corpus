package org.netbeans.modules.php.project.ui.testrunner;
/** Localizable strings for {@link org.netbeans.modules.php.project.ui.testrunner}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @param provider_name provider name
     * @param suite_name suite name
     * @return <i>[</i>{@code provider_name}<i>] </i>{@code suite_name}
     * @see TestSessionImpl
     */
    static String TestSessionImpl_suite_name(Object provider_name, Object suite_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "TestSessionImpl.suite.name", provider_name, suite_name);
    }
    /**
     * @param testing_probider testing probider
     * @return <i>Testing provider </i>{@code testing_probider}<i> does not support code coverage.</i>
     * @see UnitTestRunner
     */
    static String UnitTestRunner_error_coverage(Object testing_probider) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnitTestRunner.error.coverage", testing_probider);
    }
    /**
     * @return <i>Perhaps error occurred, verify in Output window.</i>
     * @see UnitTestRunner
     */
    static String UnitTestRunner_error_running() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "UnitTestRunner.error.running");
    }
    private void Bundle() {}
}
