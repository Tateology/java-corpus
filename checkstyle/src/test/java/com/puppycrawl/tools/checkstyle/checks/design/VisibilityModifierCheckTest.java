////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package com.puppycrawl.tools.checkstyle.checks.design;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.Test;

import static com.puppycrawl.tools.checkstyle.checks.design.VisibilityModifierCheck.MSG_KEY;

public class VisibilityModifierCheckTest
    extends BaseCheckTestSupport
{
    private Checker getChecker() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(VisibilityModifierCheck.class);
        checkConfig.addAttribute("publicMemberPattern", "^f[A-Z][a-zA-Z0-9]*$");
        return createChecker(checkConfig);
    }

    @Test
    public void testInner()
        throws Exception
    {
        final String[] expected = {
            "30:24: " + getCheckMessage(MSG_KEY, "rData"),
            "33:27: " + getCheckMessage(MSG_KEY, "protectedVariable"),
            "36:17: " + getCheckMessage(MSG_KEY, "packageVariable"),
            "41:29: " + getCheckMessage(MSG_KEY, "sWeird"),
            "43:19: " + getCheckMessage(MSG_KEY, "sWeird2"),
            "77:20: " + getCheckMessage(MSG_KEY, "someValue"),
        };
        verify(getChecker(), getPath("InputInner.java"), expected);
    }

    @Test
    public void testIgnoreAccess()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(VisibilityModifierCheck.class);
        checkConfig.addAttribute("publicMemberPattern", "^r[A-Z]");
        checkConfig.addAttribute("protectedAllowed", "true");
        checkConfig.addAttribute("packageAllowed", "true");
        final String[] expected = {
            "17:20: " + getCheckMessage(MSG_KEY, "fData"),
            "77:20: " + getCheckMessage(MSG_KEY, "someValue"),
        };
        verify(checkConfig, getPath("InputInner.java"), expected);
    }

    @Test
    public void testSimple() throws Exception
    {
        final String[] expected = {
            "39:19: " + getCheckMessage(MSG_KEY, "mNumCreated2"),
            "49:23: " + getCheckMessage(MSG_KEY, "sTest1"),
            "51:26: " + getCheckMessage(MSG_KEY, "sTest3"),
            "53:16: " + getCheckMessage(MSG_KEY, "sTest2"),
            "56:9: " + getCheckMessage(MSG_KEY, "mTest1"),
            "58:16: " + getCheckMessage(MSG_KEY, "mTest2"),
        };
        verify(getChecker(), getPath("InputSimple.java"), expected);
    }

    @Test
    public void testStrictJavadoc() throws Exception
    {
        final String[] expected = {
            "44:9: " + getCheckMessage(MSG_KEY, "mLen"),
            "45:19: " + getCheckMessage(MSG_KEY, "mDeer"),
            "46:16: " + getCheckMessage(MSG_KEY, "aFreddo"),
        };
        verify(getChecker(), getPath("InputPublicOnly.java"), expected);
    }
}