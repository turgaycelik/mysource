/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue;

import java.util.regex.Matcher;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestURLUtil
{
    @Test
    public void testParameterAddedToRawURL()
    {
        final String rawUrl = "http://example.com";
        String result = URLUtil.addRequestParameter(rawUrl, "temp=100");
        assertNotNull(result);
        assertEquals("http://example.com?temp=100", result);
    }

    @Test
    public void testParameterAddedToURLWithParameters()
    {
        final String rawUrl = "http://example.com?something=200";
        String result = URLUtil.addRequestParameter(rawUrl, "temp=100");
        assertNotNull(result);
        assertEquals("http://example.com?something=200&temp=100", result);
    }

    @Test
    public void testNullParameterAddedToRawURL()
    {
        final String rawUrl = "http://example.com";
        String result = URLUtil.addRequestParameter(rawUrl, null);
        assertNotNull(result);
        assertEquals("http://example.com", result);
    }

    @Test (expected = NullPointerException.class)
    public void testNullURLThrowsNPE()
    {
        URLUtil.addRequestParameter(null, null);
    }

    @Test
    public void shouldAddURLPrefixToPartialURL()
    {
        final String perfix = "http:";
        final String postfix = "abc/def";
        final String fullPath = URLUtil.addContextPathToURLIfAbsent(perfix, postfix);

        assertThat(fullPath, Matchers.equalTo(perfix + postfix));
    }

    @Test
    public void shouldNotModifyFullURL()
    {
        final String perfix = "ftp:";
        final String postfix = "http://www.com/a/b/c";
        final String fullPath = URLUtil.addContextPathToURLIfAbsent(perfix, postfix);

        assertThat(fullPath, Matchers.equalTo(postfix));
    }
}
