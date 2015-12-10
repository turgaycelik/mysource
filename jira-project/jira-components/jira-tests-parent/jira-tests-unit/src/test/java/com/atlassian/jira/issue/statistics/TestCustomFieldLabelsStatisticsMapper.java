package com.atlassian.jira.issue.statistics;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.query.operand.EmptyOperand.EMPTY;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.IS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldLabelsStatisticsMapper
{
    private CustomFieldLabelsStatisticsMapper statisticsMapper;

    @Mock
    private CustomFieldInputHelper customFieldInputHelper;

    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private CustomField customField;

    @Mock
    private User user;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());

        when(customField.getId()).thenReturn("666");
        when(customField.getName()).thenReturn("myfield");
        when(customField.getClauseNames()).thenReturn(new ClauseNames("myPK"));

        when(authenticationContext.getLoggedInUser()).thenReturn(user);

        when(customFieldInputHelper.getUniqueClauseName(user, "myPK", "myfield")).thenReturn("myclause");

        statisticsMapper = new CustomFieldLabelsStatisticsMapper(customField, customFieldInputHelper, authenticationContext, false);
    }

    @Test
    public void testGetUrlSuffixNullField() throws Exception
    {
        final Clause originalClause = new TerminalClauseImpl("blah", EQUALS, "blah");
        final QueryImpl originalQuery = new QueryImpl(originalClause);
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = statisticsMapper.getSearchUrlSuffix(null, originalSearchRequest);

        final AndClause expectedAndClause = new AndClause(originalClause, new TerminalClauseImpl("myclause", IS, EMPTY));
        assertEquals(expectedAndClause, newSearchRequest.getQuery().getWhereClause());
    }

    @Test
    public void testGetUrlSuffixNonNullField() throws Exception
    {
        final Clause originalClause = new TerminalClauseImpl("blah", EQUALS, "blah");
        final QueryImpl originalQuery = new QueryImpl(originalClause);
        SearchRequest originalSearchRequest = new MockJqlSearchRequest(10000L, originalQuery);

        final SearchRequest newSearchRequest = statisticsMapper.getSearchUrlSuffix("myvalue", originalSearchRequest);

        final AndClause expectedAndClause = new AndClause(originalClause, new TerminalClauseImpl("cf[0]", EQUALS, "myvalue"));
        assertEquals(expectedAndClause, newSearchRequest.getQuery().getWhereClause());
    }
}
