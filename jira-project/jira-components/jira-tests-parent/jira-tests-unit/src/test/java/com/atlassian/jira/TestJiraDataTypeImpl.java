package com.atlassian.jira;

import com.atlassian.jira.issue.Issue;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestJiraDataTypeImpl
{
    @Test
    public void testAsString() throws Exception
    {
        assertTrue(ImmutableList.of("java.lang.Object").containsAll(JiraDataTypes.ALL.asStrings()));
        assertTrue(ImmutableList.of("java.util.Date").containsAll(JiraDataTypes.DATE.asStrings()));
        assertTrue
                (
                        ImmutableList.of("java.lang.Object", "com.atlassian.jira.issue.Issue").
                                containsAll
                                        (
                                                new JiraDataTypeImpl(ImmutableList.of(Object.class, Issue.class)).
                                                        asStrings()
                                        )
                );
    }

    @Test
    public void testMatch() throws Exception
    {
        assertFalse(JiraDataTypes.DATE.matches(JiraDataTypes.ISSUE_TYPE));
        assertTrue(JiraDataTypes.ALL.matches(JiraDataTypes.ISSUE_TYPE));
        assertTrue(JiraDataTypes.ISSUE_TYPE.matches(JiraDataTypes.ALL));
        assertTrue(JiraDataTypes.ISSUE_TYPE.matches(JiraDataTypes.ISSUE_TYPE));

        final JiraDataTypeImpl bigType = new JiraDataTypeImpl(ImmutableList.of(Date.class, Issue.class, String.class));
        assertTrue(bigType.matches(JiraDataTypes.DATE));
        assertTrue(JiraDataTypes.DATE.matches(bigType));
        assertTrue(bigType.matches(JiraDataTypes.ISSUE));
        assertTrue(JiraDataTypes.ISSUE.matches(bigType));
        assertTrue(bigType.matches(JiraDataTypes.TEXT));
        assertTrue(JiraDataTypes.TEXT.matches(bigType));
        assertTrue(JiraDataTypes.ALL.matches(bigType));
    }
}
