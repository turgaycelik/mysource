package com.atlassian.jira.webtests.ztests.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @since v3.13
 */
public class ExpectedChangeHistoryRecord
{
    private final Collection<ExpectedChangeHistoryItem> expectedChangeItems;

    public ExpectedChangeHistoryRecord(ExpectedChangeHistoryItem... expectedChangeHistoryItems)
    {
        this(Arrays.asList(expectedChangeHistoryItems));
    }

    public ExpectedChangeHistoryRecord(Collection<ExpectedChangeHistoryItem> expectedChangeItems)
    {
        this.expectedChangeItems = new ArrayList<ExpectedChangeHistoryItem>();
        this.expectedChangeItems.addAll(expectedChangeItems);
    }

    public Collection<ExpectedChangeHistoryItem> getChangeItems()
    {
        return expectedChangeItems;
    }
}
