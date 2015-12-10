/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.workflow.condition;

import java.util.Map;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TestSubTaskBlockingCondition
{
    private SubTaskBlockingCondition condition;
    private Map<String, Object> transientVars;
    private Map<String, Object> args;

    @Mock
    @AvailableInContainer
    private SubTaskManager subTaskManager;


    @Mock
    private Issue issue;

    @Rule
    public MockitoContainer init = MockitoMocksInContainer.rule(this);
    private ApplicationUser caller;


    @Before
    public void setUp() throws Exception
    {

        transientVars = Maps.newHashMap();
        transientVars.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, issue);

        args = Maps.newHashMap();

        condition = new SubTaskBlockingCondition();

        when(subTaskManager.isSubTasksEnabled()).thenReturn(true);

    }

    @Test
    public void shouldReturnTrueWhenSubtasksAreDisabled(){
        when(subTaskManager.isSubTasksEnabled()).thenReturn(false);
        assertTrue(condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void shouldReturnTrueWhenSubtasksAreEmpty(){
        when(issue.getSubTasks()).thenReturn(ImmutableList.<GenericValue>of());
        assertTrue(condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void allSubtasksWhitelisted(){
        when(issue.getSubTasks()).thenReturn(ImmutableList.<GenericValue>of(
                new StatusGV("status1"),
                new StatusGV("status2"),
                new StatusGV("status3"),
                new StatusGV("status1")
        ));
        args.put("statuses", "status1,status3,status2");
        assertTrue(condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void allIssuesSameStatusWhitelisted(){
        when(issue.getSubTasks()).thenReturn(ImmutableList.<GenericValue>of(
                new StatusGV("status1"),
                new StatusGV("status1"),
                new StatusGV("status1"),
                new StatusGV("status1")
        ));
        args.put("statuses", "status1,status2");
        assertTrue(condition.passesCondition(transientVars, args, null));
    }


    @Test
    public void oneIssueNotWhitelisted(){
        when(issue.getSubTasks()).thenReturn(ImmutableList.<GenericValue>of(
                new StatusGV("status1"),
                new StatusGV("status2"),
                new StatusGV("status3"),
                new StatusGV("status4") //black sheep
        ));
        args.put("statuses", "status1 ,  , , status3 , status2");
        assertFalse(condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void allIssuesNotWhitelisted(){
        when(issue.getSubTasks()).thenReturn(ImmutableList.<GenericValue>of(
                new StatusGV("status1"),
                new StatusGV("status2"),
                new StatusGV("status3"),
                new StatusGV("status4")
        ));
        args.put("statuses", "anotherStatus , strange#$#Status");
        assertFalse(condition.passesCondition(transientVars, args, null));
    }


    @Test
    public void emptyStatusesList(){
        when(issue.getSubTasks()).thenReturn(ImmutableList.<GenericValue>of(
                new StatusGV("status1"),
                new StatusGV("status2"),
                new StatusGV("status3"),
                new StatusGV("status4")
        ));
        args.put("statuses", "");
        assertFalse(condition.passesCondition(transientVars, args, null));
    }


    private static class StatusGV extends MockGenericValue{
        public StatusGV(String status){
            super("Issue", ImmutableMap.of("status", status));
        }

    }

}
