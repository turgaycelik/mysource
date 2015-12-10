package com.atlassian.jira.util;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.3
 */
public class TestLocaleParser
{
    @Test
    public void testParseLocale() throws Exception
    {
        assertNull(LocaleParser.parseLocale(null));
        assertNull(LocaleParser.parseLocale(""));
        assertNull(LocaleParser.parseLocale("   "));

        // en
        assertEquals(new Locale("en"), LocaleParser.parseLocale("en"));
        // en_US
        assertEquals(new Locale("en", "US"), LocaleParser.parseLocale("en_US"));
        assertEquals(new Locale("en", "US"), LocaleParser.parseLocale("   en_US      "));
        // fr
        assertEquals(new Locale("fr"), LocaleParser.parseLocale("fr"));
        assertEquals("fr", LocaleParser.parseLocale("fr").getLanguage());
        assertEquals("", LocaleParser.parseLocale("fr").getCountry());
        // fr_CA
        assertEquals(Locale.CANADA_FRENCH, LocaleParser.parseLocale("fr_CA"));
        assertEquals("fr", LocaleParser.parseLocale("fr_CA").getLanguage());
        assertEquals("CA", LocaleParser.parseLocale("fr_CA").getCountry());
    }
}
