/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.MutableIssue;

import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

public class TestIssueReopenFunction
{
    @Test
    public void testIssueReopenFunction() throws GenericEntityException
    {
        IssueReopenFunction irf = new IssueReopenFunction();
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectVoid("setResolution", P.args(new IsNull()));
        Map input = EasyMap.build("issue", mockIssue.proxy());
        irf.execute(input, null, null);
        mockIssue.verify();
    }
}
