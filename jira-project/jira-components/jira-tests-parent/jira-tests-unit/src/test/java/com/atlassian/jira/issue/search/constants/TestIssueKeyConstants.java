package com.atlassian.jira.issue.search.constants;

import java.util.Set;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestIssueKeyConstants
{
    @Test
    public void testConstants() throws Exception
    {
        IssueKeyConstants constants = IssueKeyConstants.getInstance();
        assertEquals(asSet("issuekey", "key", "id", "issue"), constants.getJqlClauseNames().getJqlFieldNames());
        assertEquals("key", constants.getJqlClauseNames().getPrimaryName());
        assertEquals("key_folded", constants.getIndexField());
        assertEquals("keynumpart_range", constants.getKeyIndexOrderField());
    }

    private  static <T> Set<T> asSet(T...items)
    {
        return CollectionBuilder.newBuilder(items).asSet();
    }
}
