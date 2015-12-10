package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestDelimeterInserter
{

    @Test
    public void testEmptyString()
    {
        String targetString = "";
        String[] terms = new String[]{
                "nick", "kel", "v", "sho"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("", resultString);
    }

    @Test
    public void testNoMatchingTerms()
    {
        String targetString = "brad is not going to match";
        String[] terms = new String[]{
                "nick", "kel", "v", "sho"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("brad is not going to match", resultString);
    }

    @Test
    public void testNoTerms()
    {
        String targetString = "brad is not going to match";
        String[] terms = new String[]{

        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("brad is not going to match", resultString);
    }


    @Test
    public void testEmptyTerms()
    {
        String targetString = "brad is not going to match";
        String[] terms = { "", "", "" };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("brad is not going to match", resultString);

        DelimeterInserter nonPrefixDelimiterInserter = new DelimeterInserter("<b>", "</b>", false);
        assertEquals(targetString, nonPrefixDelimiterInserter.insert(targetString, terms));
    }

    @Test
    public void testNullTerms()
    {
        String targetString = "brad is not going to match";
        String[] terms = null;

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("brad is not going to match", resultString);
    }
    @Test
    public void testNullTerm()
    {
        String targetString = "brad is not going to match";
        String[] terms = {null};

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("brad is not going to match", resultString);

        DelimeterInserter nonPrefixDelimiterInserter = new DelimeterInserter("<b>", "</b>", false);
        assertEquals(targetString, nonPrefixDelimiterInserter.insert(targetString, terms));
    }

    @Test
    public void testInsertionPrefix()
    {
        String targetString = "nickelodean is nicks favourite tv show";
        String[] terms = new String[]{
                "nick", "kel", "v", "sho"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("<b>nickel</b>odean is <b>nick</b>s favourite tv <b>sho</b>w", resultString);
    }

    @Test
    public void testInsertionPrefixCaseInsenstive()
    {
        String targetString = "NicKelodeaN is nicks favourite tv show";
        String[] terms = new String[]{
                "nick", "kel", "v", "sho"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("<b>NicKel</b>odeaN is <b>nick</b>s favourite tv <b>sho</b>w", resultString);
    }

    @Test
    public void testInsertionPrefixCaseSenstive()
    {
        String targetString = "NicKelodeaN is nicks favourite TV show";
        String[] terms = new String[]{
                "Nick", "Kel", "v", "sho"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true, false);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("NicKelodeaN is nicks favourite TV <b>sho</b>w", resultString);
    }

    @Test
    public void testInsertionPrefixNotWild()
    {
        String targetString = "nickelodean is unicks favourite tv show";
        String[] terms = new String[]{
                "nick", "kel", "v", "sho"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("<b>nickel</b>odean is unicks favourite tv <b>sho</b>w", resultString);
    }

    @Test
    public void testInsertionWildCard()
    {
        String targetString = "nickelodean is unicks favourite tv show";
        String[] terms = new String[]{
                "nick", "kel", "v", "sho"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", false);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("<b>nickel</b>odean is u<b>nick</b>s fa<b>v</b>ourite t<b>v</b> <b>sho</b>w", resultString);
    }

    @Test
    public void testInsertionWildCardCaseSensitive()
    {
        String targetString = "NicKelodean is unicks favourite TV show";
        String[] terms = new String[]{
                "Nick", "Kel", "v", "sho"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", false, false);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("Nic<b>Kel</b>odean is unicks fa<b>v</b>ourite TV <b>sho</b>w", resultString);
    }

    @Test
    public void testMergingMarkers()
    {
        String targetString = "nickonick";
        String[] terms = new String[]{
                "nick", "kon"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", false);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("<b>nickonick</b>", resultString);
    }

    @Test
    public void testMergingMarkersAndPrefix()
    {
        String targetString = "nickonick";
        String[] terms = new String[]{
                "nick", "kon"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", true);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("<b>nickon</b>ick", resultString);
    }

    @Test
    public void testWhiteSpaceConsideration()
    {
        String targetString = "nickel-odean jra-1234 can go an get f&#&$^$'ed";
        String[] terms = new String[]{
                "nick", "1234", "ed"
        };

        DelimeterInserter delimeterInserter = new DelimeterInserter("|", "--]");
        delimeterInserter.setConsideredWhitespace("&#$^-'");
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("|nick--]el-odean jra-|1234--] can go an get f&#&$^$'|ed--]", resultString);

    }

    @Test
    public void testAdjacentMatches()
    {
        String targetString = "lulalalo";
        String[] terms = new String[]{"la"};

        DelimeterInserter delimeterInserter = new DelimeterInserter("-","-",false);
        String resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("lu-lala-lo", resultString);

        terms = new String[]{"la","lo"};

        resultString = delimeterInserter.insert(targetString, terms);
        assertEquals("lu-lalalo-", resultString);
    }

}
