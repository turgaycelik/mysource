package com.atlassian.jira.issue.search.quicksearch;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPrefixedSingleWordQuickSearchHandler
{
    @Test
    public void testPrefixedSingleWordQuickSearchHandler()
    {
        //test cases where the word is not handled as it does not start with the prefix
        _testPrefixedSingleWordQuickSearchHandler("p:", "", "", false);//no word
        _testPrefixedSingleWordQuickSearchHandler("p:", null, "", false);//null word
        _testPrefixedSingleWordQuickSearchHandler("p:", "p:", null, false);//no suffix

        //test cases where the word is handled
        _testPrefixedSingleWordQuickSearchHandler("p:", "p:a", "a", true);
        _testPrefixedSingleWordQuickSearchHandler("pre:", "pre:suff", "suff", true);
        _testPrefixedSingleWordQuickSearchHandler("p:", "p:p:suff", "p:suff", true);
        _testPrefixedSingleWordQuickSearchHandler("", "word", "word", true);
    }

    public void _testPrefixedSingleWordQuickSearchHandler(final String prefix, final String word, final String expectedSuffix, final boolean isCanHandleWord)
    {
        final AtomicBoolean isGetPrefixCalled = new AtomicBoolean(false);
        final AtomicBoolean isHandleWordSuffixCalled = new AtomicBoolean(false);

        final PrefixedSingleWordQuickSearchHandler prefixHandler = new PrefixedSingleWordQuickSearchHandler()
        {
            @Override
            protected String getPrefix()
            {
                isGetPrefixCalled.set(true);
                return prefix;
            }

            @Override
            protected Map<?, ?> handleWordSuffix(final String wordSuffix, final QuickSearchResult searchResult)
            {
                isHandleWordSuffixCalled.set(true);
                assertEquals(expectedSuffix, wordSuffix);
                return Collections.emptyMap();
            }
        };

        assertEquals(isCanHandleWord ? Collections.EMPTY_MAP : null, prefixHandler.handleWord(word, null));
        assertTrue(isGetPrefixCalled.get()); //should always be called to check if word starts with it
        assertEquals(isCanHandleWord, isHandleWordSuffixCalled.get()); //if the word can be handled than the handle suffix should be called
    }
}
