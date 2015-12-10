package com.atlassian.jira.jql.query;

import java.util.Collections;

import com.atlassian.jira.issue.changehistory.ChangeHistoryFieldConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ChangeHistoryFieldIdResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.clause.WasClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Lists;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestWasClauseQueryFactory
{
    @Mock private SearchProviderFactory mockSearchProviderFactory;
    @Mock private MockJqlOperandResolver mockOperandResolver;
    @Mock private HistoryPredicateQueryFactory mockHistoryPredicateQueryFactory;
    @Mock private ChangeHistoryFieldConstants mockChangeHistoryFieldConstants;
    @Mock private IndexSearcher mockSearcher;
    @Mock private IndexReader mockReader;
    @Mock private OperandHandler<FunctionOperand> mockOperandHandler;
    @Mock private ChangeHistoryFieldIdResolver mockChangeHistoryFieldIdResolver;

    private MockUser fred = new MockUser("Fred");

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
        when(mockSearcher.getIndexReader()).thenReturn(mockReader);
    }

    @After
    public void tearDown()
    {
        mockSearchProviderFactory = null;
        mockOperandResolver = null;
        mockHistoryPredicateQueryFactory = null;
        mockChangeHistoryFieldConstants = null;
        mockSearcher = null;
        mockOperandHandler = null;
        mockChangeHistoryFieldIdResolver = null;
        fred = null;
    }


    @Test
    public void testSupportedOperator() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("testOperand");
        final QueryLiteral queryLiteral = new QueryLiteral(singleValueOperand, "testOperand");
        when(mockChangeHistoryFieldConstants.getIdsForField("status", queryLiteral))
                .thenReturn(Collections.singleton("1"));
        when(mockSearchProviderFactory.getSearcher("changes"))
                .thenReturn(mockSearcher);
        when(mockChangeHistoryFieldIdResolver.resolveIdsForField("status", queryLiteral, false))
                .thenReturn(Collections.singleton("1"));

        WasClauseQueryFactory wasClauseQueryFactory = new WasClauseQueryFactory(mockSearchProviderFactory,
                mockOperandResolver, mockHistoryPredicateQueryFactory, null, mockChangeHistoryFieldIdResolver);
        WasClause wasClause = new WasClauseImpl("status", Operator.WAS, singleValueOperand, null);
        final QueryFactoryResult result = wasClauseQueryFactory.create(fred, wasClause);
        final ConstantScoreQuery query = (ConstantScoreQuery) result.getLuceneQuery();
        assertTrue("Query wraps an IssueIdFilter", query.getFilter() instanceof IssueIdFilter);
    }

    @Test
    public void testSingleValueFunctionSearching()
    {
        final FunctionOperand functionOperand = new FunctionOperand("testOperand");
        final QueryLiteral queryLiteral = new QueryLiteral(functionOperand, "testValue");
        mockOperandResolver.addHandler("testOperand",mockOperandHandler);

        when(mockChangeHistoryFieldConstants.getIdsForField("status", queryLiteral))
                .thenReturn(Collections.singleton("1"));
        when(mockSearchProviderFactory.getSearcher("changes"))
                .thenReturn(mockSearcher);
        when(mockChangeHistoryFieldIdResolver.resolveIdsForField("status", queryLiteral, false))
                .thenReturn(Collections.singleton("1"));

        WasClauseQueryFactory wasClauseQueryFactory = new WasClauseQueryFactory(mockSearchProviderFactory,
                 mockOperandResolver, mockHistoryPredicateQueryFactory, null, mockChangeHistoryFieldIdResolver);

        WasClause wasClause = new WasClauseImpl("status", Operator.WAS, functionOperand, null);
        when(mockOperandHandler.getValues(new QueryCreationContextImpl(fred), functionOperand, wasClause))
                .thenReturn(Lists.newArrayList(queryLiteral));
        wasClauseQueryFactory.create(fred, wasClause);
    }

    @Test
    public void testListFunctionSearching()
    {
        final FunctionOperand functionOperand = new FunctionOperand("testOperand");
        final QueryLiteral firstQueryLiteral = new QueryLiteral(functionOperand, "testValue1");
        final QueryLiteral secondQueryLiteral = new QueryLiteral(functionOperand, "testValue2");

        mockOperandResolver.addHandler("testOperand",mockOperandHandler);

        when(mockChangeHistoryFieldConstants.getIdsForField("status", firstQueryLiteral))
                        .thenReturn(Collections.singleton("1"));
        when(mockChangeHistoryFieldConstants.getIdsForField("status", secondQueryLiteral))
                        .thenReturn(Collections.singleton("2"));
        when(mockSearchProviderFactory.getSearcher("changes")).thenReturn(mockSearcher);
        when(mockChangeHistoryFieldIdResolver.resolveIdsForField("status", firstQueryLiteral, false)).thenReturn(Collections.singleton("1"));
        when(mockChangeHistoryFieldIdResolver.resolveIdsForField("status", secondQueryLiteral, false)).thenReturn(Collections.singleton("2"));

        WasClauseQueryFactory wasClauseQueryFactory = new WasClauseQueryFactory(mockSearchProviderFactory,
                 mockOperandResolver, mockHistoryPredicateQueryFactory, null, mockChangeHistoryFieldIdResolver);

        WasClause wasClause = new WasClauseImpl("status", Operator.WAS, functionOperand, null);
        when(mockOperandHandler.getValues(new QueryCreationContextImpl(fred), functionOperand, wasClause))
                .thenReturn(Lists.newArrayList(firstQueryLiteral, secondQueryLiteral));
        wasClauseQueryFactory.create(fred, wasClause);
    }
}
