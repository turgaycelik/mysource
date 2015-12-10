/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRegexpUtils
{
    @Test
    public void testReplaceAll()
    {
        assertEquals("foobaz", RegexpUtils.replaceAll("foobar", "bar", "baz"));
        assertEquals("foo", RegexpUtils.replaceAll("<a href='sdf'>foo</a>", "<a href='.*'>(.*)</a>", "$1"));
        assertEquals("http://something", RegexpUtils.replaceAll("<a href=\"http://something\">foo</a>", "<a (?:target=\"_new\" )?href=['\"](?:mailto:)?(.*?)['\"](?: target=\"_new\")?>.*</a>", "$1"));
        assertEquals("<foo><bar>", RegexpUtils.replaceAll("&lt;foo>&lt;bar>", "&lt;", "<"));

        // add your hairy regexp here!
    }
}
