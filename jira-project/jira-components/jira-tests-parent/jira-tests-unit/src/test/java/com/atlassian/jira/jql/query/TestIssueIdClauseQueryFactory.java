package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.jql.query.IssueIdClauseQueryFactory}.
 *
 * @since v4.0
 */

public class TestIssueIdClauseQueryFactory
{
    @Rule
    public final TestRule init = MockitoMocksInContainer.forTest(this);
    @Mock
    private JqlIssueKeySupport issueKeySupport;
    @Mock
    private JqlIssueSupport issueSupport;
    private JqlOperandResolver jqlOperandResolver;
    @Mock
    private QueryCreationContext queryCreationContext;

    private final ApplicationUser applicationUser = new MockApplicationUser("bob");
    private final User theUser = applicationUser.getDirectoryUser();

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        when(queryCreationContext.getUser()).thenReturn(theUser);
        when(queryCreationContext.getApplicationUser()).thenReturn(applicationUser);
    }

    @Test
    public void testValidationIs() throws Exception
    {
        final String fieldName = "field";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IS, singleValueOperand);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(query, QueryFactoryResult.createFalseResult());
    }

    @Test
    public void testValidationIsNot() throws Exception
    {
        final String fieldName = "field";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IS_NOT, singleValueOperand);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(query, QueryFactoryResult.createFalseResult());
    }

    @Test
    public void testIsEmptyClause() throws Exception
    {
        final String fieldName = "field";
        final EmptyOperand emptyOperand = new EmptyOperand();

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.IS,
                emptyOperand));

        assertFalse(query.mustNotOccur());
        assertEquals(new BooleanQuery(), query.getLuceneQuery());
    }

    @Test
    public void testEqualsNoValues() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);

        jqlOperandResolver = Mockito.mock(JqlOperandResolver.class);
        when(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).thenReturn(null);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testEqualsSingleValueId() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand(1L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "1"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testEqualsMultipleValueId() throws Exception
    {
        final String fieldName = "equals";
        final Operand operand = new MultiValueOperand(1L, 2L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "1")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "2")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testEqualsSingleValueKey() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("KEY1");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);
        when(issueSupport.getIssue("KEY1")).thenReturn(new MockIssue(123L));

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "123"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testEqualsMultipleValueIdAndKey() throws Exception
    {
        final String fieldName = "equals";
        final Operand operand = new MultiValueOperand(createLiteral(147l), createLiteral("KEY"), createLiteral("Key2"), new QueryLiteral());
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IN, operand);

        when(issueSupport.getIssue("KEY")).thenReturn(new MockIssue(123L));
        when(issueSupport.getIssue("Key2")).thenReturn(new MockIssue(124L));

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "147")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "123")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "124")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testNotEqualsSingleValueKey() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("KEY1");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_EQUALS, singleValueOperand);

        when(issueSupport.getIssue("KEY1")).thenReturn(new MockIssue(123L));

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "123"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertTrue(query.mustNotOccur());
    }

    @Test
    public void testNotEqualsMultipleValueIdAndKey() throws Exception
    {
        final String fieldName = "notEquals";
        final Operand operand = new MultiValueOperand(createLiteral(147l), createLiteral("KEY"), createLiteral("Key2"), new QueryLiteral());
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, operand);

        when(issueSupport.getIssue("KEY")).thenReturn(new MockIssue(123L));
        when(issueSupport.getIssue("Key2")).thenReturn(new MockIssue(124L));


        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "147")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "123")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueId().getIndexField(), "124")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertTrue(query.mustNotOccur());
    }

    @Test
    public void testNotEqualsNoValues() throws Exception
    {
        final String fieldName = "notEquals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, singleValueOperand);

        jqlOperandResolver = Mockito.mock(JqlOperandResolver.class);
        when(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).thenReturn(null);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testLessThanSingleValue() throws Exception
    {
        final String fieldName = "lessThan";
        final String keyValue = "KEY-200";
        final SingleValueOperand singleValueOperand = new SingleValueOperand(keyValue);
        final MockIssue issue = new MockIssue(1L);
        issue.setKey(keyValue);
        issue.setProjectObject(new MockProject(78));
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, singleValueOperand);

        when(issueSupport.getIssue(keyValue, applicationUser)).thenReturn(issue);
        when(issueKeySupport.parseKeyNum(keyValue)).thenReturn(200L);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermRangeQuery("keynumpart_range", null, "0000000000005k", true, false), BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("projid", "78")), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testLessThanSingleValueOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final String fieldName = "lessThan";
        final String keyValue = "KEY-200";
        final SingleValueOperand singleValueOperand = new SingleValueOperand(keyValue);
        final MockIssue issue = new MockIssue(1L);
        issue.setKey(keyValue);
        issue.setProjectObject(new MockProject(78));
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, singleValueOperand);

        when(issueSupport.getIssue(keyValue)).thenReturn(issue);
        when(issueKeySupport.parseKeyNum(keyValue)).thenReturn(200L);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermRangeQuery("keynumpart_range", null, "0000000000005k", true, false), BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("projid", "78")), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testLessThanSingleEmptyValue() throws Exception
    {
        final String fieldName = "lessThan";
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, operand);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testGreaterThanSingleEmptyValue() throws Exception
    {
        final String fieldName = "greaterThan";
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN, operand);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testGreaterThanEqualsWithId() throws Exception
    {
        final String fieldName = "testGreaterThanEqualsWithId";
        final String keyValue = "KEY-4527549837489534";
        final long issueId = 3484L;

        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, singleValueOperand);

        final MockIssue issue2 = new MockIssue(2L);
        issue2.setKey(keyValue);
        issue2.setProjectObject(new MockProject(29));

        jqlOperandResolver = Mockito.mock(JqlOperandResolver.class);
        when(jqlOperandResolver.getSingleValue(theUser, singleValueOperand, clause)).thenReturn(createLiteral(issueId));
        when(jqlOperandResolver.isListOperand(singleValueOperand)).thenReturn(false);

        when(issueSupport.getIssue(issueId, applicationUser)).thenReturn(issue2);
        when(issueKeySupport.parseKeyNum(keyValue)).thenReturn(4527549837489534L);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add(new TermRangeQuery("keynumpart_range", "00018kvrojd9q6", null, true, true), BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("projid", "29")), BooleanClause.Occur.MUST);

        assertEquals(subQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testGreaterThanEqualsWithIdOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final String fieldName = "testGreaterThanEqualsWithId";
        final String keyValue = "KEY-4527549837489534";
        final long issueId = 3484L;

        final SingleValueOperand singleValueOperand = new SingleValueOperand(3484L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, singleValueOperand);

        final MockIssue issue2 = new MockIssue(2L);
        issue2.setKey(keyValue);
        issue2.setProjectObject(new MockProject(29));

        when(issueSupport.getIssue(issueId)).thenReturn(issue2);
        when(issueKeySupport.parseKeyNum(keyValue)).thenReturn(4527549837489534L);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermRangeQuery("keynumpart_range", "00018kvrojd9q6", null, true, true), BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("projid", "29")), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testListTypeWithRelationalOperator() throws Exception
    {
        final Operand operand = new MultiValueOperand("value", "value2");
        final TerminalClauseImpl clause = new TerminalClauseImpl("dontCare", Operator.LESS_THAN, operand);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testBadOperator() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl("dontCare", Operator.LIKE, singleValueOperand);

        final IssueIdClauseQueryFactory idClauseQueryFactory = new IssueIdClauseQueryFactory(jqlOperandResolver, issueKeySupport, issueSupport);
        final QueryFactoryResult query = idClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }
}
