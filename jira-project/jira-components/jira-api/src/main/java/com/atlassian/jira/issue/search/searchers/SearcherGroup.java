package com.atlassian.jira.issue.search.searchers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A group of searchers related by their {@link com.atlassian.jira.issue.search.searchers.SearcherGroupType}.
 *
 */
@PublicApi
public final class SearcherGroup
{
    private final SearcherGroupType type;
    private final List<IssueSearcher<?>> searchers;
    private final boolean printHeader;

    public SearcherGroup(SearcherGroupType type, Collection<IssueSearcher<?>> searchers)
    {
        this.type = notNull("type", type);
        this.printHeader = StringUtils.isNotBlank(type.getI18nKey());
        this.searchers = CollectionUtil.copyAsImmutableList(containsNoNulls("searchers", searchers));
    }

    public String getTitleKey()
    {
        return type.getI18nKey();
    }

    public List<IssueSearcher<?>> getSearchers()
    {
        return searchers;
    }

    public boolean isPrintHeader()
    {
        return printHeader;
    }

    public SearcherGroupType getType()
    {
        return type;
    }

    public boolean isShown(final User searcher, SearchContext searchContext)
    {
        notNull("searchContext", searchContext);
        for (IssueSearcher<?> issueSearcher : searchers)
        {
            if (issueSearcher.getSearchRenderer().isShown(searcher, searchContext))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("Searcher Group: [Type: %s, Searchers: %s].", type, searchers);
    }
}
