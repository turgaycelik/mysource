package com.atlassian.jira.bc.subtask.conversion;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.MockProject;

import com.mockobjects.dynamic.Mock;

import org.junit.Test;

import junit.framework.TestCase;

public class TestIssueToSubTaskWorkflowUpdate extends TestCase
{

    private JiraServiceContext ctx = new MockJiraServiceContext();

    @Test
    public void testIsStatusChangeRequired()
    {

        Mock mockIssueType = new Mock(IssueType.class);
        mockIssueType.expectAndReturn("getId", "9");

        Mock mockStatus = new Mock(Status.class);
        final Status status = (Status) mockStatus.proxy();

        MockProject project = new MockProject();
        project.setId((long) 999);

        Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getStatusObject", status);
        mockIssue.expectAndReturn("getProjectObject", project);

        DefaultIssueToSubTaskConversionService service = new DefaultIssueToSubTaskConversionService(null, null, null, null, null, null, null, null, null)
        {
            protected boolean isStatusInWorkflowForProjectAndIssueType(Status status, Long projectId, String subTaskId)
            {
                return true;
            }

        };

        assertFalse(service.isStatusChangeRequired(ctx, (Issue) mockIssue.proxy(), (IssueType) mockIssueType.proxy()));

    }

    @Test
    public void testIsStatusNoChangeRequired()
    {

        Mock mockIssueType = new Mock(IssueType.class);
        mockIssueType.expectAndReturn("getId", "9");

        Mock mockStatus = new Mock(Status.class);
        final Status status = (Status) mockStatus.proxy();

        MockProject project = new MockProject();
        project.setId(new Long(999));

        Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getStatusObject", status);
        mockIssue.expectAndReturn("getProjectObject", project);

        DefaultIssueToSubTaskConversionService service = new DefaultIssueToSubTaskConversionService(null, null, null, null, null, null, null, null, null)
        {
            protected boolean isStatusInWorkflowForProjectAndIssueType(Status status, Long projectId, String subTaskId)
            {
                return false;
            }
        };

        assertTrue(service.isStatusChangeRequired(ctx, (Issue) mockIssue.proxy(), (IssueType) mockIssueType.proxy()));

    }
}
