package com.atlassian.jira.service.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.component.MockComponentWorker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link ServiceUtils}.
 *
 * @since 6.2
 */
public class TestServiceUtils
{
    private static final String ISSUE_KEY = "PRJ-123";

    @Mock private IssueManager mockIssueManager;
    @Mock private MutableIssue mockIssue;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(mockIssueManager.getIssueObject(ISSUE_KEY)).thenReturn(mockIssue);
        new MockComponentWorker().init().addMock(IssueManager.class, mockIssueManager);
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void findIssueObjectInStringShouldTolerateTrailingSpaces()
    {
        assertFindIssueObjectInStringToleratesInexactKey(ISSUE_KEY + "   ");
    }

    @Test
    public void findIssueObjectInStringShouldTolerateTrailingNewLineAndSpaces()
    {
        assertFindIssueObjectInStringToleratesInexactKey(ISSUE_KEY + "\n   ");
    }

    @Test
    public void findIssueObjectInStringShouldTolerateTrailingCrLfAndSpaces()
    {
        assertFindIssueObjectInStringToleratesInexactKey(ISSUE_KEY + "\r\n   "); // see JRA-1616
    }

    @Test
    public void findIssueObjectInStringShouldFindIssueKeyAfterTextAndSpace()
    {
        assertFindIssueObjectInStringToleratesInexactKey("asdf " + ISSUE_KEY);
    }

    @Test
    public void findIssueObjectInStringShouldFindIssueKeyAfterOtherHyphenatedTextAndSpace()
    {
        assertFindIssueObjectInStringToleratesInexactKey("ABC-PRJ " + ISSUE_KEY);
    }

    private void assertFindIssueObjectInStringToleratesInexactKey(final String inexactKey)
    {
        // Invoke
        final Issue issue = ServiceUtils.findIssueObjectInString(inexactKey);

        // Check
        assertSame(mockIssue, issue);
    }
}
