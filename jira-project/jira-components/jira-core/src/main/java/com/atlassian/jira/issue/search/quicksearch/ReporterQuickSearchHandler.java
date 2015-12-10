package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;

import java.util.Map;

/**
 * Quick search handler for components. Note that this handler needs to run after the Project Handler has run.
 *
 * @since v4.3
 */
public class ReporterQuickSearchHandler extends PrefixedSingleWordQuickSearchHandler
{
    private static final String PREFIX = "r:";

    protected Map/*<String, String>*/handleWordSuffix(final String wordSuffix, final QuickSearchResult searchResult)
    {
        if ("me".equals(wordSuffix))
        {
            return EasyMap.build("reporterSelect", "issue_current_user");
        }
        else if ("none".equals(wordSuffix))
        {
            return EasyMap.build("reporterSelect", "issue_no_reporter");
        }
        else
        {
            return EasyMap.build("reporterSelect", "specificuser", "reporter", wordSuffix);
        }
    }

    protected String getPrefix()
    {
        return PREFIX;
    }
}
