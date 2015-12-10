package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestUserStatisticsMapper extends MockControllerTestCase
{
    private UserFieldSearchConstantsWithEmpty searchConstants;

    @Before
    public void setUp() throws Exception
    {
        searchConstants = new UserFieldSearchConstantsWithEmpty("indexField", "jqlClauseName", "fieldUrlParameter", "selectUrlParameter", "searcherId", "emptySelectFlag", "fieldId", OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    @Test
    public void testGetUrlSuffixNullUser() throws Exception
    {
        UserStatisticsMapper statisticsMapper = new UserStatisticsMapper(searchConstants, null, new MockAuthenticationContext(null));
        Clause clause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        SearchRequest sr = new MockJqlSearchRequest(10000L, new QueryImpl(clause));
        final SearchRequest searchUrlSuffix = statisticsMapper.getSearchUrlSuffix(null, sr);
        final AndClause expected = new AndClause(clause, new TerminalClauseImpl("jqlClauseName", Operator.IS, EmptyOperand.EMPTY));
        assertEquals(expected, searchUrlSuffix.getQuery().getWhereClause());
    }

    @Test
    public void testGetEmptyClause() throws Exception
    {
        UserStatisticsMapper statisticsMapper = new UserStatisticsMapper(searchConstants, null, new MockAuthenticationContext(null));
        assertEquals(new TerminalClauseImpl("jqlClauseName", Operator.IS, EmptyOperand.EMPTY), statisticsMapper.getEmptyUserClause());
    }

    @Test
    public void testGetUserClause() throws Exception
    {
        UserStatisticsMapper statisticsMapper = new UserStatisticsMapper(searchConstants, null, new MockAuthenticationContext(null));
        assertEquals(new TerminalClauseImpl("jqlClauseName", Operator.EQUALS, "name"), statisticsMapper.getUserClause("name"));
    }
}
