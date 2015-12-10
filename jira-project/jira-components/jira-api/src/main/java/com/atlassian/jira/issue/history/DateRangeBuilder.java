package com.atlassian.jira.issue.history;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;

import java.util.List;

/**
 * The change history items only include the date of change - for indexing and searching you need a date range.
 *
 * @since v4.3
 */
@PublicApi
public interface DateRangeBuilder
{

    /**
     *
     * @param issue The issue that is being currently indexed
     * @param items A Collection of all the  @link ChangeHistoryItem)s for that issue.
     * @return  A list of items with date ranges
     * This method then iterates the list and creates the date ranges - it also ensures there is always an
     * 'initial' change state.
     */
    List<ChangeHistoryItem> buildDateRanges(final Issue issue, final List<ChangeHistoryItem> items);

    String getEmptyValue();
}