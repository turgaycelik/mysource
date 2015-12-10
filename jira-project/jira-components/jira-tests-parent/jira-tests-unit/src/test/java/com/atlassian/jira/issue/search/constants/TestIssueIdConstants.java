package com.atlassian.jira.issue.search.constants;

import java.util.Set;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link IssueIdConstants}.
 *
 * @since v4.0
 */
public class TestIssueIdConstants
{
    @Test
    public void testConstants() throws Exception
    {
        IssueIdConstants constants = IssueIdConstants.getInstance();
        assertEquals(asSet("issuekey", "key", "id", "issue"), constants.getJqlClauseNames().getJqlFieldNames());
        assertEquals("key", constants.getJqlClauseNames().getPrimaryName());
        assertEquals(new ClauseNames("key", asSet("issuekey","id", "issue")), constants.getJqlClauseNames());
        assertEquals("issue_id", constants.getIndexField());
    }

    private  static <T> Set<T> asSet(T...items)
    {
        return CollectionBuilder.newBuilder(items).asSet();
    }
}
