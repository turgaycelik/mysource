package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.mock.MockConstantsManager;

import junit.framework.TestCase;

public class TestIssueTypeKeyComparator extends TestCase
{
    public void testNulls() throws Exception
    {
        IssueTypeKeyComparator issueTypeKeyComparator = new IssueTypeKeyComparator(null);

        assertEquals(0, issueTypeKeyComparator.compare(null, null));
        assertEquals(-1, issueTypeKeyComparator.compare(null, "qwe"));
        assertEquals(1, issueTypeKeyComparator.compare("qwe", null));
    }

    public void testSequence() throws Exception
    {
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        IssueType it1 = mockConstantsManager.insertIssueType("Improvement", 1L, null, "...", (String)null);
        IssueType it2 = mockConstantsManager.insertIssueType("Bug", 2L, null, "...", (String)null);
        IssueType it3 = mockConstantsManager.insertIssueType("Custom", null, null, "...", (String)null);

        IssueTypeKeyComparator issueTypeKeyComparator = new IssueTypeKeyComparator(mockConstantsManager);
        assertEquals(0, issueTypeKeyComparator.compare(it1.getId(), it1.getId()));
        assertEquals(-1, issueTypeKeyComparator.compare(it1.getId(), it2.getId()));
        assertEquals(1, issueTypeKeyComparator.compare(it2.getId(), it1.getId()));

        assertEquals(0, issueTypeKeyComparator.compare(it3.getId(), it3.getId()));
        assertEquals(1, issueTypeKeyComparator.compare(it1.getId(), it3.getId()));
        assertEquals(-1, issueTypeKeyComparator.compare(it3.getId(), it1.getId()));
    }

}
