package com.atlassian.jira.plugin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.mock.issue.MockIssue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestTabPanelUtil
{
    private static final MockIssue ISSUE_NO_VOTES = new MockIssue(new Long(1));
    private static final MockIssue ISSUE_NO_VOTES_AGAIN = new MockIssue(new Long(2));
    private static final MockIssue ISSUE_2_VOTES = new MockIssue(new Long(3));
    private static final MockIssue ISSUE_3_VOTES = new MockIssue(new Long(4));

    static
    {
        ISSUE_2_VOTES.setVotes(new Long(2));
        ISSUE_3_VOTES.setVotes(new Long(3));
    }

    @Test
    public void testFilterIssuesWithNoVotesNullParamIsOk()
    {
        TabPanelUtil.filterIssuesWithNoVotes(null);
    }

    @Test
    public void testFilterIssuesWithNoVotesEmptyListIsOk()
    {
        final List issues = new ArrayList();
        TabPanelUtil.filterIssuesWithNoVotes(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testFilterIssuesWithNoVotes()
    {
        final List issues = new ArrayList();
        issues.add(ISSUE_NO_VOTES);
        TabPanelUtil.filterIssuesWithNoVotes(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testFilterIssuesWithTwoNoVotes()
    {
        final List issues = new ArrayList();
        issues.add(ISSUE_NO_VOTES);
        issues.add(ISSUE_NO_VOTES_AGAIN);
        TabPanelUtil.filterIssuesWithNoVotes(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testFilterIssuesWithSomeVotes()
    {
        final List issues = new ArrayList();
        issues.add(ISSUE_NO_VOTES);
        issues.add(ISSUE_2_VOTES);
        issues.add(ISSUE_NO_VOTES_AGAIN);
        issues.add(ISSUE_3_VOTES);

        TabPanelUtil.filterIssuesWithNoVotes(issues);
        assertFalse(issues.isEmpty());
        assertEquals(2, issues.size());
        assertFalse(issues.contains(ISSUE_NO_VOTES));
        assertFalse(issues.contains(ISSUE_NO_VOTES_AGAIN));
        assertTrue(issues.contains(ISSUE_2_VOTES));
        assertTrue(issues.contains(ISSUE_3_VOTES));
    }

    @Test
    public void testSubSetCollectionNull()
    {
        final Collection result = TabPanelUtil.subSetCollection(null, 0);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testSubSetCollectionEmpty()
    {
        final Collection result = TabPanelUtil.subSetCollection(Collections.EMPTY_LIST, 0);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testSubSetCollectionInvalidSubsetSize()
    {
        final List inputList = new ArrayList();
        inputList.add("Stuff");
        inputList.add("More Stuff");
        inputList.add("Third Stuff");
        inputList.add("I've had enough of this Stuff!!");

        Collection result = TabPanelUtil.subSetCollection(inputList, -1);
        assertNotNull(result);
        assertEquals(inputList, result);

        result = TabPanelUtil.subSetCollection(inputList, 500);
        assertNotNull(result);
        assertEquals(inputList, result);

    }

    @Test
    public void testSubSetCollection()
    {
        final List inputList = new ArrayList();
        inputList.add("Stuff");
        inputList.add("More Stuff");
        inputList.add("Third Stuff");
        inputList.add("I've had enough of this Stuff!!");

        Collection result = TabPanelUtil.subSetCollection(inputList, 2);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("Stuff"));
        assertTrue(result.contains("More Stuff"));
    }

}
