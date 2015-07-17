package org.netbeans.modules.php.project.connections;
/** Localizable strings for {@link org.netbeans.modules.php.project.connections}. */
@javax.annotation.Generated(value="org.netbeans.modules.openide.util.NbBundleProcessor")
class Bundle {
    /**
     * @return <i>Download ignored by user.</i>
     * @see RemoteClient
     */
    static String RemoteClient_download_ignored_byUser() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteClient.download.ignored.byUser");
    }
    /**
     * @param file_name file name
     * @return <i>Cannot open a local temporary file </i>{@code file_name}<i>.</i>
     * @see RemoteClient
     */
    static String RemoteClient_error_cannotOpenTmpLocalFile(Object file_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteClient.error.cannotOpenTmpLocalFile", file_name);
    }
    /**
     * @return <i>Given remote file is not a file.</i>
     * @see RemoteClient
     */
    static String RemoteClient_error_notFile() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteClient.error.notFile");
    }
    /**
     * @param file_name file name
     * @return <i>Replace unsaved file "</i>{@code file_name}<i>" with the content from the server?</i>
     * @see RemoteClient
     */
    static String RemoteClient_file_replaceUnsavedContent_question(Object file_name) {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteClient.file.replaceUnsavedContent.question", file_name);
    }
    /**
     * @return <i>Confirm replacement</i>
     * @see RemoteClient
     */
    static String RemoteClient_file_replaceUnsavedContent_title() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "RemoteClient.file.replaceUnsavedContent.title");
    }
    private void Bundle() {}
}
