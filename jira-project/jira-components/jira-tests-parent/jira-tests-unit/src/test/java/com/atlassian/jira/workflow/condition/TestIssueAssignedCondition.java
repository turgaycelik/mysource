/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestIssueAssignedCondition
{
    @Test
    public void testAssigned()
    {
        IssueAssignedCondition condition = new IssueAssignedCondition();

        Map inputs = EasyMap.build("assignee", "yep");
        assertTrue(condition.passesCondition(inputs, null, null));

        inputs = EasyMap.build("uhm", "nope");
        assertTrue(!condition.passesCondition(inputs, null, null));

        inputs = EasyMap.build("assignee", "");
        assertTrue(!condition.passesCondition(inputs, null, null));
    }
}
