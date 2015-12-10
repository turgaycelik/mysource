/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.MutableIssue;

import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

public class TestIssueAssignFunction
{
    @Test
    public void testIssueAssignNoAssignee() throws GenericEntityException
    {
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectVoid("setAssignee", P.args(new IsNull()));

        Map input = EasyMap.build("issue", mockIssue.proxy());

        IssueAssignFunction iaf = new IssueAssignFunction();
        iaf.execute(input, null, null);

        mockIssue.verify();
    }

    @Test
    public void testIssueAssignWithAssignee() throws GenericEntityException
    {
        String assigneeId = "New Assignee";
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectVoid("setAssigneeId", P.args(new IsEqual(assigneeId)));
        Map input = EasyMap.build("issue", mockIssue.proxy(), "assignee", assigneeId);

        IssueAssignFunction iaf = new IssueAssignFunction();
        iaf.execute(input, null, null);
        mockIssue.verify();
    }
}
