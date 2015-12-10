package com.atlassian.jira.issue.views.util;

import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestRssViewUtils
{
    @Test
    public void testRssViewLocale()
    {
        assertNull(RssViewUtils.getRssLocale(null));
        assertEquals("fr-ca", RssViewUtils.getRssLocale(Locale.CANADA_FRENCH));
        assertEquals("fr-fr", RssViewUtils.getRssLocale(Locale.FRANCE));
        assertEquals("ko", RssViewUtils.getRssLocale(Locale.KOREAN));
    }
}
