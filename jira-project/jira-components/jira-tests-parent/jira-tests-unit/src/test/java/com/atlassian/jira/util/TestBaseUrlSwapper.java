package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Simple unit test for {@link com.atlassian.jira.util.BaseUrlSwapper}.
 *
 * @since v5.0
 */
public class TestBaseUrlSwapper
{
    @Test
    public void testBasicSwap()
    {
        final String url = "http://something.com/blah/something/else";
        final String oldBaseUrl = "http://something.com/blah";
        final String newBaseUrl = "http://anothersite.com";
        final String expectedResult = "http://anothersite.com/something/else";
        assertEquals(expectedResult, BaseUrlSwapper.swapBaseUrl(url, oldBaseUrl, newBaseUrl));
    }

    @Test
    public void testOldBaseNotInUrl()
    {
        final String url = "http://something.com/blah/something/else";
        final String oldBaseUrl = "http://somethingelse.com/blah";
        final String newBaseUrl = "http://anothersite.com";
        assertEquals(url, BaseUrlSwapper.swapBaseUrl(url, oldBaseUrl, newBaseUrl));
    }

    @Test
    public void testOldBaseWithTrailingSlash()
    {
        final String url = "http://something.com/blah/something/else";
        final String oldBaseUrl = "http://something.com/blah/";
        final String newBaseUrl = "http://anothersite.com";
        final String expectedResult = "http://anothersite.com/something/else";
        assertEquals(expectedResult, BaseUrlSwapper.swapBaseUrl(url, oldBaseUrl, newBaseUrl));
    }

    @Test
    public void testNewBaseWithTrailingSlash()
    {
        final String url = "http://something.com/blah/something/else";
        final String oldBaseUrl = "http://something.com/blah";
        final String newBaseUrl = "http://anothersite.com/";
        final String expectedResult = "http://anothersite.com/something/else";
        assertEquals(expectedResult, BaseUrlSwapper.swapBaseUrl(url, oldBaseUrl, newBaseUrl));
    }
}
