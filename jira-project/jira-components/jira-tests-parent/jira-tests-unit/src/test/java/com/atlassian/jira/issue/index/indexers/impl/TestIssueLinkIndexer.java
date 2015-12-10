package com.atlassian.jira.issue.index.indexers.impl;

import java.util.ArrayList;
import java.util.HashSet;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.mock.issue.MockIssue;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

/**
 * Unit test for {@link IssueLinkIndexer}
 *
 * @since v4.4
 */
public class TestIssueLinkIndexer
{

    @Test
    public void basicIndexedValues()
    {
        long thisIssueId = 12345L;
        Issue thisIssue = new MockIssue(thisIssueId);

        ArrayList<IssueLink> inwardLinks = new ArrayList<IssueLink>();
        inwardLinks.add(mockInLink(thisIssue, 1, 1001));
        inwardLinks.add(mockInLink(thisIssue, 2, 1002));
        inwardLinks.add(mockInLink(thisIssue, 3, 1003));
        ArrayList<IssueLink> outwardLinks = new ArrayList<IssueLink>();
        outwardLinks.add(mockOutLink(thisIssue, 100, 50));
        outwardLinks.add(mockOutLink(thisIssue, 200, 50));
        outwardLinks.add(mockOutLink(thisIssue, 300, 50));

        IssueLinkManager ilm = Mockito.mock(IssueLinkManager.class);
        Mockito.when(ilm.getInwardLinks(thisIssueId)).thenReturn(inwardLinks);
        Mockito.when(ilm.getOutwardLinks(thisIssueId)).thenReturn(outwardLinks);

        IssueLinkIndexer issueLinkIndexer = new IssueLinkIndexer(ilm);

        Document doc = new Document();
        issueLinkIndexer.addIndex(doc, thisIssue);

        // check that all and only expected values are present as field values
        HashSet<String> expectedValues = new HashSet<String>();

        // with link id, direction and issue id
        expectedValues.add("l:1001,d:i,i:1");
        expectedValues.add("l:1002,d:i,i:2");
        expectedValues.add("l:1003,d:i,i:3");
        expectedValues.add("l:50,d:o,i:100");
        expectedValues.add("l:50,d:o,i:200");
        expectedValues.add("l:50,d:o,i:300");

        // just link id and direction
        expectedValues.add("l:1001,d:i");
        expectedValues.add("l:1002,d:i");
        expectedValues.add("l:1003,d:i");
        expectedValues.add("l:50,d:o");

        // just link id, no direction
        expectedValues.add("l:1001");
        expectedValues.add("l:1002");
        expectedValues.add("l:1003");
        expectedValues.add("l:50");

        HashSet<String> actualValues = new HashSet<String>();
        Field[] linkFields = doc.getFields(DocumentConstants.ISSUE_LINKS);
        for (Field linkField : linkFields)
        {
            actualValues.add(linkField.stringValue());
        }
        boolean setsAreSame = expectedValues.equals(actualValues);
        if (!setsAreSame)
        {
            HashSet<String> missing = new HashSet<String>(expectedValues);
            missing.removeAll(actualValues);
            HashSet<String> extra = new HashSet<String>(actualValues);
            extra.removeAll(expectedValues);

            StringBuilder explanation = new StringBuilder();
            explanation.append("extra values: ").append(extra);
            explanation.append("missing values: ").append(missing);
            Assert.fail(explanation.toString());

        }
    }

    @Test
    public void checkAlwaysInScope()
    {
        IssueLinkIndexer issueLinkIndexer = new IssueLinkIndexer(null);
        
        Assert.assertTrue("Field should always be in scope because we use links to make epics work in JIRA Agile", issueLinkIndexer.isFieldVisibleAndInScope(null));
    }

    private static IssueLink mockOutLink(Issue thisIssue, long thatIssueId, long linkTypeId)
    {
        IssueLink mock = Mockito.mock(IssueLink.class);
        Mockito.when(mock.getSourceId()).thenReturn(thisIssue.getId());
        Mockito.when(mock.getDestinationId()).thenReturn(thatIssueId);
        Mockito.when(mock.getLinkTypeId()).thenReturn(linkTypeId);
        // using system links because you have to opt in to include them, not that their systemness is indexed
        Mockito.when(mock.isSystemLink()).thenReturn(true);
        return mock;
    }

    private static IssueLink mockInLink(Issue thisIssue, long thatIssueId, long linkTypeId)
    {
        IssueLink mock = Mockito.mock(IssueLink.class);
        Mockito.when(mock.getSourceId()).thenReturn(thatIssueId);
        Mockito.when(mock.getDestinationId()).thenReturn(thisIssue.getId());
        Mockito.when(mock.getLinkTypeId()).thenReturn(linkTypeId);
        // using system links because you have to opt in to include them, not that their systemness is indexed
        Mockito.when(mock.isSystemLink()).thenReturn(true);
        return mock;
    }
}
