package com.atlassian.jira.web.action.issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.web.bean.PagerFilter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Some simple tests for IssueNavigator.
 * (Not the Kitchen Sink type in TestIssueNavigator)
 */
public class TestIssueNavigator2
{
    @Test
    public void testIntersectionOverlapAll() throws Exception
    {
        List<Issue> issuesInPage = getMockIssues(2, 4);
        final PagerFilter pager = new PagerFilter(2, 4);
        final int fromIndex = 1;
        final int toIndexExclusive = 8;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.<Issue>asList(mockIssue(2), mockIssue(3), mockIssue(4), mockIssue(5));
        assertEquals(expected, intersection);
    }

    @Test
    public void testIntersectionOverlapAllPageHalfFull() throws Exception
    {
        // The page is not full
        List<Issue> issuesInPage = getMockIssues(2, 2);
        final PagerFilter pager = new PagerFilter(2, 4);
        final int fromIndex = 1;
        final int toIndexExclusive = 8;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.<Issue>asList(mockIssue(2), mockIssue(3));
        assertEquals(expected, intersection);
    }

    @Test
    public void testIntersectionOverlapAllPageEmpty() throws Exception
    {
        // The page is not full
        List<Issue> issuesInPage = getMockIssues(2, 0);
        final PagerFilter pager = new PagerFilter(2, 4);
        final int fromIndex = 1;
        final int toIndexExclusive = 8;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.asList();
        assertEquals(expected, intersection);
    }

    @Test
    public void testIntersectionOverlapLeft() throws Exception
    {
        List<Issue> issuesInPage = getMockIssues(2, 4);
        final PagerFilter pager = new PagerFilter(2, 4);
        final int fromIndex = 0;
        final int toIndexExclusive = 4;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.<Issue>asList(mockIssue(2), mockIssue(3));
        assertEquals(expected, intersection);
    }

    @Test
    public void testIntersectionOverlapLeftPageEmpty() throws Exception
    {
        // Empty Page
        List<Issue> issuesInPage = getMockIssues(2, 0);
        final PagerFilter pager = new PagerFilter(2, 4);
        final int fromIndex = 0;
        final int toIndexExclusive = 4;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.asList();
        assertEquals(expected, intersection);
    }

    @Test
    public void testIntersectionOverlapRight() throws Exception
    {
        List<Issue> issuesInPage = getMockIssues(2, 4);
        final PagerFilter pager = new PagerFilter(2, 4);
        final int fromIndex = 4;
        final int toIndexExclusive = 8;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.<Issue>asList(mockIssue(4), mockIssue(5));
        assertEquals(expected, intersection);
    }

    @Test
    public void testIntersectionOverlapRightEmptyPage() throws Exception
    {
        List<Issue> issuesInPage = getMockIssues(2, 0);
        final PagerFilter pager = new PagerFilter(2, 4);
        final int fromIndex = 4;
        final int toIndexExclusive = 8;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.asList();
        assertEquals(expected, intersection);
    }

    @Test
    public void testNotEnoughIssues() throws Exception
    {
        List<Issue> issuesInPage = getMockIssues(20, 7);
        final PagerFilter pager = new PagerFilter(20, 10);
        final int fromIndex = 27;
        final int toIndexExclusive = 48;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.asList();
        assertEquals(expected, intersection);
    }

    @Test
    public void testJustEnoughIssues() throws Exception
    {
        List<Issue> issuesInPage = getMockIssues(20, 7);
        final PagerFilter pager = new PagerFilter(20, 10);
        final int fromIndex = 26;
        final int toIndexExclusive = 48;
        List<Issue> intersection = IssueNavigatorSearchResultsHelperImpl.intersection(issuesInPage, pager, fromIndex, toIndexExclusive);
        List<Issue> expected = Arrays.<Issue>asList(mockIssue(26));
        assertEquals(expected, intersection);
    }

    private List<Issue> getMockIssues(int from, int count)
    {
        final List<Issue> issues = new ArrayList();
        for (int i = from; i < from + count; i++)
        {
             issues.add(mockIssue(i));
        }
        return issues;
    }

    private MockIssue mockIssue(final long id)
    {
        MockIssue mockIssue = new MockIssue(id);
        mockIssue.setKey("MOCK-" + id);
        return mockIssue;
    }
}
