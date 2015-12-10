package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_TYPE;
import static com.atlassian.jira.issue.IssueFieldConstants.PROJECT;
import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestComponentSearchRequestAppender
{
    @Mock
    private ProjectManager projectManager;

    @Mock
    private ProjectComponentManager projectComponentManager;

    private final Project project = new MockProject(13L, "PR");
    private final GenericValue componentGV = new MockGenericValue("componentGV", ImmutableMap.of("id", 555L, "name", "New Component 555", "project", project.getId()));
    private final ProjectComponent component = new MockProjectComponent(555L, "New Component 555", project.getId());
    private final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl(ISSUE_TYPE, EQUALS, "Bug");

    private ComponentStatisticsMapper.ComponentSearchRequestAppender searchRequestAppender;

    @Before
    public void setUp() throws Exception
    {
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(ProjectManager.class, projectManager);
        componentWorker.registerMock(ProjectComponentManager.class, projectComponentManager);
        ComponentAccessor.initialiseWorker(componentWorker);

        searchRequestAppender = new ComponentStatisticsMapper.ComponentSearchRequestAppender(projectComponentManager, projectManager);

        when(projectComponentManager.find(555L)).thenReturn(component);
        when(projectManager.getProjectObj(13L)).thenReturn(project);
    }

    @Test
    public void appendInclusiveSingleValueClause() throws Exception
    {
        final SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(
                componentGV,
                searchRequest(issueTypeClause)
        );

        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();
        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new AndClause(
                        new TerminalClauseImpl("component", EQUALS, "New Component 555"),
                        new TerminalClauseImpl(PROJECT, EQUALS, "PR")
                )
        )));
    }

    @Test
    public void appendInclusiveSingleNullClause() throws Exception
    {
        final SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(
                null,
                searchRequest(issueTypeClause)
        );

        final AndClause whereClause = (AndClause) searchRequest.getQuery().getWhereClause();
        assertThat(whereClause, is(new AndClause(
                issueTypeClause,
                new TerminalClauseImpl("component", IS, EMPTY)
        )));
    }

    @Test
    public void appendExclusiveMultiValueClause() throws Exception
    {
        final SearchRequest searchRequest = searchRequestAppender.appendExclusiveMultiValueClause(
                asList(componentGV, null),
                searchRequest(issueTypeClause)
        );

        assertThat(searchRequest, is(nullValue()));
    }

    private static MockJqlSearchRequest searchRequest(Clause clause)
    {
        return new MockJqlSearchRequest(10000L, new QueryImpl(clause));
    }
}