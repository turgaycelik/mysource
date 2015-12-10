package com.atlassian.jira.issue.search.quicksearch;

import java.util.Map;

/**
 * Convenient class for all quick search handlers that handle single words with a prefix.
 *
 * @since v3.13
 */
public abstract class PrefixedSingleWordQuickSearchHandler extends SingleWordQuickSearchHandler
{
    protected Map/*<String, String>*/handleWord(final String word, final QuickSearchResult searchResult)
    {
        final String prefix = getPrefix();
        if ((word != null) && (word.length() > prefix.length()) && word.startsWith(prefix))
        {
            return handleWordSuffix(word.substring(prefix.length()), searchResult);
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the prefix this quick search handler handles
     *
     * @return handled prefix
     */
    protected abstract String getPrefix();

    /**
     * Handle the word suffix (original word without the prefix)
     *
     * @param wordSuffix   the original word minus the prefix
     * @param searchResult search result
     * @return Map of search parameters
     */
    protected abstract Map/*<String, String>*/handleWordSuffix(String wordSuffix, QuickSearchResult searchResult);
}
