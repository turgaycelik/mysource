/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.query.Query;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.IssueFieldConstants.PROJECT;
import static com.atlassian.jira.util.collect.MapBuilder.singletonMap;
import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestProjectSearchRequestAppender
{
    @Mock
    private ProjectManager projectManager;

    @Mock
    private Project project;

    private ProjectStatisticsMapper.ProjectSearchRequestAppender searchRequestAppender;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        searchRequestAppender = new ProjectStatisticsMapper.ProjectSearchRequestAppender(projectManager, PROJECT);
    }

    @Test
    public void appendInclusiveSingleValueClause() throws Exception
    {
        when(projectManager.getProjectObj(23232L)).thenReturn(project);
        when(project.getKey()).thenReturn("PZQ");

        final Query originalQuery = JqlQueryBuilder.newBuilder().where().addNumberCondition("SomeCondition", 88L).buildQuery();
        final SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);
        final MockGenericValue projectGenericValue = new MockGenericValue("Project", singletonMap("id", 23232L));

        final SearchRequest newSearchRequest = searchRequestAppender.appendInclusiveSingleValueClause(projectGenericValue, originalSearchRequest);
        final AndClause andClause = (AndClause) newSearchRequest.getQuery().getWhereClause();

        final List<?> expectedAndClauses = asList(
                new TerminalClauseImpl("SomeCondition", EQUALS, 88L),
                new TerminalClauseImpl(PROJECT, EQUALS, "PZQ")
        );

        assertEquals(expectedAndClauses, andClause.getClauses());
    }

    @Test
    public void appendInclusiveSingleNullClause() throws Exception
    {
        when(projectManager.getProjectObj(23232L)).thenReturn(project);
        when(project.getKey()).thenReturn("PZQ");

        final Query originalQuery = JqlQueryBuilder.newBuilder().where().addNumberCondition("SomeCondition", 88L).buildQuery();
        final SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = searchRequestAppender.appendInclusiveSingleValueClause(null, originalSearchRequest);
        final AndClause andClause = (AndClause) newSearchRequest.getQuery().getWhereClause();

        final List<?> expectedAndClauses = asList(
                new TerminalClauseImpl("SomeCondition", EQUALS, 88L),
                new TerminalClauseImpl(PROJECT, IS, EMPTY)
        );
        assertEquals(expectedAndClauses, andClause.getClauses());
    }

    @Test
    public void appendExclusiveMultiValueClause() throws Exception
    {
        when(projectManager.getProjectObj(23232L)).thenReturn(project);
        when(project.getKey()).thenReturn("PZQ");

        final MockGenericValue projectGenericValue = new MockGenericValue("Project", singletonMap("id", 23232L));

        final Query originalQuery = JqlQueryBuilder.newBuilder().where().addNumberCondition("SomeCondition", 88L).buildQuery();
        final SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        List values = asList(projectGenericValue, null);

        final SearchRequest newSearchRequest = searchRequestAppender.appendExclusiveMultiValueClause(values, originalSearchRequest);
        final AndClause andClause = (AndClause) newSearchRequest.getQuery().getWhereClause();

        final List<?> expectedAndClauses = asList(
                new TerminalClauseImpl("SomeCondition", EQUALS, 88L),
                new NotClause(
                        new OrClause(
                                new TerminalClauseImpl(PROJECT, EQUALS, "PZQ"),
                                new TerminalClauseImpl(PROJECT, IS, EMPTY)
                        )
                )
        );
        assertEquals(expectedAndClauses, andClause.getClauses());
    }
}