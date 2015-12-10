package com.atlassian.jira.bc.subtask.conversion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class TestIssueToSubtaskUpdateFields
{
    @Test
    public void testFetFieldLayoutItems()
    {
        // complicated, most tests are Functional
        // just testing that the right project is being used to get field layout

        final Long projectId = (long) 1234;
        final String issueTypeId = "999";

        final MockProject targetProject = new MockProject(projectId);
        Mock issueTypeMock = new Mock(IssueType.class);
        issueTypeMock.expectAndReturn("getId", issueTypeId);
        IssueType mockIssueType = (IssueType) issueTypeMock.proxy();

        MockIssue issue = new MockIssue(){
            public Project getProjectObject()
            {
                return targetProject;
            }
        };
        issue.setIssueTypeObject(mockIssueType);

        List issueTypeIdList = EasyList.build(issueTypeId);

        Mock fieldLayoutMock = new Mock(FieldLayout.class);
        fieldLayoutMock.expectAndReturn("getVisibleLayoutItems", P.args(P.eq(targetProject), P.eq(issueTypeIdList)), Collections.EMPTY_LIST);
        FieldLayout fieldLayout = (FieldLayout) fieldLayoutMock.proxy();

        Mock fieldLayoutManagerMock = new Mock(FieldLayoutManager.class);
        fieldLayoutManagerMock.expectAndReturn("getFieldLayout", P.args(P.eq(issue)), fieldLayout);
        FieldLayoutManager fieldLayoutManager = (FieldLayoutManager) fieldLayoutManagerMock.proxy();

        IssueToSubTaskConversionService service = new DefaultIssueToSubTaskConversionService(null, null, null, null, null, fieldLayoutManager, null, null, null);

        Collection items = service.getFieldLayoutItems(null, issue);
        assertTrue(items.isEmpty());
        fieldLayoutMock.verify();

    }
}
