/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugin.report.impl;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.plugin.report.ReportSubTaskFetcher;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.AnswerWith;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.util.AuthorizationSupport;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TestDeveloperWorkloadReport
{
    @Rule
    public RuleChain mockito = MockitoMocksInContainer.forTest(this);

    @Mock
    private ProjectManager projectManager;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private UserManager userManager;

    @Mock
    private JiraDurationUtils jiraDurationUtils;

    @Mock
    @AvailableInContainer
    private SearchProvider searchProvider;

    @Mock
    @AvailableInContainer
    private ReportSubTaskFetcher reportSubTaskFetcher;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    @AvailableInContainer
    private AuthorizationSupport authorizationSupport;

    @Mock
    private I18nHelper i18nHelper;

    private DeveloperWorkloadReport workloadReport;

    @Before
    public void setUp() throws Exception
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText(anyString())).thenAnswer(AnswerWith.firstParameter());

        workloadReport = new DeveloperWorkloadReport(projectManager, applicationProperties, userManager, jiraDurationUtils, searchProvider, reportSubTaskFetcher);
    }

    @Test
    public void validationShouldRejectMissingUser() throws Exception
    {
        final ProjectActionSupport projectActionSupport = new ProjectActionSupport();
        workloadReport.validate(projectActionSupport, Collections.emptyMap());
        assertEquals(ImmutableMap.of("developer", "report.developerworkload.developer.is.required"), projectActionSupport.getErrors());
    }

    @Test
    public void countMapShouldReturnAggregatedIssuesOverDifferentProjects() throws Exception
    {
        final Function<Long, Issue> longToIssueWithProjectId = new Function<Long, Issue>()
        {
            @Override
            public Issue apply(final Long input)
            {
                return mockIssue(input);
            }
        };

        assertEquals(
                ImmutableMap.of(1L, mutable(2L)),
                workloadReport.initCountMap(copyOf(transform(ImmutableList.of(1L, 1L), longToIssueWithProjectId))));

        assertEquals(
                ImmutableMap.of(1L, mutable(4L), 2L, mutable(3L), 3L, mutable(3L), 4L, mutable(1L)),
                workloadReport.initCountMap(copyOf(transform(ImmutableList.of(1L, 2L, 1L, 4L, 3L, 3L, 1L, 3L, 2L, 2L, 1L), longToIssueWithProjectId))));

        assertEquals(
                ImmutableMap.of(1000L, mutable(4L), 2000L, mutable(3L)),
                workloadReport.initCountMap(copyOf(transform(ImmutableList.of(1000L, 2000L, 1000L, 1000L, 1000L, 2000L, 2000L), longToIssueWithProjectId))));
    }

    @Test
    public void totalsIssueCountShouldSumUpIssuesFromDifferentProjects() throws Exception
    {
        assertEquals(
                Long.valueOf(2),
                workloadReport.getTotalIssuesCount(ImmutableMap.of(1L, mutable(1L), 2L, mutable(1L)))
        );

        assertEquals(
                Long.valueOf(9999),
                workloadReport.getTotalIssuesCount(ImmutableMap.of(Long.MAX_VALUE, mutable(1234L), Long.MIN_VALUE, mutable(8765L)))
        );

        assertEquals(
                Long.valueOf(18),
                workloadReport.getTotalIssuesCount(ImmutableMap.of(1L, mutable(3L), 2L, mutable(5L), 3L, mutable(10L)))
        );
    }

    @Test
    public void countMapShouldReturnAggregatedEstimationsOverDifferentProjects() throws Exception
    {
        final List<Issue> issues = ImmutableList.<Issue>of(
                mockIssue(1L, 1000L),
                mockIssue(2L, 1000L),
                mockIssue(1L, 2000L),
                mockIssue(2L, 2000L),
                mockIssue(3L, 2000L),
                mockIssue(1L, 3000L),
                mockIssue(3L, 3000L),
                mockIssue(5L, 3000L)
        );

        assertEquals(
                ImmutableMap.of(1L, mutable(6000L), 2L, mutable(3000L), 3L, mutable(5000L), 5L, mutable(3000L)),
                workloadReport.initWorkloadMap(issues)
        );
    }

    private AbstractReport.MutableLong mutable(final long value)
    {
        return new AbstractReport.MutableLong(value);
    }

    private MockIssue mockIssue(final long projectId)
    {
        return mockIssue(projectId, 1000L);
    }

    private MockIssue mockIssue(final long projectId, final long estimate)
    {
        final MockIssue issue = new MockIssue();
        issue.setProjectObject(new MockProject(projectId));
        issue.setEstimate(estimate);
        return issue;
    }
}
