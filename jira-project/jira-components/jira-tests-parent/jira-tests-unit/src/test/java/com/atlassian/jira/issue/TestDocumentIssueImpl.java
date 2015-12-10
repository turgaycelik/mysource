package com.atlassian.jira.issue;

import com.atlassian.jira.issue.index.DocumentConstants;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/** @since v3.12 */
public class TestDocumentIssueImpl
{

    @Test
    public void testGetNullSecurityLevelId()
    {
        final Document mockDocument = new Document();
        DocumentIssueImpl documentIssue = new DocumentIssueImpl(mockDocument, null, null, null, null, null, null, null);
        Long securityLevelId = documentIssue.getSecurityLevelId();
        assertNull(securityLevelId);
    }

    //Test for JRA-13744
    @Test
    public void testGetMinusOneSecurityLevelId()
    {
        final Document mockDocument = new Document();
        mockDocument.add(new Field("issue_security_level", "-1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        DocumentIssueImpl documentIssue = new DocumentIssueImpl(mockDocument, null, null, null, null, null, null, null);
        Long securityLevelId = documentIssue.getSecurityLevelId();
        assertNull(securityLevelId);
    }

    @Test
    public void testGetSecurityLevelId()
    {
        final Document mockDocument = new Document();
        mockDocument.add(new Field("issue_security_level", "5", Field.Store.YES, Field.Index.NOT_ANALYZED));

        DocumentIssueImpl documentIssue = new DocumentIssueImpl(mockDocument, null, null, null, null, null, null, null);
        Long securityLevelId = documentIssue.getSecurityLevelId();
        assertNotNull(securityLevelId);
        assertEquals(new Long(5), securityLevelId);
    }

    @Test
    public void testGetAssigneeIdWithUser()
    {
        final Document mockDocument = new Document();
        mockDocument.add(new Field(DocumentConstants.ISSUE_ASSIGNEE, "fred", Field.Store.YES, Field.Index.NOT_ANALYZED));

        DocumentIssueImpl documentIssue = new DocumentIssueImpl(mockDocument, null, null, null, null, null, null, null);
        String assigneeUserId = documentIssue.getAssigneeId();
        assertNotNull(assigneeUserId);
        assertEquals("fred", assigneeUserId);
    }

    @Test
    public void testGetAssigneeIdWithnNoAssignee()
    {
        final Document mockDocument = new Document();
        mockDocument.add(new Field(DocumentConstants.ISSUE_ASSIGNEE, DocumentConstants.ISSUE_UNASSIGNED, Field.Store.YES, Field.Index.NOT_ANALYZED));

        DocumentIssueImpl documentIssue = new DocumentIssueImpl(mockDocument, null, null, null, null, null, null, null);
        String assigneeUserId = documentIssue.getAssigneeId();
        assertNull(assigneeUserId);
    }

     @Test
     public void testGetReporterIdWithUser()
    {
        final Document mockDocument = new Document();
        mockDocument.add(new Field(DocumentConstants.ISSUE_AUTHOR, "fred", Field.Store.YES, Field.Index.NOT_ANALYZED));

        DocumentIssueImpl documentIssue = new DocumentIssueImpl(mockDocument, null, null, null, null, null, null, null);
        String reporterUserId = documentIssue.getReporterId();
        assertNotNull(reporterUserId);
        assertEquals("fred", reporterUserId);
    }

    @Test
    public void testGetReporterIdWithNoReporter()
    {
        final Document mockDocument = new Document();
        mockDocument.add(new Field(DocumentConstants.ISSUE_AUTHOR, DocumentConstants.ISSUE_NO_AUTHOR, Field.Store.YES, Field.Index.NOT_ANALYZED));

        DocumentIssueImpl documentIssue = new DocumentIssueImpl(mockDocument, null, null, null, null, null, null, null);
        String reporterUserId = documentIssue.getReporterId();
        assertNull(reporterUserId);
    }
}
