/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client;

import org.netbeans.modules.subversion.client.commands.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 * intended to be run with 1.8 client
 * @author tomas
 */
public class SvnKitClientTest extends NbTestCase {
    // XXX test cancel

    public SvnKitClientTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static Test suite() throws Exception {

        System.setProperty("svnClientAdapterFactory", "svnkit");

        TestSuite suite = new TestSuite();        
        suite.addTestSuite(AddTestHidden.class);
        suite.addTestSuite(BlameTestHidden.class);
        suite.addTestSuite(CatTestHidden.class);
        suite.addTestSuite(CheckoutTestHidden.class);
        suite.addTestSuite(CommitTestHidden.class);
        suite.addTestSuite(CopyTestHidden.class);
        suite.addTestSuite(DifferentWorkingDirsTestHidden.class);
        suite.addTestSuite(ImportTestHidden.class);
        suite.addTestSuite(InfoTestHidden.class);
        suite.addTestSuite(ListTestHidden.class);
        suite.addTestSuite(LogTestHidden.class);
        suite.addTestSuite(MergeTestHidden.class);
        suite.addTestSuite(MkdirTestHidden.class);
        suite.addTestSuite(MoveTestHidden.class);
        suite.addTestSuite(ParsedStatusTestHidden.class);
        suite.addTestSuite(PropertyTestHidden.class);
        suite.addTestSuite(RelocateTestHidden.class);
        suite.addTestSuite(RemoveTestHidden.class);
        suite.addTestSuite(ResolvedTestHidden.class);
        suite.addTestSuite(RevertTestHidden.class);
        suite.addTestSuite(StatusTestHidden.class);
        suite.addTestSuite(TreeConflictsTestHidden.class);
        suite.addTestSuite(SwitchToTestHidden.class);
        suite.addTestSuite(UpdateTestHidden.class);
        
        return suite;
    }
}
