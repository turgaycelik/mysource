package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestProgressIndexer extends MockControllerTestCase
{
    private FieldVisibilityManager fieldVisibilityManager;
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        this.fieldVisibilityManager = mockController.getMock(FieldVisibilityManager.class);
        this.applicationProperties = mockController.getMock(ApplicationProperties.class);
    }

    @Test
    public void testAddIndex()
    {
        MockIssue issue = new MockIssue();
        issue.setEstimate(new Long(100));
        issue.setTimeSpent(new Long(100));

        fieldVisibilityManager.isFieldVisible(IssueFieldConstants.TIMETRACKING, issue);
        mockController.setReturnValue(true);

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(true);
        mockController.replay();

        ProgressIndexer indexer = new ProgressIndexer(fieldVisibilityManager, applicationProperties);
        Document doc = new Document();


        indexer.addIndex(doc, issue);
        final Field field = doc.getField(DocumentConstants.ISSUE_PROGRESS);
        assertNotNull("should have progress", field);
        assertEquals("50", field.stringValue());
        assertTrue(field.isIndexed());
    }

    @Test
    public void testAddIndexTimeTrackingNotVisible()
    {
        MockIssue issue = new MockIssue();
        issue.setEstimate(new Long(100));
        issue.setTimeSpent(new Long(100));

        fieldVisibilityManager.isFieldVisible(IssueFieldConstants.TIMETRACKING, issue);
        mockController.setReturnValue(false);

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(true);

        mockController.replay();

        ProgressIndexer indexer = new ProgressIndexer(fieldVisibilityManager, applicationProperties);
        Document doc = new Document();


        indexer.addIndex(doc, issue);
        final Field field = doc.getField(DocumentConstants.ISSUE_PROGRESS);
        assertNotNull("should have progress", field);
        assertEquals("50", field.stringValue());
        assertFalse(field.isIndexed());
    }
    
    @Test
    public void testAddIndexFailsIfNegativeEstimate()
    {
        mockController.replay();
        ProgressIndexer indexer = new ProgressIndexer(fieldVisibilityManager, applicationProperties);
        Document doc = new Document();
        
        MockIssue issue = new MockIssue();
        issue.setEstimate(new Long(-100));
        issue.setTimeSpent(new Long(100));
        indexer.addIndex(doc, issue);
        final Field field = doc.getField(DocumentConstants.ISSUE_PROGRESS);
        assertNull("should not have progress indexed", field);
    }

    @Test
    public void testAddIndexFailsIfNegativeTimeSpent()
    {
        mockController.replay();
        ProgressIndexer indexer = new ProgressIndexer(fieldVisibilityManager, applicationProperties);
        Document doc = new Document();
        
        MockIssue issue = new MockIssue();
        issue.setEstimate(new Long(100));
        issue.setTimeSpent(new Long(-100));
        indexer.addIndex(doc, issue);
        final Field field = doc.getField(DocumentConstants.ISSUE_PROGRESS);
        assertNull("should not have progress indexed", field);
    }
}
