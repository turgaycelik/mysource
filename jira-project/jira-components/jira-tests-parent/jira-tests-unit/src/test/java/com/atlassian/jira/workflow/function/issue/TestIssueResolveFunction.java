/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestIssueResolveFunction
{

    private IssueResolveFunction issueResolveFunction;
    @Mock
    private MutableIssue issue;
    @Mock
    private ConstantsManager constantsManager;

    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    @Before
    public void setup(){
        issueResolveFunction = new IssueResolveFunction();
        new MockComponentWorker().addMock(ConstantsManager.class, constantsManager).init();
    }

    @Test
    public void shouldSetFixVersionsWhenGiven(){
        List<Version> fixVersions = ImmutableList.<Version>of(new MockVersion(1L, "1.0"), new MockVersion(3L, "3.0"), new MockVersion(2L, "strangeVersion"));
        Map vars = ImmutableMap.of(
                "issue", issue,
                "fixVersions", fixVersions
        );
        issueResolveFunction.execute(vars, null, null);
        verify(issue).setFixVersions(fixVersions);
    }

    @Test
    public void shouldNotSetAnyFixVersionsWhenNotGiven(){
        Map vars = ImmutableMap.of(
                "issue", issue
        );
        issueResolveFunction.execute(vars, null, null);
        verify(issue, never()).setFixVersions(anyCollection());
    }

    @Test
    public void shouldSetResolutionWhenGiven(){
        Map vars = ImmutableMap.of(
                "issue", issue,
                "resolution", "HappyIssue"
        );
        final GenericValue resolution = new MockGenericValue("Resolution", 987L);
        when(constantsManager.getResolution("HappyIssue")).thenReturn(resolution);
        issueResolveFunction.execute(vars, null, null);
        verify(issue).setResolution(resolution);
    }

    @Test
    public void shouldSetResolutionToNullWhenWasNotGiven(){
        Map vars = ImmutableMap.of(
                "issue", issue
        );
        issueResolveFunction.execute(vars, null, null);
        verify(issue).setResolution(null);
    }



}
