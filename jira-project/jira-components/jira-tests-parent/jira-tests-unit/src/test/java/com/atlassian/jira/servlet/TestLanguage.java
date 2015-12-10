package com.atlassian.jira.servlet;

import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests only the English language at the moment.
 */
public class TestLanguage
{
    private static final Language ENGLISH = Language.forLocale(Locale.ENGLISH);

    /** Tests that the censor does not treat an innocuous word as offensive */
    @Test
    public void notOffensive() {
        assertFalse(ENGLISH.isOffensive("innocuous"));
    }

    /** Tests that an exact match with an offensive word is detected */
    @Test
    public void exactMatch() {
        assertTrue(ENGLISH.isOffensive("fuck"));
    }

    /**
    * Tests that the censor considers words starting
    * with offensive words to be offensive.
    */
    @Test
    public void offensiveWordPrefix() {
        assertTrue(ENGLISH.isOffensive("fucketty"));
    }

    /**
    * Tests that the censor considers words ending
    * with offensive words to be offensive.
    */
    @Test
    public void offensiveWordPostfix() {
        assertTrue(ENGLISH.isOffensive("ohfuck"));
    }

    /**
    * Tests that offensive words are not detected when surrounded by
    * other characters.
    */
    @Test
    public void embeddedOffensiveWord() {
        assertFalse(ENGLISH.isOffensive("ohfuckme"));
    }

    /** Tests that a word that is near to an offensive word it itself offensive */
    @Test
    public void nearlyOffensive() {
        assertTrue(ENGLISH.isOffensive("luck"));
    }

    @Test
    public void surroundedByWhiteSpace() {
        assertTrue(ENGLISH.isOffensive("    fuck"));
        assertTrue(ENGLISH.isOffensive("fuck    "));
        assertTrue(ENGLISH.isOffensive("    fuck    "));
    }

    @Test
    public void capitals() {
        assertTrue(ENGLISH.isOffensive("Fuck"));
        assertTrue(ENGLISH.isOffensive("FUck"));
        assertTrue(ENGLISH.isOffensive("FUCk"));
        assertTrue(ENGLISH.isOffensive("FUCK"));
    }
}
