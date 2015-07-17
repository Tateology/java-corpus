package org.netbeans.modules.php.project.connections.sync.diff;
/** Localizable strings for {@link org.netbeans.modules.php.project.connections.sync.diff}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Local Version</i>
     * @see BaseStreamSource
     */
    static String BaseStreamSource_title_local() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "BaseStreamSource.title.local");
    }
    /**
     * @return <i>Remote Version</i>
     * @see BaseStreamSource
     */
    static String BaseStreamSource_title_remote() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "BaseStreamSource.title.remote");
    }
    /**
     * @return <i>&amp;Take Over Local Changes</i>
     * @see DiffPanel
     */
    static String DiffPanel_button_titleWithMnemonics() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DiffPanel.button.titleWithMnemonics");
    }
    /**
     * @param file_name file name
     * @return <i>File </i>{@code file_name}<i> cannot be downloaded.</i>
     * @see DiffPanel
     */
    static String DiffPanel_error_cannotDownload(Object file_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DiffPanel.error.cannotDownload", file_name);
    }
    /**
     * @return <i>Cannot read files for comparison.</i>
     * @see DiffPanel
     */
    static String DiffPanel_error_cannotReadFiles() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DiffPanel.error.cannotReadFiles");
    }
    /**
     * @return <i>Content of file cannot be copied to temporary file.</i>
     * @see DiffPanel
     */
    static String DiffPanel_error_copyContent() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DiffPanel.error.copyContent");
    }
    /**
     * @return <i>Local file cannot be opened.</i>
     * @see DiffPanel
     */
    static String DiffPanel_error_opening() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DiffPanel.error.opening");
    }
    /**
     * @param file_path file path
     * @return <i>Remote Diff for </i>{@code file_path}
     * @see DiffPanel
     */
    static String DiffPanel_title(Object file_path) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "DiffPanel.title", file_path);
    }
    /**
     * @return <i>File is too big. Do you really want to open it?</i>
     * @see EditableTmpLocalFileStreamSource
     */
    static String EditableTmpLocalFileStreamSource_open_confirm() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "EditableTmpLocalFileStreamSource.open.confirm");
    }
    private void Bundle() {}
}
