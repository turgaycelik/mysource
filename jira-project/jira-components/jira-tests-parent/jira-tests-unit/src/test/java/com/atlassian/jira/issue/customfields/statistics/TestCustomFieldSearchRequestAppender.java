package com.atlassian.jira.issue.customfields.statistics;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_TYPE;
import static com.atlassian.jira.issue.IssueFieldConstants.PROJECT;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IN;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldSearchRequestAppender
{
    private CustomFieldSearchRequestAppender mapper;

    private final Clause originalClause = new TerminalClauseImpl("somefield", EQUALS, "somevalue");
    private final SearchRequest baseSearchRequest = new MockJqlSearchRequest(10000L, new QueryImpl(originalClause));


    @Mock
    private CustomField customField;

    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private User user;

    @Mock
    private SearchService searchService;

    @Mock
    private SearchContextFactory searchContextFactory;

    @Mock
    private SearchRenderer searchRenderer;

    @Mock
    private CustomFieldSearcher customFieldSearcher;

    @Mock
    private SearchContext searchContext;

    @Mock
    private AbstractCustomFieldStatisticsMapper customFieldStatisticsMapper;

    @Before
    public void setUp() throws Exception
    {
        mapper = new CustomFieldSearchRequestAppender(customField, customFieldStatisticsMapper);

        when(authenticationContext.getLoggedInUser()).thenReturn(user);
        when(customField.getCustomFieldSearcher()).thenReturn(customFieldSearcher);
        when(customFieldSearcher.getSearchRenderer()).thenReturn(searchRenderer);
        when(searchService.getSearchContext(user, baseSearchRequest.getQuery())).thenReturn(searchContext);
        when(searchContextFactory.create(searchContext)).thenReturn(searchContext);
        when(searchRenderer.isShown(isA(User.class), isA(SearchContext.class))).thenReturn(true);
        when(customFieldStatisticsMapper.getSearchValue(isA(String.class))).thenAnswer(new Answer<String>()
        {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        });

        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.registerMock(JiraAuthenticationContext.class, authenticationContext);
        mockComponentWorker.registerMock(SearchService.class, searchService);
        mockComponentWorker.registerMock(SearchContextFactory.class, searchContextFactory);
        ComponentAccessor.initialiseWorker(mockComponentWorker);
    }

    @Test
    public void getAppendInclusiveSingleValueClauseWithFieldSpecificProjectsAndIssueTypes()
    {
        when(searchContext.getProjectIds()).thenReturn(asList(Long.valueOf(6), Long.valueOf(7)));
        when(searchContext.getIssueTypeIds()).thenReturn(asList("bug", "feature"));

        final SearchRequest searchRequest = mapper.appendInclusiveSingleValueClause("meh", baseSearchRequest);
        assertThat(searchRequest, is(notNullValue()));
        AndClause where = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(where, is(new AndClause(
                originalClause,
                new TerminalClauseImpl(PROJECT, IN, new MultiValueOperand(6L, 7L)),
                new TerminalClauseImpl(ISSUE_TYPE, IN, new MultiValueOperand("bug", "feature")),
                new TerminalClauseImpl("cf[0]", EQUALS, "meh")
        )));
    }

    @Test
    public void getAppendInclusiveSingleValueClauseWithoutFieldSpecificProjectsOrIssueTypes()
    {
        final SearchRequest searchRequest = mapper.appendInclusiveSingleValueClause("meh", baseSearchRequest);
        assertThat(searchRequest, is(notNullValue()));
        AndClause where = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(where, is(new AndClause(
                originalClause,
                new TerminalClauseImpl("cf[0]", EQUALS, "meh")
        )));
    }

    @Test
    public void getAppendInclusiveSingleValueClauseWithNullValueWillReturnNull()
    {
        final SearchRequest searchRequest = mapper.appendInclusiveSingleValueClause(null, baseSearchRequest);
        assertThat(searchRequest, is(nullValue()));
    }

    @Test
    public void appendExclusiveMultiValueClause()
    {
        when(searchContext.getProjectIds()).thenReturn(asList(Long.valueOf(6), Long.valueOf(7)));
        when(searchContext.getIssueTypeIds()).thenReturn(asList("bug", "feature"));

        List values = asList("foo", "bar");

        final SearchRequest searchRequest = mapper.appendExclusiveMultiValueClause(values, baseSearchRequest);
        assertThat(searchRequest, is(notNullValue()));
        AndClause where = (AndClause) searchRequest.getQuery().getWhereClause();

        assertThat(where, is(new AndClause(
                originalClause,
                new NotClause(
                        new OrClause(
                                new AndClause(
                                        new TerminalClauseImpl(PROJECT, IN, new MultiValueOperand(6L, 7L)),
                                        new TerminalClauseImpl(ISSUE_TYPE, IN, new MultiValueOperand("bug", "feature")),
                                        new TerminalClauseImpl("cf[0]", EQUALS, "foo")
                                ),
                                new AndClause(
                                        new TerminalClauseImpl(PROJECT, IN, new MultiValueOperand(6L, 7L)),
                                        new TerminalClauseImpl(ISSUE_TYPE, IN, new MultiValueOperand("bug", "feature")),
                                        new TerminalClauseImpl("cf[0]", EQUALS, "bar")
                                )
                        )
                )
        )));
    }
}