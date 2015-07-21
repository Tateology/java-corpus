/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * markiewb@netbeans.org
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.source.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.apache.lucene.document.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClassIndex.SearchScopeType;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.java.BinaryElementOpen;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexImplEvent;
import org.netbeans.modules.java.source.usages.ClassIndexImplListener;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.DocumentUtil;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.IndexManager.Action;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hrebejk
 * @author Tomas Zezula
 * @author markiewb
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.type.TypeProvider.class)
public class JavaTypeProvider implements TypeProvider {
    private static final Logger LOGGER = Logger.getLogger(JavaTypeProvider.class.getName());
    private static final Level LEVEL = Level.FINE;
    private static final Collection<? extends JavaTypeDescription> ACTIVE = Collections.unmodifiableSet(new HashSet<JavaTypeDescription>());  //Don't replace with C.emptySet() has to be identity unique.

    //@NotThreadSafe //Confinement within a thread
    private Map<URI,CacheItem> rootCache;    

    private volatile boolean isCanceled = false;
    private ClasspathInfo cpInfo;

    private final TypeElementFinder.Customizer customizer;

    @Override
    public String name() {
        return "java"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        // TODO - i18n
        return "Java Classes";
    }

    @Override
    public void cleanup() {
        isCanceled = false;
        DataCache.clear();
        setRootCache(null);
    }

    @Override
    public void cancel() {
        isCanceled = true;
    }

    public JavaTypeProvider() {
        this(null, null);
    }

    public JavaTypeProvider(ClasspathInfo cpInfo, TypeElementFinder.Customizer customizer) {
        this.cpInfo = cpInfo;
        this.customizer = customizer;
    }

    @Override
    public void computeTypeNames(Context context, final Result res) {
        isCanceled = false;
        String originalText = context.getText();
        SearchType searchType = context.getSearchType();

        final DataCache dataCache = DataCache.forText(originalText, searchType);
        assert dataCache != null;
        final CacheItem.DataCacheCallback callBack = new CacheItem.DataCacheCallback() {
            @Override
            public void handleDataCacheChange(@NonNull final CacheItem ci) {
                assert ci != null;
                dataCache.put(ci, null);
            }
        };

        boolean hasBinaryOpen = Lookup.getDefault().lookup(BinaryElementOpen.class) != null;
        final ClassIndex.NameKind nameKind;
        switch (searchType) {
        case EXACT_NAME: nameKind = ClassIndex.NameKind.SIMPLE_NAME; break;
        case CASE_INSENSITIVE_EXACT_NAME: nameKind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP; break;
        case PREFIX: nameKind = ClassIndex.NameKind.PREFIX; break;
        case CASE_INSENSITIVE_PREFIX: nameKind = ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX; break;
        case REGEXP: nameKind = ClassIndex.NameKind.REGEXP; break;
        case CASE_INSENSITIVE_REGEXP: nameKind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP; break;
        case CAMEL_CASE: nameKind = ClassIndex.NameKind.CAMEL_CASE; break;
        default: throw new RuntimeException("Unexpected search type: " + searchType);
        }

        if (getRootCache() == null) {
            Map<URI,CacheItem> sources = null;

            if (cpInfo == null) {
                sources = new HashMap<URI,CacheItem>();

                // Sources - ClassPath.SOURCE and translated ClassPath.COMPILE & ClassPath.BOOT
                Collection<FileObject> srcRoots = QuerySupport.findRoots(
                        (Project)null,
                        Collections.singleton(ClassPath.SOURCE),
                        Collections.<String>emptySet(),
                        Collections.<String>emptySet());

                for(FileObject root : srcRoots) {
                    if ( isCanceled ) {
                        return;
                    }
                    final URL rootUrl = root.toURL();
                    if ( isCanceled ) {
                        return;
                    } else {
                        try {
                            sources.put(rootUrl.toURI(), new CacheItem( rootUrl, ClassPath.SOURCE, callBack));
                        } catch (URISyntaxException ex) {
                            LOGGER.log(Level.INFO, "Cannot convert root {0} into URI, ignoring.", rootUrl);  //NOI18N
                        }
                    }
                }

                // Binaries - not translated ClassPath.COMPILE & ClassPath.BOOT
                Collection<FileObject> binRoots = QuerySupport.findRoots(
                        (Project)null,
                        Collections.<String>emptySet(),
                        Collections.<String>emptySet(),
                        Arrays.asList(new String [] { ClassPath.COMPILE, ClassPath.BOOT}));

                for(FileObject root : binRoots) {
                    if ( isCanceled ) {
                        return;
                    }
                    URL rootUrl = root.toURL();
                    if (!hasBinaryOpen) {
                        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(rootUrl);
                        if ( result.getRoots().length == 0 ) {
                            continue;
                        }
                    }
                    if ( isCanceled ) {
                        return;
                    }
                    else {
                        try {
                            final URI rootURI = rootUrl.toURI();
                            if (!sources.containsKey(rootURI)) {
                                sources.put(rootURI, new CacheItem( rootUrl, ClassPath.BOOT, callBack));
                            }
                        } catch (URISyntaxException ex) {
                            LOGGER.log(Level.INFO, "Cannot convert root {0} into URI, ignoring.", rootUrl);  //NOI18N
                        }
                    }
                }
            } else { // user provided classpath

                final List<ClassPath.Entry> bootRoots = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT).entries();
                final List<ClassPath.Entry> compileRoots = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE).entries();
                final List<ClassPath.Entry> sourceRoots = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).entries();
                sources = new HashMap<URI,CacheItem>(bootRoots.size() + compileRoots.size() + sourceRoots.size());

                // bootPath
                for (ClassPath.Entry entry : bootRoots) {
                    if ( isCanceled ) {
                        return;
                    }
                    else {
                        try {
                            sources.put(entry.getURL().toURI(), new CacheItem(entry.getURL(),ClassPath.BOOT, callBack));
                        } catch (URISyntaxException ex) {
                            LOGGER.log(Level.INFO, "Cannot convert root {0} into URI, ignoring.", entry.getURL());  //NOI18N
                        }
                    }
                }

                // classPath
                for (ClassPath.Entry entry : compileRoots) {
                    if ( isCanceled ) {
                        return;
                    }
                    else {
                        try {
                            sources.put(entry.getURL().toURI(), new CacheItem(entry.getURL(),ClassPath.COMPILE, callBack));
                        } catch (URISyntaxException ex) {
                            LOGGER.log(Level.INFO, "Cannot convert root {0} into URI, ignoring.", entry.getURL());  //NOI18N
                        }
                    }
                }

                // sourcePath
                for (ClassPath.Entry entry : sourceRoots) {
                    if ( isCanceled ) {
                        return;
                    }
                    else {
                        try {
                            sources.put(entry.getURL().toURI(), new CacheItem(entry.getURL(),ClassPath.SOURCE, callBack));
                        } catch (URISyntaxException ex) {
                            LOGGER.log(Level.INFO, "Cannot convert root {0} into URI, ignoring.", entry.getURL());  //NOI18N
                        }
                    }
                }
            }

            if ( !isCanceled ) {
//                cache = sources;
                if (LOGGER.isLoggable(LEVEL)) {
                    LOGGER.log(LEVEL, "Querying following roots:"); //NOI18N
                    for(CacheItem ci : sources.values()) {
                        LOGGER.log(LEVEL, "  {0}; binary={1}", new Object[]{ci.getRoot().toURI(), ci.isBinary()}); //NOI18N
                    }
                    LOGGER.log(LEVEL, "-------------------------"); //NOI18N
                }
                setRootCache(sources);
            }
            else {
                return;
            }

        }

        final Map<URI,CacheItem> c = getRootCache();
        if (c == null) return;
        final ArrayList<JavaTypeDescription> types = new ArrayList<JavaTypeDescription>(c.size() * 20);

        // is scan in progress? If so, provide a message to user.
        final boolean scanInProgress = SourceUtils.isScanInProgress();
        if (scanInProgress) {
            // ui message
            String message = NbBundle.getMessage(JavaTypeProvider.class, "LBL_ScanInProgress_warning");
            res.setMessage(message);
        } else {
            res.setMessage(null);
        }
        int lastIndexOfDot = originalText.lastIndexOf("."); //NOI18N
        boolean isFullyQualifiedName = -1 != lastIndexOfDot;
        final Pattern packageName;
        final String typeName;
        if (isFullyQualifiedName) {
            packageName = createPackageRegExp(originalText.substring(0, lastIndexOfDot));
            typeName = originalText.substring(lastIndexOfDot + 1);
            res.setHighlightText(typeName);
        } else {
            packageName = null;
            typeName = originalText;
        }
        final String textForQuery = getTextForQuery(typeName, nameKind, context.getSearchType());

        LOGGER.log(Level.FINE, "Text For Query ''{0}''.", originalText);
        if (customizer != null) {
            for (final CacheItem ci : c.values()) {
                final Set<ElementHandle<TypeElement>> names = new HashSet<ElementHandle<TypeElement>> (customizer.query(
                        ci.getClasspathInfo(), textForQuery, nameKind,  //Needs to pass slow cpinfo to keep compatibility
                        EnumSet.of(ci.isBinary ? ClassIndex.SearchScope.DEPENDENCIES : ClassIndex.SearchScope.SOURCE)
                ));
                if (nameKind == ClassIndex.NameKind.CAMEL_CASE) {
                    names.addAll(customizer.query(
                        ci.getClasspathInfo(), textForQuery, ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX, //Needs to pass slow cpinfo to keep compatibility
                        EnumSet.of(ci.isBinary ? ClassIndex.SearchScope.DEPENDENCIES : ClassIndex.SearchScope.SOURCE)
                    ));
                }
                for (ElementHandle<TypeElement> name : names) {
                    ci.initIndex();
                    JavaTypeDescription td = new JavaTypeDescription(ci, name);
                    types.add(td);
                    if (isCanceled) {
                        return;
                    }
                }
            }
        } else {
            final Collection<CacheItem> nonCached = new ArrayDeque<CacheItem>(c.size());
            for (CacheItem ci : c.values()) {
                Collection<? extends JavaTypeDescription> cacheLine = dataCache.get(ci);
                if (cacheLine != null) {                    
                    types.addAll(cacheLine);
                } else {
                    nonCached.add(ci);
                }
            }
            if (!nonCached.isEmpty()) {
                try {
                    //Perform queries in single readAccess to suspend RU for all queries.
                    IndexManager.priorityAccess(new Action<Void>() {
                        @Override
                        public Void run() throws IOException, InterruptedException {
                            for (final CacheItem ci : nonCached) {
                                if (isCanceled) {
                                    return null;
                                }
                                try {
                                    final Collection<JavaTypeDescription> ct = new ArrayList<JavaTypeDescription>();
                                    boolean exists = false;
                                    //WB(dataCache[ci], ACTIVE)
                                    dataCache.put(ci, ACTIVE);
                                    try {
                                        exists = ci.collectDeclaredTypes(packageName, textForQuery,nameKind, ct);                                        
                                        if (exists) {
                                            types.addAll(ct);
                                        }
                                    } finally {
                                        if (exists) {
                                            //CAS(dataCache[ci], ACTIVE, ct)
                                            dataCache.compareAndSet(
                                                ci,
                                                ACTIVE,
                                                ct.isEmpty() ? Collections.<JavaTypeDescription>emptySet() : ct);
                                        } else {
                                            //WB(dataCache[ci], NULL)
                                            dataCache.put(ci, null);
                                        }
                                    }
                                } catch (IOException ioe) {
                                    Exceptions.printStackTrace(ioe);
                                } catch (InterruptedException ie) {
                                    //Never happens
                                    throw new AssertionError(ie);
                                }
                            }
                            return null;
                        }
                    });
                } catch (IOException ex) {
                    //Never happens
                    throw new AssertionError(ex);
                } catch (InterruptedException ex) {
                    //Never happens
                    throw new AssertionError(ex);
                }
            }
            if ( isCanceled ) {
                return;
            }
            if (scanInProgress) {
                res.pendingResult();
            }
        }
        if ( !isCanceled ) {
            // Sorting is now done on the Go To Tpe dialog side
            // Collections.sort(types);
            res.addResult(types);
        }
    }

    static String removeNonJavaChars(String text) {
       StringBuilder sb = new StringBuilder();

       for( int i = 0; i < text.length(); i++) {
           char c = text.charAt(i);
           if( Character.isJavaIdentifierPart(c) || c == '*' || c == '?') {
               sb.append(c);
           }
       }
       return sb.toString();
    }

    @CheckForNull
    private Map<URI, CacheItem> getRootCache() {
        if (LOGGER.isLoggable(LEVEL) && rootCache == null) {
            LOGGER.log(LEVEL, "Returning null cache entries.", new Exception());
        }
        return rootCache == null ? null : Collections.<URI, CacheItem>unmodifiableMap(rootCache);
    }

    private void setRootCache(@NullAllowed final Map<URI,CacheItem> cache) {
        if (LOGGER.isLoggable(LEVEL)) {
            LOGGER.log(LEVEL, "Setting cache entries from " + this.rootCache + " to " + cache + ".", new Exception());
        }
        if (this.rootCache != null) {
            for (CacheItem ci : rootCache.values()) {
                ci.dispose();
            }
        }
        this.rootCache = cache;
    }

    private static String getTextForQuery(String text, final NameKind nameKind, SearchType searchType) {
        String textForQuery;
        switch (nameKind) {
            case REGEXP:
            case CASE_INSENSITIVE_REGEXP:
                textForQuery = NameMatcherFactory.wildcardsToRegexp(removeNonJavaChars(text), searchType != SearchType.CASE_INSENSITIVE_EXACT_NAME);
                break;
            default:
                textForQuery = text;
        }
        return textForQuery;
    }

    @CheckForNull
    private static Pattern createPackageRegExp(@NonNull String pkgName) {
        final StringBuilder sb = new StringBuilder();
        sb.append("(.*\\.)?");  //NOI18N
        boolean valid = false;
        for (int i=0; i< pkgName.length(); i++) {
            char c = pkgName.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                sb.append(c);
                valid = true;
            } else if (c == '.') {  //NOI18N
                sb.append(".*\\."); //NOI18N
            }
        }
        final Pattern p;
        if (valid) {
            sb.append(".*(\\..*)?");  //NOI18N
            p = Pattern.compile(sb.toString());
        } else {
            p = null;
        }
        LOGGER.log(Level.FINE, "Package pattern: {0}", p); //NOI18N
        return p;
    }

    /**
     * Todo: Create an API and remove
     */
    @NonNull
    private static NameKind translateSearchType(
            @NonNull String simpleName,
            @NonNull final NameKind originalSearchType) {
        if (originalSearchType == NameKind.SIMPLE_NAME ||
            originalSearchType == NameKind.CASE_INSENSITIVE_REGEXP) {
            return originalSearchType;
        } else if ((isAllUpper(simpleName) && simpleName.length() > 1) || isCamelCase(simpleName)) {
            return NameKind.CAMEL_CASE;
        } else if (containsWildCard(simpleName) != -1) {
            return isCaseSensitive(originalSearchType) ? NameKind.REGEXP : NameKind.CASE_INSENSITIVE_REGEXP;
        } else {
            return isCaseSensitive(originalSearchType) ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX;
        }
    }

    private static boolean isCaseSensitive(@NonNull final NameKind originalNameKind) {
        switch (originalNameKind) {
            case CAMEL_CASE_INSENSITIVE:
            case CASE_INSENSITIVE_PREFIX:
            case CASE_INSENSITIVE_REGEXP:
                return false;
            default:
                return true;
        }
    }

    private static int containsWildCard(@NonNull final String text) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( text.charAt( i ) == '?' || text.charAt( i ) == '*' ) { // NOI18N
                return i;
            }
        }
        return -1;
    }

    private static boolean isAllUpper(@NonNull final String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( !Character.isUpperCase( text.charAt( i ) ) ) {
                return false;
            }
        }
        return true;
    }

    private static Pattern camelCasePattern = Pattern.compile("(?:\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\p{Digit}|\\.|\\$)*){2,}"); // NOI18N
    
    private static boolean isCamelCase(String text) {
         return camelCasePattern.matcher(text).matches();
    }

    //@NotTreadSafe
    static final class CacheItem implements ClassIndexImplListener {
        
        private final URI rootURI;
        private final boolean isBinary;
        private final String cpType;

        private DataCacheCallback callBack;
        private String projectName;
        private Icon projectIcon;
        private ClasspathInfo cpInfo;
        private ClassIndexImpl index;        
        private FileObject cachedRoot;

        public CacheItem (
                @NullAllowed final URL root,
                @NullAllowed final String cpType,
                @NullAllowed final DataCacheCallback callBack) throws URISyntaxException  {
            this.cpType = cpType;
            this.isBinary = ClassPath.BOOT.equals(cpType) || ClassPath.COMPILE.equals(cpType);
            this.rootURI = root == null ? null : root.toURI();
            this.callBack = callBack;
        }

        @Override
        public int hashCode () {
            return this.rootURI == null ? 0 : this.rootURI.hashCode();
        }

        @Override
        public boolean equals (Object other) {
            if (other == this) {
                return true;
            }
            if (other instanceof CacheItem) {
                CacheItem otherItem = (CacheItem) other;
                return this.rootURI == null ? otherItem.rootURI == null : this.rootURI.equals(otherItem.rootURI);
            }
            return false;
        }

        public FileObject getRoot() {
            synchronized (this) {
                if (cachedRoot != null) {
                    return cachedRoot;
                }
            }
            final URL root = toURL(rootURI);
            final FileObject _tmp = root == null ?
                    null :
                    URLMapper.findFileObject(root);
            synchronized (this) {
                if (cachedRoot == null) {
                    cachedRoot = _tmp;
                }
            }
            return _tmp;
        }

        public boolean isBinary() {
            return isBinary;
        }

        public synchronized String getProjectName() {
            if ( !isBinary && projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }

        public synchronized Icon getProjectIcon() {
            if ( !isBinary && projectIcon == null ) {
                initProjectInfo();
            }
            return projectIcon;
        }
        
        public ClasspathInfo getClasspathInfo() {
            if (cpInfo == null) {            
                final ClassPath cp = ClassPathSupport.createClassPath(toURL(rootURI));
                cpInfo = isBinary ? 
                    ClassPath.BOOT.equals(cpType) ?
                        ClasspathInfo.create(cp,ClassPath.EMPTY,ClassPath.EMPTY):
                        ClasspathInfo.create(ClassPath.EMPTY,cp,ClassPath.EMPTY):
                    ClasspathInfo.create(ClassPath.EMPTY,ClassPath.EMPTY,cp);                
            }
            return cpInfo;
        }
        
        private boolean initIndex() {
            if (index == null) {
                final URL root = toURL(rootURI);
                index = root == null ?
                    null :
                    ClassIndexManager.getDefault().getUsagesQuery(root, true);
                if (index == null) {
                    return false;
                }
                index.addClassIndexImplListener(this);
            }            
            return true;
        }

        public  boolean collectDeclaredTypes(
            @NullAllowed final Pattern packageName,
            @NonNull final String typeName,
            @NonNull NameKind kind,
            @NonNull Collection<? super JavaTypeDescription> collector) throws IOException, InterruptedException {
            if (!initIndex()) {
                return false;
            }
            final SearchScope baseSearchScope = isBinary ? ClassIndex.SearchScope.DEPENDENCIES : ClassIndex.SearchScope.SOURCE;
            SearchScopeType searchScope;
            if (packageName != null) {
                //FQN
                final Set<String> allPackages = new HashSet<String>();
                index.getPackageNames("", false, allPackages);  //NOI18N
                final Set<? extends String> packages = filterPackages(packageName, allPackages);
                searchScope = ClassIndex.createPackageSearchScope(baseSearchScope, packages.toArray(new String[packages.size()]));
                kind = translateSearchType(typeName, kind);
            } else {
                //simple name
                searchScope = baseSearchScope;
            }
            try {
                index.getDeclaredTypes(
                    typeName,
                    kind,
                    Collections.unmodifiableSet(Collections.<SearchScopeType>singleton(searchScope)),
                    DocumentUtil.declaredTypesFieldSelector(true),
                    new JavaTypeDescriptionConvertor(this),
                    collector);
            } catch (Index.IndexClosedException ice) {
                //Closed after put into rootCache, ignore
            }
            return true;
        }
        
        @Override
        public void typesAdded(@NonNull final ClassIndexImplEvent event) {
            if (callBack != null) {
                callBack.handleDataCacheChange(this);
            }
        }

        @Override
        public void typesRemoved(@NonNull final ClassIndexImplEvent event) {
            if (callBack != null) {
                callBack.handleDataCacheChange(this);
            }
        }

        @Override
        public void typesChanged(@NonNull final ClassIndexImplEvent event) {
            if (callBack != null) {
                callBack.handleDataCacheChange(this);
            }
        }

        @CheckForNull
        URI getRootURI() {
            return rootURI;
        }
        
        @CheckForNull
        ClassIndexImpl getClassIndex() {
            return index;
        }
        
        private void initProjectInfo() {
            Project p = FileOwnerQuery.getOwner(this.rootURI);
            if (p != null) {
                final ProjectInformation pi = p.getLookup().lookup(ProjectInformation.class);   //Intentionally does not use ProjectUtil.getInformation() as it does slow icon annotation
                projectName = pi == null ?
                    p.getProjectDirectory().getNameExt() :
                    pi.getDisplayName();
                projectIcon = pi == null ?
                    null :
                    pi.getIcon();
            }
        }

        @NonNull
        private Set<? extends String> filterPackages(
                @NonNull final Pattern packageName,
                @NonNull final Set<? extends String> basePackages) {
            final Set<String> result = new HashSet<String>();
            for (String pkg : basePackages) {
                if (packageName.matcher(pkg).matches()) {
                    result.add(pkg);
                }
            }
            return result;
        }

        private void dispose() {
            callBack = null;
            if (index != null) {
                index.removeClassIndexImplListener(this);
            }
        }

        @CheckForNull
        private static URL toURL(@NullAllowed final URI uri) {
            try {
                return uri == null ?
                    null :
                    uri.toURL();
            } catch (MalformedURLException ex) {
                LOGGER.log(
                    Level.FINE,
                    "Cannot convert URI to URL",    //NOI18N
                    ex);
                return null;
            }
        }

        static interface DataCacheCallback {
            void handleDataCacheChange(@NonNull final CacheItem ci);
        }

        private static class JavaTypeDescriptionConvertor implements Convertor<Document, JavaTypeDescription> {

            private static final Pattern ANONYMOUS = Pattern.compile(".*\\$\\d+(C|I|E|A|\\$.+)");   //NOI18N
            private static final Convertor<Document,ElementHandle<TypeElement>> HANDLE_CONVERTOR = DocumentUtil.elementHandleConvertor();
            private static Convertor<Document,String> SOURCE_CONVERTOR = DocumentUtil.sourceNameConvertor();

            private final CacheItem ci;

            JavaTypeDescriptionConvertor(@NonNull final CacheItem ci) {
                this.ci = ci;
            }

            @Override
            public JavaTypeDescription convert(Document p) {
                final String binName = DocumentUtil.getSimpleBinaryName(p);
                if (binName == null || ANONYMOUS.matcher(binName).matches()) {
                    return null;
                }
                final ElementHandle<TypeElement> eh = HANDLE_CONVERTOR.convert(p);
                final String sourceName = SOURCE_CONVERTOR.convert(p);
                return eh == null ? null : new JavaTypeDescription(ci, eh, sourceName);
            }

        }

    }

    //@ThreadSafe
    private static final class DataCache {

        //@GuardedBy("DataCache.class")
        private static String forText;
        //@GuardedBy("DataCache.class")
        private static final Map<SearchType,DataCache> instances =
            new EnumMap<>(SearchType.class);

        //@GuardedBy("this")
        private final Map<CacheItem,Collection<? extends JavaTypeDescription>> dataCache = new HashMap<>();


        private DataCache() {}

        @CheckForNull
        synchronized Collection<? extends JavaTypeDescription> get(@NonNull final CacheItem item) {
            return dataCache.get(item);
        }

        synchronized void put (
            @NonNull final CacheItem item,
            @NullAllowed final Collection<? extends JavaTypeDescription> data) {
            dataCache.put(item, data);
        }

        synchronized boolean compareAndSet(
            @NonNull final CacheItem item,
            @NullAllowed final Collection<? extends JavaTypeDescription> expected,
            @NullAllowed final Collection<? extends JavaTypeDescription> update) {
            if (dataCache.get(item) == expected) {
                dataCache.put(item,update);
                return true;
            }
            return false;
        }        

        static synchronized void clear() {
            forText = null;
            instances.clear();
        }

        @NonNull
        static synchronized DataCache forText(
            @NonNull final String text,
            @NonNull final SearchType searchType) {
            Parameters.notNull("text", text);   //NOI18N
            Parameters.notNull("searchType", searchType);   //NOI18N
            if (!text.equals(forText)) {
                clear();
                forText = text;
            }
            DataCache cacheInstance = instances.get(searchType);
            if (cacheInstance == null) {
                cacheInstance = new DataCache();
                instances.put(searchType, cacheInstance);
            }
            return cacheInstance;
        }

    }
}