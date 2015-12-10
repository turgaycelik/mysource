package com.atlassian.jira.servlet;

import java.util.Locale;

import com.octo.captcha.component.word.wordgenerator.WordGenerator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the behaviour of the {@link CensoringWordGenerator} class.
 *
 * @since v5.1
 */
public class TestCensoringWordGenerator
{
    /**
     * Tests that the censoring word generator refuses to return offensive words.
     */
    @Test
    public void censoringOfOffensiveWords()
    {
        WordGenerator censored = mockWithTwoOffensiveWords();
        assertEquals("noprob", new CensoringWordGenerator(censored, 5).getWord(6));
    }

    /**
     * Tests that the censoring word generator refuses to return offensive words
     * when a locale is specified.
     */
    @Test
    public void censoringOfOffensiveWordsInLocale()
    {
        WordGenerator censored = mockWithTwoOffensiveWords();
        assertEquals("noprob", new CensoringWordGenerator(censored, 5).getWord(6, Locale.ENGLISH));
    }

    /**
     * Tests that, after 20 attempts to find a non-offensive word, we'll just accept the 21st word regardless.
     */
    @Test
    public void nicenessHasItsLimits()
    {
        WordGenerator censored = mockWithTwoOffensiveWords();
        assertEquals("dickly", new CensoringWordGenerator(censored, 1).getWord(6));
    }

    @Test
    public void nicenessHasItsLimitsInLocale()
    {
        WordGenerator censored = mockWithTwoOffensiveWords();
        assertEquals("dickly", new CensoringWordGenerator(censored, 1).getWord(6, Locale.ENGLISH));
    }

    /**
     * Creates a mock censored word generator that produces three words, the first two of which are offensive
     */
    private WordGenerator mockWithTwoOffensiveWords()
    {
        WordGenerator censored = mock(WordGenerator.class);
        when(censored.getWord(anyInt())).thenReturn("fucker", "dickly", "noprob");
        when(censored.getWord(anyInt(), Locale.class.cast(anyObject()))).thenReturn("fucker", "dickly", "noprob");
        return censored;
    }
}
