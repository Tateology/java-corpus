/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.spi.model.services;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public abstract class CodeModelProblemResolver {
    private static final CodeModelProblemResolver DEFAULT = new Default();

    protected CodeModelProblemResolver() {
    }

    /** 
     * Static method to obtain the problem resolver
     * @return the problem detector
     */
    public static ParsingProblemDetector getParsingProblemDetector(CsmProject project) {
        return DEFAULT.createResolver(project);
    }

    public interface ParsingProblemDetector {
        void start();
        void switchToDeterminate(int maxWorkUnits);
        void finish();
        String nextCsmFile(CsmFile file, int current, int allWork);
        String getRemainingTime();
    }
    
    public abstract ParsingProblemDetector createResolver(CsmProject project);
    
    /**
     * Implementation of the default selector
     */
    private static final class Default extends CodeModelProblemResolver {
        private final Lookup.Result<CodeModelProblemResolver> res;
        Default() {
            res = Lookup.getDefault().lookupResult(CodeModelProblemResolver.class);
        }

        @Override
        public ParsingProblemDetector createResolver(CsmProject project) {
            for (CodeModelProblemResolver service : res.allInstances()) {
                ParsingProblemDetector createResolver = service.createResolver(project);
                if (createResolver != null) {
                    return createResolver;
                }
            }
            return null;
        }
    }
}
