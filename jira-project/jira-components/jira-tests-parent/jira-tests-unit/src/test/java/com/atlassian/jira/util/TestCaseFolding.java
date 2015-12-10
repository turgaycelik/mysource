package com.atlassian.jira.util;

import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestCaseFolding
{
    @Test
    public void testFoldString() throws Exception
    {
        //This also checks that the capital I goes to i and not the Turkish lowercase i.
        assertEquals("i am in english", CaseFolding.foldString("I am in ENGLISH"));
        assertEquals("i live at strasse", CaseFolding.foldString("I live at Stra\u00dfe"));
    }

    @Test
    public void testFoldStringLocale() throws Exception
    {
        final Locale turkishLocale = new Locale("tr", "TR");

        assertEquals("\u0131 am eng", CaseFolding.foldString("I am ENG", turkishLocale));
        assertEquals("\u0131 i at strasse", CaseFolding.foldString("I i at Stra\u00dfe", turkishLocale));
    }
    
    @Test
    public void testFoldUsername() throws Exception
    {
        assertEquals("ian", CaseFolding.foldUsername("ian"));
        assertEquals("iansmith", CaseFolding.foldUsername("IanSmith"));
        assertEquals(null, CaseFolding.foldUsername(null));

        // Crowd does not handle this at the moment:
//        assertEquals("strasse", CaseFolding.foldUsername("Stra\u00dfe"));
    }
}
